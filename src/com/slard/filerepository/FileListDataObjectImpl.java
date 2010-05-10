package com.slard.filerepository;

import org.jgroups.util.Util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Class FileListDataObject Implementation.
 */
public class FileListDataObjectImpl implements SystemFileList {

  /**
   * The file list names collection.
   */
  private Set<String> fileList;

  /**
   * The file name of the file that holds a filelist.
   */
  private String fileName;

  /**
   * The data object that represents the filelist file.
   */
  private DataObject dataObject;

  /**
   * The data store.
   */
  private DataStore store;

  /**
   * Instantiates a new file list data object implementation.
   *
   * @param store the store
   */
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

  /**
   * Reloads the filelist data.
   */
  @SuppressWarnings("unchecked")
  private void reload() {
    try {
      dataObject = store.retrieve(fileName);
      fileList = (Set<String>) Util.objectFromByteBuffer(this.dataObject.getData());
    } catch (Exception ex) {
      // TODO some exception handling
    }
  }

  /**
   * Saves current fileList set into the file.
   *
   * @return true, if successful
   */
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

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addFileName(String fileName) {
    fileList.add(fileName);
    return this.save();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(String fileName) {
    return fileList.contains(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getFileNames() {
    return new ArrayList<String>(fileList);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeFileName(String fileName) {
    fileList.remove(fileName);
    return true;
  }
}
