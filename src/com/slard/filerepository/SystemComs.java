package com.slard.filerepository;

import java.util.Map;
import java.util.Vector;


// Provides methods to call to other nodes in the system
public interface SystemComs {

  //Returns a list of known nodes
  Map<Long, NodeDescriptor> getNodelist();

  //Returns file list of files that are in nodes data storage
  Vector<String> syncFilelist();

  //Stores a file in nodes data storage
  Boolean store(DataObject dataObject);

  //Retrieve a file from nodes storage
  DataObject retrieve(String name);
}
