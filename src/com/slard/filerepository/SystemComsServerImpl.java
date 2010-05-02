package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 * Created by IntelliJ IDEA. User: kbrady Date: 28-Apr-2010 Time: 01:24:59 To
 * change this template use File | Settings | File Templates.
 */
public class SystemComsServerImpl implements SystemComs {

  private final Logger logger = Logger.getLogger(this.getClass().getName());
	private DataStore store = null;
	private RpcDispatcher dispatcher = null;

	public SystemComsServerImpl(Channel channel, DataStore store,
			MessageListener messages, MembershipListener members) {
		this.store = store;
		this.dispatcher = new RpcDispatcher(channel, messages, members, this);
}

	public RpcDispatcher GetDispatcher(){
	  return this.dispatcher;
	}
	
	@Override
	public Boolean store(DataObject dataObject) {
	  this.logger.info("Requested to store: " + dataObject.getName());
		try {
			store.storeDataObject(dataObject);
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	@Override
	public DataObject retrieve(String name) {
    this.logger.info("Requested to retrieve: " + name);
	  try{
		return store.getDataObject(name);
	  } catch(IOException ex){
	    return null;
	  }
	  
	}

	public void stop() {
		dispatcher.stop();
	}

  @Override
  public Long getCRC(String fileName) {
    this.logger.info("Requested CRC: " + fileName);
    try{
    return store.getDataObject(fileName).getCRC();
    } catch(Exception ex){
      return null;
    }
  }

  @Override
  public boolean hasFile(String name) {
    this.logger.info("Requested hasFile: " + name);    
    return store.contains(name);
  }

  @Override
  public Vector<String> list() {
    this.logger.info("Requested list.");    
    Vector<String> result = new Vector<String>();
    for(DataObject dataObj: store.getAllDataObjects()){
      result.add(dataObj.getName());
    }
    return result;
  }
}
