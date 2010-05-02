package com.slard.filerepository;

import java.util.Vector;
import java.util.zip.CRC32;

import org.jgroups.Address;

public class NodeDescriptorImpl implements NodeDescriptor {

  private Address address;
  private long[] ids;
  private ConsistentHash ch;
  private SystemComs systemComs;

  @Override
  public Address getAddress() {
    return this.address;
  }

  @Override
  public long[] getIds() {
    if (ids == null) {
      ids = ch.getIDs(this.address);
    }
    return ids;
  }

  public NodeDescriptorImpl(Address address, ConsistentHash ch,
      SystemComs systemComsClient) {
    this.address = address;
    this.ch = ch;
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
}
