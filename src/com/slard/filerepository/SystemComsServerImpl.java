package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

public class SystemComsServerImpl implements FileOperations, SystemFileList {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private DataStore store = null;
  private RpcDispatcher dispatcher = null;
  private Node node = null;
  private SystemFileList fileList;

  public SystemComsServerImpl(Channel channel, DataStore store, MessageListener messages, MembershipListener members, Node node,
      SystemFileList fileList) {

    this.store = store;
    this.dispatcher = new RpcDispatcher(channel, messages, members, this);
    this.node = node;
    this.fileList = fileList;
  }

  public RpcDispatcher GetDispatcher() {
    return this.dispatcher;
  }

  @Override
  public synchronized Boolean store(DataObject dataObject) {
    this.logger.info("Requested to store: " + dataObject.getName());
    try {
      store.store(dataObject);
      // If I'm master make sure the thing get's replicated
      if (this.node.amIMaster(dataObject.getName())) {
        if (!this.fileList.contains(dataObject.getName()))
          this.fileList.addFileName(dataObject.getName());
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
  public synchronized Vector<String> list() {
    this.logger.info("Requested list.");
    Vector<String> result = new Vector<String>();
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
    return store.delete(name) && this.fileList.removeFileName(name);
  }

  @Override
  public boolean addFileName(String fileName) {
    if(!this.node.amIMaster(this.store.getFileListName())) return false;
    return this.fileList.addFileName(fileName);
  }

  @Override
  public boolean contains(String fileName) {
    if(!this.node.amIMaster(this.store.getFileListName())) return false;
    return this.fileList.contains(fileName);
  }

  @Override
  public List<String> getFileNames() {
    if(!this.node.amIMaster(this.store.getFileListName())) return null;
    return this.fileList.getFileNames();
  }

  @Override
  public boolean removeFileName(String fileName) {
    if(!this.node.amIMaster(this.store.getFileListName())) return false;
    return this.fileList.removeFileName(fileName);
  }
}
