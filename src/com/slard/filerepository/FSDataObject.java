package com.slard.filerepository;

import com.google.inject.ImplementedBy;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 21-May-2010
 * Time: 10:59:48
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(FSDataObjectImpl.class)
public interface FSDataObject extends DataObject {
  @Override
  byte[] getData() throws IOException;

  void fill() throws IOException;

  void flush() throws IOException;

  void scrub();

  /**
   * Created by IntelliJ IDEA.
   * User: kbrady
   * Date: 21-May-2010
   * Time: 13:09:44
   * To change this template use File | Settings | File Templates.
   */
  interface FSDataObjectFactory {
    FSDataObject create(DataObject file, FileSystemHelper fs);
  }
}
