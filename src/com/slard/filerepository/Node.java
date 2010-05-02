package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.ChannelException;

public interface Node {

  //Below will be used in Template Methods in abstract class for NodeImpl
  public abstract void start() throws ChannelException;

  public abstract void initializeDataStore();

  //This method will loop to keep check the replica nodes and keep the replica count
  public abstract void replicaGuard();

  void nodeJoined(NodeDescriptor node, NewBetterCHT<Address> oldCh);

  void nodeLeft(Address nodeAddress, NewBetterCHT<Address> oldCh);

  void replicateDataObject(DataObject obj);

  boolean amIMaster(String fileName);

  
  
}
