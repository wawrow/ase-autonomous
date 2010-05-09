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
  List<String> getFileNames();
  
  /**
   * Store a data object.
   *
   * @param dataObject the data object
   * @return true, if successful
   */
  Boolean store(DataObject dataObject);
  
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
}

