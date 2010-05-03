package com.slard.filerepository;

import java.util.List;
import java.util.Vector;

import org.jgroups.Address;

public class NodeDescriptorImpl implements NodeDescriptor {

  private Address address;
  private FileOperations systemComs;
  private SystemFileList systemFileList;

  @Override
  public Address getAddress() {
    return this.address;
  }

  public NodeDescriptorImpl(Address address, FileOperations systemComsClient, SystemFileList systemFileList) {
    this.address = address;
    this.systemComs = systemComsClient;
    this.systemFileList = systemFileList;
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

  @Override
  public boolean delete(String name) {
    return this.systemComs.delete(name);
  }

  @Override
  public boolean addFileName(String fileName) {
    return this.systemFileList.addFileName(fileName);
  }

  @Override
  public boolean contains(String fileName) {
    return this.systemFileList.contains(fileName);
  }

  @Override
  public List<String> getFileNames() {
    return this.systemFileList.getFileNames();
  }

  @Override
  public boolean removeFileName(String fileName) {
    return this.systemFileList.removeFileName(fileName);
  }
}
