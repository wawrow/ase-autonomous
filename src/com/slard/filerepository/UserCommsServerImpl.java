package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.io.File;
import java.util.List;
import java.util.logging.Logger;

public class UserCommsServerImpl implements UserOperations {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private DataStore store = null;
  private RpcDispatcher rpcDispatcher = null;
  private NodeImpl node = null;
  private Channel channel = null;

  public UserCommsServerImpl(Channel channel, DataStore store, MessageListener messages, MembershipListener members, NodeImpl node) {
    this.channel = channel;
    this.store = store;
    this.rpcDispatcher = new RpcDispatcher(channel, messages, members, this);
    this.node = node;
  }

  public RpcDispatcher GetDispatcher() {
    return this.rpcDispatcher;
  }

  public void stop() {
    rpcDispatcher.stop();
  }

  @Override
  public Boolean isServer() {
    return true;
  }

  // Clients may ask about whether this node is the master of a file
  public Address isMaster(String name) {
    this.logger.info("A client has enquired if we are the master of: " + name);
    if (node.amIMaster(name))
      return channel.getAddress();
    return null;
  }

  // Clients may ask about file ownership before directing their requests
  public Address hasFile(String name) {
    this.logger.info("A client has enquired if we have file: " + name);
    if (store.hasFile(name)) {
      Address address = channel.getAddress();
      System.out.println("Returning " + address.toString() + " type=" + ((Object) address).getClass().getName().toString());
      return channel.getAddress();
    }
    return null;
  }

  @Override
  public List<String> getFileNames() {
    this.logger.info("A client has requested the list of files");
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(store.getFileListName()));
    List<String> ret = nodeDescriptor.getFileNames();
    logger.info("We have " + ret.size() + " filed.");
    return ret;
  }

  @Override
  public Boolean store(DataObject dataObject) {
    // The client should have directed this at the master, but we double check    
    this.logger.info("A client has requested to store: " + dataObject.getName());
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(dataObject.getName()));
    return nodeDescriptor.store(dataObject);
  }

  @Override
  public DataObject retrieve(String name) {
    // Try from our own store first - should work because clients target the master 
    this.logger.info("A client has requested to retrieve: " + name);
    DataObject dataObject = store.retrieve(name);
    if (dataObject != null)
      return dataObject;

    // Otherwise we can ask the current master
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(name));
    return nodeDescriptor.retrieve(name);
  }

  @Override
  public boolean replace(DataObject dataObject) {
    // The client should have directed this at the master, but we double check    
    this.logger.info("A client has requested to replace: " + dataObject.getName());
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(dataObject.getName()));
    return nodeDescriptor.replace(dataObject);
  }

  @Override
  public boolean delete(String name) {
    this.logger.info("A client has requested to delete: " + name);
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(name));
    nodeDescriptor.delete(name);

    // Now delete from all the replicas also
    for (Address nodeAddress : node.ch.getPreviousNodes(name, NodeImpl.REPLICA_COUNT)) {
      nodeDescriptor = node.createNodeDescriptor(nodeAddress);
      nodeDescriptor.delete(name);
    }
    return true;
  }

  @Override
  public DiskSpace getDiskSpace() {
    return new DiskSpace(store.getHostname(), File.listRoots()[0].getUsableSpace(),
        File.listRoots()[0].getTotalSpace());
  }
}
