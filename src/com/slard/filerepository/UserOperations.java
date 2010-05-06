package com.slard.filerepository;

import java.util.List;

import org.jgroups.Address;

public interface UserOperations {
  Boolean isServer();
  List<String> getFileNames();
  Address whoIsMaster(String name);
  Boolean store(DataObject dataObject);
  DataObject retrieve(String name);
  boolean replace(DataObject dataObject);
  boolean delete(String name);
}

