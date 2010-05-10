package com.slard.filerepository;


import org.jgroups.Address;
import org.jgroups.blocks.*;
import org.jgroups.util.RspList;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserCommsClientImpl implements UserOperations {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 3000;

  private Address target;
  private RpcDispatcher dispatcher = null;

  // Used when broadcasting for who has a given file name or if a node is a master
  static public class NullResponseFilter implements RspFilter {
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

  // Broadcast isMaster(fileName) to all members and take the first non-null response
  public static Object getMaster(RpcDispatcher dispatcher, String name) {
    MethodCall methodCall = new MethodCall("isMaster", null, new Class[]{String.class});
    methodCall.setArgs(new String[]{name});
    try {
      RspList responseList = dispatcher.callRemoteMethods(null, methodCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT, false, new UserCommsClientImpl.NullResponseFilter()));
      return responseList.getFirst();
    } catch (Throwable throwable) {
      return null;
    }
  }

  // Broadcast hasFile(fileName) to all members and take the first non-null response
  public static Object getQuickestFileLocation(RpcDispatcher dispatcher, String name) {
    MethodCall methodCall = new MethodCall("hasFile", null, new Class[]{String.class});
    methodCall.setArgs(new String[]{name});
    try {
      RspList responseList = dispatcher.callRemoteMethods(null, methodCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT, false, new UserCommsClientImpl.NullResponseFilter()));
      return responseList.getFirst();
    } catch (Throwable throwable) {
      return null;
    }
  }

  private Object callWithMethod(MethodCall method) {
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
    MethodCall storeCall = new MethodCall("isServer", null, new Class[]{});
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(storeCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "isServer rpc failed", throwable);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getFileNames() {
    MethodCall getFileNamesCall = new MethodCall("getFileNames", null, new Class[]{});
    Set<String> files = new HashSet<String>();
    try {
      RspList responseList = dispatcher.callRemoteMethods(null, getFileNamesCall,
          new RequestOptions(Request.GET_ALL, RPC_TIMEOUT, false,
              new UserCommsClientImpl.NullResponseFilter()));
      for (Object rsp : responseList.getResults()) {
        files.addAll((Collection<String>) rsp);
      }
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "getFileNames rpc failed", throwable);
    }
    return new ArrayList<String>(files);
  }

  @Override
  public Boolean store(DataObject dataObject) {
    MethodCall storeCall = new MethodCall("store", null, new Class[]{DataObject.class});
    storeCall.setArgs(new DataObject[]{dataObject});
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(storeCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "store rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public DataObject retrieve(String name) {
    MethodCall retrieveCall = new MethodCall("retrieve", null, new Class[]{String.class});
    retrieveCall.setArgs(new String[]{name});
    DataObject ret = null;
    try {
      ret = (DataObject) this.callWithMethod(retrieveCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "retrieve rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public boolean replace(DataObject dataObject) {
    MethodCall replaceDataObjectCall = new MethodCall("replace", null, new Class[]{DataObject.class});
    replaceDataObjectCall.setArgs(new DataObject[]{dataObject});
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(replaceDataObjectCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "replace rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public boolean delete(String name) {
    MethodCall deleteCall = new MethodCall("delete", null, new Class[]{String.class});
    deleteCall.setArgs(new String[]{name});
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(deleteCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "delete rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public DiskSpace getDiskSpace() {
    MethodCall methodCall = new MethodCall("getDiskSpace", null, new Class[]{});
    long free = 0;
    long total = 0;
    Set<String> hosts = new HashSet<String>();
    try {
      RspList responseList = dispatcher.callRemoteMethods(null, methodCall,
          new RequestOptions(Request.GET_ALL, RPC_TIMEOUT, false, new UserCommsClientImpl.NullResponseFilter()));
      for (Object rsp : responseList.getResults()) {
        DiskSpace space = (DiskSpace) rsp;
        if (!hosts.contains(space.hostname)) {
          hosts.add(space.hostname);
          free += space.free;
          total += space.total;
        }
      }
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "getDiskSpace rpc failed", throwable);
    }
    return new DiskSpace(dispatcher.getChannel().getClusterName(), free, total);
  }
}
