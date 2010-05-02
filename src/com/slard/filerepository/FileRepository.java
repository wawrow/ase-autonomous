package com.slard.filerepository;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.SimpleFormatter;

import org.jgroups.ChannelException;


public class FileRepository {
  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    // Intend to replace these instantiations with Guicey modules and injectors.
    // Move to using getopt for handiness.
    logManager.reset();
    Handler console = new ConsoleHandler();
    console.setFormatter(new SimpleFormatter());
    console.setLevel(Level.FINEST);
    logManager.getLogger("").addHandler(console);
    
    String curDir = System.getProperty("user.dir");
    Properties options = new Properties();
//    File configFile = new File(curDir, "filerepository.config");
//    try {
//      options.load(new FileInputStream(configFile));
//    } catch (Exception e) {
//      // should die here since we didn't load the config.
//    }
    options.put("datastore.dir", curDir + "\\store");
    DataStore store = new DataStoreImpl(options);
    CHT cht = new CHTImpl();
    //DataStore store = new DataStoreImpl(curDir + "/store", cht);
    
     NodeImpl node = new NodeImpl(store, cht, options);
    try {
      node.start();
    } catch (ChannelException e) {
    }
    //node.stop();
  }
}
