package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

// TODO: Auto-generated Javadoc

/**
 * The Class Node Implementation.
 */
public class NodeImpl implements Node {
  /**
   * How many places on the Consistent Hash table would one node take.
   */
  private static final int CHT_TICK_COUNT = 4;

  /**
   * Replica count of files in the system
   */
  private static final int REPLICA_COUNT = 1;

  /**
   * logger.
   */
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * The Constant USER_CHANNEL_NAME - Name of the channel used in communicating with end users.
   */
  public static final String USER_CHANNEL_NAME = "FileRepositoryClusterClient";

  /**
   * The system communications implementation.
   */
  private SystemCommsClient systemComms = null;

  private UserOperations userComms = null;

  /**
   * The data store.
   */
  private DataStore dataStore;

  /**
   * The Consistent Hash Table.
   */
  public ConsistentHashTableImpl<Address> cht;

  /**
   * The options.
   */
  Properties options;

  /**
   * The user channel.
   */
  private Channel userChannel;

  /**
   * The replica guard timer.
   */
  private Timer replicaGuardTimer;

  private Address myAddress;

  /**
   * Instantiates a new node implementation.
   *
   * @param dataStore the data store
   * @param options   the options
   */
  public NodeImpl(DataStore dataStore, Properties options) {
    this.logger.setLevel(Level.ALL);
    this.dataStore = dataStore;
    this.cht = new ConsistentHashTableImpl<Address>(CHT_TICK_COUNT, null);
    this.options = options;
  }

  /**
   * {@inheritDoc}
   * Connects to the system
   * Initializes Data Store
   * Creates Client communications channel
   * Schedules replica guard runs.
   */
  public void start() throws ChannelException {
    systemComms = new SystemComms(this);
    myAddress = systemComms.getAddress();

    logger.fine("system channel connected and system coms server ready");
    logger.info("My Address: " + myAddress.toString());


    userComms = new UserCommsServer(this);

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
    userChannel.close();
  }

  @Override
  public int getReplicaCount() {
    return REPLICA_COUNT;
  }

  @Override
  public DataStore getDataStore() {
    return dataStore;
  }

  @Override
  public SystemCommsClient getSystemComms() {
    return systemComms;
  }

  @Override
  public Address getMaster(String name) {
    return cht.get(name);
  }

  @Override
  public Set<Address> getReplicas(String name) {
    return new HashSet<Address>(cht.getPreviousNodes(name, REPLICA_COUNT));
  }

  /**
   * {@inheritDoc}
   */
  public void replicaGuard() {
    logger.fine("replicaGuard tick.");
    for (DataObject file : dataStore.getAllDataObjects()) {
      String name = file.getName();
      if (amIMaster(name)) {
        replicateDataObject(file);
      } else if (!amIReplica(name)) {
        logger.fine("ensuring master has " + name);
        Address master = cht.get(name);
        if (file.getData() != null && !systemComms.hasFile(name, master)) {
          systemComms.store(file, master);
        }
        dataStore.delete(name);
      }
    }
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean amIMaster(String fileName) {
    return cht.get(fileName).equals(myAddress);
  }

  /**
   * Am i replica.
   *
   * @param filename the filename
   * @return true, if successful
   */
  private boolean amIReplica(String filename) {
    return cht.getPreviousNodes(filename, REPLICA_COUNT).contains(myAddress);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void replicateDataObject(DataObject obj) {
    Set<Address> replace = new HashSet<Address>(REPLICA_COUNT);
    Set<Address> store = new HashSet<Address>(REPLICA_COUNT);
    final String name = obj.getName();
    for (Address replica : cht.getPreviousNodes(name, REPLICA_COUNT)) {
      if (obj.getData() != null && systemComms.hasFile(name, replica)) {

        if (!systemComms.getCRC(name, replica).equals(obj.getCRC())) {
          logger.info("Replacing replica file " + name + " on " + replica.toString());
          replace.add(replica);
        }
      } else if (obj.getData() != null) {
        logger.info("Replicating file " + name + " on " + replica.toString());
        store.add(replica);
      }
    }
    if (!replace.isEmpty()) {
      systemComms.replace(obj, replace);
    }
    if (!store.isEmpty()) {
      systemComms.store(obj, store);
    }
  }

  /**
   * {@inheritDoc}
   * Checks whether there are any changes in the system that would make me or new node master of the files
   * if so - manages that.
   */
  @Override
  public void nodeJoined(Address address) {
    for (DataObject file : dataStore.getAllDataObjects()) {
      final String name = file.getName();
      // If the guy is master of any of my files
      if (cht.get(file.getName()).equals(myAddress)) {
        logger.info("Joining node " + address.toString() + " is master for " + name);
        // Check if i was master before
        if (cht.get(name).equals(myAddress)) {
          if (systemComms.hasFile(name, address)) {
            if (!systemComms.getCRC(name, address).equals(file.getCRC())) {
              systemComms.replace(file, address);
            }
          } else {
            systemComms.store(file, address);
          }
        }
        if (!cht.getPreviousNodes(name, REPLICA_COUNT).contains(myAddress)) {
          // If I'm not replica - delete that file
          try {
            dataStore.delete(name);
          } catch (Exception ex) {
            // TODO better exception handing
          }
        }
      } else if (amIMaster(name)) {
        // Check if he'll become a replica
        if (cht.getPreviousNodes(name, REPLICA_COUNT).contains(address)) {
          replicateDataObject(file);
        }
      }
    }
  }

  /**
   * {@inheritDoc}
   * Check whether I will become a master of any files that leaving node left behind
   * if so - takes ownership and manages replicas.
   */
  @Override
  public void nodeLeft(Address nodeAddress) {
    for (DataObject file : dataStore.getAllDataObjects()) {
      final String name = file.getName();
      // Was he master for any of my files?
      if (cht.get(name).equals(nodeAddress)) {
        // Am i Master now?
        if (amIMaster(name)) {
          replicateDataObject(file);
        }
      }
      // Was he a replica for my file?
      else if (cht.getPreviousNodes(name, REPLICA_COUNT).contains(nodeAddress)) {
        replicateDataObject(file);
      }
    }
  }

  /**
   * {@inheritDoc}
   * Joined/Left the network - synchronized causes less problems and solves lots
   * Fires relevant events when node leaves the system - when node joins it waits for
   * joinedAndInitialized message - to avoid mess and allow node to sort out it's internal
   * state first.
   */
  @Override
  public void update(Set<Address> members) {
    logger.fine("ViewAccepted");

    // find nodes that joined
    ConsistentHashTable.Changes<Address> changes = cht.update(members);
    for (Address address : changes.getAdded()) {
      nodeJoined(address);
    }
    for (Address address : changes.getRemoved()) {
      nodeLeft(address);
    }
  }

  /**
   * {@inheritDoc}
   * Suspect that node has left the network
   * fires appropriate nodeLeft event.
   */
  @Override
  public void remove(Address address) {
    logger.info("Suspecting node: " + address.toString());
    if (cht.contains(address)) {
      cht.remove(address);
      nodeLeft(address);
    }
  }
}
