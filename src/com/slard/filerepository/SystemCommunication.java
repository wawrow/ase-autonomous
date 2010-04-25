package com.slard.filerepository;

import java.util.Vector;


//Provides methods to call by other nodes in the system
public interface SystemCommunication {
	
	//Returns a list of known nodes - might not be required since we use JGroups - we'll see
	public abstract Vector<NodeDescriptor> SyncNodelist();
	
	//Returns file list of files that are in nodes data storage 
	public abstract Vector<String> SyncFilelist();
	
	//Stores a file in nodes data storage
	public abstract void Store(DataObject dataObject);
	
	//Retrieve a file form nodes storage
	public abstract DataObject Retrieve(String name);
}
