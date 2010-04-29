package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.View;

public interface CHT {
  //Will return an array of 4 Ids of the node based on some unique identifier (MAC+PATH or IP+PATH)
  //Might require providing parameter - if required just add it
  long[] GetIds();

  // use view to completely recalculate the CHT.
  void recalculate(View view);

  // Remove the node with address from the CHT.
  void leave(Address address);

  // Give me the node responsible for this DataObject.
  NodeDescriptor master(DataObject file);

  // Give me the node before this one.
  NodeDescriptor previous(NodeDescriptor node);
}
