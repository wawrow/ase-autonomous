package com.slard.filerepository;

import com.google.inject.ImplementedBy;
import org.jgroups.Address;

import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 15-May-2010
 * Time: 17:52:18
 * Interface presented by servers to clients.
 */
@ImplementedBy(UserCommsServer.class)
public interface UserCommsInterface {
  /**
   * Default client cluster name.
   */
  static final String CHANNEL_NAME = "ClientCluster";
  /**
   * Default client listen port.
   */
  static final String CLIENT_PORT = "45589";
  /**
   * Allows us to override the channel name.
   */
  static final String CLIENT_COMMS_PROP = "client.channel.name";
  /**
   * The property we need to change to affect the listen port.
   */
  static final String JGROUPS_PORT_PROP = "jgroups.udp.mcast_port";

  /**
   * Are we a server?
   *
   * @return true for anything that fully implements this interface (not stubs).
   */
  Boolean isServer();

  /**
   * Clients may ask about whether this node is the master of a file.
   *
   * @param name Filename to check.
   * @return true if we are the master for this file.
   */
  Address isMaster(String name);

  /**
   * Do we have a file?
   *
   * @param name The file to check.
   * @return Address of master of the file.
   */
  Address hasFile(String name);

  /**
   * List all files we have.
   *
   * @param regex Pattern to select which files to report.
   * @return Filtered list of files.
   */
  List<String> getFileNames(String regex);

  /**
   * Store a file. Uses eventual consistency.
   *
   * @param file The file.
   * @return true if master has file.
   */
  Boolean store(DataObject file);

  /**
   * Store a file. Uses guaranteed consistency.
   *
   * @param file The file.
   * @return True if master and all replicas have the file.
   */
  Boolean storeAll(DataObject file);

  DataObject retrieve(String name);

  boolean replace(DataObject file);

  boolean delete(String name);

  Usage getDiskSpace();

  /**
   * Created by IntelliJ IDEA.
   * User: kbrady
   * Date: 21-May-2010
   * Time: 14:20:05
   * To change this template use File | Settings | File Templates.
   */
  interface UserCommsFactory {
    UserCommsInterface create(Node parent, Properties options);
  }
}
