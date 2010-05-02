package com.slard.filerepository;

import java.util.Vector;

// Provides user methods for storing/retrieving/removing files
public interface ClientComs {

  //Stores DataObject in the System
  public abstract void Store(DataObject object);

  //Retrieves DataObject by name
  public abstract DataObject Retrieve(String name);

  //Retrieves of all available names from the system
  public abstract Vector<String> List();

  //Deletes DataObject from the System
  public abstract void Delete(String name);

}
