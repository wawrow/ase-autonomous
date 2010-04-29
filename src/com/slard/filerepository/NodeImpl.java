package com.slard.filerepository;

import org.jgroups.*;

import java.util.Map;
import java.util.logging.Logger;

public class NodeImpl implements Node, MessageListener, MembershipListener {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "FileRepositoryCluster";
  private DataStore dataStore;
  private CHT cht;

  private SystemComs systemComs = null;
  private long[] ids;
  private Map<Long, NodeDescriptor> nodes;
  private Channel channel;

  //Constructor
  public NodeImpl(DataStore dataStore, CHT cht) {
    this.dataStore = dataStore;
    this.cht = cht;
  }

  public void start() throws ChannelException {
    this.channel = new JChannel();
    channel.connect(CHANNEL_NAME);
    this.systemComs = new SystemComsServer(channel, dataStore, this, this);
    logger.fine("channel connected and system coms server ready");

    // start even loop here (in new thread?)
  }

  public long[] getIds() {
    return new long[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void initializeDataStore() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  public void replicaGuard() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

//  @Override
//  public void nodeLeft(long[] nodeIds) {
//    //To change body of implemented methods use File | Settings | File Templates.
//  }
//
//  @Override
//  public void nodeJoined(long[] nodeIds) {
//    //To change body of implemented methods use File | Settings | File Templates.
//  }

  @Override
  public void receive(Message message) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public byte[] getState() {
    return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void setState(byte[] bytes) {
    // Not totally sure what messages we can receive, probably broadcast of system state (disk space etc)
    // probably in rdf.
  }

  @Override
  public void viewAccepted(View view) {
    cht.recalculate(view);
  }

  @Override
  public void suspect(Address address) {
    cht.leave(address);
  }

  @Override
  public void block() {
    // probably can be left empty.
  }
}
     