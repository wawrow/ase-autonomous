package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.util.NotifyingFuture;
import org.jgroups.util.Tuple;

import java.util.Collection;

/**
 * The Interface UserOperations.
 */
public interface UserOperations {

  /**
   * Checks if node is server.
   *
   * @return the boolean
   */
  Boolean isServer(Address address);

  /**
   * Gets the file names.
   *
   * @return the file names
   */
  Collection<String> getFileNames(String regex);

  /**
   * Store a data object.
   *
   * @param file the data object
   * @return true, if successful
   */
  Boolean store(DataObject file, Address address);

  Boolean storeAll(DataObject file, Address address);

  NotifyingFuture<Object> storeAllAsync(DataObject file, Address address);

  /**
   * Retrieve a data object.
   *
   * @param name the name
   * @return the data object
   */
  DataObject retrieve(String name, Address address);

  /**
   * Replace a data object.
   *
   * @param file the data object
   * @return true, if successful
   */
  boolean replace(DataObject file, Address address);

  /**
   * Deletes a file by name.
   *
   * @param name the name
   * @return true, if successful
   */
  boolean delete(String name, Address address);

  // Broadcast isMaster(fileName) to all members and take the first non-null response

  Address getMaster(String name);

  // Broadcast hasFile(fileName) to all members and take the first non-null response

  Address getQuickestFileLocation(String name);

  Channel getChannel();

  void setTimeout(int timeout);

  /**
   * Find the free and total disk space of a node (or the cluster).
   *
   * @return disk usage and hostname of node.
   */
  Usage getDiskSpace();

  Tuple<Collection<Address>, Collection<Address>> listNodes();
}

