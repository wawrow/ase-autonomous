package com.slard.filerepository;

import org.jgroups.Address;

import java.util.List;

/*
 * Intended to be used by clients which don't provide any server functionality 
 * other than returning false from the isServer() method
 */
public class UserCommsDummyServerImpl implements UserCommsInterface {

  @Override
  public Address isMaster(String name) {
    return null;
  }

  @Override
  public Address hasFile(String name) {
    return null;
  }

  @Override
  public Boolean isServer() {
    return false;
  }

  @Override
  public List<String> getFileNames(String regex) {
    return null;
  }

  @Override
  public Boolean store(DataObject dataObject) {
    return false;
  }

  @Override
  public Boolean storeAll(DataObject file) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
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

  @Override
  public Usage getDiskSpace() {
    return null;
  }
}
