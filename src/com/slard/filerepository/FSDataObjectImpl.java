package com.slard.filerepository;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 14-May-2010
 * Time: 13:21:52
 * To change this template use File | Settings | File Templates.
 */
public class FSDataObjectImpl extends DataObjectImpl implements FSDataObject {
  transient
  @InjectLogger
  Logger logger;
  transient private FileSystemHelper fs;

  @Inject
  public FSDataObjectImpl(@Assisted DataObject file, @Assisted FileSystemHelper fs) {
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

  @Override
  public void fill() throws IOException {
    super.setContent(fs.readFile(getName()));
  }

  @Override
  public void flush() throws IOException {
    fs.writeFile(getName(), super.getData());
    super.setContent(null);
  }

  @Override
  public void scrub() {
    super.setContent(null);
  }
}
