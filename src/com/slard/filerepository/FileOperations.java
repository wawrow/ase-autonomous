package com.slard.filerepository;

import java.util.ArrayList;


/**
 * The Interface FileOperations.
 * Provides methods to call to other nodes in the system.
 */
public interface FileOperations {

  /**
   * Stores a file in nodes data storage.
   *
   * @param dataObject the data object to store
   * @return the boolean - was the action sucecsfull
   */
  Boolean store(DataObject dataObject);

  /**
   * Retrieve a file from nodes storage
   *
   * @param name the name
   * @return the data object
   */
  DataObject retrieve(String name);

  /**
   * Lists Files that this node has.
   *
   * @return the array list
   */
  ArrayList<String> list();

  /**
   * Checks whether file exists.
   *
   * @param name the name
   * @return true, if successful
   */
  boolean hasFile(String name);

  /**
   * Gets the CRC.
   *
   * @param fileName the file name
   * @return the cRC
   */
  Long getCRC(String fileName);

  /**
   * Replace current data object.
   *
   * @param dataObject the data object
   * @return true, if successful
   */
  boolean replace(DataObject dataObject);

  /**
   * Deletes the object by name.
   *
   * @param name the name
   * @return true, if successful
   */
  boolean delete(String name);

}
