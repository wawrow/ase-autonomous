package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SystemComsClientImpl implements FileOperations, SystemFileList {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 3000;

  private Address target;

  private RpcDispatcher dispatcher = null;

  private SystemComsClientImpl(RpcDispatcher dispatcher, Address target) {
    this.dispatcher = dispatcher;
    this.target = target;
  }

  @Override
  public Boolean store(DataObject dataObject) {
    MethodCall storeCall = new MethodCall("store", null, new Class[]{DataObject.class});
    storeCall.setArgs(new DataObject[]{dataObject});
    Boolean ret = false;
    try {
      ret = (Boolean) this.callWithMethod(storeCall);
    } catch (Exception ex) {
    }
    return ret;
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
  public DataObject retrieve(String name) {
    MethodCall retrieveCall = new MethodCall("retrieve", null, new Class[]{String.class});
    retrieveCall.setArgs(new String[]{name});
    DataObject ret = null;
    try {
      ret = (DataObject) this.callWithMethod(retrieveCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  // Factory Method
  public static SystemComsClientImpl getSystemComsClient(RpcDispatcher dispatcher, Address target) {
    return new SystemComsClientImpl(dispatcher, target);
  }

  @Override
  public Long getCRC(String fileName) {
    MethodCall getCRCCall = new MethodCall("getCRC", null, new Class[]{String.class});
    getCRCCall.setArgs(new String[]{fileName});
    Long ret = null;
    try {
      ret = (Long) this.callWithMethod(getCRCCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public boolean hasFile(String name) {
    MethodCall hasFileCall = new MethodCall("hasFile", null, new Class[]{String.class});
    hasFileCall.setArgs(new String[]{name});
    Boolean ret = null;
    try {
      ret = (Boolean) this.callWithMethod(hasFileCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ArrayList<String> list() {
    MethodCall listCall = new MethodCall("list", null, new Class[]{String.class});
    ArrayList<String> ret = null;
    try {
      ret = (ArrayList<String>) this.callWithMethod(listCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
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
    } catch (Exception ex) {
    }
    return ret;
  }

  @Override
  public boolean delete(String name) {
    MethodCall deleteCall = new MethodCall("delete", null, new Class[]{String.class});
    deleteCall.setArgs(new String[]{name});
    Boolean ret = null;
    try {
      ret = (Boolean) this.callWithMethod(deleteCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  //SystemFileList

  @Override
  public boolean addFileName(String fileName) {
    MethodCall addFileNameCall = new MethodCall("addFileName", null, new Class[]{String.class});
    addFileNameCall.setArgs(new String[]{fileName});
    Boolean ret = null;
    try {
      ret = (Boolean) this.callWithMethod(addFileNameCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  @Override
  public boolean contains(String fileName) {
    MethodCall containsCall = new MethodCall("contains", null, new Class[]{String.class});
    containsCall.setArgs(new String[]{fileName});
    Boolean ret = null;
    try {
      ret = (Boolean) this.callWithMethod(containsCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;

  }

  @SuppressWarnings("unchecked")
  @Override
  public List<String> getFileNames() {
    MethodCall getFileNamesCall = new MethodCall("getFileNames", null, new Class[]{String.class});
    List<String> ret = null;
    try {
      ret = (List<String>) this.callWithMethod(getFileNamesCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;

  }

  @Override
  public boolean removeFileName(String fileName) {
    MethodCall removeFileNameCall = new MethodCall("removeFileName", null, new Class[]{String.class});
    removeFileNameCall.setArgs(new String[]{fileName});
    Boolean ret = null;
    try {
      ret = (Boolean) this.callWithMethod(removeFileNameCall);
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;

  }
}
