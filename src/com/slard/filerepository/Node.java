package com.slard.filerepository;

import org.jgroups.ChannelException;

public interface Node {

  //Below will be used in Template Methods in abstract class for NodeImpl
  public abstract void start() throws ChannelException;

  public abstract void initializeDataStore();

  //This method will loop to keep check the replica nodes and keep the replica count
  public abstract void replicaGuard();

  void nodeLeft(NodeDescriptor node);
  void nodeJoined(NodeDescriptor node);
  
}
