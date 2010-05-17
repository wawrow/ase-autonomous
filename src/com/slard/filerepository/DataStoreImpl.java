package com.slard.filerepository;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;
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

  private Cache fileCache;

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
    CacheManager.create();
    CacheManager manager = CacheManager.getInstance();
    this.fileCache = new Cache(
        new CacheConfiguration("files", 10000)
            .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
            .overflowToDisk(false)
            .eternal(false)
            .timeToLiveSeconds(120)
            .timeToIdleSeconds(60)
            .diskPersistent(false)
            .diskExpiryThreadIntervalSeconds(0));
    manager.addCache(fileCache);
    logger.debug("Filecache has status: {}, stats {}", fileCache.getStatus(), fileCache.getStatistics());
    for (String name : new File(storeLocation).list()) {
      try {
        FSDataObject file = new FSDataObject(new DataObjectImpl(name, fs.readFile(name)), fs);
        file.flush(false);  // we don't need the data, we can get it later.
        fileCache.put(new Element(name, file));
      } catch (IOException e) {
        logger.warn("failed to read file " + name);
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
    return fileCache.isKeyInCache(name);
  }

  /**
   * Gets all the DataObjects in the object store.
   *
   * @return ArrayList
   */
  @Override
  public Collection<DataObject> getAllDataObjects() {
    Set<DataObject> ret = new HashSet<DataObject>();
    for (String name : new File(storeLocation).list()) {
      try {
        if (!ret.contains(name)) {
          FSDataObject file = new FSDataObject(new DataObjectImpl(name, fs.readFile(name)), fs);
          fileCache.put(new Element(name, file));
          ret.add(file);
        }
      } catch (IOException e) {
        logger.warn("failed to load file {}, {}", name, e);
      }
    }
    return ret;
  }

  @Override
  public Boolean fillObject(DataObject file) {
    try {
      ((FSDataObject) file).fill();
    } catch (IOException e) {
      logger.warn("unable to populate file " + file.getName());
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
      FSDataObject file = (FSDataObject) fileCache.get(name).getObjectValue();
      file.fill();
      return file;
    } catch (IOException ex) {
      logger.warn("Exception while retrieving the file: {}", ex);
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
    logger.info("Deleting data object: {}", name);
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
    logger.trace("About to store data object: " + name);
    if (fs.exists(name)) {
      logger.warn("Attempt to store file that already exists: " + name);
      return false;
    }
    try {
      FSDataObject file = new FSDataObject(obj, fs);
      logger.trace("Storing the file: {}, {}", name, file.getData().length);
      file.flush(true);
      fileCache.put(new Element(name, file));
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
    fileCache.replace(new Element(name, file));
    return true;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Long getCRC(String fileName) {
    return ((FSDataObject) fileCache.get(fileName).getValue()).getCRC();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<String> list() {
    return new ArrayList<String>(fileCache.getKeys());
  }


  @Override
  public String getHostname() {
    return hostname;
  }

}
