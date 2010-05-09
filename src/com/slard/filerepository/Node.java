package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.ChannelException;

/**
 * The Interface Node.
 */
public interface Node {

  /**
   * Starts the node.
   *
   * @throws ChannelException the channel exception
   */
  public abstract void start() throws ChannelException;

  /**
   * Initialize data store.
   */
  public abstract void initializeDataStore();

  /**
   * Replica guard.
   * This method loops to keep check the replica nodes and keep the replica count
   */
  public abstract void replicaGuard();

  /**
   * Node joined.
   * Event fired when node joins the system.
   *
   * @param node the node
   * @param oldCh Hash table state from before the node joins
   */
  void nodeJoined(NodeDescriptor node, ConsistentHashTable<Address> oldCh);

  /**
   * Node left.
   * Event fired when node leaves the system.
   *
   * @param nodeAddress the node address
   * @param oldCh Hash table state from before the node joins
   */
  void nodeLeft(Address nodeAddress, ConsistentHashTable<Address> oldCh);

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
  
  /**
   * Creates the node descriptor of node that is owner of given file.
   *
   * @param fileName the file name
   * @return the node descriptor
   */
  NodeDescriptor createNodeDescriptor(String fileName);
}
