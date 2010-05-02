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
import java.util.zip.CRC32;

public class SystemComsClientImpl implements SystemComs {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int RPC_TIMEOUT = 90;

  private Address target;
  
  private RpcDispatcher dispatcher = null;
  private MethodCall storeCall = new MethodCall("store", null, new Class[]{DataObject.class});
  private MethodCall retrieveCall = new MethodCall("retrieve", null, new Class[]{String.class});

  private SystemComsClientImpl(RpcDispatcher dispatcher, Address target) {
    this.dispatcher = dispatcher;
    this.target = target;
  }

  @Override
  public Boolean store(DataObject dataObject) {
    storeCall.setArgs(new DataObject[]{dataObject});
    Boolean ret = false;
    try {
      ret = (Boolean) dispatcher.callRemoteMethod(this.target, storeCall,
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
      ret = (DataObject) dispatcher.callRemoteMethod(this.target, storeCall,
          new RequestOptions(Request.GET_FIRST, RPC_TIMEOUT));
    } catch (Throwable throwable) {
      logger.log(Level.WARNING, "rpc failed", throwable);
    }
    return ret;
  }

  //Factory Method
  public static SystemComs getSystemComsClient(RpcDispatcher dispatcher, Address target) {
    return new SystemComsClientImpl(dispatcher, target);
  }

  @Override
  public CRC32 getCRC(String fileName) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean hasFile(String name) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Vector<String> list() {
    // TODO Auto-generated method stub
    return null;
  }
}
