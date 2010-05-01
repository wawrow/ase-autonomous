package com.slard.filerepository;

public interface DataObject {

  long getId();

  byte[] getData();

  String getName();

  Long getCRC();

  //Returns true if this object is master object, otherwise it's replica
  boolean isMaster();

  int getReplicaCount();

  //Returns an Array of locations of the file
  NodeDescriptor[] getLocations();
}
