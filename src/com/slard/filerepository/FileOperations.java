package com.slard.filerepository;

import java.util.ArrayList;

// Provides methods to call to other nodes in the system
public interface FileOperations {

  //Stores a file in nodes data storage
  Boolean store(DataObject dataObject);

  //Retrieve a file from nodes storage
  DataObject retrieve(String name);

  //Lists Files that this node has
  ArrayList<String> list();

  boolean hasFile(String name);

  Long getCRC(String fileName);

  boolean replace(DataObject dataObject);

  boolean delete(String name);

}
