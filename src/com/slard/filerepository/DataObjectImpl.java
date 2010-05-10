package com.slard.filerepository;

import java.util.zip.CRC32;

/**
 * The Class DataObject Implementation.
 */
public class DataObjectImpl implements DataObject {

  /**
   * The Constant serialVersionUID - required for serialization.
   */
  private static final long serialVersionUID = 2411701715160473042L;

  /**
   * The name.
   */
  private String name;

  /**
   * The byte content.
   */
  private byte[] content;

  private Long checksum;

  /**
   * Instantiates a new data object implementation.
   *
   * @param name    the name
   * @param content the content
   */
  public DataObjectImpl(String name, byte[] content) {
    this.name = name;
    this.content = content;
    CRC32 crc = new CRC32();
    crc.reset();
    crc.update(this.getData());
    checksum = crc.getValue();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getData() {
    return content;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getName() {
    return name;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getCRC() {
    return checksum;
  }
}
