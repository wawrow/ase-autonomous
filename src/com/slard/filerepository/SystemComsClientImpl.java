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

public class SystemComsClientImpl implements SystemComsClient {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 90;

  private RpcDispatcher dispatcher = null;
  private MethodCall storeCall = new MethodCall("store", null, new Class[]{DataObject.class});
  private MethodCall retrieveCall = new MethodCall("retrieve", null, new Class[]{String.class});

  private SystemComsClientImpl(Channel channel) {
    this.dispatcher = new RpcDispatcher(channel, null, null, null);
  }

  @Override
  public Boolean store(DataObject dataObject, Address target) {
    storeCall.setArgs(new DataObject[]{dataObject});
    Boolean ret = false;
    try {
      ret = (Boolean) dispatcher.callRemoteMethod(target, storeCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT));
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public DataObject retrieve(String name, Address target) {
    retrieveCall.setArgs(new String[]{name});
    DataObject ret = null;
    try {
      ret = (DataObject) dispatcher.callRemoteMethod(target, storeCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT));
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }
}
