package com.slard.filerepository;

public interface Node {
	
	public abstract long[] getIds();

  // Set up the logger and some reused types.
  public abstract void initialise();

	//Below will be used in Template Methods in abstract class for NodeImpl
	public abstract void start() throws Exception;
	public abstract void initializeDataStore();

  //This method will loop to keep check the replica nodes and keep the replica count
	public abstract void replicaGuard();

}
