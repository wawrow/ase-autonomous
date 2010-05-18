package com.slard.filerepository;

import org.jgroups.*;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 13-May-2010
 * Time: 09:44:48
 * To change this template use File | Settings | File Templates.
 */
public class SystemComms implements MessageListener, MembershipListener, SystemCommsClient {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "FileRepoCluster";
  private final RpcDispatcher dispatcher;
  private final Node parent;
  private byte[] state = new byte[0];
  private static final int RPC_TIMEOUT = 30000;
  private CommsPrep commsPrep;


  enum Calls implements CommsPrep.Calls {
    STORE("store"),
    HAS_FILE("hasFile"),
    GET_CRC("getCRC"),
    REPLACE("replace"),
    RETRIEVE("retrieve"),
    DELETE("delete");

    private String name;

    Calls(String name) {
      this.name = name;
    }

    public String method() {
      return name;
    }
  }

  SystemComms(Node parent, Properties options) {
    this.parent = parent;
    RpcDispatcher tmp;
    try {
      JChannel channel = new JChannel();
      String channel_name = options.getProperty(SystemCommsClient.SYSTEM_NAME_PROP);
      if (channel_name != null) {
        channel.setName(channel_name);
      }
      channel.connect(CHANNEL_NAME);
      tmp = new RpcDispatcher(channel, this, this, this);
      parent.registerChannel(channel);
    } catch (ChannelException e) {
      logger.error("Failed to connect to channel and initialise dispatcher:", e);
      tmp = null;
    }
    dispatcher = tmp;
    commsPrep = new CommsPrep(dispatcher, RPC_TIMEOUT);
  }

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

  @Override
  public Address getAddress() {
    return dispatcher.getChannel().getAddress();
  }
  // Client side

  @Override
  public Boolean store(DataObject file, Set<Address> addresses) {
    logger.debug("sending {} to {}", file.getName(), addresses.toString());
    Boolean ret = true;
    try {
      file.getData();
    } catch (IOException e) {
      logger.warn("failed to load content for {}", file.getName());
    }
    for (Object cur : issueRpcs(Calls.STORE, addresses, Request.GET_ALL, file)) {
      ret &= (Boolean) cur;
    }
    return ret;
  }

  @Override
  public Boolean store(DataObject dataObject, Address address) {
    if (address == null) {
      logger.warn("trying to send {} to a null address", dataObject.getName());
    }
    return store(dataObject, new HashSet<Address>(Arrays.asList(address)));
  }
  // Server side

  public Boolean store(DataObject dataObject) {
    logger.trace("asked to store {}", dataObject.getName());
    return parent.getDataStore().store(dataObject);
  }

  // Client side

  @Override
  public Boolean hasFile(String name, Set<Address> addresses) {
    Boolean ret = true;
    for (Object cur : issueRpcs(Calls.HAS_FILE, addresses, GroupRequest.GET_ALL, name)) {
      ret &= (Boolean) cur;
    }
    return ret;
  }

  @Override
  public Boolean hasFile(String name, Address address) {
    return hasFile(name, new HashSet<Address>(Arrays.asList(address)));
  }
  // Server side

  public Boolean hasFile(String name) {
    return parent.getDataStore().hasFile(name);
  }

  // Client side

  @Override
  public Long getCRC(String name, Address address) {
    return (Long) issueRpc(Calls.GET_CRC, address, GroupRequest.GET_FIRST, name);
  }
  // Server side

  public Long getCRC(String name) {
    return parent.getDataStore().getCRC(name);
  }

  // Client side

  @Override
  public Boolean replace(DataObject file, Set<Address> addresses) {
    Boolean ret = true;
    try {
      file.getData();
    } catch (IOException e) {
      logger.warn("failed to load content for {}", file.getName());
    }
    for (Object cur : issueRpcs(Calls.REPLACE, addresses, GroupRequest.GET_ALL, file)) {
      ret &= (Boolean) cur;
    }
    return ret;
  }

  @Override
  public Boolean replace(DataObject file, Address address) {
    return replace(file, new HashSet<Address>(Arrays.asList(address)));
  }
  // Server side

  public Boolean replace(DataObject file) {
    return parent.getDataStore().replace(file);
  }

  // Client side

  @Override
  public Boolean delete(String name, Set<Address> addresses) {
    Boolean ret = true;
    for (Object cur : issueRpcs(Calls.DELETE, addresses, GroupRequest.GET_ALL, name)) {
      ret &= (Boolean) cur;
    }
    return ret;
  }

  @Override
  public Boolean delete(String name, Address address) {
    return delete(name, new HashSet<Address>(Arrays.asList(address)));
  }
  // Server side

  public Boolean delete(String name) {
    return parent.getDataStore().delete(name);
  }

  //Client Side

  @Override
  public DataObject retrieve(String name, Address address) {
    return (DataObject) issueRpc(Calls.RETRIEVE, address, GroupRequest.GET_FIRST, name);
  }
  //Server side

  public DataObject retrieve(String name) {
    DataObject ret = parent.getDataStore().retrieve(name);
    try {
      ret.getData();
    } catch (IOException e) {
      logger.warn("failed to load content for {}", ret.getName());
    }
    return ret;
  }

  @Override
  public void receive(Message message) {
  }

  @Override
  public byte[] getState() {
    return state;
  }

  @Override
  public void setState(byte[] bytes) {
    state = bytes;
  }

  @Override
  public void viewAccepted(View view) {
    logger.info("View of size {} accepted", view.size());
    parent.update(new HashSet<Address>(view.getMembers()));  // we delegate update actions to the node.
  }

  @Override
  public void suspect(Address address) {
    logger.info("Warned about {}", address);
    parent.remove(address);  // parent takes care of calling anything that cares.
  }

  @Override
  public void block() {
    logger.info("Got block() call but ignoring it.");
  }
}
