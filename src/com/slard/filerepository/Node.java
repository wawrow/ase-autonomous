package com.slard.filerepository;

public interface Node {
	
	public abstract long[] GetIds();
	
	//Below will be used in Template Methods in abstract class for NodeImpl
	public abstract void Start();
	public abstract void InitializeDataStore();
	public abstract void JoinTheNetwork();
	
	//Method for keeping your replicas in order
	public abstract void RelpicaGuard();
	
	//Event fired every time node has left
	public abstract void NodeLeft(long[] nodeIds);
	
	//Event fired every time node has joined
	public abstract void NodeJoined(long[] nodeIds);

}
