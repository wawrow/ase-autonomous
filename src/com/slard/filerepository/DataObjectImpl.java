package com.slard.filerepository;

import com.google.protobuf.ByteString;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 27-Apr-2010
 * Time: 00:59:03
 * To change this template use File | Settings | File Templates.
 */
public class DataObjectImpl implements DataObject {
  private final byte[] data;
  private final String name;

  private DataObjectImpl(String name, byte[] data) {
    this.name = name;
    this.data = data;
  }

  @Override
  public long getId() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public byte[] getData() {
    return data;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean isMaster() {
    return false;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public int getReplicaCount() {
    return 0;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public NodeDescriptor[] getLocations() {
    return new NodeDescriptor[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public SystemCommunications.FileContents toMessageFileContents() {
    SystemCommunications.FileName fname = SystemCommunications.FileName.newBuilder()
        .setName(getName())
        .build();
    SystemCommunications.FileContents content = SystemCommunications.FileContents.newBuilder()
        .setContent(ByteString.copyFrom(getData()))
        .setName(fname)
        .build();
    return content;
  }

  static DataObject fromMessageFileContents(SystemCommunications.FileContents content){
    return new DataObjectImpl(content.getName().getName(), content.getContent().toByteArray());
  }
}
