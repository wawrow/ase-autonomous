package com.slard.filerepository;

import org.jgroups.*;

import java.util.logging.Logger;
import java.util.Dictionary;
import java.util.Map;
import java.util.Vector;

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
	  if(this.ids == null){
		  this.ids = this.cht.GetIdsForNode(this);
	  }
    return this.ids;
  }

	@Override
	public void joinTheNetwork() {
		// Get current node list - probably not required as JGroups will tak care of most - we'll see
		this.nodes = this.systemComs.getNodelist();
		
	    Vector<DataObject> ownedObjects = this.cht.getOwnedObjects(this.nodes.keySet().toArray(new Long[0]), this.ids, this.dataStore.GetAllDataObjects());
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

  private void nodeJoined(NodeDescriptor node){
	  //Check if I should pass over any owned files 
	  //If so - pass them over
	  //Check if any of the replica files I've had aren't required to store any more
	  //If so - delete them
  }
  
  private void nodeLeft(NodeDescriptor node){
	  //remove node from CHT
	  //Check if I'm replica of files that leaving node left behind
	  //if so, check if I'll become owner
	  //if so, mark as owned and fire replication event 
  }
  
  
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
  //Joined the network
  @Override
  public void viewAccepted(View view) {
	  System.out.println("View accepted");
	  System.out.println(view.printDetails());
    cht.recalculate(view);
  }

  //Left the network
  @Override
  public void suspect(Address address) {
	  System.out.println("Suspect ");
	  System.out.println(address.toString());
    cht.leave(address);
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
     