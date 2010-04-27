package com.slard.filerepository;

import com.slard.filerepository.Node;

public abstract class BaseNode implements Node {

	public void start(){
		this.initializeDataStore();
		this.joinTheNetwork();
	  //Start the node
	}
	
}
