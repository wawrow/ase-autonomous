package com.slard.filerepository;

import java.util.ArrayList;

// Provides user methods for storing/retrieving/removing files
public interface ClientComs {
  //Stores DataObject in the System
  void Store(DataObject object);

  //Retrieves DataObject by name
  DataObject Retrieve(String name);

  //Retrieves of all available names from the system
  ArrayList<String> List();

  //Deletes DataObject from the System
  void Delete(String name);
}
