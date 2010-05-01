package com.slard.filerepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Vector;

public class DataStoreImpl implements DataStore {
  private CHT cht;
  private String storeLocation;

  public DataStoreImpl(CHT cht) {
    this(".", cht);
  }

  public DataStoreImpl(String storeLocation, CHT cht) {
    this.cht = cht;
    this.storeLocation = storeLocation;
  }

  /**
   * Checks if the named object is present in the object store
   * @param name the data object name 
   */
  @Override
  public Boolean contains(String name) {
    File file = new File(storeLocation, name);
    if (!file.exists()) {
      return true;
    } else {
    	return false;
    }
  }

  /**
   * Gets all the DataObjects in the object store
   * @return Vector<DataObject>
   */
  @Override
  public Vector<DataObject> getAllDataObjects() {
    Vector<DataObject> vector = new Vector<DataObject>();
    File directory = new File(storeLocation);
    String[] files = directory.list();
    for (int i=0; i<files.length; i++) {
      try {
        vector.add(getDataObject(files[i]));
      } catch (IOException e) {
    	// Ignore this IOException and keep going
      }
    }
    return vector;
  }

  /**
   * Gets the named object from the data store
   * @param name the data object name
   * @return DataObjectImpl
   * @throws IOException
   */
  @Override
  public DataObjectImpl getDataObject(String name) throws IOException {
  	return new DataObjectImpl(name, readFile(name), this.cht);
  }

  /**
   * Deletes the named object from the object store
   * @param name the data object name
   * @throws IOException
   * @throws FileNotFoundException 
   */
  @Override
  public void deleteDataObject(String name) throws IOException {
    File file = new File(storeLocation, name);
    if (!file.exists()) {
      throw new FileNotFoundException(name + " not found");
    }
    if (!file.delete()) {
      throw new IOException(name + " deletion failed");
    }
  }

  /**
   * Adds an object to the object store. If the object already exists 
   * then the add operation will fail with a DataObjectExistsException
   * @param dataObject the DataObject containing the object name and data 
   *        to be added to the object store
   * @throws FileAlreadyExistsException 
   * @throws IOException 
   */
  @Override
  public void storeDataObject(DataObject dataObject) throws IOException {
    File file = new File(storeLocation, dataObject.getName());
    //if (file.exists()) {  // keith: I think it's better to silently replace.
    //  throw new DataObjectExistsException(dataObject.getName() + " already exists");
    //}
    writeFile(file, dataObject.getData());
  }

  /**
   * Replaces an object in the object store. If the object does not 
   * already exist then the operation fails with a FileNotFoundException
   * @param dataObject the DataObject to be replaced
   * @throws Exception 
   */
  @Override
  public void replaceDataObject(DataObject dataObject) throws Exception{
    File file = new File(storeLocation, dataObject.getName());
    if (!file.exists()) {
      throw new FileNotFoundException(dataObject.getName() + " not found");
    }

    // Rename to something temporary until we know the add worked
    String tempFileName = new String(dataObject.getName() + ".$tmp");
    file.renameTo(new File(tempFileName));
    try {
      storeDataObject(dataObject);
    } catch(Exception e){
      // The add failed so rename the temporary file back to the original
      file.renameTo(new File(dataObject.getName()));
      throw e;
    }
    deleteDataObject(tempFileName);
  }
  
  private byte[] readFile(String fileName) throws IOException { 
    File file = new File(storeLocation, fileName);
    byte[] fileContents = new byte[(int)file.length()];
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