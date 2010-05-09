package com.slard.filerepository;

import java.io.Serializable;

/**
 * The Interface DataObject.
 */
public interface DataObject extends Serializable {
  
  /**
   * Gets the binary data of an object.
   *
   * @return the data
   */
  byte[] getData();
  
  /**
   * Gets the name.
   *
   * @return the name
   */
  String getName();
  
  /**
   * Gets the CRC.
   *
   * @return the CRC
   */
  Long getCRC();
}
