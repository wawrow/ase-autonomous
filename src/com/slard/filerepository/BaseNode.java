package com.slard.filerepository;

public abstract class BaseNode implements Node {

  public void start() {
    this.initializeDataStore();
    //this.joinTheNetwork();
    //Start the node
  }

}
