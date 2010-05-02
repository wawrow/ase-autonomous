package com.slard.filerepository;

import org.jgroups.*;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Properties;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NodeImpl implements Node, MessageListener, MembershipListener {
  private static final int REPLICA_COUNT = 1;

  private static final String JOINED_AND_INITIALIZED = "joinedAndInitialized";

  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private static final String CHANNEL_NAME = "FileRepositoryCluster";
  SystemComsServerImpl systemComs = null;
  private DataStore dataStore;
  private ConsistentHash ch;
  Properties options;
  byte[] state;

  private long[] ids = null;

  public long[] getIds() {
    if (ids == null) {
      ids = this.ch.calculateHashes(this.channel.getAddress());
    }
    return ids;
  }

  private Channel channel;

  // Constructor
  public NodeImpl(DataStore dataStore, ConsistentHash cht, Properties options) {
    this.logger.setLevel(Level.ALL);
    this.dataStore = dataStore;
    this.ch = cht;
    this.options = options;
  }

  public void start() throws ChannelException {
    this.channel = new JChannel();
    channel.connect(CHANNEL_NAME);
    // this should probably be passed in as a parameter in constructor /Larry
    systemComs = new SystemComsServerImpl(channel, dataStore, this, this, this);
    logger.fine("channel connected and system coms server ready");
    logger.finer("My Address: " + channel.getAddress().toString());
    this.ch.recalculate(this.channel.getView());

    this.initializeDataStore();

    for (Long id : this.getIds()) {
      System.out.println("ID: " + id.toString());
    }

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
  public synchronized void receive(Message message) {
    if (message.getSrc() == this.channel.getAddress()) {
      return;
    }
    if (message.getObject().toString().equalsIgnoreCase(JOINED_AND_INITIALIZED)) {
      this.logger.fine("Node joined: " + message.getSrc().toString());
      this.nodeJoined(this.createNodeDescriptor(message.getSrc()));
    }
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

  // Joined/Left the network - synchronized causes less problems and solves lots
  // issues i find
  @Override
  public synchronized void viewAccepted(View view) {
    logger.fine("ViewAccepted");
    ConsistentHash.MemberDelta changes = ch.recalculate(view);
    // Don't do anything for freshly joined nodes until they send initialize
    // message
    for (Address address : changes.removed) {
      this.logger.fine("Node left: " + address.toString());
      this.nodeLeft(address);
    }
  }

  private NodeDescriptor createNodeDescriptor(Address address) {
    NodeDescriptor node = new NodeDescriptorImpl(address, this.ch, SystemComsClientImpl.getSystemComsClient(this.systemComs
        .GetDispatcher(), address));
    return node;
  }

  // Suspect Left the network
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
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      long masterId = this.ch.findMaster(obj.getName());
      if (this.channel.getAddress().equals(this.ch.getAddress(masterId))) {
        NodeDescriptor oldMaster = this.createNodeDescriptor(this.ch.findPreviousUniqueAddresses(masterId, 1).elementAt(0));
        if (!oldMaster.hasFile(obj.getName())) {
          this.replicateDataObject(obj);
        }
      } else {
        NodeDescriptor master = this.createNodeDescriptor(this.ch.getAddress(masterId));
        if (!master.hasFile(obj.getName())) {
          master.store(obj);
        }
        try {
          this.dataStore.deleteDataObject(obj.getName());
        } catch (Exception ex) {
          // TODO Implement some better error handling
        }
      }
    }

    // If i'm not the first in cluster - sent the message that I'm ready to go
    if (this.channel.getView().size() > 1) {
      try {
        this.channel.send(new Message(null, null, JOINED_AND_INITIALIZED));
      } catch (Exception ex) {
        // make me crash!!!
      }
    }
  }

  @Override
  public void nodeJoined(NodeDescriptor node) {
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      // If the guy is master of any of my files
      if (node.getAddress().equals(this.ch.findMasterAddress(obj.getName()))) {
        // Check if i was master before
        if (this.ch.findPreviousUniqueAddresses(this.ch.findMaster(obj.getName()), 1).contains(this.channel.getAddress())) {
          if (node.hasFile(obj.getName())) {
            if (node.getCRC(obj.getName()) != obj.getCRC()) {
              node.replace(obj);
            }
          } else {
            node.store(obj);
          }
        }
        try {
          this.dataStore.deleteDataObject(obj.getName());
        } catch (Exception ex) {
          // TODO better exception handing
        }
      }
    }
  }

  @Override
  public void nodeLeft(Address nodeAddress) {
    for(DataObject obj: this.dataStore.getAllDataObjects()){
      //Was he master for any of mine files?
      if(this.ch.findMasterAddress(obj.getName(), nodeAddress).equals(nodeAddress)){
        //Am i Master now?
        if(this.amIMaster(obj.getName())){
          this.replicateDataObject(obj);
        }
      }
      //Was he a replica for my file?
      else if(this.ch.findPreviousUniqueAddresses(this.ch.findMaster(obj.getName(), nodeAddress), REPLICA_COUNT, nodeAddress).contains(nodeAddress)){
        this.replicateDataObject(obj);
      }
    }
  }

  @Override
  public boolean amIMaster(String fileName) {
    return this.ch.findMasterAddress(fileName).equals(this.channel.getAddress());
  }
  
  @Override
  public void replicateDataObject(DataObject obj){
    for(Address nodeAddress: this.ch.findPreviousUniqueAddresses(this.ch.findMaster(obj.getName()), REPLICA_COUNT)){
      NodeDescriptor node = this.createNodeDescriptor(nodeAddress);
      if(node.hasFile(obj.getName()) ){
        if(node.getCRC(obj.getName()) != obj.getCRC()){
          node.replace(obj);
        }
      } else {
        node.store(obj);
      }
    }
  }
}
