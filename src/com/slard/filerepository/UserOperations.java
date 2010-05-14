package com.slard.filerepository;

import java.util.List;

/**
 * The Interface UserOperations.
 */
public interface UserOperations {

  /**
   * Checks if node is server.
   *
   * @return the boolean
   */
  Boolean isServer();

  /**
   * Gets the file names.
   *
   * @return the file names
   */
  List<String> getFileNames(String regex);

  /**
   * Store a data object.
   *
   * @param dataObject the data object
   * @return true, if successful
   */
  Boolean store(DataObject dataObject);

  Boolean storeAll(DataObject dataObject);

  /**
   * Retrieve a data object.
   *
   * @param name the name
   * @return the data object
   */
  DataObject retrieve(String name);

  /**
   * Replace a data object.
   *
   * @param dataObject the data object
   * @return true, if successful
   */
  boolean replace(DataObject dataObject);

  /**
   * Deletes a file by name.
   *
   * @param name the name
   * @return true, if successful
   */
  boolean delete(String name);


  interface Usage {
    Long getDiskFree();

    Long getFileTotals();

    String getHostname();
  }

  /**
   * Find the free and total disk space of a node (or the cluster).
   *
   * @return disk usage and hostname of node.
   */
  Usage getDiskSpace();
}

