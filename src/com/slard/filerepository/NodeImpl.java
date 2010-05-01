package com.slard.filerepository;

import org.jgroups.*;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.util.Properties;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeImpl implements Node, MessageListener, MembershipListener {
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private static final String CHANNEL_NAME = "FileRepositoryCluster";
  SystemComsServerImpl systemComs = null;
  private DataStore dataStore;
  private CHT cht;
  Properties options;
  byte[] state;

  private long[] ids = null;

  public long[] getIds() {
    if (ids == null) {
      ids = this.cht.getIDs(this.channel.getAddress());
    }
    return ids;
  }

  private Channel channel;

  // Constructor
  public NodeImpl(DataStore dataStore, CHT cht, Properties options) {
    this.logger.setLevel(Level.ALL);
    this.dataStore = dataStore;
    this.cht = cht;
    this.options = options;
  }

  public void start() throws ChannelException {
    this.channel = new JChannel();
    channel.connect(CHANNEL_NAME);
    // this should probably be passed in as a parameter in constructor /Larry
    systemComs = new SystemComsServerImpl(channel, dataStore, this, this);
    logger.fine("channel connected and system coms server ready");
    logger.finer("My Address: " + channel.getAddress().toString());
    this.initializeDataStore();
    // start even loop here (in new thread?)

    for (Long id : this.getIds()) {
      System.out.println("ID: " + id.toString());
    }

    // initial cht
    this.cht.recalculate(this.channel.getView());

  }

  public void stop() {
    systemComs.stop();
    channel.close();
  }

  public void replicaGuard() {
    // To change body of implemented methods use File | Settings | File
    // Templates.
  }

  @Override
  public void receive(Message message) {
    // To change body of implemented methods use File | Settings | File
    // Templates.
  }

  @Override
  public byte[] getState() {
    return state;
  }

  @Override
  public void setState(byte[] bytes) {
    // Not totally sure what messages we can receive, probably broadcast of
    // system state (disk space etc)
    // probably in rdf.
    state = bytes;
  }

  // Joined the network
  @Override
  public void viewAccepted(View view) {
    CHT.MemberDelta changes = cht.recalculate(view); // cht updated but need to
    // apply net chages.
    for (Address address : changes.added) {
      this.logger.fine("Node joined: " + address.toString());
      NodeDescriptor node = new NodeDescriptorImpl(address, this.cht,
          SystemComsClientImpl.getSystemComsClient(this.systemComs
              .GetDispatcher(), address));
      this.nodeJoined(node);
    }
    for (Address address : changes.removed) {
      this.logger.fine("Node left: " + address.toString());
      NodeDescriptor node = new NodeDescriptorImpl(address, this.cht,
          SystemComsClientImpl.getSystemComsClient(this.systemComs
              .GetDispatcher(), address));
      this.nodeLeft(node);
    }
  }

  // Left the network
  @Override
  public void suspect(Address address) {
    logger.info("Suspecting node: " + address.toString());
    // cht.remove(address);
  }

  @Override
  public void block() {
    // probably can be left empty.
  }

  @Override
  public void initializeDataStore() {
    System.out.println("My Current Data Store:");
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      System.out.println(obj.getName() + " CRC: " + obj.getCRC().toString());
    }
  }

  @Override
  public void nodeJoined(NodeDescriptor node) {
    System.out.println("Let's check the files.");
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      long owner = this.cht.findMaster(obj.getName());
      if (this.cht.getAddress(owner) == this.channel.getAddress()) {

        for(Address replica: this.cht.findPrevousUniqueAddresses(owner, 1)){
          // check if joining node is previous
          if(replica == node.getAddress()){
              // take care of replicas
              System.out.println("Will Have to replicate this file.");
          }          
        }       
      }
    }
  }

  @Override
  public void nodeLeft(NodeDescriptor node) {
    // TODO Auto-generated method stub

  }

}
