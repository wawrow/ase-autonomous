package com.slard.filerepository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

public class DataStoreImpl implements DataStore {
  private String storeLocation;
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  public DataStoreImpl(Properties options) {
    this.storeLocation = options.getProperty("datastore.dir", System.getProperty("user.dir", "."));

    // Make sure the directory exists...
    File directory = new File(storeLocation);
    directory.mkdirs();
    this.logger.info("Data store initialized in " + this.storeLocation);
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
   * @param name
   *          the data object name
   */
  @Override
  public boolean hasFile(String name) {
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
      DataObject obj = retrieve(file);
      if (obj == null)
        list.add(obj);
    }
    return list;
  }

  /**
   * Gets the named object from the data store
   * 
   * @param name
   *          the data object name
   * @return DataObjectImpl
   */
  @Override
  public DataObjectImpl retrieve(String name) {
    try {
      return new DataObjectImpl(name, readFile(name));
    } catch (IOException ex) {
      logger.warning("Exception while retrieving the file: " + ex.toString());
      return null;
    }
  }

  /**
   * Deletes the named object from the object store
   * 
   * @param name
   *          the data object name
   * @throws IOException
   * @throws FileNotFoundException
   */
  @Override
  public boolean delete(String name){
    this.logger.info("Deleting data object: " + name);
    File file = new File(storeLocation, name);
    return file.delete();
  }

  /**
   * Adds an object to the object store. If the object already exists then the
   * add operation will fail with a DataObjectExistsException
   * 
   * @param dataObject
   *          the DataObject containing the object name and data to be added to
   *          the object store
   * @throws DataObjectExistsException
   * @throws IOException
   */
  @Override
  public Boolean store(DataObject dataObject) {
    this.logger.info("About to store data object: " + dataObject.getName());
    File file = new File(storeLocation, dataObject.getName());
    if (file.exists()) {
      logger.warning("Attept to store file that already exist: " + dataObject.getName());
      return false;
    }
    try {
      writeFile(file, dataObject.getData());
    } catch (IOException ex) {
      logger.warning("Exception while storing the file: " + dataObject.getName() + " : " + ex.toString());
      return false;
    }
    return true;
  }

  /**
   * Replaces an object in the object store. If the object does not already
   * exist then the operation fails with a FileNotFoundException
   * 
   * @param dataObject
   *          the DataObject to be replaced
   * @throws Exception
   */
  @Override
  public boolean replace(DataObject dataObject){
    File file = new File(storeLocation, dataObject.getName());
    if (!file.exists()) {
      //throw new FileNotFoundException(dataObject.getName() + " not found");
      return false;
    }

    // Rename to something temporary until we know the add worked
    String tempFileName = dataObject.getName() + ".tmp";
    if (!file.renameTo(new File(storeLocation, tempFileName))) {
      //throw new Exception("Could not rename " + dataObject.getName() + " to " + tempFileName);
      return false;
    }
    try {
      store(dataObject);
    } catch (Exception e) {
      // The add failed so rename the temporary file back to the original
      file.renameTo(new File(dataObject.getName()));
      //throw e;
      return false;
    }
    delete(tempFileName);
    return true;
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

  @Override
  public Long getCRC(String fileName) {
    return this.retrieve(fileName).getCRC();
  }

  @Override
  public Vector<String> list() {
    File directory = new File(storeLocation);
    return new Vector<String>(Arrays.asList(directory.list()));      
  }

}