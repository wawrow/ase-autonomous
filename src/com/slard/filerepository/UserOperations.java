package com.slard.filerepository;

import java.util.List;

public interface UserOperations {
  Boolean isServer();
  List<String> getFileNames();
  Boolean store(DataObject dataObject);
  DataObject retrieve(String name);
  boolean replace(DataObject dataObject);
  boolean delete(String name);
}

