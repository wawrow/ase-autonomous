package com.slard.filerepository;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * The Class DataStore Implementation.
 */
public class DataStoreImpl implements DataStore {
  
  /** The store location path. */
  private String storeLocation;
  private String hostname;
  private final FileSystemHelper fileSystemHelper = new FileSystemHelper();  
  
  /** The logger. */
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  
  /** The Constant FILE_LIST_FILENAME - name of the file that holds a system wide file list. */
  private static final String FILE_LIST_FILENAME = "filelist.txt";
  
  /** The system file list helper - provides access to file list operations. */
  private SystemFileList fileList = null;

  /**
   * Instantiates a new data store implementation.
   *
   * @param options the options
   */
  public DataStoreImpl(Properties options) {
    this.storeLocation = options.getProperty("datastore.dir", System.getProperty("user.dir", "."));

    // Make sure the directory exists...
    File directory = new File(storeLocation);
    directory.mkdirs();
    this.logger.info("Data store initialized in " + this.storeLocation);
    this.hostname = options.getProperty("datastore.hostname", "localhost");
  }

  /**
   * Gets the directory in which the DataStore stores its files.
   *
   * @return String
   */
  @Override
  public String getStoreLocation() {
    return this.storeLocation;
  }

  /**
   * Checks if the named object is present in the object store.
   *
   * @param name the data object name
   * @return true, if successful
   */
  @Override
  public boolean hasFile(String name) {
    File file = new File(storeLocation, name);
    return file.exists();
  }

  /**
   * Gets all the DataObjects in the object store.
   *
   * @return ArrayList
   */
  @Override
  public ArrayList<DataObject> getAllDataObjects() {
    ArrayList<DataObject> list = new ArrayList<DataObject>();
    File directory = new File(storeLocation);
    for (String file : directory.list()) {
      DataObject obj = retrieve(file);
      if (obj != null)
        list.add(obj);
    }
    return list;
  }

  /**
   * Gets the named object from the data store.
   *
   * @param name the data object name
   * @return DataObjectImpl
   */
  @Override
  public DataObjectImpl retrieve(String name) {
    try {
      File file = new File(storeLocation, name);
      return new DataObjectImpl(name, fileSystemHelper.readFile(file));
    } catch (IOException ex) {
      logger.warning("Exception while retrieving the file: " + ex.toString());
      return null;
    }
  }

  /**
   * Deletes the named object from the object store.
   *
   * @param name the data object name
   * @return true, if successful
   */
  @Override
  public boolean delete(String name) {
    this.logger.info("Deleting data object: " + name);
    File file = new File(storeLocation, name);
    return file.delete();
  }

  /**
   * Adds an object to the object store. If the object already exists then the
   * add operation will fail with a DataObjectExistsException
   *
   * @param dataObject the DataObject containing the object name and data to be added to
   * the object store
   * @return the boolean
   */
  @Override
  public Boolean store(DataObject dataObject) {
    this.logger.info("About to store data object: " + dataObject.getName());
    File file = new File(storeLocation, dataObject.getName());
    if (file.exists()) {
      logger.warning("Attempt to store file that already exists: " + dataObject.getName());
      return false;
    }
    try {
      fileSystemHelper.writeFile(file, dataObject.getData());
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
   * @param dataObject the DataObject to be replaced
   * @return true, if successful
   */
  @Override
  public boolean replace(DataObject dataObject) {
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

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getCRC(String fileName) {
    return this.retrieve(fileName).getCRC();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public ArrayList<String> list() {
    File directory = new File(storeLocation);
    return new ArrayList<String>(Arrays.asList(directory.list()));
  }

  /**
   * Provides object for system file list updates.
   *
   * @return SystemFileList implementation that provides access to file list updates
   */
  private SystemFileList getSystemFileList() {
    if (this.fileList == null) {
      // TODO This probably is too thigh coupling
      this.fileList = new FileListDataObjectImpl(this);
    }
    return fileList;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addFileName(String fileName) {
    return this.getSystemFileList().addFileName(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(String fileName) {
    return this.getSystemFileList().contains(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getFileNames() {
    return this.getSystemFileList().getFileNames();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeFileName(String fileName) {
    return this.getSystemFileList().removeFileName(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String getFileListName() {
    return FILE_LIST_FILENAME;
  }

  @Override
  public String getHostname() {
    return hostname;
  }

}