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
  private final FileSystemHelper fs;

  /**
   * The logger.
   */
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private Map<String, FSDataObject> fileCache = null;  // really shiould be an LRU

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
    this.logger.info("Data store initialized in " + storeLocation);
    this.hostname = options.getProperty("datastore.hostname", "localhost");
    this.fs = new FileSystemHelper(directory);
  }

  @Override
  public void initialise() {
    fileCache = new HashMap<String, FSDataObject>();
    for (String name : new File(storeLocation).list()) {
      try {
        FSDataObject file = new FSDataObject(new DataObjectImpl(name, fs.readFile(name)), fs);
        file.flush(false);  // we don't need the data, we can get it later.
        fileCache.put(name, file);
      } catch (IOException e) {
        logger.warning("failed to read file " + name);
      }
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
  public Collection<DataObject> getAllDataObjects() {
    return new HashSet<DataObject>(fileCache.values());
  }

  @Override
  public Boolean fillObject(DataObject file) {
    try {
      ((FSDataObject) file).fill();
    } catch (IOException e) {
      logger.warning("unable to populate file " + file.getName());
      return false;
    }
    return true;
  }

  /**
   * Gets the named object from the data store.
   *
   * @param name the data object name
   * @return DataObjectImpl
   */
  @Override
  public DataObject retrieve(String name) {
    try {
      FSDataObject file = fileCache.get(name);
      file.fill();
      return file;
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
    logger.info("Deleting data object: " + name);
    fileCache.remove(name);
    return fs.delete(name);
  }

  /**
   * Adds an object to the object store. If the object already exists then the
   * add operation will fail with a DataObjectExistsException
   *
   * @param obj the DataObject containing the object name and data to be added to
   *            the object store
   * @return the boolean
   */
  @Override
  public Boolean store(DataObject obj) {
    String name = obj.getName();
    logger.info("About to store data object: " + name);
    if (fs.exists(name)) {
      logger.warning("Attempt to store file that already exists: " + name);
      return false;
    }
    try {
      FSDataObject file = new FSDataObject(obj, fs);
      file.flush(true);
      fileCache.put(name, file);
    } catch (IOException ex) {
      logger.warning("Exception while storing the file: " + name + ": " + ex.toString());
      return false;
    }
    return true;
  }

  /**
   * Replaces an object in the object store. If the object does not already
   * exist then the operation fails with a FileNotFoundException
   *
   * @param file the DataObject to be replaced
   * @return true, if successful
   */
  @Override
  public boolean replace(DataObject file) {
    String name = file.getName();
    if (!fs.exists(name)) {
      return false;
    }
    // Rename to something temporary until we know the add worked
    String tempFileName = name + ".tmp";
    if (!fs.rename(name, tempFileName)) {
      //throw new Exception("Could not rename " + dataObject.getName() + " to " + tempFileName);
      return false;
    }
    if (!store(file)) {
      fs.rename(tempFileName, name);
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
    return fileCache.get(fileName).getCRC();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> list() {
    return new ArrayList<String>(fileCache.keySet());
  }


  @Override
  public String getHostname() {
    return hostname;
  }

}