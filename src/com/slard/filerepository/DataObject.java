package com.slard.filerepository;

import com.google.inject.ImplementedBy;

import java.io.IOException;
import java.io.Serializable;

/**
 * The Interface DataObject.
 */
@ImplementedBy(DataObjectImpl.class)
public interface DataObject extends Serializable {

  /**
   * Gets the binary data of an object.
   *
   * @return the data
   */
  byte[] getData() throws IOException;

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

  int getSize();

  /**
   * Created by IntelliJ IDEA.
   * User: kbrady
   * Date: 21-May-2010
   * Time: 13:06:59
   * To change this template use File | Settings | File Templates.
   */
  interface DataObjectFactory {
    DataObject create(String name, byte[] Content);
  }
}
