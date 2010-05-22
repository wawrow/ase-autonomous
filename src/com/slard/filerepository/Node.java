package com.slard.filerepository;

import com.google.inject.ImplementedBy;
import org.jgroups.Address;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;

import java.util.Properties;
import java.util.Set;

/**
 * The Interface Node.
 */
@ImplementedBy(NodeImpl.class)
public interface Node {

  /**
   * Starts the node.
   *
   * @throws ChannelException the channel exception
   */
  public abstract void start(Properties options) throws ChannelException;

  /**
   * Initialize data store.
   */
  // public abstract void initializeDataStore();

  /**
   * Replica guard.
   * This method loops to keep check the replica nodes and keep the replica count
   */
  public abstract void replicaGuard();

  /**
   * Node joined.
   * Event fired when node joins the system.
   */
  void nodeJoined(Address address);

  /**
   * Node left.
   * Event fired when node leaves the system.
   *
   * @param nodeAddress the node address
   * @param oldCh       Hash table state from before the node joins
   */
  void nodeLeft(Address nodeAddress);

  /**
   * Replicate data object.
   *
   * @param obj the obj
   */
  void replicateDataObject(DataObject obj);

  /**
   * Am i master of that key
   *
   * @param fileName the file name
   * @return true, if i am master
   */
  boolean amIMaster(String fileName);

  void update(Set<Address> members);

  void remove(Address address);

  DataStore getDataStore();

  SystemCommsClient getSystemComms();

  Address getMaster(String name);

  Set<Address> getReplicas(String name);

  void registerChannel(JChannel channel);

  int getReplicaCount();

}
