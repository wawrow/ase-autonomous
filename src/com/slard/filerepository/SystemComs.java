package com.slard.filerepository;

import java.util.Vector;
import java.util.zip.CRC32;

// Provides methods to call to other nodes in the system
public interface SystemComs {

  //Stores a file in nodes data storage
  Boolean store(DataObject dataObject);

  //Retrieve a file from nodes storage
  DataObject retrieve(String name);
  
  //Lists Files that this node has
  Vector<String> list();
  
  boolean hasFile(String name);
  
  CRC32 getCRC(String fileName);
  
}
