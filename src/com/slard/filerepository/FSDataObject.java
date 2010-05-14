package com.slard.filerepository;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 14-May-2010
 * Time: 13:21:52
 * To change this template use File | Settings | File Templates.
 */
public class FSDataObject extends DataObjectImpl {
  private FileSystemHelper fs;

  public FSDataObject(DataObject file, FileSystemHelper fs) {
    super(file);
    this.fs = fs;
  }

  @Override
  public byte[] getData() throws IOException {
    fill();
    return super.getData();
  }

  public void fill() throws IOException {
    super.setContent(fs.readFile(getName()));
  }

  public void flush(boolean write) throws IOException {
    if (write) {
      fs.writeFile(getName(), super.getData());
    }
    super.setContent(null);
  }
}
