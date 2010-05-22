package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;

import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 13-May-2010
 * Time: 19:43:05
 * <p/>
 * The interface used to request system-peer operations.
 */
public interface SystemCommsClient {
  /**
   * Store data on a set of peers. Tries to minimise call number.
   *
   * @param dataObject Content to store.
   * @param addresses  Set of peers to store data on.
   * @return true if all stores succeeded.
   */
  Boolean store(DataObject dataObject, Set<Address> addresses);

  /**
   * Store data on one peer. Convenience wrapper on multi-peer call.
   *
   * @param dataObject data to store.
   * @param address    Peer on which to store data.
   * @return true if stored.
   */
  Boolean store(DataObject dataObject, Address address);

  /**
   * Check whether a peer has a file.
   *
   * @param name    name of file to check.
   * @param address peer to check.
   * @return true if it does.
   */
  Boolean hasFile(String name, Address address);

  /**
   * Replace a file on multiple peers.
   *
   * @param file      The new version of the file.
   * @param addresses Peers to update.
   * @return true if all succeeded.
   */
  Boolean replace(DataObject file, Set<Address> addresses);

  /**
   * Replace a file on one peer. Convenience wrapper around previous method.
   *
   * @param file    File to replace.
   * @param address Peer to update.
   * @return true if it succeeds.
   */
  Boolean replace(DataObject file, Address address);

  /**
   * Delete file from multiple peers.
   *
   * @param name      Name of file to delete.
   * @param addresses Peers to update.
   * @return true if all succeeded.
   */
  Boolean delete(String name, Set<Address> addresses);

  /**
   * Convenience call to previous.
   *
   * @param name
   * @param address
   * @return
   */
  Boolean delete(String name, Address address);

  /**
   * Ask peer for the CRC of their copy of a file.
   *
   * @param name    Name of file to check.
   * @param address Address of peer.
   * @return CRC of the file on the peer.
   */
  Long getCRC(String name, Address address);

  /**
   * Get a file from a peer.
   *
   * @param name    Name of file to retrieve.
   * @param address Peer to ask.
   * @return Contents of the file.
   */
  DataObject retrieve(String name, Address address);

  /**
   * Get the address of my channel.
   *
   * @return My address.
   */
  Address getAddress();

  /**
   * Get my server channel.
   *
   * @return My server channel.
   */
  Channel getChannel();

  /**
   * Allows us to specify a channel name to override the default.
   */
  final String SYSTEM_NAME_PROP = "system.channel.name";

  /**
   * Created by IntelliJ IDEA.
   * User: kbrady
   * Date: 21-May-2010
   * Time: 14:15:52
   * To change this template use File | Settings | File Templates.
   */
  interface SystemCommsFactory {
    SystemCommsClient create(Node parent, Properties options);
  }
}
