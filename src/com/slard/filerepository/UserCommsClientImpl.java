package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.blocks.RspFilter;
import org.jgroups.util.RspList;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.util.logging.Level;
import java.util.logging.Logger;

public class UserCommsClientImpl implements UserOperations {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 3000;

  private Address target;
  private RpcDispatcher dispatcher = null;

  static public class FileResponseFilter implements RspFilter {
    @Override
    public boolean isAcceptable(Object response, Address source) {
      if (response != null) {
        return true;
      }
      return false;
    }
    @Override
    public boolean needMoreResponses() {
      return true;
    }
  }
  
  private UserCommsClientImpl(RpcDispatcher dispatcher, Address target) {
    this.dispatcher = dispatcher;
    this.target = target;
  }

  // Factory Method
  public static UserCommsClientImpl getUserCommsClient(RpcDispatcher dispatcher, Address target) {
    return new UserCommsClientImpl(dispatcher, target);
  }

  public static Object getQuickestFileLocation(RpcDispatcher dispatcher, String name) {
    MethodCall methodCall = new MethodCall("hasFile", null, new Class[] { String.class });
    methodCall.setArgs(new String[] { name });
    try {
      // Broadcast file name to all members and take the first non-null response
      RspList responseList = dispatcher.callRemoteMethods(null, methodCall,  
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT, false, new UserCommsClientImpl.FileResponseFilter()));
      return responseList.getFirst();
    } catch (Throwable throwable) {
      return null;
    }
  }

  private synchronized Object callWithMethod(MethodCall method) {
    return this.callWithMethod(method, 0);
  }

  private Object callWithMethod(MethodCall method, int attempt) {
    Object result = null;
    try {
      result = dispatcher.callRemoteMethod(this.target, method, new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT));
    } catch (Throwable throwable) {
      if (attempt > 3)
        logger.log(Level.WARNING, "rpc failed", throwable);
      else {  
        // retry
        return this.callWithMethod(method, attempt + 1);
      }
    }
    return result;
  }

  @Override
  public Boolean isServer() {
    MethodCall storeCall = new MethodCall("isServer", null, new Class[] {});
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(storeCall);
    } catch (Exception ex) {
    }
    return ret;
  }
  
  @Override
  public Boolean store(DataObject dataObject) {
    MethodCall storeCall = new MethodCall("store", null, new Class[] { DataObject.class });
    storeCall.setArgs(new DataObject[] { dataObject });
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(storeCall);
    } catch (Exception ex) {
    }
    return ret;
  }

  @Override
  public DataObject retrieve(String name) {
    MethodCall retrieveCall = new MethodCall("retrieve", null, new Class[] { String.class });
    retrieveCall.setArgs(new String[] { name });
    DataObject ret = null;
    try {
      ret = (DataObject) this.callWithMethod(retrieveCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public boolean replace(DataObject dataObject) {
    MethodCall replaceDataObjectCall = new MethodCall("replace", null, new Class[] { DataObject.class });
    replaceDataObjectCall.setArgs(new DataObject[] { dataObject });
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(replaceDataObjectCall);
    } catch (Exception ex) {
    }
    return ret;
  }

  @Override
  public boolean delete(String name) {
    MethodCall deleteCall = new MethodCall("delete", null, new Class[] { String.class });
    deleteCall.setArgs(new String[] { name });
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(deleteCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }
}