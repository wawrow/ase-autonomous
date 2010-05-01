package com.slard.filerepository;

import org.jgroups.Address;

// Provides methods to call to other nodes in the system
public interface SystemComsClient {

  //Stores a file in nodes data storage
  Boolean store(DataObject dataObject, Address target);

  //Retrieve a file from nodes storage
  DataObject retrieve(String name, Address target);
}
