package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.View;

import java.util.Set;
import java.util.Vector;

public interface ConsistentHash  {
  class MemberDelta {
    public MemberDelta(Set<Address> added, Set<Address> removed) {
      this.added = added;
      this.removed = removed;
    }

    public Set<Address> removed;
    public Set<Address> added;
  }

  // We can user the number of ids to load-balance the new member.

  void insert(Address newMember);

  void insert(Address newMember, byte numberIDs);

  void remove(Address address);

  MemberDelta recalculate(View view);

  // Use findMaster to find the unique master ID for a string.

  Long findMaster(String name);
  // Find the previous ID (ie the replica candidate) for an ID.

  // Actually return the address of the node we care about.

  Address getAddress(Long id);

  long[] getIDs(Address member);

  Long findPreviousId(Long id);

  Vector<Address> findPreviousUniqueAddresses(Long startId, int depth);
  
  int getNodeCount();

  Address findMasterAddress(String name);
  
  Address findMasterAddress(String name, Address nodeThatIsNoLongerInCh);
  Vector<Address> findPreviousUniqueAddresses(Long startId, int depth, Address nodeThatIsNoLongerInCh);

  Long findMaster(String name, Address nodeThatIsNoLongerInCh);
}
