package com.slard.filerepository;

import org.jgroups.ChannelException;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.Properties;

public class FileRepository {
  public static void main(String[] args) {
    SLF4JBridgeHandler.install();

    String currentDirectory = System.getProperty("user.dir");
    Properties options = new Properties();
    String dir = currentDirectory + File.separator + "store";
    options.put("datastore.dir", dir);
    String hostname;
    try {
      hostname = java.net.InetAddress.getLocalHost().getHostName();
      options.put("datastore.hostname", hostname);
    }
    catch (java.net.UnknownHostException uhe) {
      hostname = null;
    }

    if (dir != null && hostname != null) {
      File tmp = new File(dir);
      options.put(SystemCommsClient.SYSTEM_NAME_PROP, hostname + "-" + tmp.getParentFile().getName());
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
