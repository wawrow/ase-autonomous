package com.slard.filerepository;

import org.jgroups.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeImpl implements Node, MessageListener, MembershipListener {
  private static final int CH_REPLICA_COUNT = 4;
  private static final int REPLICA_COUNT = 1;
  private static final String JOINED_AND_INITIALIZED = "joinedAndInitialized";
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "FileRepositoryCluster";

  SystemComsServerImpl systemComs = null;
  private DataStore dataStore;
  private ConsistentHashTableImpl<Address> ch;
  Properties options;
  byte[] state;
  private Channel commonChannel;

  private Timer replicaGuardTimer;

  // Constructor
  public NodeImpl(DataStore dataStore, Properties options) {
    this.logger.setLevel(Level.ALL);
    this.dataStore = dataStore;
    this.ch = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, null);
    this.options = options;
  }

  private NodeDescriptor createNodeDescriptor(Address address) {
    NodeDescriptor node = new NodeDescriptorImpl(address, SystemComsClientImpl.getSystemComsClient(this.systemComs
        .GetDispatcher(), address));
    return node;
  }
  
  public void start() throws ChannelException {
//    this.commonChannel = new JChannel("mping.xml");
    this.commonChannel = new JChannel();
    commonChannel.connect(CHANNEL_NAME);

    systemComs = new SystemComsServerImpl(commonChannel, dataStore, this, this, this);

    logger.fine("channel connected and system coms server ready");
    logger.finer("My Address: " + commonChannel.getAddress().toString());

    for(Address addr: this.commonChannel.getView().getMembers()){
      this.ch.add(addr);
    }

    this.initializeDataStore();

    this.replicaGuardTimer = new Timer();
    replicaGuardTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        replicaGuard();
      }
    }, 15000, 30000);

  }

  public void stop() {
    systemComs.stop();
    commonChannel.close();
  }

  public void replicaGuard() {
    System.out.println("Replica Guardian!!!");
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

        oldAddr.remove(this.commonChannel.getAddress());
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
    }

    System.out.println("Now send the message");
    // If i'm not the first in cluster - sent the message that I'm ready to go
    if (this.commonChannel.getView().size() > 1) {
      try {
        this.commonChannel.send(new Message(null, null, JOINED_AND_INITIALIZED));
        System.out.println("message sent");
      } catch (Exception ex) {
        System.out.println(ex.toString());
        // make me crash!!!
      }
    }
  }

  @Override
  public void nodeJoined(NodeDescriptor node, ConsistentHashTable<Address> oldCh) {
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      // If the guy is master of any of my files
      System.out.println("Checking file " + obj.getName());
      if (this.ch.get(obj.getName()).equals(node.getAddress())) {
        System.out.println("Joining node is master for " + obj.getName());
        // Check if i was master before
        if (oldCh.get(obj.getName()).equals(this.commonChannel.getAddress())) {
          if (node.hasFile(obj.getName())) {
            if (!node.getCRC(obj.getName()).equals(obj.getCRC())) {
              node.replace(obj);
            }
          } else {
            node.store(obj);
          }
        }
        if (!this.ch.getPreviousNodes(obj.getName(), REPLICA_COUNT).contains(this.commonChannel.getAddress())) {
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
    return this.ch.get(fileName).equals(this.commonChannel.getAddress());
  }

  @Override
  public void replicateDataObject(DataObject obj) {
    logger.fine("Replicating file: " + obj.getName() + " ch " + this.ch.getAllValues().size());
    for (Address nodeAddress : this.ch.getPreviousNodes(obj.getName(), REPLICA_COUNT)) {
      logger.fine("Replicating file: " + obj.getName() + " to " + nodeAddress);
      NodeDescriptor node = this.createNodeDescriptor(nodeAddress);
      if (node.hasFile(obj.getName())) {

        if (!node.getCRC(obj.getName()).equals(obj.getCRC())) {
          node.replace(obj);
        }
      } else {
        node.store(obj);
      }
    }
  }


  //JGroups related implementation
  
  @Override
  public synchronized void receive(Message message) {
    if (message.getSrc() == this.commonChannel.getAddress()) {
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


}
