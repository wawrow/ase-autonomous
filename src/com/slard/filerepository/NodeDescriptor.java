package com.slard.filerepository;

import org.jgroups.Address;

public interface NodeDescriptor extends SystemComsServer {
  Address getAddress();
  long[] getIds();
}
