package com.slard.filerepository;

import org.jgroups.Address;

public interface NodeDescriptor {
	public abstract long getId();
	public abstract Address getAddress();
}
