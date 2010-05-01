package com.slard.filerepository;

import org.jgroups.Address;

public class NodeDescriptorImpl implements NodeDescriptor {

  private Address address;
  private long[] ids;
  private CHT cht;
  private SystemComsClient systemComsClient;
  
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
  
  public NodeDescriptorImpl(Address address, CHT cht, SystemComsClient systemComsClient)
  {
    this.cht = cht; 
    this.systemComsClient = systemComsClient;
  }

  @Override
  public DataObject retrieve(String name) {
    return this.systemComsClient.retrieve(name, this.address);
  }

  @Override
  public Boolean store(DataObject dataObject) {
    return this.systemComsClient.store(dataObject, this.address);
  }

}
