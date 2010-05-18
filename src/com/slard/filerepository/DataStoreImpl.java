package com.slard.filerepository;

import org.jgroups.blocks.Cache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

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
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private static final int CACHE_TIME = 1200000;
  private static final int CACHE_SIZE = 100000;

  //private Cache fileCache;
  private Cache<String, FSDataObject> fileCache;

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

  private FSDataObject getFile(String name) {
    File file = new File(storeLocation, name);
    if (!file.canRead()) {
      //logger.warn("can't read file {}", name);
      return null;
    }
    FSDataObject ret = null;
    try {
      ret = new FSDataObject(new DataObjectImpl(name, fs.readFile(name)), fs);
      ret.scrub();
      fileCache.put(name, ret, 0);
    } catch (IOException e) {
      logger.warn("failed to get file {} from FileSystemHelper", name);
    }
    return ret;
  }

  @Override
  public void initialise() {
    fileCache = new Cache<String, FSDataObject>();
    fileCache.setMaxNumberOfEntries(CACHE_SIZE);
    fileCache.enableReaping(CACHE_TIME);
    for (String name : new File(storeLocation).list()) {
      getFile(name);  // has side-effect of populating cache.
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
    logger.trace("checking entry {} gives {}", name, fileCache.getEntry(name));
    if (fileCache.getEntry(name) != null) {
      return true;
    }
    DataObject file = getFile(name);
    return (file != null);
  }

  /**
   * Gets all the DataObjects in the object store.
   *
   * @return ArrayList
   */
  @Override
  public Collection<DataObject> getAllDataObjects() {
    logger.trace("cache has {} entries", fileCache.getSize());
    Collection<DataObject> ret = new HashSet<DataObject>();
    for (String name : new File(storeLocation).list()) {
      if (fileCache.get(name) == null) {
        FSDataObject file = getFile(name);
        if (file == null) {
          logger.warn("got null file for {}", name);
          continue;
        }
        ret.add(file);
      } else {
        ret.add(fileCache.get(name));
      }
    }
    logger.trace("size of datastore is {}", ret.size());
    return ret;
  }

  /**
   * Gets the named object from the data store.
   *
   * @param name the data object name
   * @return DataObjectImpl
   */
  @Override
  public DataObject retrieve(String name) {
    FSDataObject file = fileCache.get(name);
    if (file == null) {
      file = getFile(name);
    }
    return file;
  }

  /**
   * Deletes the named object from the object store.
   *
   * @param name the data object name
   * @return true, if successful
   */
  @Override
  public boolean delete(String name) {
    logger.info("Deleting data object: {}", name);
    fileCache.remove(name);
    return fs.delete(name);
  }

  /**
   * Adds an object to the object store. If the object already exists then the
   * add operation will fail.
   *
   * @param obj the DataObject containing the object name and data to be added to
   *            the object store
   * @return the boolean
   */
  @Override
  public Boolean store(DataObject obj) {
    String name = obj.getName();
    logger.trace("About to store data object: " + name);
    if (fs.exists(name)) {
      logger.debug("Attempt to store file that already exists: " + name);
      return false;
    }
    try {
      FSDataObject file = new FSDataObject(obj, fs);
      logger.trace("Storing the file: {}, {}", name, file.getData().length);
      fileCache.put(name, file, 0);
      file.flush();
    } catch (IOException ex) {
      logger.warn("Exception while storing the file: {}, {}", name, ex);
      return false;
    }
    return true;
  }

  /**
   * Replaces an object in the object store. If the object does not already
   * exist then the operation fails with a FileNotFoundException
   *
   * @param obj the DataObject to be replaced
   * @return true, if successful
   */
  @Override
  public boolean replace(DataObject obj) {
    FSDataObject file = new FSDataObject(obj, fs);
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
    fileCache.remove(name);
    fileCache.put(name, file, 0);  // so we have the right content.
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getCRC(String name) {
    DataObject file = fileCache.get(name);
    if (file == null) {
      file = getFile(name);
      if (file == null) {
        return null;
      }
    }
    return file.getCRC();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> list() {
    return Arrays.asList(new File(storeLocation).list());
  }


  @Override
  public String getHostname() {
    return hostname;
  }

}
