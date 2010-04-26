package com.slard.filerepository;

import java.util.Dictionary;
import java.util.Vector;


//Provides methods to call by other nodes in the system
public interface SystemCommunication {

  //Initialises connection to cluster.
  public abstract void initialise();
	
	//Returns a list of known nodes
	public abstract Dictionary<Long, NodeDescriptor> getNodelist();
	
	//Returns file list of files that are in nodes data storage 
	public abstract Vector<String> syncFilelist();
	
	//Stores a file in nodes data storage
	public abstract void store(DataObject dataObject);
	
	//Retrieve a file from nodes storage
	public abstract DataObject retrieve(String name);
	
	//Should file NodeImpl.NodeLeft event with node ids
	public abstract void nodeLeft();
	
	//Should file NodeImpl.NodeJoined event with node ids
	public abstract void nodeJoined();
	
}
