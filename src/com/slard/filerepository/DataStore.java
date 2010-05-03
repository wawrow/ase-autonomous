package com.slard.filerepository;

import java.util.ArrayList;

// Provides internal node interface into Data Store operations
public interface DataStore extends FileOperations, SystemFileList {

  // Gets the object store location
  String getStoreLocation();
  
  // Fetches all Objects
  ArrayList<DataObject> getAllDataObjects();

  // File List Name
  String getFileListName(); 
}
