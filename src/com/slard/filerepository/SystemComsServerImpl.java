package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA. User: kbrady Date: 28-Apr-2010 Time: 01:24:59 To
 * change this template use File | Settings | File Templates.
 */
public class SystemComsServerImpl implements SystemComsServer {

	private DataStore store = null;
	private RpcDispatcher dispatcher = null;

	public SystemComsServerImpl(Channel channel, DataStore store,
			MessageListener messages, MembershipListener members) {
		this.store = store;
		this.dispatcher = new RpcDispatcher(channel, messages, members, this);
	}

	@Override
	public Boolean store(DataObject dataObject) {
		try {
			store.storeDataObject(dataObject);
		} catch (IOException ex) {
			return false;
		}
		return true;
	}

	@Override
	public DataObject retrieve(String name) {
	  try{
		return store.getDataObject(name);
	  } catch(IOException ex){
	    return null;
	  }
	  
	}

	public void stop() {
		dispatcher.stop();
	}
}