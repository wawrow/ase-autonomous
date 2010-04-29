package com.slard.filerepository;

import org.jgroups.*;

import java.util.logging.Logger;
import java.util.Dictionary;
import java.util.Map;

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

	@Override
	public void joinTheNetwork() {
		// Find the network
		// TODO I don't know whether we need additional method for finding the network or will finding the node list be enough
		// Get current node list
		this.nodes = this.systemComm.getNodelist();
		
	    Vector<DataObject> ownedObjects = this.chtHelper.getOwnedObjects(this.nodes.keySet().toArray(new Long[0]), this.ids, this.dataStore.GetAllDataObjects());
	    for(DataObject ownedObject: ownedObjects){
	    	//Get previous master Id
	    	//Match the CRC
	    	//Retrieve if required
	    	//Check if previous master will become replica
	    	//if not then delete
	    	//Fire Replicate Event on the file
	    }
	
		// TODO For each file in dataStore if any, check if I'll become a replica or master and perform actions
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

  }

@Override
public void initializeDataStore() {
	// TODO Auto-generated method stub
  @Override
  public void block() {
    // probably can be left empty.
	
}
}
     