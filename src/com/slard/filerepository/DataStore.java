package com.slard.filerepository;

import java.io.IOException;
import java.util.ArrayList;

// Provides internal node interface into Data Store operations
public interface DataStore {

  // Gets the object store location
  public String getStoreLocation();
  
  // Fetches all Objects
  public ArrayList<DataObject> getAllDataObjects();

  // Gets object by name from storage
  public abstract DataObject getDataObject(String name) throws IOException;

  // Removes object from DataStore
  public abstract void deleteDataObject(String name) throws IOException;

  // Stores object in DataStore
  public abstract void storeDataObject(DataObject dataObject) throws IOException, DataObjectExistsException;

  // Replaces object in DataStore
  public abstract void replaceDataObject(DataObject dataObject) throws Exception;

  // Returns whether particular object is contained in storage
  public abstract Boolean contains(String name);
}
