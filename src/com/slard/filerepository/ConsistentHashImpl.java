package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.View;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

public class ConsistentHashImpl implements ConsistentHash {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final int DEFFAULT_NUMBER_OF_REPLICAS = 4;

  private ReentrantReadWriteLock locks = new ReentrantReadWriteLock();
  private SortedMap<Long, Address> ring = new TreeMap<Long, Address>();
  private Map<Address, Integer> memberReplicaCounts = new HashMap<Address, Integer>();
  private MessageDigest md;

  public ConsistentHashImpl() {
    try {
      this.md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
    }
  }

  @Override
  public void insertMember(Address newMember) {
    insertMember(newMember, DEFFAULT_NUMBER_OF_REPLICAS);
  }

  @Override
  public void insertMember(Address newMember, int numberOfReplicas) {
    long[] hashes = calculateHashes(newMember, numberOfReplicas);
    try {
      locks.writeLock().lock();
      for (long hash : hashes) {
        ring.put(hash, newMember);
      }
      memberReplicaCounts.put(newMember, numberOfReplicas);
    } finally {
      locks.writeLock().unlock();
    }
  }

  @Override
  public void removeMember(Address address) {
    int numberOfReplicas = memberReplicaCounts.get(address);
    long[] hashes = calculateHashes(address, numberOfReplicas);
    try {
      locks.writeLock().lock();
      for (long hash : hashes) {
        ring.remove(hash);
      }
      memberReplicaCounts.remove(address);
    } finally {
      locks.writeLock().unlock();
    }
  }

  @Override
  public long[] calculateHashes(Address member) {
    return this.calculateHashes(member, DEFFAULT_NUMBER_OF_REPLICAS);
  }

  private long[] calculateHashes(Address member, int numberOfReplicas) {
    long[] hashes = new long[numberOfReplicas];
    byte[] prefix = new byte[1];
    for (int i = 0; i < numberOfReplicas; i++) {
      md.reset();
      prefix[0] = (byte) i;
      md.update(prefix); // md5 is strong and so this works well.
      md.update(member.toString().getBytes());
      hashes[i] = bytesToLong(md.digest());
    }
    return hashes;
  }

  private static long bytesToLong(byte[] in) {
    long ret = 0;
    for (int i = 0; i < 8; i++) {
      ret <<= 8;
      ret ^= (long) in[i] & 0xFF;
    }
    return ret;
  }

  @Override
  public MemberDelta recalculate(View view) {
    Set<Address> newMembers = new HashSet<Address>(view.getMembers());
    Set<Address> deadMembers = new HashSet<Address>();

    try {
      locks.readLock().lock();
      Set<Address> currentMembers = memberReplicaCounts.keySet();
      for (Address member : currentMembers) {
        if (!newMembers.contains(member)) {
          deadMembers.add(member);
        }
      } // deadMembers contains removed nodes.
      newMembers.removeAll(currentMembers); // now contains new nodes.
    } finally {
      locks.readLock().unlock();
    }

    for (Address member : newMembers) {
      insertMember(member);
    }
    for (Address member : deadMembers) {
      removeMember(member);
    }
    return new MemberDelta(newMembers, deadMembers);
  }

  @Override
  public Long findMaster(String name) {
    md.reset();
    long hash = bytesToLong(md.digest(name.getBytes()));
    if (!ring.containsKey(hash)) {
      return findPreviousId(hash);
    }
    return hash;
  }

  @Override
  public Long findPreviousId(Long id) {
    if (ring.isEmpty()) {
      return null;
    }
    try {
      locks.readLock().lock();
      SortedMap<Long, Address> headMap = ring.headMap(id);
      return headMap.isEmpty() ? ring.lastKey() : headMap.lastKey();
    } finally {
      locks.readLock().unlock();
    }
  }

  @Override
  public Address getAddress(Long id) {
    try {
      locks.readLock().lock();
      return ring.get(id);
    } finally {
      locks.readLock().unlock();
    }
  }

  @Override
  public Vector<Address> findPreviousUniqueAddresses(Long startId, int depth) {
    Vector<Address> result = new Vector<Address>();
    for (int i = 0; i < depth; i++) {
      Address prevAddress = this.findPrevousUniqueAddress(startId, (Vector<Address>)result.clone());
      if (prevAddress == null)
        break;
      result.add(prevAddress);
    }
    return result;
  }

  private Address findPrevousUniqueAddress(Long startId, Vector<Address> avoid) {
    if (avoid == null) {
      avoid = new Vector<Address>();
    }
    if (!avoid.contains(this.getAddress(startId))) {
      avoid.add(this.getAddress(startId));
    }

    Address result = null;
    // Special cases
    try {
      locks.readLock().lock();
      if (memberReplicaCounts.size() == 1 || memberReplicaCounts.size() == avoid.size())
        return null;

      SortedMap<Long, Address> headMap = ring.headMap(startId);
      while (result == null && headMap.size() > 0) {
        if (!avoid.contains(ring.get(headMap.lastKey())))
          result = ring.get(headMap.lastKey());        
        headMap = ring.headMap(startId);
      }
    } finally {
      locks.readLock().unlock();
    }
    return result;
  }

  @Override
  public int getNodeCount() {
    return memberReplicaCounts.size();
  }

  @Override
  public Address findMasterAddress(String name) {
    return this.getAddress(this.findMaster(name));
  }

  @Override
  public Address findMasterAddress(String name, Address nodeThatIsNoLongerInCh) {
    md.reset();
    long id = bytesToLong(md.digest(name.getBytes()));

    SortedMap<Long, Address> idToAddressCopy = getCopyOfIdToAddress(nodeThatIsNoLongerInCh);

    ArrayList<Long> ids = new ArrayList<Long>(ring.keySet());
    int i = Collections.binarySearch(ids, id);
    if (i < 0) { // not sure, how it won't be.
      i = -(i + 2);
      if (i < 0) {
        i = ids.size() - 1;
      }
    }
    Long masterId = ids.get(i);
    return idToAddressCopy.get(masterId);
  }

  private SortedMap<Long, Address> getCopyOfIdToAddress(Address nodeThatIsNoLongerInCh) {
    SortedMap<Long, Address> idToAddressCopy = new TreeMap<Long, Address>();
    try {
      locks.readLock().lock();
      for (Long idToCopy : ring.keySet()) {
        idToAddressCopy.put(idToCopy, ring.get(idToCopy));
      }
    } finally {
      locks.readLock().unlock();
    }

    for (Long leavingNodeId : this.calculateHashes(nodeThatIsNoLongerInCh)) {
      idToAddressCopy.put(leavingNodeId, nodeThatIsNoLongerInCh);
    }
    return idToAddressCopy;
  }

  @Override
  public Vector<Address> findPreviousUniqueAddresses(Long startId, int depth, Address nodeThatIsNoLongerInCh) {

    SortedMap<Long, Address> idToAddressCopy = getCopyOfIdToAddress(nodeThatIsNoLongerInCh);

    Vector<Address> result = new Vector<Address>();
    for (int i = 0; i < depth; i++) {
      Address prevAddress = this.findPrevousUniqueAddress(startId, result, idToAddressCopy);
      if (prevAddress == null)
        break;
      result.add(prevAddress);
    }
    return result;
  }

  private Address findPrevousUniqueAddress(Long startId, Vector<Address> avoid, SortedMap<Long, Address> copyIdToAddress) {
    if (avoid == null)
      avoid = new Vector<Address>();
    if (!avoid.contains(this.getAddress(startId))) {
      avoid.add(this.getAddress(startId));
    }

    Address result = null;
    SortedMap<Long, Address> headMap = copyIdToAddress.headMap(startId);
    while (result == null && headMap.size() > 0) {
      if (!avoid.contains(copyIdToAddress.get(headMap.lastKey())))
        result = copyIdToAddress.get(headMap.lastKey());
      headMap = copyIdToAddress.headMap(startId);
    }
    return result;
  }

  @Override
  public Long findMaster(String name, Address nodeThatIsNoLongerInCh) {
    md.reset();
    long id = bytesToLong(md.digest(name.getBytes())); // no need to seed.
    Long ret = null;

    SortedMap<Long, Address> idToAddressCopy = getCopyOfIdToAddress(nodeThatIsNoLongerInCh);

    ArrayList<Long> ids = new ArrayList<Long>(idToAddressCopy.keySet());

    int i = Collections.binarySearch(ids, id);
    if (i < 0) { // not sure, how it won't be.
      i = -(i + 2);
      if (i < 0) {
        i = ids.size() - 1;
      }
    }
    ret = ids.get(i);
    return ret;
  }
}
