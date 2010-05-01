package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.View;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA. User: kbrady Date: 28-Apr-2010 Time: 09:26:08 To
 * change this template use File | Settings | File Templates.
 */
public class CHTImpl implements CHT {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final byte DEFAULT_NODES = 4; // default number of hashes per
                                               // address.

  private ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
  private SortedMap<Long, Address> idToAddress = new TreeMap<Long, Address>();
  private Map<Address, long[]> addressToID = new HashMap<Address, long[]>();

  MessageDigest md;

  private static long bytesToLong(byte[] in) {
    long ret = in[0];
    for (int i = 1; i < in.length && i < 8; i++) {
      ret = (ret << 8) & in[i];
    }
    return ret;
  }

  public CHTImpl() {
    try {
      this.md = MessageDigest.getInstance("MD5"); // no need to be secure.
    } catch (NoSuchAlgorithmException e) {
    }
  }

  @Override
  public long[] getIDs(Address member) {
    return this.getIDs(member, DEFAULT_NODES);
  }

  private long[] getIDs(Address member, byte size) {
    long[] ret = new long[size];
    byte[] prefix = new byte[1];
    for (int i = 0; i < size; i++) {
      md.reset();
      prefix[0] = (byte) i;
      md.update(prefix); // md5 is strong and so this works well.
      md.update(member.toString().getBytes());
      ret[i] = bytesToLong(md.digest());
    }
    return ret;
  }

  @Override
  public void insert(Address newMember) {
    insert(newMember, DEFAULT_NODES);
  }

  @Override
  public void insert(Address newMember, byte size) {
    long[] ids = getIDs(newMember, size);
    try {
      locks.writeLock().lock();
      for (long id : ids) {
        idToAddress.put(id, newMember);
      }
      addressToID.put(newMember, ids);
    } finally {
      locks.writeLock().unlock();
    }
  }

  @Override
  public void remove(Address address) {
    try {
      locks.writeLock().lock();
      for (long ids : addressToID.get(address)) {
        idToAddress.remove(ids);
      }
      addressToID.remove(address);
    } finally {
      locks.writeLock().unlock();
    }
  }

  @Override
  public MemberDelta recalculate(View view) {
    Set<Address> newView = new HashSet<Address>(view.getMembers());
    Set<Address> deadMembers = new HashSet<Address>();

    try {
      locks.readLock().lock();

      Set<Address> entries = addressToID.keySet();
      for (Address member : entries) {
        if (!newView.contains(member)) {
          deadMembers.add(member);
        }
      } // deadMembers contains removed nodes.

      newView.removeAll(entries); // now contains new nodes.
    } finally {
      locks.readLock().unlock();
    }

    for (Address member : newView) {
      insert(member);
    }
    for (Address member : deadMembers) {
      remove(member);
    }
    return new MemberDelta(newView, deadMembers);
  }

  @Override
  public Long findMaster(String name) {
    md.reset();
    long id = bytesToLong(md.digest(name.getBytes())); // no need to seed.
    Long ret = null;

    try {
      locks.readLock().lock();
      ArrayList<Long> ids = new ArrayList<Long>(idToAddress.keySet());

      int i = Collections.binarySearch(ids, id);
      if (i < 0) { // not sure, how it won't be.
        i = -(i + 2);
        if (i < 0) {
          i = ids.size() - 1;
        }
      }
      ret = ids.get(i);
    } finally {
      locks.readLock().unlock();
    }
    return ret;
  }

  @Override
  public Long findPrevious(Long id) {
    Long ret = null;
    try {
      locks.readLock().lock();
      SortedMap<Long, Address> headMap = idToAddress.headMap(id);
      if (headMap == null) {
        ret = idToAddress.lastKey();
      } else {
        ret = headMap.lastKey();
      }
    } finally {
      locks.readLock().unlock();
    }
    return ret;
  }

  @Override
  public Address getAddress(Long id) {
    Address ret = null;
    try {
      locks.readLock().lock();
      ret = idToAddress.get(id);
    } finally {
      locks.readLock().unlock();
    }
    return ret;
  }
}
