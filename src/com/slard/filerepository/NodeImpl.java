package com.slard.filerepository;

import org.jgroups.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeImpl implements Node, MessageListener, MembershipListener, SystemFileList {
  private static final int CH_REPLICA_COUNT = 4;
  public static final int REPLICA_COUNT = 1;
  private static final String JOINED_AND_INITIALIZED = "joinedAndInitialized";
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String SYSTEM_CHANNEL_NAME = "FileRepositoryCluster";
  public static final String USER_CHANNEL_NAME = "FileRepositoryClusterClient";

  SystemCommsServerImpl systemComms = null;
  UserCommsServerImpl userComms = null;
  private DataStore dataStore;
  public ConsistentHashTableImpl<Address> ch;
  Properties options;
  byte[] state;
  private Channel systemChannel;
  private Channel userChannel;
  private SystemFileList fileList;
  private Timer replicaGuardTimer;

  public NodeImpl(DataStore dataStore, Properties options) {
    this.logger.setLevel(Level.ALL);
    this.dataStore = dataStore;
    this.ch = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, null);
    this.options = options;
    this.fileList = this;
  }

  public NodeDescriptor createNodeDescriptor(Address address) {
    SystemCommsClientImpl systemCommsClient = SystemCommsClientImpl.getSystemComsClient(this.systemComms
        .GetDispatcher(), address);    
    NodeDescriptor node = new NodeDescriptorImpl(address, systemCommsClient, systemCommsClient);
    return node;
  }

  public void start() throws ChannelException {
    // Channel for system communications (within the cluster)
    this.systemChannel = new JChannel();
    systemChannel.connect(SYSTEM_CHANNEL_NAME);
    systemComms = new SystemCommsServerImpl(systemChannel, dataStore, this, this, this);
        
    logger.fine("channel connected and system coms server ready");
    logger.finer("My Address: " + systemChannel.getAddress().toString());
    for(Address addr: this.systemChannel.getView().getMembers()){
      this.ch.add(addr);
    }
    this.initializeDataStore();

    // Channel from user communications (from outside the cluster)
    System.setProperty("jgroups.udp.mcast_port", "45589");
    this.userChannel = new JChannel();
    userChannel.connect(USER_CHANNEL_NAME);
    userComms = new UserCommsServerImpl(userChannel, dataStore, null, null, this);
    
    this.replicaGuardTimer = new Timer();
    replicaGuardTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        replicaGuard();
      }
    }, 15000, 30000);
  }

  public void stop() {
    systemComms.stop();
    systemChannel.close();
    userComms.stop();
    userChannel.close();
  }

  public void replicaGuard() {
    logger.info("replicaGuard tick.");
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      if (this.amIMaster(obj.getName())) {
        this.replicateDataObject(obj);
      }
    }
  }

  @Override
  public void initializeDataStore() {
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      List<Address> oldAddr = this.ch.getAllValues();
      if (oldAddr.size() > 1) {

        oldAddr.remove(this.systemChannel.getAddress());
        ConsistentHashTable<Address> oldCh = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, oldAddr);

        if (this.amIMaster(obj.getName())) {
          Address oldMasterAddress = oldCh.get(obj.getName());
          NodeDescriptor oldMaster = this.createNodeDescriptor(oldMasterAddress);
          if (!oldMaster.hasFile(obj.getName())) {
            this.replicateDataObject(obj);
          }
        } else {
          NodeDescriptor master = this.createNodeDescriptor(this.ch.get(obj.getName()));
          if (!master.hasFile(obj.getName())) {
            master.store(obj);
          }
          try {
            this.dataStore.delete(obj.getName());
          } catch (Exception ex) {
            logger.warning(ex.toString());
            // TODO Implement some better error handling
          }
        }
      }
      // Now check the filelist (for the files I'm master)
      if (this.amIMaster(obj.getName()) && !this.fileList.contains(obj.getName())) {
        this.fileList.addFileName(obj.getName());
      }
    }

    // If I'm not the first in cluster - sent the message that I'm ready to go
    if (this.systemChannel.getView().size() > 1) {
      try {
        this.systemChannel.send(new Message(null, null, JOINED_AND_INITIALIZED));
      } catch (Exception ex) {
        logger.warning(ex.toString());
        // TODO make me crash!!!
      }
    }
  }

  @Override
  public void nodeJoined(NodeDescriptor node, ConsistentHashTable<Address> oldCh) {
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      // If the guy is master of any of my files
      if (this.ch.get(obj.getName()).equals(node.getAddress())) {
        logger.fine("Joining node is master for " + obj.getName());
        // Check if i was master before
        if (oldCh.get(obj.getName()).equals(this.systemChannel.getAddress())) {
          if (node.hasFile(obj.getName())) {
            if (!node.getCRC(obj.getName()).equals(obj.getCRC())) {
              node.replace(obj);
            }
          } else {
            node.store(obj);
          }
        }
        if (!this.ch.getPreviousNodes(obj.getName(), REPLICA_COUNT).contains(this.systemChannel.getAddress())) {
          // If I'm not replica - delete that file
          try {
            this.dataStore.delete(obj.getName());
          } catch (Exception ex) {
            // TODO better exception handing
          }
        }
      } else if (this.amIMaster(obj.getName())) {
        // Check if he'll become a replica

        if (this.ch.getPreviousNodes(obj.getName(), REPLICA_COUNT).contains(node.getAddress())) {
          this.replicateDataObject(obj);
        }
      }
    }
  }

  @Override
  public void nodeLeft(Address nodeAddress, ConsistentHashTable<Address> oldCh) {
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      // Was he master for any of mine files?
      if (oldCh.get(obj.getName()).equals(nodeAddress)) {
        // Am i Master now?
        if (this.amIMaster(obj.getName())) {
          this.replicateDataObject(obj);
        }
      }
      // Was he a replica for my file?
      else if (oldCh.getPreviousNodes(obj.getName(), REPLICA_COUNT).contains(nodeAddress)) {
        this.replicateDataObject(obj);
      }
    }
  }

  @Override
  public boolean amIMaster(String fileName) {
    return this.ch.get(fileName).equals(this.systemChannel.getAddress());
  }

  @Override
  public void replicateDataObject(DataObject obj) {
    logger.fine("Replicating file: " + obj.getName() + " ch " + this.ch.getAllValues().size());
    for (Address nodeAddress : this.ch.getPreviousNodes(obj.getName(), REPLICA_COUNT)) {
      logger.fine("Replicating file: " + obj.getName() + " to " + nodeAddress);
      NodeDescriptor node = this.createNodeDescriptor(nodeAddress);
      if (obj.getData() != null && node.hasFile(obj.getName())) {

        if (!node.getCRC(obj.getName()).equals(obj.getCRC())) {
          node.replace(obj);
        }
      } else if (obj.getData() != null) {
        node.store(obj);
      } else if (obj.getData() == null) {
        node.delete(obj.getName());
      }
    }
  }

  // JGroups related implementation

  @Override
  public synchronized void receive(Message message) {
    if (message.getSrc() == this.systemChannel.getAddress()) {
      return;
    }
    if (message.getObject().toString().equalsIgnoreCase(JOINED_AND_INITIALIZED)) {
      this.logger.fine("Node joined: " + message.getSrc().toString());
      List<Address> oldCh = new ArrayList<Address>(this.ch.getAllValues());
      oldCh.remove(message.getSrc());
      this.nodeJoined(this.createNodeDescriptor(message.getSrc()), new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, oldCh));
    }
  }

  @Override
  public byte[] getState() {
    return state;
  }

  @Override
  public void setState(byte[] bytes) {
    // Not totally sure what messages we can receive, probably broadcast of
    // system state (disk space etc)
    // probably in rdf.
    state = bytes;
  }

  // Joined/Left the network - synchronized causes less problems and solves lots
  // issues i find
  @Override
  public synchronized void viewAccepted(View view) {
    logger.fine("ViewAccepted");

    // find nodes that joined
    List<Address> removedValues = this.ch.getAllValues();
    List<Address> newNodes = new ArrayList<Address>();

    ConsistentHashTable<Address> oldCh = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, this.ch.getAllValues());

    for (Address addr : view.getMembers()) {
      if (removedValues.contains(addr))
        removedValues.remove(addr);
      else {
        newNodes.add(addr);
      }
    }

    // Now update new ch
    for (Address addr : removedValues) {
      this.ch.remove(addr);
    }
    for (Address addr : newNodes) {
      this.ch.add(addr);
    }

    for (Address addr : removedValues) {
      this.nodeLeft(addr, oldCh);
    }

  }

  // Suspect Left the network
  @Override
  public void suspect(Address address) {
    logger.info("Suspecting node: " + address.toString());
    if (this.ch.contains(address)) {
      ConsistentHashTable<Address> oldCh = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, this.ch.getAllValues());
      this.ch.remove(address);
      this.nodeLeft(address, oldCh);
    }
  }

  @Override
  public void block() {
    // probably can be left empty.
  }

  // File List operations

  @Override
  public boolean addFileName(String fileName) {
    if (this.amIMaster(this.dataStore.getFileListName())) {
      boolean result = this.dataStore.addFileName(fileName);
      if (result)
        this.replicateDataObject(this.dataStore.retrieve(this.dataStore.getFileListName()));
      return result;
    } else {
      NodeDescriptor node = this.createNodeDescriptor(this.ch.get(this.dataStore.getFileListName()));
      return node.addFileName(fileName);
    }
  }

  @Override
  public boolean contains(String fileName) {
    if (this.amIMaster(this.dataStore.getFileListName())) {
      return this.dataStore.contains(fileName);
    } else {
      NodeDescriptor node = this.createNodeDescriptor(this.ch.get(this.dataStore.getFileListName()));
      return node.contains(fileName);
    }
  }

  @Override
  public List<String> getFileNames() {
    if (this.amIMaster(this.dataStore.getFileListName())) {
      return this.dataStore.getFileNames();
    } else {
      NodeDescriptor node = this.createNodeDescriptor(this.ch.get(this.dataStore.getFileListName()));
      return node.getFileNames();
    }
  }

  @Override
  public boolean removeFileName(String fileName) {
    if (this.amIMaster(this.dataStore.getFileListName())) {
      boolean result = this.dataStore.removeFileName(fileName);
      if (result)
        this.replicateDataObject(this.dataStore.retrieve(this.dataStore.getFileListName()));
      return result;
    } else {
      NodeDescriptor node = this.createNodeDescriptor(this.ch.get(this.dataStore.getFileListName()));
      return node.removeFileName(fileName);
    }
  }

}
