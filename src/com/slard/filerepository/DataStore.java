package com.slard.filerepository;

import com.google.inject.ImplementedBy;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Properties;

/**
 * The Interface DataStore.
 * Provides internal node interface into Data Store operations.
 */
@ImplementedBy(DataStoreImpl.class)
public interface DataStore extends FileOperations {

  void initialise(Properties options);

  /**
   * Gets the object store location
   *
   * @return the store location
   */
  //String getStoreLocation();

  /**
   * Fetches all Objects in this DataStore.
   *
   * @return the all data objects
   */
  Collection<DataObject> getAllDataObjects();

  /**
   * Returns the hostname of the data store.
   *
   * @return the hostname
   */
  String getHostname();

  void setLogger(Logger logger);
}
