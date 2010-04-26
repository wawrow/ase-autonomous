package com.slard.filerepository;

import java.util.Dictionary;
import java.util.Vector;

import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class NodeImpl implements Node		{

	private DataStore dataStore;
	private SystemCommunication systemComm;
	private ClientCommunication clientComm;
	private CHTHelper chtHelper;
	private long[] ids;
	private Dictionary<Long, NodeDescriptor> nodes;	
	
	//Constructor
	public NodeImpl(DataStore _dataStore, SystemCommunication _systemComm, ClientCommunication _clientComm, CHTHelper _chtHelper){
		this.dataStore = _dataStore;
		this.systemComm = _systemComm;
		this.clientComm = _clientComm;
		this.chtHelper = _chtHelper;
	}
	
	@Override
	public long[] GetIds() {
		if(ids == null) {
			this.ids = this.chtHelper.GetIds();
		}
		return this.ids;
	}

	@Override
	public void InitializeDataStore() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void JoinTheNetwork() {
		// Find the network
		this.nodes = this.systemComm.GetNodelist();
		//
		    Vector<DataObject> ownedObjects = this.chtHelper.getOwnedObjects(this.nodes, this.ids, this.dataStore.GetAllDataObjects());
		    for(DataObject ownedObject: ownedObjects){
		    	//Get previous master Id
		    	//Match the CRC
		    	//Retrieve if required
		    	//Check if previous master will become replica
		    	//if not then delete
		    	//Fire Replicate Event on the file
		    }
		    
		// TODO For each file in dataStore if any, check if I'll become a replica or master and perform actions
	}

	//Fire this method when join or leave node message received
	@Override
	public void RelpicaGuard() {
		// TODO Check your replica nodes if they hold the replicas. Delete unnecessary replicas.
		
	}

	@Override
	public void Start() {
		this.JoinTheNetwork();		
	}

	@Override
	public void NodeJoined(long[] nodeIds) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void NodeLeft(long[] nodeIds) {
		// TODO Check if we're meant to become a master for any of the files that belonged to leaving node
		
	}
  
	
}
     