package com.slard.filerepository;


public interface DataObject {

	public abstract long getId();
	public abstract Byte[] getData();
	public abstract String getName();
	public abstract String getCRC(); 
	
	//Returns true if this object is master object, otherwise it's replica
	public abstract Boolean isMaster();
	
	public abstract int getReplicaCount();
	
	//Returns an Array of locations of the file
	public abstract NodeDescriptor[] getLocations();
}
