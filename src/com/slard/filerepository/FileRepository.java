package com.slard.filerepository;

import java.util.logging.LogManager;

public class FileRepository {
  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    // Intend to replace these instantiations with Guicey modules and injectors.
    DataStore store = new DataStoreImpl();
    CHT cht = new CHTImpl();
    NodeImpl node = new NodeImpl(store, cht);
    try {
      node.start();
    } catch (Exception e) {
    }
  }
}
