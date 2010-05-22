package com.slard.filerepository;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import org.jgroups.blocks.Cache;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

/**
 * The Class DataStore Implementation.
 */
@Singleton
public class DataStoreImpl implements DataStore {

  /**
   * The store location path.
   */
  private String hostname;
  private FileSystemHelper fs;

  /**
   * The logger.
   */
  @InjectLogger
  Logger logger;

  private static final int CACHE_TIME = 1200000;
  private static final int CACHE_SIZE = 100000;

  private Properties options;

  private Cache<String, FSDataObject> fileCache;
  public final FileSystemHelper.FileSystemHelperFactory fileSystemFactory;
  public final DataObject.DataObjectFactory dataObjectFactory;
  public final FSDataObject.FSDataObjectFactory fsdFactory;

  public Provider<Cache<String, FSDataObject>> cacheProvider;

  /**
   * Instantiates a new data store implementation.
   */
  @Inject
  public DataStoreImpl(FileSystemHelper.FileSystemHelperFactory fileSystemFactory,
                       DataObject.DataObjectFactory dataObjectFactory,
                       FSDataObject.FSDataObjectFactory fsdFactory,
                       Provider<Cache<String, FSDataObject>> cacheProvider) {
    this.dataObjectFactory = dataObjectFactory;
    this.fsdFactory = fsdFactory;
    this.fileSystemFactory = fileSystemFactory;
    this.cacheProvider = cacheProvider;
  }


  @Override
  public void initialise(Properties options) {
    this.options = options;
    hostname = options.getProperty("datastore.hostname", "localhost");
    String location = options.getProperty("datastore.dir", System.getProperty("user.dir", "."));
    File directory = new File(location);
    fileCache = cacheProvider.get();
    fs = fileSystemFactory.create(directory);
    fs.mkdirs();
    fileCache.setMaxNumberOfEntries(CACHE_SIZE);
    fileCache.enableReaping(CACHE_TIME);
    for (String name : fs.list()) {
      getFile(name);  // has side-effect of populating cache.
    }
    logger.info("Data store initialized in " + location);
  }

  private FSDataObject getFile(String name) {
    if (!fs.canRead(name)) {
      logger.warn("can't read file {}", name);
      return null;
    }
    FSDataObject ret = null;
    try {
      ret = fsdFactory.create(dataObjectFactory.create(name, fs.readFile(name)), fs);
      ret.scrub();
      fileCache.put(name, ret, 0);
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    return ret;
  }

  @Override
  public void setLogger(Logger logger) {
    this.logger = logger;
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
    for (String name : fs.list()) {
      if (fileCache.get(name) == null) {
        FSDataObject file = getFile(name);
        if (file == null) {
          logger.warn("got null file for {}", name);
          continue;
        }
        ret.add(file);
      } else {
        DataObject file = fileCache.get(name);
        if (ret.contains(file)) {
          System.out.println(name + " is in the retval");
          System.out.println(file + " " + file.getName());
          System.out.println(ret.toArray()[0]);
        }
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
      FSDataObject file = fsdFactory.create(obj, fs);
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
    FSDataObject file = fsdFactory.create(obj, fs);
    String name = file.getName();
    if (!fs.exists(name)) {
      return false;
    }
    // Rename to something temporary until we know the add worked
    String tempFileName = name + ".tmp";
    if (!fs.rename(name, tempFileName)) {
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
    return fs.list();
  }


  @Override
  public String getHostname() {
    return hostname;
  }

}
