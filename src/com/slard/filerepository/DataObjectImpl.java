package com.slard.filerepository;

import java.util.zip.CRC32;

public class DataObjectImpl implements DataObject {
  private static final long serialVersionUID = 2411701715160473042L;
  private String name;
  private byte[] content;

  public DataObjectImpl(String name, byte[] content) {
    this.name = name;
    this.content = content;
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
  public Long getCRC() {
    CRC32 crc = new CRC32();
    crc.reset();
    crc.update(this.getData());
    return crc.getValue();
  }
}
