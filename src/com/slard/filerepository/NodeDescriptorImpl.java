package com.slard.filerepository;

import java.util.Vector;

import org.jgroups.Address;

public class NodeDescriptorImpl implements NodeDescriptor {

  private Address address;
  private SystemComs systemComs;

  @Override
  public Address getAddress() {
    return this.address;
  }

  public NodeDescriptorImpl(Address address, SystemComs systemComsClient) {
    this.address = address;
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
  public Long getCRC(String fileName) {
    return this.systemComs.getCRC(fileName);
  }

  @Override
  public boolean hasFile(String name) {
    return this.systemComs.hasFile(name);
  }

  @Override
  public Vector<String> list() {
    return this.systemComs.list();
  }

  @Override
  public boolean replace(DataObject dataObject) {
    return this.systemComs.replace(dataObject);
  }
}
