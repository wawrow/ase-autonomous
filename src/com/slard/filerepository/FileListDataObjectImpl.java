package com.slard.filerepository;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Vector;

public class FileListDataObjectImpl implements SystemFileList {

  private Vector<String> fileList;
  private String fileName;
  private DataObject dataObject;
  private DataStore store;

  public FileListDataObjectImpl(DataStore store) {
    this.store = store;
    this.fileList = new Vector<String>();
    this.fileName = store.getFileListName();
    if (this.store.hasFile(fileName)) {
      this.reload();
    } else {
      this.save();
    }
  }

  @SuppressWarnings("unchecked")
  private void reload() {
    try {
      this.dataObject = store.retrieve(fileName);
      this.fileList = (Vector<String>) deserialize(this.dataObject.getData());
    } catch (Exception ex) {
      // TODO some exception handling
    }
  }

  private boolean save() {
    try {
      this.dataObject = new DataObjectImpl(fileName, serialize(this.fileList));
      if (this.store.hasFile(this.dataObject.getName())) {
        return this.store.replace(this.dataObject);
      } else {
        return this.store.store(this.dataObject);
      }
    } catch (Exception ex) {
      // TODO add some better error handling
      return false;
    }
  }

  @Override
  public boolean addFileName(String fileName) {
    this.fileList.add(fileName);
    return this.save();
  }

  @Override
  public boolean contains(String fileName) {
    return this.fileList.contains(fileName);
  }

  @Override
  public List<String> getFileNames() {
    return this.fileList;
  }

  @Override
  public boolean removeFileName(String fileName) {
    if(this.fileList.contains(fileName)){
      return this.fileList.remove(fileName);
    }
    //it isn't in there so let's assume true is ok
    return true;
  }

  protected static byte[] serialize(Object o) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    ObjectOutputStream oos = new ObjectOutputStream(baos);
    oos.writeObject(o);
    oos.close();

    byte[] bytes = baos.toByteArray();
    return bytes;
  }

  protected static Object deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
    return new ObjectInputStream(new ByteArrayInputStream(bytes)).readObject();
  }

}
