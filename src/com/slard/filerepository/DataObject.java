package com.slard.filerepository;


public interface DataObject {

	public abstract long GetId();
	public abstract Byte[] GetData();
	public abstract String GetName();
	
	//Returns true if this object is master object, otherwise it's replica
	public abstract Boolean IsMaster();
	
	public abstract int GetReplicaCount();
	
	//Returns an Array of locations of the file
	public abstract NodeDescriptor[] GetLocations();
}
