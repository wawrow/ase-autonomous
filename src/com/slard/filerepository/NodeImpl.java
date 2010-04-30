package com.slard.filerepository;

import org.jgroups.*;

import java.util.logging.Logger;


public class NodeImpl implements Node, MessageListener, MembershipListener {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "FileRepositoryCluster";
  SystemComsServer systemComs = null;
  private DataStore dataStore;
  private CHT cht;
  byte[] state;

  // private long[] ids;
  //private Map<Long, NodeDescriptor> nodes;
  private Channel channel;

  //Constructor
  public NodeImpl(DataStore dataStore, CHT cht) {
    this.dataStore = dataStore;
    this.cht = cht;
  }

  public void start() throws ChannelException {
    this.channel = new JChannel();
    channel.connect(CHANNEL_NAME);
    systemComs = new SystemComsServer(channel, dataStore, this, this);
    logger.fine("channel connected and system coms server ready");

    // start even loop here (in new thread?)
  }

  public void stop() {
    systemComs.stop();
    channel.close();
  }

  public void replicaGuard() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void receive(Message message) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public byte[] getState() {
    return state;
  }

  @Override
  public void setState(byte[] bytes) {
    // Not totally sure what messages we can receive, probably broadcast of system state (disk space etc)
    // probably in rdf.
    state = bytes;
  }

  //Joined the network
  @Override
  public void viewAccepted(View view) {
    CHT.MemberDelta changes = cht.recalculate(view);  // cht updated but need to apply net chages.
  }

  //Left the network
  @Override
  public void suspect(Address address) {
    cht.remove(address);
  }

  @Override
  public void block() {
    // probably can be left empty.	
  }

  @Override
  public void initializeDataStore() {
    // TODO Auto-generated method stub
  }

}
     