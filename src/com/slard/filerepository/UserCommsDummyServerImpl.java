package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.Address;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.util.List;
import java.util.logging.Logger;

/*
 * Intended to be used by clients which don't provide any server functionality 
 * other than returning false from the isServer() method
 */
public class UserCommsDummyServerImpl implements UserOperations {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private RpcDispatcher dispatcher = null;

  public UserCommsDummyServerImpl(Channel channel, MessageListener messages, MembershipListener members) {
    this.dispatcher = new RpcDispatcher(channel, messages, members, this);
  }

  public RpcDispatcher GetDispatcher() {
    return this.dispatcher;
  }

  public void stop() {
    dispatcher.stop();
  }

  public Address isMaster(String name) {
    return null;
  }

  public Address hasFile(String name) {
    return null;
  }

  @Override
  public Boolean isServer() {    
    return false;
  }

  @Override
  public List<String> getFileNames() {
    return null;
  }

  @Override
  public Boolean store(DataObject dataObject) {
    return false;
  }

  @Override
  public DataObject retrieve(String name) {
    return null;
  }

  @Override
  public boolean replace(DataObject dataObject) {
    return false;
  }

  @Override
  public boolean delete(String name) {
    return false;
  }
}
