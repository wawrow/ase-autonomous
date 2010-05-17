package com.slard.filerepository;


import org.jgroups.*;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.Tuple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;


public class UserCommsClient implements UserOperations {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 30000;

  private Address myAddress;
  private RpcDispatcher dispatcher = null;
  private final CommsPrep commsPrep;

  enum Calls implements CommsPrep.Calls {
    STORE("store"),
    HAS_FILE("hasFile"),
    GET_DISK_SPACE("getDiskSpace"),
    REPLACE("replace"),
    RETRIEVE("retrieve"),
    DELETE("delete"),
    STORE_ALL("storeAll"),
    IS_SERVER("isServer"),
    GET_FILE_NAMES("getFileNames"),
    IS_MASTER("isMaster");

    private String name;

    Calls(String name) {
      this.name = name;
    }

    public String method() {
      return name;
    }
  }

  public UserCommsClient() {
    System.setProperty("jgroups.udp.mcast_port", UserCommsInterface.CLIENT_PORT);
    RpcDispatcher tmp;
    try {
      Channel channel = new JChannel();
      channel.connect(UserCommsInterface.CHANNEL_NAME);
      myAddress = channel.getAddress();
      tmp = new RpcDispatcher(channel, null, null, new UserCommsDummyServerImpl());
    } catch (ChannelException e) {
      tmp = null;
    }
    dispatcher = tmp;
    commsPrep = new CommsPrep(dispatcher, RPC_TIMEOUT);
  }

  @Override
  public void setTimeout(int timeout) {
    commsPrep.setTimeout(timeout);
  }

  @Override
  public Channel getChannel() {
    return dispatcher.getChannel();
  }

  private List<Object> issueRpcs(CommsPrep.Calls toCall, Collection<Address> addresses, int gatherOption,
                                 Object... obj) {
    return commsPrep.issueRpcs(toCall, addresses, gatherOption, obj);
  }

  private Object issueRpc(CommsPrep.Calls toCall, Address address, int gatherOption, Object... obj) {
    return commsPrep.issueRpc(toCall, address, gatherOption, obj);
  }

  private Object issueRpc(CommsPrep.Calls toCall, Address address, int gatherOptions) {
    return commsPrep.issueRpc(toCall, address, gatherOptions);
  }

  // Broadcast isMaster(fileName) to all members and take the first non-null response
  @Override
  public Address getMaster(String name) {
    return (Address) commsPrep.issueRpcs(Calls.IS_MASTER, null, GroupRequest.GET_FIRST, name).get(0);
  }

  // Broadcast hasFile(fileName) to all members and take the first non-null response
  @Override
  public Address getQuickestFileLocation(String name) {
    return (Address) commsPrep.issueRpcs(Calls.HAS_FILE, null, GroupRequest.GET_FIRST, name).get(0);
  }

  @Override
  public Boolean isServer(Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.IS_SERVER, address, GroupRequest.GET_FIRST);
  }

  @Override
  public Collection<String> getFileNames(String regex) {
    List<Object> responses = commsPrep.issueRpcs(Calls.GET_FILE_NAMES, null, GroupRequest.GET_ALL, regex);
    Collection<String> ret = new HashSet<String>();
    for (Object rsp : responses) {
      ret.addAll((Collection<String>) rsp);
    }
    return ret;
  }

  @Override
  public Boolean store(DataObject file, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.STORE, address, GroupRequest.GET_ALL, file);
  }

  @Override
  public Boolean storeAll(DataObject file, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.STORE_ALL, address, GroupRequest.GET_ALL, file);
  }

  @Override
  public DataObject retrieve(String name, Address address) {
    return (DataObject) commsPrep.issueRpc(Calls.RETRIEVE, address, GroupRequest.GET_ALL, name);
  }

  @Override
  public boolean replace(DataObject file, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.REPLACE, address, GroupRequest.GET_ALL, file);
  }

  @Override
  public boolean delete(String name, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.DELETE, address, GroupRequest.GET_ALL, name);
  }

  @Override
  public Usage getDiskSpace() {
    long free = 0;
    long total = 0;
    Collection<String> hosts = new HashSet<String>();
    for (Object usage : commsPrep.issueRpcs(Calls.GET_DISK_SPACE, null, GroupRequest.GET_ALL)) {
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
        if ((Boolean) commsPrep.issueRpc(Calls.IS_SERVER, address, GroupRequest.GET_ALL)) {
          servers.add(address);
        } else {
          clients.add(address);
        }
      }
    }
    return new Tuple<Collection<Address>, Collection<Address>>(clients, servers);
  }
}

