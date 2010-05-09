package com.slard.filerepository;

import java.util.List;

/**
 * The Interface SystemFileList.
 */
public interface SystemFileList {
  
  /**
   * Gets all file names from file list.
   *
   * @return the file names
   */
  List<String> getFileNames();
  
  /**
   * Adds the file name to file list.
   *
   * @param fileName the file name
   * @return true, if successful
   */
  boolean addFileName(String fileName);
  
  /**
   * Removes the file name from file list.
   *
   * @param fileName the file name
   * @return true, if successful
   */
  boolean removeFileName(String fileName);
  
  /**
   * Checks whether file list contains a file name.
   *
   * @param fileName the file name
   * @return true, if successful
   */
  boolean contains(String fileName);
}
