package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.View;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 30-Apr-2010
 * Time: 15:43:23
 * To change this template use File | Settings | File Templates.
 */
public interface CHT {
  class MemberDelta {
    public MemberDelta(Set<Address> added, Set<Address> removed) {
      this.added = added;
      this.removed = removed;
    }

    public Set<Address> removed;
    public Set<Address> added;
  }

  void insert(Address newMember);

  void remove(Address address);

  MemberDelta recalculate(View view);

  // Use findMaster to find the unique master ID for a string.

  Long findMaster(String name);
  // Find the previous ID (ie the replica candidate) for an ID.

  Long findPrevious(Long id);
  // Actually return the address of the node we care about.

  Address getAddress(Long id);

  long calculateId(byte[] data);
}
