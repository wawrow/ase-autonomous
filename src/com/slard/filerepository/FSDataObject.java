package com.slard.filerepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 14-May-2010
 * Time: 13:21:52
 * To change this template use File | Settings | File Templates.
 */
public class FSDataObject extends DataObjectImpl {
  transient private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  transient private FileSystemHelper fs;

  public FSDataObject(DataObject file, FileSystemHelper fs) {
    super(file);
    this.fs = fs;
  }

  @Override
  public byte[] getData() throws IOException {
    if (super.getData() == null) {
      fill();
    }
    return super.getData();
  }

  public void fill() throws IOException {
    super.setContent(fs.readFile(getName()));
  }

  public void flush(boolean write) throws IOException {
    if (write) {
      logger.trace("writing file {} of length {}", getName(), super.getData().length);
      fs.writeFile(getName(), super.getData());
    }
    super.setContent(null);
  }
}
