package com.slard.filerepository;

import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileListDataObjectImpl implements SystemFileList {
  private Set<String> fileList;
  private String fileName;
  private DataObject dataObject;
  private DataStore store;

  public FileListDataObjectImpl(DataStore store) {
    this.store = store;
    this.fileList = new HashSet<String>();
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
      dataObject = store.retrieve(fileName);
      fileList = (Set<String>) Util.objectFromByteBuffer(this.dataObject.getData());
    } catch (Exception ex) {
      // TODO some exception handling
    }
  }

  private boolean save() {
    try {
      //this.dataObject = new DataObjectImpl(fileName, serialize(this.fileList));
      dataObject = new DataObjectImpl(fileName, Util.objectToByteBuffer(this.fileList));
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
    fileList.add(fileName);
    return this.save();
  }

  @Override
  public boolean contains(String fileName) {
    return fileList.contains(fileName);
  }

  @Override
  public List<String> getFileNames() {
    return new ArrayList<String>(fileList);
  }

  @Override
  public boolean removeFileName(String fileName) {
    fileList.remove(fileName);
    return true;
  }
}
