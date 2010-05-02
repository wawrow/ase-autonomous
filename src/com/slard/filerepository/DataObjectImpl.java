package com.slard.filerepository;

import java.util.Vector;
import java.util.zip.CRC32;

public class DataObjectImpl implements DataObject {
  /**
   * 
   */
  private static final long serialVersionUID = 2411701715160473042L;
  private String name;
  private byte[] content;
  private long id = 0;

  private Vector<NodeDescriptor> replicaNodes;
  private NodeDescriptor masterNode;

  public DataObjectImpl(String name, byte[] content) {
    this.name = name;
    this.content = content;
    this.replicaNodes = new Vector<NodeDescriptor>();
//    if (master)
//      this.setMasterNode(node);
//    else
//      this.replicaNodes.add(node);
  }

  @Override
  public byte[] getData() {
    return content;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isMaster() {
    return false; // To change body of implemented methods use File | Settings |
    // File Templates.
  }

  @Override
  public int getReplicaCount() {
    return 0; // To change body of implemented methods use File | Settings |
    // File Templates.
  }

  @Override
  public NodeDescriptor[] getLocations() {
    return new NodeDescriptor[0]; // To change body of implemented methods use
    // File | Settings | File Templates.
  }

  @Override
  public Long getCRC() {
    CRC32 crc = new CRC32();
    crc.reset();
    crc.update(this.getData());
    return crc.getValue();
  }

  @Override
  public void addReplicaNode(NodeDescriptor node) {
    this.replicaNodes.add(node);
  }

  @Override
  public NodeDescriptor getMasterNode() {
    return this.masterNode;
  }

  @Override
  public Vector<NodeDescriptor> getReplicaNodes() {
    return this.replicaNodes;
  }

  @Override
  public void removeReplicaNode(NodeDescriptor node) {
    this.replicaNodes.remove(node);
  }

  @Override
  public void setMasterNode(NodeDescriptor node) {
    this.masterNode = node;
  }
}
