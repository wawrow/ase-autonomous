package com.slard.filerepository;

import org.jgroups.*;
import org.jgroups.blocks.GroupRequest;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 13-May-2010
 * Time: 09:44:48
 * To change this template use File | Settings | File Templates.
 */
public class SystemComms implements MessageListener, MembershipListener, SystemCommsClient {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "FileRepoCluster";
  private final RpcDispatcher dispatcher;
  private final Node parent;
  private byte[] state = new byte[0];
  private int sendTimeout = 90;

  enum Calls {
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

  SystemComms(Node parent) {
    this.parent = parent;
    RpcDispatcher tmp;
    try {
      Channel channel = new JChannel(CHANNEL_NAME);
      tmp = new RpcDispatcher(channel, this, this, this);
    } catch (ChannelException e) {
      tmp = null;
    }
    dispatcher = tmp;
  }

  public void setSendTimeout(int timeout) {
    sendTimeout = timeout;
  }


  @Override
  public Address getAddress() {
    return dispatcher.getChannel().getAddress();
  }

  private MethodCall getMethodCall(Calls call, Object... args) {
    Class[] types = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      types[i] = args[i].getClass();
    }
    return new MethodCall(call.method(), args, types);
  }

  private Boolean issueBooleanRpcs(Calls toCall, Object obj, Set<Address> addresses) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(GroupRequest.GET_ALL, sendTimeout);
    Boolean ret = true;
    try {
      for (Object rsp : dispatcher.callRemoteMethods(addresses, call, options).getResults()) {
        ret &= (Boolean) rsp;
      }
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, toCall.method() + " rpc failed", throwable);
    }
    return ret;
  }

  private Object issueRpc(Calls toCall, Object obj, Address address) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(GroupRequest.GET_ALL, sendTimeout);
    Object ret = false;
    try {
      ret = dispatcher.callRemoteMethod(address, call, options);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, toCall.method() + " rpc failed", throwable);
    }
    return ret;
  }

  // Client side

  @Override
  public Boolean store(DataObject dataObject, Set<Address> addresses) {
    return issueBooleanRpcs(Calls.STORE, dataObject, addresses);
  }

  @Override
  public Boolean store(DataObject dataObject, Address address) {
    return store(dataObject, new HashSet<Address>(Arrays.asList(address)));
  }
  // Server side

  public Boolean store(DataObject dataObject) {
    return parent.getDataStore().store(dataObject);
  }

  // Client side

  @Override
  public Boolean hasFile(String name, Set<Address> addresses) {
    return issueBooleanRpcs(Calls.HAS_FILE, name, addresses);
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
    return (Long) issueRpc(Calls.GET_CRC, name, address);
  }
  // Server side

  public Long getCRC(String name) {
    return parent.getDataStore().getCRC(name);
  }

  // Client side

  @Override
  public Boolean replace(DataObject file, Set<Address> addresses) {
    return issueBooleanRpcs(Calls.REPLACE, file, addresses);
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
    return issueBooleanRpcs(Calls.DELETE, name, addresses);
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
    return (DataObject) issueRpc(Calls.RETRIEVE, name, address);
  }
  //Server side

  public DataObject retrieve(String name) {
    return parent.getDataStore().retrieve(name);
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
    logger.info("View of size " + view.size() + " accepted");
    parent.update(new HashSet<Address>(view.getMembers()));  // we delegate update actions to the node.
  }

  @Override
  public void suspect(Address address) {
    logger.info("Warned about " + address);
    parent.remove(address);  // parent takes care of calling anything that cares.
  }

  @Override
  public void block() {
    logger.info("Got block() call but ignoring it.");
  }
}
