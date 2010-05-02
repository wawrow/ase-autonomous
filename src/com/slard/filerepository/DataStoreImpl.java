package com.slard.filerepository;

import java.io.*;
import java.util.ArrayList;
import java.util.Properties;

public class DataStoreImpl implements DataStore {
  private String storeLocation;
  private Properties options;

  public DataStoreImpl(Properties options) {
    this.options = options;
    this.storeLocation = options.getProperty("datastore.dir", System.getProperty("user.dir", "."));
    
    // Make sure the directory exists...
    File directory = new File(storeLocation);
    directory.mkdirs();
  }

  /**
   * Gets the directory in which the DataStore stores its files
   *
   * @return String
   */
  @Override
  public String getStoreLocation() {
    return this.storeLocation;
  }
  
  /**
   * Checks if the named object is present in the object store
   *
   * @param name the data object name
   */
  @Override
  public Boolean contains(String name) {
    File file = new File(storeLocation, name);
    return file.exists();
  }

  /**
   * Gets all the DataObjects in the object store
   *
   * @return ArrayList<DataObject>
   */
  @Override
  public ArrayList<DataObject> getAllDataObjects() {
    ArrayList<DataObject> list = new ArrayList<DataObject>();
    File directory = new File(storeLocation);
    for (String file : directory.list()) {
      try {
        list.add(getDataObject(file));
      } catch (IOException e) {
        // Ignore this IOException and keep going
      }
    }
    return list;
  }

  /**
   * Gets the named object from the data store
   *
   * @param name the data object name
   * @return DataObjectImpl
   * @throws IOException
   */
  @Override
  public DataObjectImpl getDataObject(String name) throws IOException {
    return new DataObjectImpl(name, readFile(name));
  }

  /**
   * Deletes the named object from the object store
   *
   * @param name the data object name
   * @throws IOException
   * @throws FileNotFoundException
   */
  @Override
  public void deleteDataObject(String name) throws IOException {
    File file = new File(storeLocation, name);
    if (!file.delete()) {
      throw new IOException(name + " deletion failed");
    }
  }

  /**
   * Adds an object to the object store. If the object already exists
   * then the add operation will fail with a DataObjectExistsException
   *
   * @param dataObject the DataObject containing the object name and data
   *                   to be added to the object store
   * @throws DataObjectExistsException
   * @throws IOException
   */
  @Override
  public void storeDataObject(DataObject dataObject) throws IOException, DataObjectExistsException {
    File file = new File(storeLocation, dataObject.getName());
    if (file.exists()) {
      throw new DataObjectExistsException(dataObject.getName() + " already exists");
    }
    writeFile(file, dataObject.getData());
  }

  /**
   * Replaces an object in the object store. If the object does not
   * already exist then the operation fails with a FileNotFoundException
   *
   * @param dataObject the DataObject to be replaced
   * @throws Exception
   */
  @Override
  public void replaceDataObject(DataObject dataObject) throws Exception {
    File file = new File(storeLocation, dataObject.getName());
    if (!file.exists()) {
      throw new FileNotFoundException(dataObject.getName() + " not found");
    }

    // Rename to something temporary until we know the add worked
    String tempFileName = dataObject.getName() + ".tmp";
    if (!file.renameTo(new File(storeLocation, tempFileName))){
      throw new Exception("Could not rename " + dataObject.getName() + " to " + tempFileName);
    }
    try {
      storeDataObject(dataObject);
    } catch (Exception e) {
      // The add failed so rename the temporary file back to the original
      file.renameTo(new File(dataObject.getName()));
      throw e;
    }
    deleteDataObject(tempFileName);
  }

  private byte[] readFile(String fileName) throws IOException {
    File file = new File(storeLocation, fileName);
    byte[] fileContents = new byte[(int) file.length()];
    FileInputStream fis = new FileInputStream(file);
    try {
      BufferedInputStream bis = new BufferedInputStream(fis);
      try {
        bis.read(fileContents, 0, fileContents.length);
        return fileContents;
      } finally {
        bis.close();
      }
    } finally {
      fis.close();
    }
  }

  private void writeFile(File file, byte[] fileContents) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    try {
      bos.write(fileContents);
    } finally {
      bos.flush();
      bos.close();
    }
  }
}