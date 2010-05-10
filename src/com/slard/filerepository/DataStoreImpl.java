package com.slard.filerepository;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * The Class DataStore Implementation.
 */
public class DataStoreImpl implements DataStore {

  /**
   * The store location path.
   */
  private String storeLocation;
  private String hostname;
  private final FileSystemHelper fileSystemHelper = new FileSystemHelper();

  /**
   * The logger.
   */
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  /**
   * The system file list helper - provides access to file list operations.
   */
//  private SystemFileList fileList = null;

  private Map<String, Long> fileCache = null;

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

  @Override
  public void initialise() {
    fileCache = new HashMap<String, Long>();
    for (String fname : new File(storeLocation).list()) {
      fileCache.put(fname, getCRC(fname));
    }
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
    return fileCache.containsKey(name);
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
      if (obj != null) {
        list.add(obj);
        fileCache.put(obj.getName(), obj.getCRC());
      }
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
    fileCache.remove(name);
    return file.delete();
  }

  /**
   * Adds an object to the object store. If the object already exists then the
   * add operation will fail with a DataObjectExistsException
   *
   * @param dataObject the DataObject containing the object name and data to be added to
   *                   the object store
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
      fileCache.put(dataObject.getName(), dataObject.getCRC());
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
    return new ArrayList<String>(fileCache.keySet());
  }

  /**
   * Provides object for system file list updates.
   *
   * @return SystemFileList implementation that provides access to file list updates
   */
  private SystemFileList getSystemFileList() {
    // if (this.fileList == null) {
    // TODO This probably is too thigh coupling
    //   this.fileList = new FileListDataObjectImpl(this);
    // }
    // return fileList;
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean addFileName(String fileName) {
    return true;
    //return this.getSystemFileList().addFileName(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(String fileName) {
    return true;
    //return this.getSystemFileList().contains(fileName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> getFileNames() {
    return list();
    //return this.getSystemFileList().getFileNames();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean removeFileName(String fileName) {
    return true;
    //return this.getSystemFileList().removeFileName(fileName);
  }

  @Override
  public String getHostname() {
    return hostname;
  }

}