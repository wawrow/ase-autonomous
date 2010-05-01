package com.slard.filerepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.LogManager;

public class FileRepository {
  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    // Intend to replace these instantiations with Guicey modules and injectors.
    // Move to using getopt for handiness.
    String curDir = System.getProperty("user.dir");
    Properties options = new Properties();
    File configFile = new File(curDir, "filerepository.config");
    try {
      options.load(new FileInputStream(configFile));
    } catch (Exception e) {
      // should die here since we didn't load the config.
    }
    DataStore store = new DataStoreImpl(options);
    CHT cht = new CHTImpl();
    NodeImpl node = new NodeImpl(store, cht, options);
    try {
      node.start();
    } catch (Exception e) {
    }
    node.stop();
  }
}
