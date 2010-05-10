package com.slard.filerepository;

import java.util.ArrayList;

/**
 * The Interface DataStore.
 * Provides internal node interface into Data Store operations.
 */
public interface DataStore extends FileOperations {

  void initialise();

  /**
   * Gets the object store location
   *
   * @return the store location
   */
  String getStoreLocation();

  /**
   * Fetches all Objects in this DataStore.
   *
   * @return the all data objects
   */
  ArrayList<DataObject> getAllDataObjects();

  /**
   * Returns the hostname of the data store.
   *
   * @return the hostname
   */
  String getHostname();
}
