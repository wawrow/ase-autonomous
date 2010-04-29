package com.slard.filerepository;

import java.util.Enumeration;
import java.util.Vector;

public interface CHTHelper {

	//Will return an array of 4 Ids of the node based on some unique identifier (MAC+PATH or IP+PATH)
	//Might require providing parameter - if required just add it
	public abstract long[] GetIds();
	
	//Returns objects that I'm owner of
	public Vector<DataObject> getOwnedObjects(Long[] allNodes, long[] myIds, Vector<DataObject> myObjects);
}
