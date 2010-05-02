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

  // We can user the number of replicas to load-balance the new member.
  void insertMember(Address newMember);
  void insertMember(Address newMember, int numberOfReplicas);

  void removeMember(Address address);

  MemberDelta recalculate(View view);

  // Use findMaster to find the unique master ID for a string.

  Long findMaster(String name);
  // Find the previous ID (ie the replica candidate) for an ID.

  // Actually return the address of the node we care about.

  Address getAddress(Long id);

  long[] calculateHashes(Address member);

  Long findPreviousId(Long id);

  Vector<Address> findPrevousUniqueAddresses(Long startId, int depth);
  
  int getNodeCount();

  Address findMasterAddress(String name);
  
}
