package com.slard.filerepository;

import org.jgroups.Address;

public interface NodeDescriptor extends FileOperations {
  Address getAddress();
}
