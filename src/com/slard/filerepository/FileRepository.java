package com.slard.filerepository;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.tools.jmx.Manager;
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

    if (hostname != null) {
      File tmp = new File(dir);
      options.put(SystemCommsClient.SYSTEM_NAME_PROP, hostname + "-" + tmp.getParentFile().getName());
    }
    if (hostname != null) {
      File tmp = new File(dir);
      options.put(UserCommsInterface.CLIENT_COMMS_PROP,
          hostname + "-client-" + tmp.getParentFile().getName());
    }

    Injector injector = Guice.createInjector(new FileRepositoryModule());
    DataStore store = injector.getInstance(DataStore.class);
    store.initialise(options);

    Node node = injector.getInstance(Node.class);
    Manager.manage("Repository", injector);
    try {
      node.start(options);
    } catch (ChannelException e) {
    }
  }
}
