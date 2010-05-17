package com.slard.filerepository;

import org.jgroups.ChannelException;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.Properties;

public class FileRepository {
//  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    SLF4JBridgeHandler.install();
//    logManager.reset();
//    Handler console = new ConsoleHandler();
//    console.setFormatter(new Formatter() {
//      public String format(LogRecord log) {
//        String className = log.getSourceClassName();
//        return new StringBuilder(log.getLevel().getLocalizedName())
//            .append("\t[").append(log.getThreadID())
//            .append("] ").append(className.substring(className.lastIndexOf('.') + 1))
//            .append(".").append(log.getSourceMethodName())
//            .append("\t").append(log.getMessage())
//            .append("\n")
//            .toString();
//      }
//    });

//    console.setLevel(Level.FINEST);
//    logManager.getLogger("").addHandler(console);

    String currentDirectory = System.getProperty("user.dir");
    Properties options = new Properties();
    options.put("datastore.dir", currentDirectory + File.separator + "store");
    try {
      java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
      options.put("datastore.hostname", localMachine.getHostName());
    }
    catch (java.net.UnknownHostException uhe) {
    }
    DataStore store = new DataStoreImpl(options);
    store.initialise();

    Node node = new NodeImpl(store, options);
    try {
      node.start();
    } catch (ChannelException e) {
    }
  }
}
