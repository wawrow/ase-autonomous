package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class UserCommsServer implements UserCommsInterface {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private Node parent = null;
  private Address myAddress;

  public UserCommsServer(Node parent) {
    this.parent = parent;
    System.setProperty("jgroups.udp.mcast_port", CLIENT_PORT);
    RpcDispatcher tmp;
    try {
      Channel channel = new JChannel();
      channel.connect(CHANNEL_NAME);
      myAddress = channel.getAddress();
      new RpcDispatcher(channel, null, null, this);
    } catch (ChannelException e) {
      logger.warn("failed to connect userserver to channel {}", CHANNEL_NAME);
    }
  }

  @Override
  public Boolean isServer() {
    return true;
  }

  // Clients may ask about whether this node is the master of a file
  @Override
  public Address isMaster(String name) {
    logger.trace("Are we master of " + name);
    Address master = parent.getMaster(name);
    logger.debug("got the master as {}", master.toString());
    if (master == parent.getSystemComms().getAddress()) {
      logger.debug("returning my client address {}", myAddress.toString());
      return myAddress;
    }
    return null;
  }

  // Clients may ask about file ownership before directing their requests
  @Override
  public Address hasFile(String name) {
    logger.trace("Do we have file " + name);
    if (parent.getDataStore().hasFile(name)) {
      return myAddress;
    }
    return null;
  }

  @Override
  public List<String> getFileNames(String regex) {
    logger.trace("List files");
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
    logger.trace("Client store " + file.getName());
    Address master = parent.getMaster(file.getName());
    if (master == null) {
      logger.error("Storing to all");
    }
    return parent.getSystemComms().store(file, master);
  }

  @Override
  public Boolean storeAll(DataObject file) {
    // The client should have directed this at the master, but we double check
    logger.trace("Client storeAll " + file.getName());
    parent.getSystemComms().store(file, parent.getMaster(file.getName()));
    parent.replicateDataObject(file);
    return true;
  }

  @Override
  public DataObject retrieve(String name) {
    // Try from our own store first - should work because clients target the master 
    logger.trace("Retrieve: " + name);
    DataObject file = parent.getDataStore().retrieve(name);
    if (file != null)
      return file;
    return parent.getSystemComms().retrieve(name, parent.getMaster(name));
  }

  @Override
  public boolean replace(DataObject file) {
    // The client should have directed this at the master, but we double check    
    logger.trace("replace: " + file.getName());
    return parent.getSystemComms().replace(file, parent.getMaster(file.getName()));
  }

  @Override
  public boolean delete(String name) {
    logger.trace("delete: " + name);
    Set<Address> copies = new HashSet<Address>();
    copies.add(parent.getMaster(name));
    copies.addAll(parent.getReplicas(name));
    return parent.getSystemComms().delete(name, copies);
  }

  @Override
  public Usage getDiskSpace() {
    String hostname = parent.getDataStore().getHostname();
    Long free = File.listRoots()[0].getUsableSpace();
    Long total = 0L;
    for (DataObject file : parent.getDataStore().getAllDataObjects()) {
      total += file.getSize();
    }

    return new Usage(hostname, free, total);
  }
}
