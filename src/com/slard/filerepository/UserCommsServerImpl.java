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
  private RpcDispatcher dispatcher = null;
  private NodeImpl node = null;

  public UserCommsServerImpl(Channel channel, DataStore store, MessageListener messages, MembershipListener members, NodeImpl node) {

    this.store = store;
    this.dispatcher = new RpcDispatcher(channel, messages, members, this);
    this.node = node;
  }

  public RpcDispatcher GetDispatcher() {
    return this.dispatcher;
  }

  public void stop() {
    dispatcher.stop();
  }

  @Override
  public synchronized Boolean store(DataObject dataObject) {
    this.logger.info("Requested to store: " + dataObject.getName());
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(dataObject.getName()));
    return nodeDescriptor.store(dataObject);
  }

  @Override
  public synchronized DataObject retrieve(String name) {
    this.logger.info("Requested to retrieve: " + name);
    
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
    this.logger.info("Requested to replaceDataObject: " + dataObject.getName());
    NodeDescriptor nodeDescriptor = node.createNodeDescriptor(node.ch.get(dataObject.getName()));
    return nodeDescriptor.replace(dataObject);
  }

  @Override
  public synchronized boolean delete(String name) {
    this.logger.info("Requested delete: " + name);
    
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
