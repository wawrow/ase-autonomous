package com.slard.filerepository;

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
    logManager.reset();
    Handler console = new ConsoleHandler();
    console.setFormatter(new SimpleFormatter());
    console.setLevel(Level.FINEST);
    logManager.getLogger("").addHandler(console);
    
    String curDir = System.getProperty("user.dir");
    CHT cht = new CHTImpl();
    DataStore store = new DataStoreImpl(curDir + "/store", cht);
    NodeImpl node = new NodeImpl(store, cht);
    try {
      node.start();
    } catch (ChannelException e) {
    }
  }
}
