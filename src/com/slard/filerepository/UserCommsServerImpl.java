package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

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
  public synchronized Boolean isServer() {
    return true;
  }

  public synchronized Address whoIsMaster(String name) {
    this.logger.info("A client has asked for master of: " + name);
    return node.ch.get(name);
  }

  public synchronized Address hasFile(String name) {
    this.logger.info("A client has enquired about: " + name);
    if (store.hasFile(name)) {
      return channel.getAddress();
    }
    return null;
  }
  
  @Override
  public synchronized Boolean store(DataObject dataObject) {
    this.logger.info("A client has requested to store: " + dataObject.getName());
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(dataObject.getName()));
    return nodeDescriptor.store(dataObject);
  }

  @Override
  public synchronized DataObject retrieve(String name) {
    this.logger.info("A client has requested to retrieve: " + name);
    
    // Try to get it from our own store first
    DataObject dataObject = store.retrieve(name);
    if (dataObject != null)
      return dataObject;
    
    // Ask the master otherwise
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(name));
    return nodeDescriptor.retrieve(name);
  }

  @Override
  public synchronized boolean replace(DataObject dataObject) {
    this.logger.info("A client has requested to replace: " + dataObject.getName());
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(dataObject.getName()));
    return nodeDescriptor.replace(dataObject);
  }

  @Override
  public synchronized boolean delete(String name) {
    this.logger.info("A client has requested to delete: " + name);
    
    // Send a delete to the master first
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(name));
    nodeDescriptor.delete(name);
    
    // Now delete from all the replicas
    for (Address nodeAddress : node.ch.getPreviousNodes(name, NodeImpl.REPLICA_COUNT)) {
      nodeDescriptor = node.createNodeDescriptor(nodeAddress);
      nodeDescriptor.delete(name);
    }
    return true;
  }
}
