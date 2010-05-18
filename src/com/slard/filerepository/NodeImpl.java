package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.jmx.JmxConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;

// TODO: Auto-generated Javadoc

/**
 * The Class Node Implementation.
 */
public class NodeImpl implements Node {
  /**
   * How many places on the Consistent Hash table would one node take.
   */
  private static final int CHT_TICK_COUNT = 10;

  /**
   * Replica count of files in the system
   */
  private static final int REPLICA_COUNT = 2;

  /**
   * logger.
   */
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * The system communications implementation.
   */
  private SystemCommsClient systemComms = null;

  private UserCommsInterface userComms = null;

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
  private String name;

  /**
   * Instantiates a new node implementation.
   *
   * @param dataStore the data store
   * @param options   the options
   */
  public NodeImpl(DataStore dataStore, Properties options) {
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
    systemComms = new SystemComms(this, options);
    myAddress = systemComms.getAddress();
    cht.add(myAddress);
    logger.trace("system channel connected and system coms server ready");
    logger.info("My Address: " + myAddress.toString());

    userComms = new UserCommsServer(this, options);
    logger.trace("User channel connected and user coms server ready");

    replicaGuardTimer = new Timer();
    replicaGuardTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        replicaGuard();
      }
    }, 15000, 30000);

    update(new HashSet<Address>(systemComms.getChannel().getView().getMembers()));
  }

  public void registerChannel(JChannel channel) {
    try {
      JmxConfigurator.registerChannel(channel, ManagementFactory.getPlatformMBeanServer(),
          "JGroups." + channel.getClusterName());
    } catch (Exception e) {
      logger.warn("unable to register channel {} with JMX", channel.getName());
    }
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
    logger.trace("replicaGuard tick.");
    //update(new HashSet<Address>(systemComms.getChannel().getView().getMembers()));
    for (DataObject file : dataStore.getAllDataObjects()) {
      String name = file.getName();
      if (amIMaster(name)) {
        logger.trace("master for {}, replicating", name);
        replicateDataObject(file);
      } else {
        Address master = cht.get(name);
        logger.debug("ensuring master {} has {}", master.toString(), name);
        try {
          if (file.getData() != null && !systemComms.hasFile(name, master)) {
            logger.warn("need to send {} to master {}", name, master.toString());
            systemComms.store(file, master);
          }
        } catch (IOException e) {
          logger.warn("couldn't get data for {} {}", name, e.toString());
        }
        if (!amIReplica(name)) {
          dataStore.delete(name);
        }
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
    logger.trace("previous nodes are {}", cht.getPreviousNodes(name, REPLICA_COUNT).toString());
    for (Address replica : cht.getPreviousNodes(name, REPLICA_COUNT)) {
      logger.trace("Checking replica {}", replica.toString());
      byte[] data;
      try {
        data = obj.getData();
      } catch (IOException e) {
        logger.warn("unable to get data for {} {}", name, e);
        data = null;
      }
      if (data != null && systemComms.hasFile(name, replica)) {
        Long crc = systemComms.getCRC(name, replica);
        if (crc == null) {
          logger.warn("failed to check crc of file {} on {}", name, replica.toString());
          continue;
        }
        if (!crc.equals(obj.getCRC())) {
          logger.info("Replacing replica file " + name + " on " + replica.toString());
          replace.add(replica);
        } else {
          logger.trace("{} already has a matching copy of {}", replica.toString(), name);
        }
      } else if (data != null) {
        logger.info("Replicating file {} on {}", name, replica.toString());
        store.add(replica);
      } else {
        logger.error("file {} has no content.", name);
      }
    }
    if (replace.isEmpty() && store.isEmpty()) {
      logger.trace("no need to replicate {}", name);
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
    if (address.equals(systemComms.getAddress())) {
      return;  // no need to update me.
    }
    cht.add(address);
    for (DataObject file : dataStore.getAllDataObjects()) {
      final String name = file.getName();
      // If the guy is master of any of my files
      if (cht.get(name).equals(address)) {
        logger.info("Joining node {} is master for {}", address.toString(), name);
        // Check if i was master before
        if (cht.get(name).equals(myAddress)) {
          if (systemComms.hasFile(name, address)) {
            if (!(systemComms.getCRC(name, address).equals(file.getCRC()))) {
              systemComms.replace(file, address);
            }
          } else {
            logger.warn("sending {} to {}", file.getName(), address.toString());
            systemComms.store(file, address);
          }
        }
        if (!(cht.getPreviousNodes(name, REPLICA_COUNT).equals(myAddress))) {
          // If I'm not replica - delete that file
          try {
            dataStore.delete(name);
          } catch (Exception ex) {
            logger.warn("unable to delete file {}", name);
          }
        }
      } else if (amIMaster(name)) {
        // Check if he'll become a replica
        if (cht.getPreviousNodes(name, REPLICA_COUNT).equals(address)) {
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
    if (nodeAddress.equals(systemComms.getAddress())) {
      return; // no need to remove me.
    }
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
    logger.trace("ViewAccepted");

    members.add(systemComms.getAddress()); // make sure I'm in.
    // find nodes that joined
    ConsistentHashTable.Changes<Address> changes = cht.update(members);
    logger.trace("got {} new nodes", changes.getAdded().size());
    for (Address address : changes.getAdded()) {
      nodeJoined(address);
    }
    logger.trace("{} nodes left", changes.getRemoved().size());
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
    if (address.equals(systemComms.getAddress())) {
      return; // no need to remove me.
    }
    logger.info("Suspecting node: " + address.toString());
    if (cht.contains(address)) {
      cht.remove(address);
      nodeLeft(address);
    }
  }
}
