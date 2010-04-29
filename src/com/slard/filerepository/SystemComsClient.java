package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 26-Apr-2010
 * Time: 23:30:43
 * To change this template use File | Settings | File Templates.
 */
public class SystemComsClient implements SystemComs {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 90;

  private RpcDispatcher dispatcher = null;
  private Address address = null;
  private MethodCall storeCall = new MethodCall("store", null, new Class[]{DataObject.class});
  private MethodCall retrieveCall = new MethodCall("retrieve", null, new Class[]{String.class});

  private SystemComsClient(Channel channel, Address target) {
    this.dispatcher = new RpcDispatcher(channel, null, null, null);
    this.address = target;
  }

  @Override
  public Map<Long, NodeDescriptor> getNodelist() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Vector<String> syncFilelist() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Boolean store(DataObject dataObject) {
    storeCall.setArgs(new DataObject[]{dataObject});
    Boolean ret = false;
    try {
      ret = (Boolean) dispatcher.callRemoteMethod(address, storeCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT));
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public DataObject retrieve(String name) {
    retrieveCall.setArgs(new String[]{name});
    DataObject ret = null;
    try {
      ret = (DataObject) dispatcher.callRemoteMethod(address, storeCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT));
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

//  @Override
//  public void nodeLeft() {
//    //To change body of implemented methods use File | Settings | File Templates.
//  }
//
//  @Override
//  public void nodeJoined() {
//    //To change body of implemented methods use File | Settings | File Templates.
//  }
}
