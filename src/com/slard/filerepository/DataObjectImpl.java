package com.slard.filerepository;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 * The Class DataObject Implementation.
 */
public class DataObjectImpl implements DataObject {

  /**
   * The Constant serialVersionUID - required for serialization.
   */
  private static final long serialVersionUID = 2411701715160473042L;

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * The name.
   */
  private final String name;

  /**
   * The byte content.
   */
  private byte[] content = null;

  private Long checksum = null;

  private int size = 0;

  /**
   * Instantiates a new data object implementation.
   *
   * @param name    the name
   * @param content the content
   */
  public DataObjectImpl(String name, byte[] content) {
    this.name = name;
    this.content = content;
    this.size = content.length;
    this.checksum = getChecksum(content);
  }

  public DataObjectImpl(DataObject source) {
    this.name = source.getName();
    try {
      this.content = source.getData();
    } catch (IOException e) {
      this.content = null;
    }
    this.size = source.getSize();
    this.checksum = source.getCRC();
  }

  private static Long getChecksum(byte[] content) {
    CRC32 crc = new CRC32();
    crc.reset();
    crc.update(content);
    return crc.getValue();
  }

  protected void setContent(byte[] bytes) {
    content = bytes;
    if (content != null) {
      checksum = getChecksum(bytes);
      size = bytes.length;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public byte[] getData() throws IOException {
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

  @Override
  public int getSize() {
    return size;
  }
}
