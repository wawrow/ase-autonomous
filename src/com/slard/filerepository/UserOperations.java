package com.slard.filerepository;

import org.jgroups.Address;

public interface UserOperations {
  Boolean isServer();
  Address whoIsMaster(String name);
  Boolean store(DataObject dataObject);
  DataObject retrieve(String name);
  boolean replace(DataObject dataObject);
  boolean delete(String name);
}

