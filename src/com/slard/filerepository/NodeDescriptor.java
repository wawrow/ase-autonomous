package com.slard.filerepository;

import org.jgroups.Address;

public interface NodeDescriptor extends SystemComs {
  Address getAddress();
  long[] getIds();
}
