package com.slard.filerepository;

public interface UserOperations {
  Boolean store(DataObject dataObject);
  DataObject retrieve(String name);
  boolean replace(DataObject dataObject);
  boolean delete(String name);
}

