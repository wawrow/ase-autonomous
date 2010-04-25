package com.slard.filerepository;

public interface CHTHelper {

	//Will return an array of 4 Ids of the node based on some unique identifier (MAC+PATH or IP+PATH)
	//Might require providing parameter - if required just add it
	public abstract long[] GetIds();
}
