package com.slard.filerepository;

import org.jgroups.Address;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 15-May-2010
 * Time: 17:52:18
 * To change this template use File | Settings | File Templates.
 */
public interface UserCommsInterface {
  static final String CHANNEL_NAME = "ClientCluster";
  static final String CLIENT_PORT = "45589";
  static final String CLIENT_COMMS_PROP = "client.channel.name";
  static final String JGROUPS_PORT_PROP = "jgroups.udp.mcast_port";

  Boolean isServer();

  // Clients may ask about whether this node is the master of a file

  Address isMaster(String name);

  // Clients may ask about file ownership before directing their requests

  Address hasFile(String name);

  List<String> getFileNames(String regex);

  Boolean store(DataObject file);

  Boolean storeAll(DataObject file);

  DataObject retrieve(String name);

  boolean replace(DataObject file);

  boolean delete(String name);

  Usage getDiskSpace();
}