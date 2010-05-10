package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.util.ArrayList;
import java.util.logging.Logger;

public class SystemCommsServerImpl implements FileOperations {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private DataStore store = null;
  private RpcDispatcher dispatcher = null;
  private Node node = null;
  // private SystemFileList fileList;

  public SystemCommsServerImpl(Channel channel, DataStore store, MessageListener messages, MembershipListener members, Node node) {

    this.store = store;
    this.dispatcher = new RpcDispatcher(channel, messages, members, this);
    this.node = node;
    //  this.fileList = this;
  }

  public RpcDispatcher GetDispatcher() {
    return this.dispatcher;
  }

  @Override
  public Boolean store(DataObject dataObject) {
    this.logger.info("Requested to store: " + dataObject.getName());
    try {
      store.store(dataObject);
      // If I'm master make sure the thing gets replicated
      if (this.node.amIMaster(dataObject.getName())) {
        //  if (!this.fileList.contains(dataObject.getName()))
        //    this.fileList.addFileName(dataObject.getName());
        this.node.replicateDataObject(dataObject);
      }
    } catch (Exception ex) {
      return false;
    }
    return true;
  }

  @Override
  public DataObject retrieve(String name) {
    this.logger.info("Requested to retrieve: " + name);
    return store.retrieve(name);
  }

  public void stop() {
    dispatcher.stop();
  }

  @Override
  public Long getCRC(String fileName) {
    this.logger.info("Requested CRC: " + fileName);
    try {
      return store.retrieve(fileName).getCRC();
    } catch (Exception ex) {
      return null;
    }
  }

  @Override
  public boolean hasFile(String name) {
    this.logger.info("Requested hasFile: " + name);
    return store.hasFile(name);
  }

  @Override
  public ArrayList<String> list() {
    this.logger.info("Requested list.");
    return store.list();
//    ArrayList<String> result = new ArrayList<String>();
//    for (DataObject dataObj : store.list()) {
//      result.add(dataObj.getName());
//    }
//    return result;
  }

  @Override
  public boolean replace(DataObject dataObject) {
    this.logger.info("Requested to replaceDataObject: " + dataObject.getName());
    boolean result = store.replace(dataObject);
    // If I'm master make sure the thing get's replicated
    if (result && this.node.amIMaster(dataObject.getName())) {
      this.node.replicateDataObject(dataObject);
    }
    return result;
  }

  @Override
  public boolean delete(String name) {
    this.logger.info("Requested delete: " + name);
    boolean result = store.delete(name);
    //Make sure it's off the list
    //if(this.node.amIMaster(name) && this.fileList.contains(name)){
    //  this.fileList.removeFileName(name);
    //}
    // If I'm master make sure the thing get's replicated
    if (result && this.node.amIMaster(name)) {
      DataObject deleteObj = new DataObjectImpl(name, null);
      this.node.replicateDataObject(deleteObj);
    }
    return result;
  }
}
