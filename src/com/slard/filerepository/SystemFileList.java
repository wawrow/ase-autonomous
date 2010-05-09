package com.slard.filerepository;

import java.util.List;

public interface SystemFileList {
  List<String> getFileNames();
  boolean addFileName(String fileName);
  boolean removeFileName(String fileName);
  boolean contains(String fileName);
}
