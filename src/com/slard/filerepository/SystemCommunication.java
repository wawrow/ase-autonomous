package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.ChannelException;

import java.util.Dictionary;
import java.util.Map;
import java.util.Vector;


//Provides methods to call by other nodes in the system
public interface SystemCommunication {
  void initialise() throws ChannelException;

  //Returns a list of known nodes
	Map<Long, NodeDescriptor> getNodelist();
	
	//Returns file list of files that are in nodes data storage 
	Vector<String> syncFilelist();
	
	//Stores a file in nodes data storage
	boolean store(DataObject dataObject, Address target);
	
	//Retrieve a file from nodes storage
	DataObject retrieve(String name);
	
	//Should file NodeImpl.NodeLeft event with node ids
	void nodeLeft();
	
	//Should file NodeImpl.NodeJoined event with node ids
	void nodeJoined();	
}
