package com.slard.filerepository;

import org.jgroups.Address;

import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class NodeDescriptor Implementation.
 */
public class NodeDescriptorImpl implements NodeDescriptor {

  /** The address of the node described. */
  private Address address;
  
  /** The system communications interface to described node. */
  private FileOperations systemComms;
  
  /** The system file list. */
  private SystemFileList systemFileList;

  /** {@inheritDoc} */
  @Override
  public Address getAddress() {
    return this.address;
  }

  /**
   * Instantiates a new node descriptor implementation.
   *
   * @param address the address
   * @param systemCommsClient the system communications client for communications with node described (created outside of the class for lesser coupling)
   * @param systemFileList the system file list
   */
  public NodeDescriptorImpl(Address address, FileOperations systemCommsClient, SystemFileList systemFileList) {
    this.address = address;
    this.systemComms = systemCommsClient;
    this.systemFileList = systemFileList;
  }

  /** {@inheritDoc} */
  @Override
  public DataObject retrieve(String name) {
    return this.systemComms.retrieve(name);
  }

  /** {@inheritDoc} */
  @Override
  public Boolean store(DataObject dataObject) {
    return this.systemComms.store(dataObject);
  }

  /** {@inheritDoc} */
  @Override
  public Long getCRC(String fileName) {
    return this.systemComms.getCRC(fileName);
  }

  /** {@inheritDoc} */
  @Override
  public boolean hasFile(String name) {
    return this.systemComms.hasFile(name);
  }

  /** {@inheritDoc} */
  @Override
  public ArrayList<String> list() {
    return this.systemComms.list();
  }

  /** {@inheritDoc} */
  @Override
  public boolean replace(DataObject dataObject) {
    return this.systemComms.replace(dataObject);
  }

  /** {@inheritDoc} */
  @Override
  public boolean delete(String name) {
    return this.systemComms.delete(name);
  }

  /** {@inheritDoc} */
  @Override
  public boolean addFileName(String fileName) {
    return this.systemFileList.addFileName(fileName);
  }

  /** {@inheritDoc} */
  @Override
  public boolean contains(String fileName) {
    return this.systemFileList.contains(fileName);
  }

  /** {@inheritDoc} */
  @Override
  public List<String> getFileNames() {
    return this.systemFileList.getFileNames();
  }

  /** {@inheritDoc} */
  @Override
  public boolean removeFileName(String fileName) {
    return this.systemFileList.removeFileName(fileName);
  }
}
