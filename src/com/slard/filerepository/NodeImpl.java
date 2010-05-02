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
  private static final String JOINED_AND_INITIALIZED = "joinedAndInitialized";

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
    this.cht.recalculate(this.channel.getView());

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
  public void receive(Message message) {
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
    CHT.MemberDelta changes = cht.recalculate(view);
    // Don't do anything for freshly joined nodes until they send initialize
    // message
    for (Address address : changes.removed) {
      this.logger.fine("Node left: " + address.toString());
      this.nodeLeft(this.createNodeDescriptor(address));
    }
  }

  private NodeDescriptor createNodeDescriptor(Address address) {
    NodeDescriptor node = new NodeDescriptorImpl(address, this.cht, SystemComsClientImpl.getSystemComsClient(this.systemComs
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
    System.out.println("My Current Data Store:");
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      System.out.println("File: " + obj.getName() + " CRC " + obj.getCRC());
      long masterId = this.cht.findMaster(obj.getName());
      System.out.println("My: " + this.channel.getAddress() + " Belongs to: " + this.cht.getAddress(masterId));
      if (this.channel.getAddress() == this.cht.getAddress(masterId)) {
        System.out.println("Me is master");
        NodeDescriptor oldMaster = this.createNodeDescriptor(this.cht.findPrevousUniqueAddresses(masterId, 1).elementAt(0));
        if (!oldMaster.hasFile(obj.getName())) {
          // TODO Call to manage replicas
        }
      } else {
        NodeDescriptor master = this.createNodeDescriptor(this.cht.getAddress(masterId));
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
    System.out.println("Let's check the files.");
    for (DataObject obj : this.dataStore.getAllDataObjects()) {
      long owner = this.cht.findMaster(obj.getName());
      Address ownerAddress = this.cht.getAddress(owner);
      if (ownerAddress == this.channel.getAddress()) {
        for (Address replica : this.cht.findPrevousUniqueAddresses(owner, 1)) {
          // check if joining node is previous
          if (replica == node.getAddress()) {
            // take care of replicas
            System.out.println("Will Have to replicate this file.");
          }
        }
      } else if (ownerAddress == node.getAddress()) {
        // I have a file and this guy will be master
        // I should check with this fella if I shold keep the file
      }
    }
  }

  @Override
  public void nodeLeft(NodeDescriptor node) {
    // TODO Auto-generated method stub

  }

}
