package com.slard.filerepository;

public interface NodeDescriptor {
	public abstract long GetId();
	public abstract com.sun.jndi.cosnaming.IiopUrl.Address GetAddress();	
}
