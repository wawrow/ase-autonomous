package com.slard.filerepository;

// Provides methods to call to other nodes in the system
public interface SystemComsServer {

  //Stores a file in nodes data storage
  Boolean store(DataObject dataObject);

  //Retrieve a file from nodes storage
  DataObject retrieve(String name);
}
