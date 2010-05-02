package com.slard.filerepository;

import java.util.Vector;

public interface DataObject {

  
  NodeDescriptor getMasterNode();
  void setMasterNode(NodeDescriptor node);
  
  Vector<NodeDescriptor> getReplicaNodes();
  
  void addReplicaNode(NodeDescriptor node);
  void removeReplicaNode(NodeDescriptor node);
  
  
  byte[] getData();

  String getName();

  Long getCRC();

  //Returns true if this object is master object, otherwise it's replica
  boolean isMaster();

  int getReplicaCount();

  //Returns an Array of locations of the file
  NodeDescriptor[] getLocations();
}
