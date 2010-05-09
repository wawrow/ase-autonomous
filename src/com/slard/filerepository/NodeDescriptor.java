package com.slard.filerepository;

import org.jgroups.Address;

/**
 * The Interface NodeDescriptor.
 */
public interface NodeDescriptor extends FileOperations, SystemFileList {
  
  /**
   * Gets the address of this node.
   *
   * @return the address
   */
  Address getAddress();
}
