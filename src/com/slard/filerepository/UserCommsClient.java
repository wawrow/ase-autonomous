package com.slard.filerepository;


import org.jgroups.*;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.NotifyingFuture;
import org.jgroups.util.Tuple;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class UserCommsClient implements UserOperations {
  @InjectLogger
  Logger logger;
  private static final int RPC_TIMEOUT = 30000;

  private RpcDispatcher dispatcher = null;
  private final CommsPrep commsPrep;

  enum Calls implements CommsPrepImpl.Calls {
    STORE("store", 90000),
    HAS_FILE("hasFile", 500),
    GET_DISK_SPACE("getDiskSpace", 3000),
    REPLACE("replace", 90000),
    RETRIEVE("retrieve", 90000),
    DELETE("delete", 3000),
    STORE_ALL("storeAll", 120000),
    IS_SERVER("isServer", 500),
    GET_FILE_NAMES("getFileNames", 3000),
    IS_MASTER("isMaster", 500);

    private String name;
    private int timeout;

    Calls(String name, int timeout) {
      this.name = name;
      this.timeout = timeout;
    }

    public String method() {
      return name;
    }

    public int timeout() {
      return timeout;
    }
  }

  public UserCommsClient() {
    System.setProperty("jgroups.udp.mcast_port", UserCommsInterface.CLIENT_PORT);
    RpcDispatcher tmp;
    try {
      Channel channel = new JChannel();
      channel.connect(UserCommsInterface.CHANNEL_NAME);
      tmp = new RpcDispatcher(channel, null, null, new UserCommsDummyServerImpl());
    } catch (ChannelException e) {
      tmp = null;
    }
    dispatcher = tmp;
    commsPrep = new CommsPrepImpl(dispatcher, RPC_TIMEOUT);
  }

  @Override
  public void setTimeout(int timeout) {
    commsPrep.setDefaultTimeout(timeout);
  }

  @Override
  public Channel getChannel() {
    return dispatcher.getChannel();
  }

  // Broadcast isMaster(fileName) to all members and take the first non-null response
  @Override
  public Address getMaster(String name) {
    return (Address) commsPrep.issueRpcs(Calls.IS_MASTER, null, GroupRequest.GET_FIRST, -1, name).get(0);
  }

  // Broadcast hasFile(fileName) to all members and take the first non-null response
  @Override
  public Address getQuickestFileLocation(String name) {
    return (Address) commsPrep.issueRpcs(Calls.HAS_FILE, null, GroupRequest.GET_FIRST, -1, name).get(0);
  }

  @Override
  public Boolean isServer(Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.IS_SERVER, address, GroupRequest.GET_FIRST, -1);
  }

  @Override
  public Collection<String> getFileNames(String regex) {
    List<Object> responses = commsPrep.issueRpcs(Calls.GET_FILE_NAMES, null, GroupRequest.GET_ALL, -1, regex);
    Collection<String> ret = new HashSet<String>();
    for (Object rsp : responses) {
      ret.addAll((Collection<String>) rsp);
    }
    return ret;
  }

  @Override
  public Boolean store(DataObject file, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.STORE, address, GroupRequest.GET_ALL, -1, file);
  }

  @Override
  public Boolean storeAll(DataObject file, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.STORE_ALL, address, GroupRequest.GET_ALL, -1, file);
  }

  @Override
  public NotifyingFuture<Object> storeAllAsync(DataObject file, Address address) {
    return commsPrep.issueAsyncRpc(Calls.STORE_ALL, address, GroupRequest.GET_FIRST, 500, file);
  }

  @Override
  public DataObject retrieve(String name, Address address) {
    return (DataObject) commsPrep.issueRpc(Calls.RETRIEVE, address, GroupRequest.GET_ALL, -1, name);
  }

  @Override
  public boolean replace(DataObject file, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.REPLACE, address, GroupRequest.GET_ALL, -1, file);
  }

  @Override
  public boolean delete(String name, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.DELETE, address, GroupRequest.GET_ALL, -1, name);
  }

  @Override
  public Usage getDiskSpace() {
    long free = 0;
    long total = 0;
    Collection<String> hosts = new HashSet<String>();
    for (Object usage : commsPrep.issueRpcs(Calls.GET_DISK_SPACE, null, GroupRequest.GET_ALL, -1)) {
      Usage space = (Usage) usage;
      if (!hosts.contains(space.getHostname())) {
        hosts.add(space.getHostname());
        free += space.getFree();
      }
      total += space.getTotal();  // count even if we have already seen this machine.
    }
    return (new Usage(dispatcher.getChannel().getClusterName(), free, total));
  }

  @Override
  public Tuple<Collection<Address>, Collection<Address>> listNodes() {
    Channel channel = dispatcher.getChannel();
    View view = channel.getView();
    final Collection<Address> clients = new LinkedList<Address>();
    final Collection<Address> servers = new LinkedList<Address>();
    for (Address address : view.getMembers()) {
      if (address.equals(channel.getAddress())) {
        clients.add(address);
      } else {
        if ((Boolean) commsPrep.issueRpc(Calls.IS_SERVER, address, GroupRequest.GET_ALL, -1)) {
          servers.add(address);
        } else {
          clients.add(address);
        }
      }
    }
    return new Tuple<Collection<Address>, Collection<Address>>(clients, servers);
  }
}

