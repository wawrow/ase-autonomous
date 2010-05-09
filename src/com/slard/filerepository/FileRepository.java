package com.slard.filerepository;

import org.jgroups.ChannelException;

import java.io.File;
import java.util.Properties;
import java.util.logging.*;

public class FileRepository {
  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    logManager.reset();
    Handler console = new ConsoleHandler();
    console.setFormatter(new Formatter() {
      public String format(LogRecord log) {
        String className = log.getSourceClassName();
        return new StringBuilder(log.getLevel().getLocalizedName())
            .append("\t[").append(log.getThreadID())
            .append("] ").append(className.substring(className.lastIndexOf('.') + 1))
            .append(".").append(log.getSourceMethodName())
            .append("\t").append(log.getMessage())
            .append("\n")
            .toString();
      }
    });

    //SimpleFormatter());
    console.setLevel(Level.FINEST);
    logManager.getLogger("").addHandler(console);

    String currentDirectory = System.getProperty("user.dir");
    Properties options = new Properties();
    options.put("datastore.dir", currentDirectory + File.separator + "store");
    DataStore store = new DataStoreImpl(options);

    Node node = new NodeImpl(store, options);
    try {
      node.start();
    } catch (ChannelException e) {
    }
    //node.stop();
  }
}
