package com.slard.filerepository;

import java.util.logging.LogManager;

public class FileRepository {
  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    // Intend to replace these instantiations with Guicey modules and injectors.
    String curDir = System.getProperty("user.dir");
    CHT cht = new CHTImpl();
    DataStore store = new DataStoreImpl(curDir, cht);
    NodeImpl node = new NodeImpl(store, cht);
    try {
      node.start();
    } catch (Exception e) {
    }
  }
}
