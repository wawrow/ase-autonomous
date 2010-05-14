package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class UserCommsServer implements UserOperations {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "ClientCluster";
  private RpcDispatcher dispatcher = null;
  private Node parent = null;
  private Address myAddress;

  public UserCommsServer(Node parent) {
    this.parent = parent;
    System.setProperty("jgroups.udp.mcast_port", "45589");
    RpcDispatcher tmp;
    try {
      Channel channel = new JChannel(CHANNEL_NAME);
      myAddress = channel.getAddress();
      tmp = new RpcDispatcher(channel, null, null, this);
    } catch (ChannelException e) {
      tmp = null;
    }
    dispatcher = tmp;
  }

  @Override
  public Boolean isServer() {
    return true;
  }

  // Clients may ask about whether this node is the master of a file
  public Address isMaster(String name) {
    logger.fine("Are we master of " + name);
    return parent.getMaster(name);
  }

  // Clients may ask about file ownership before directing their requests
  public Address hasFile(String name) {
    logger.fine("Do we have file " + name);
    if (parent.getDataStore().hasFile(name)) {
      return myAddress;
    }
    return null;
  }

  @Override
  public List<String> getFileNames(String regex) {
    logger.fine("List files");
    Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    List<String> ret = new LinkedList<String>();
    for (String name : parent.getDataStore().list()) {
      if (pattern.matcher(name).matches()) {
        ret.add(name);
      }
    }
    return ret;
  }

  @Override
  public Boolean store(DataObject file) {
    // The client should have directed this at the master, but we double check    
    logger.fine("Client store " + file.getName());
    return parent.getSystemComms().store(file, parent.getMaster(file.getName()));
  }

  @Override
  public Boolean storeAll(DataObject file) {
    // The client should have directed this at the master, but we double check
    logger.fine("Client storeAll " + file.getName());
    parent.getSystemComms().store(file, parent.getMaster(file.getName()));
    parent.replicateDataObject(file);
    return true;
  }

  @Override
  public DataObject retrieve(String name) {
    // Try from our own store first - should work because clients target the master 
    logger.fine("Retrieve: " + name);
    DataObject file = parent.getDataStore().retrieve(name);
    if (file != null)
      return file;
    return parent.getSystemComms().retrieve(name, parent.getMaster(name));
  }

  @Override
  public boolean replace(DataObject file) {
    // The client should have directed this at the master, but we double check    
    logger.fine("replace: " + file.getName());
    return parent.getSystemComms().replace(file, parent.getMaster(file.getName()));
  }

  @Override
  public boolean delete(String name) {
    this.logger.info("delete: " + name);
    Set<Address> copies = new HashSet<Address>();
    copies.add(parent.getMaster(name));
    copies.addAll(parent.getReplicas(name));
    return parent.getSystemComms().delete(name, copies);
  }

  @Override
  public Usage getDiskSpace() {
    return new Usage() {
      public String getHostname() {
        return parent.getDataStore().getHostname();
      }

      public Long getDiskFree() {
        return File.listRoots()[0].getUsableSpace();
      }

      public Long getFileTotals() {
        long ret = 0;
        for (DataObject file : parent.getDataStore().getAllDataObjects()) {
          ret += file.getSize();
        }
        return ret;
      }
    };
  }
}
