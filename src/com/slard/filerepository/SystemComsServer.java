package com.slard.filerepository;

import org.jgroups.Channel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.blocks.RpcDispatcher;

import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 28-Apr-2010
 * Time: 01:24:59
 * To change this template use File | Settings | File Templates.
 */
public class SystemComsServer implements SystemComs {

  private DataStore store = null;
  private RpcDispatcher dispatcher = null;

  public SystemComsServer(Channel channel, DataStore store, MessageListener messages, MembershipListener members) {
    this.store = store;
    this.dispatcher = new RpcDispatcher(channel, messages, members, this);
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
    store.StoreDataObject(dataObject);
    return true;
  }

  @Override
  public DataObject retrieve(String name) {
    return store.GetDataObject(name);
  }

  public void stop() {
    dispatcher.stop();
  }
}
