package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 13-May-2010
 * Time: 19:43:05
 * To change this template use File | Settings | File Templates.
 */
public interface SystemCommsClient {
  Boolean store(DataObject dataObject, Set<Address> addresses);

  Boolean store(DataObject dataObject, Address address);

  Boolean hasFile(String name, Set<Address> addresses);

  Boolean hasFile(String name, Address address);

  Boolean replace(DataObject file, Set<Address> addresses);

  Boolean replace(DataObject file, Address address);

  Boolean delete(String name, Set<Address> addresses);

  Boolean delete(String name, Address address);

  Long getCRC(String name, Address address);

  DataObject retrieve(String name, Address address);

  Address getAddress();

  Channel getChannel();
}
