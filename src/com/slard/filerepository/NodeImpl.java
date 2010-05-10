package com.slard.filerepository;

import org.jgroups.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc

/**
 * The Class Node Implementation.
 */
public class NodeImpl implements Node, MessageListener, MembershipListener {

  /**
   * The Constant CH_REPLICA_COUNT - Replica count of how many places on the Consistent Hash table would one node take.
   */
  private static final int CH_REPLICA_COUNT = 4;

  /**
   * The Constant REPLICA_COUNT - Replica count of files in the system
   */
  public static final int REPLICA_COUNT = 1;

  /**
   * The Constant JOINED_AND_INITIALIZED - Message title of message that's being sent after node has joined and initialized it's internals
   */
  private static final String JOINED_AND_INITIALIZED = "joinedAndInitialized";

  /**
   * The logger.
   */
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * The Constant SYSTEM_CHANNEL_NAME - Name of the channel used in system communications.
   */
  private static final String SYSTEM_CHANNEL_NAME = "FileRepositoryCluster";

  /**
   * The Constant USER_CHANNEL_NAME - Name of the channel used in communicating with end users.
   */
  public static final String USER_CHANNEL_NAME = "FileRepositoryClusterClient";

  /**
   * The system communications implementation.
   */
  SystemCommsServerImpl systemComms = null;

  /**
   * The user commumications implementation.
   */
  UserCommsServerImpl userComms = null;

  /**
   * The data store.
   */
  private DataStore dataStore;

  /**
   * The Consistent Hash Table.
   */
  public ConsistentHashTableImpl<Address> ch;

  /**
   * The options.
   */
  Properties options;

  /**
   * The state.
   */
  byte[] state;

  /**
   * The system channel.
   */
  private Channel systemChannel;

  /**
   * The user channel.
   */
  private Channel userChannel;

  /**
   * The replica guard timer.
   */
  private Timer replicaGuardTimer;

  /**
   * Instantiates a new node implementation.
   *
   * @param dataStore the data store
   * @param options   the options
   */
  public NodeImpl(DataStore dataStore, Properties options) {
    this.logger.setLevel(Level.ALL);
    this.dataStore = dataStore;
    this.ch = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, null);
    this.options = options;
  }

  /**
   * {@inheritDoc}
   */
  public NodeDescriptor createNodeDescriptor(String fileName) {
    return createNodeDescriptor(ch.get(fileName));
  }

  /**
   * Creates and return a node descriptor for the specified address.
   *
   * @param address the address
   * @return the node descriptor
   */
  public NodeDescriptor createNodeDescriptor(Address address) {
    SystemCommsClientImpl systemCommsClient = SystemCommsClientImpl.getSystemComsClient(this.systemComms
        .GetDispatcher(), address);
    NodeDescriptor node = new NodeDescriptorImpl(address, systemCommsClient);
    return node;
  }

  /**
   * {@inheritDoc}
   * Connects to the system
   * Initializes Data Store
   * Creates Client communications channel
   * Schedules replica guard runs.
   */
  public void start() throws ChannelException {
    // Channel for system communications (within the cluster)
    this.systemChannel = new JChannel();
    systemChannel.connect(SYSTEM_CHANNEL_NAME);
    systemComms = new SystemCommsServerImpl(systemChannel, dataStore, this, this, this);

    logger.fine("channel connected and system coms server ready");
    logger.finer("My Address: " + systemChannel.getAddress().toString());
    for (Address addr : this.systemChannel.getView().getMembers()) {
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

  /**
   * Stops the node.
   */
  public void stop() {
    systemComms.stop();
    systemChannel.close();
    userComms.stop();
    userChannel.close();
  }

  /**
   * {@inheritDoc}
   */
  public void replicaGuard() {
    logger.info("replicaGuard tick.");
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      String fname = obj.getName();
      if (this.amIMaster(fname)) {
        this.replicateDataObject(obj);
      } else if (!amIReplica(fname)) {
        logger.fine("ensuring master has " + obj.getName());
        NodeDescriptor node = this.createNodeDescriptor(ch.get(obj.getName()));
        if (obj.getData() != null && !node.hasFile(obj.getName())) {
          node.store(obj);
        }
        dataStore.delete(fname);
      }
    }
  }

  /**
   * {@inheritDoc}
   * Moves the files around the system initially to it's masters
   * also replicates the files this node will be master for
   * Finally sends out joinAndInitalized message
   */
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
      //if (this.amIMaster(obj.getName()) && !this.systemComms.contains(obj.getName())) {
      //  this.systemComms.addFileName(obj.getName());
      //}
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

  /**
   * {@inheritDoc}
   * Checks whether there are any changes in the system that would make me or new node master of the files
   * if so - manages that.
   */
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

  /**
   * {@inheritDoc}
   * Check whether I will become a master of any files that leaving node left behind
   * if so - takes ownership an manages replicas.
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean amIMaster(String fileName) {
    return this.ch.get(fileName).equals(this.systemChannel.getAddress());
  }

  /**
   * Am i replica.
   *
   * @param filename the filename
   * @return true, if successful
   */
  private boolean amIReplica(String filename) {
    return ch.getPreviousNodes(filename, REPLICA_COUNT).contains(systemChannel.getAddress());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void replicateDataObject(DataObject obj) {
    logger.fine("Replicating file: " + obj.getName());
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

  /**
   * {@inheritDoc}
   * Manages messages sent to the system particulary JoinedAndInitialized messages
   * of nodes joining the system. Fires the nodeJoined once this message is received.
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getState() {
    return state;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setState(byte[] bytes) {
    // Not totally sure what messages we can receive, probably broadcast of
    // system state (disk space etc)
    // probably in rdf.
    state = bytes;
  }

  /**
   * {@inheritDoc}
   * Joined/Left the network - synchronized causes less problems and solves lots
   * Fires relevant events when node leaves the system - when node joins it waits for
   * joinedAndInitialized message - to avoid mess and allow node to sort out it's internal
   * state first.
   */
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

    // Now update new consistent hash
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

  /**
   * {@inheritDoc}
   * Suspect that node has left the network
   * fires appropriate nodeLeft event.
   */
  @Override
  public void suspect(Address address) {
    logger.info("Suspecting node: " + address.toString());
    if (this.ch.contains(address)) {
      ConsistentHashTable<Address> oldCh = new ConsistentHashTableImpl<Address>(CH_REPLICA_COUNT, this.ch.getAllValues());
      this.ch.remove(address);
      this.nodeLeft(address, oldCh);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void block() {
    // probably can be left empty.
  }
}
