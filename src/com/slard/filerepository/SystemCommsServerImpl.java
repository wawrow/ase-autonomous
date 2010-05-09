package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SystemCommsServerImpl implements FileOperations, SystemFileList {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private DataStore store = null;
  private RpcDispatcher dispatcher = null;
  private Node node = null;

  public SystemCommsServerImpl(Channel channel, DataStore store, MessageListener messages, MembershipListener members, Node node) {

    this.store = store;
    this.dispatcher = new RpcDispatcher(channel, messages, members, this);
    this.node = node;
  }

  public RpcDispatcher GetDispatcher() {
    return this.dispatcher;
  }

  @Override
  public synchronized Boolean store(DataObject dataObject) {
    this.logger.info("Requested to store: " + dataObject.getName());
    try {
      store.store(dataObject);
      // If I'm master make sure the thing gets replicated
      if (this.node.amIMaster(dataObject.getName())) {
        this.node.replicateDataObject(dataObject);
      }
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  @Override
  public synchronized DataObject retrieve(String name) {
    this.logger.info("Requested to retrieve: " + name);
    return store.retrieve(name);
  }

  public void stop() {
    dispatcher.stop();
  }

  @Override
  public synchronized Long getCRC(String fileName) {
    this.logger.info("Requested CRC: " + fileName);
    try {
      return store.retrieve(fileName).getCRC();
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  public synchronized boolean hasFile(String name) {
    this.logger.info("Requested hasFile: " + name);
    return store.hasFile(name);
  }

  @Override
  public synchronized ArrayList<String> list() {
    this.logger.info("Requested list.");
    ArrayList<String> result = new ArrayList<String>();
    for (DataObject dataObj : store.getAllDataObjects()) {
      result.add(dataObj.getName());
    }
    return result;
  }

  @Override
  public synchronized boolean replace(DataObject dataObject) {
    this.logger.info("Requested to replaceDataObject: " + dataObject.getName());
    return store.replace(dataObject);
  }

  @Override
  public synchronized boolean delete(String name) {
    this.logger.info("Requested delete: " + name);
    return store.delete(name);
  }

  // File List operations
  @Override
  public boolean addFileName(String fileName) {
    if (node.amIMaster(store.getFileListName())) {
      boolean result = store.addFileName(fileName);
      if (result)
        node.replicateDataObject(store.retrieve(store.getFileListName()));
      return result;
    } else {
      NodeDescriptor nodeDescriptor = node.createNodeDescriptor(store.getFileListName());
      return nodeDescriptor.addFileName(fileName);
    }
  }

  @Override
  public boolean contains(String fileName) {
    if (node.amIMaster(store.getFileListName())) {
      return store.contains(fileName);
    } else {
      NodeDescriptor nodeDescriptor = node.createNodeDescriptor(store.getFileListName());
      return nodeDescriptor.contains(fileName);
    }
  }

  @Override
  public List<String> getFileNames() {
    if (node.amIMaster(store.getFileListName())) {
      return store.getFileNames();
    } else {
      NodeDescriptor nodeDescriptor = node.createNodeDescriptor(store.getFileListName());
      return nodeDescriptor.getFileNames();
    }
  }

  @Override
  public boolean removeFileName(String fileName) {
    if (node.amIMaster(store.getFileListName())) {
      boolean result = store.removeFileName(fileName);
      if (result)
        node.replicateDataObject(store.retrieve(store.getFileListName()));
      return result;
    } else {
      NodeDescriptor nodeDescriptor = node.createNodeDescriptor(store.getFileListName());
      return nodeDescriptor.removeFileName(fileName);
    }
  }
}
