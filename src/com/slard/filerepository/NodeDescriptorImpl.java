package com.slard.filerepository;

import java.util.Vector;
import java.util.zip.CRC32;

import org.jgroups.Address;

public class NodeDescriptorImpl implements NodeDescriptor {

  private Address address;
  private long[] ids;
  private CHT cht;
  private SystemComs systemComs;
  
  @Override
  public Address getAddress() {
    return this.address;
  }

  @Override
  public long[] getIds() {
    if(ids == null){
      ids = cht.getIDs(this.address);
    }
    return ids;
  }
  
  public NodeDescriptorImpl(Address address, CHT cht, SystemComs systemComsClient)
  {
    this.address = address;
    this.cht = cht; 
    this.systemComs = systemComsClient;
  }

  @Override
  public DataObject retrieve(String name) {
    return this.systemComs.retrieve(name);
  }

  @Override
  public Boolean store(DataObject dataObject) {
    return this.systemComs.store(dataObject);
  }

  @Override
  public CRC32 getCRC(String fileName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasFile(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Vector<String> list() {
    // TODO Auto-generated method stub
    return null;
  }

}
