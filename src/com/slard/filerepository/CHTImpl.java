package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.View;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 28-Apr-2010
 * Time: 09:26:08
 * To change this template use File | Settings | File Templates.
 */
public class CHTImpl implements CHT {
  private final Logger logger = Logger.getLogger(this.getClass().getName());

  private ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
  private SortedMap<byte[], Address> idToAddress = new TreeMap<byte[], Address>();
  private Map<Address, byte[][]> addressToID = new HashMap<Address, byte[][]>();

  MessageDigest md;
  private static final byte[][] PREFIXES = {"one".getBytes(), "two".getBytes(), "three".getBytes(), "four".getBytes()};

  public CHTImpl() {
    try {
      this.md = MessageDigest.getInstance("MD5");  // no need to be secure.
    } catch (NoSuchAlgorithmException e) {
    }
  }

  private byte[][] getIDs(Address member) {
    byte[][] ret = new byte[PREFIXES.length][];
    for (int i = 0; i < PREFIXES.length; i++) {
      md.reset();
      md.update(PREFIXES[i]);  // md5 is strong and so this works well.
      md.update(Address.UUID_ADDR);
      ret[i] = md.digest();
    }
    return ret;
  }

  @Override
  public void insert(Address newMember) {
    try {
      locks.writeLock().lock();

      for (byte[] id : getIDs(newMember)) {
        idToAddress.put(id, newMember);
      }
    } finally {
      locks.writeLock().unlock();
    }
  }

  @Override
  public void remove(Address address) {
    try {
      locks.writeLock().lock();
      idToAddress.remove(addressToID.remove(address));
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
      }  // deadMembers contains removed nodes.

      newView.removeAll(entries);  // now contains new nodes.
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
  public byte[] findMaster(String name) {
    md.reset();
    byte[] id = md.digest(name.getBytes());  // no need to seed.
    byte[] ret = null;

    try {
      locks.readLock().lock();
      ArrayList<byte[]> ids = new ArrayList<byte[]>(idToAddress.keySet());

      class ByteCompare implements Comparator<byte[]> {
        public int compare(byte[] a, byte[] b) {
          int ret = 0;
          for (int i = 0; i < a.length && i < b.length; i++) {
            ret = b[i] - a[i];
            if (ret < 0) {
              break;
            }
          }
          if (ret >= 0) {
            ret = b.length - a.length;
          }
          return ret;
        }
      }

      int i = Collections.binarySearch(ids, id, new ByteCompare());
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
  public byte[] findPrevious(byte[] id) {
    byte[] ret = null;
    try {
      locks.readLock().lock();
      ret = idToAddress.headMap(id).lastKey();
    } finally {
      locks.readLock().unlock();
    }
    return ret;
  }

  @Override
  public Address getAddress(byte[] id) {
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