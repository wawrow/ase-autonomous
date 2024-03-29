package com.slard.filerepository;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.assistedinject.Assisted;
import org.jgroups.*;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RpcDispatcher;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 13-May-2010
 * Time: 09:44:48
 * To change this template use File | Settings | File Templates.
 */
@Singleton
public class SystemComms implements MessageListener, MembershipListener, SystemCommsClient {
  @InjectLogger
  Logger logger;
  private static final String CHANNEL_NAME = "FileRepoCluster";
  private final RpcDispatcher dispatcher;
  private final Node parent;
  private byte[] state = new byte[0];
  private static final int RPC_TIMEOUT = 30000;
  private CommsPrep commsPrep;


  enum Calls implements CommsPrepImpl.Calls {
    STORE("store", 90000),
    HAS_FILE("hasFile", 500),
    GET_CRC("getCRC", 500),
    REPLACE("replace", 90000),
    RETRIEVE("retrieve", 90000),
    DELETE("delete", 500);

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

  @Inject
  SystemComms(Node parent, @Assisted Properties options, CommsPrep.CommsPrepFactory commsPrepFactory) {
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
    commsPrep = commsPrepFactory.create(dispatcher, RPC_TIMEOUT);
  }

  public void setTimeout(int timeout) {
    commsPrep.setDefaultTimeout(timeout);
  }

  @Override
  public Channel getChannel() {
    return dispatcher.getChannel();
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
    for (Object cur : commsPrep.issueRpcs(Calls.STORE, addresses, Request.GET_ALL, -1, file)) {
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
  public Boolean hasFile(String name, Address address) {
    return (Boolean) commsPrep.issueRpc(Calls.HAS_FILE, address, GroupRequest.GET_FIRST, -1, name);
  }
  // Server side

  public Boolean hasFile(String name) {
    return parent.getDataStore().hasFile(name);
  }

  // Client side

  @Override
  public Long getCRC(String name, Address address) {
    return (Long) commsPrep.issueRpc(Calls.GET_CRC, address, GroupRequest.GET_FIRST, -1, name);
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
    for (Object cur : commsPrep.issueRpcs(Calls.REPLACE, addresses, GroupRequest.GET_ALL, -1, file)) {
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
    for (Object cur : commsPrep.issueRpcs(Calls.DELETE, addresses, GroupRequest.GET_ALL, -1, name)) {
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
    return (DataObject) commsPrep.issueRpc(Calls.RETRIEVE, address, GroupRequest.GET_FIRST, -1, name);
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
