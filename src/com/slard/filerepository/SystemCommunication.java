package com.slard.filerepository;

import java.util.Dictionary;
import java.util.Vector;


//Provides methods to call by other nodes in the system
public interface SystemCommunication {
	
	//Returns a list of known nodes
	public abstract Dictionary<Long, NodeDescriptor> GetNodelist();
	
	//Returns file list of files that are in nodes data storage 
	public abstract Vector<String> SyncFilelist();
	
	//Stores a file in nodes data storage
	public abstract void Store(DataObject dataObject);
	
	//Retrieve a file form nodes storage
	public abstract DataObject Retrieve(String name);
	
	//Should file NodeImpl.NodeLeft event with node ids
	public abstract void NodeLeft();
	
	//Should file NodeImpl.NodeJoined event with node ids
	public abstract void NodeJoined();
	
}
