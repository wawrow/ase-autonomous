package com.slard.filerepository;

/**
 * Created by IntelliJ IDEA. User: kbrady Date: 28-Apr-2010 Time: 08:45:22 To
 * change this template use File | Settings | File Templates.
 */
public class DataObjectImpl implements DataObject {
  private String name;
  private byte[] content;
  private CHT cht;
  private long id = 0;

  public DataObjectImpl(String name, byte[] content, CHT cht) {
    this.name = name;
    this.content = content;
    this.cht = cht;
  }

  @Override
  public long getId() {
    if (id == 0) {
      id = cht.calculateId(this.name.getBytes());
    }
    return id; 
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
  public String getCRC() {
    // TODO Auto-generated method stub
    return null;
  }
}
