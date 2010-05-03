package com.slard.filerepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class ConsistentHashTableImpl<T> implements ConsistentHashTable<T> {

  private final int numberOfReplicas;
  private final SortedMap<Long, T> circle = new TreeMap<Long, T>();
  private MessageDigest md5;

  public ConsistentHashTableImpl(int numberOfReplicas, Iterable<T> nodes) {
    this.numberOfReplicas = numberOfReplicas;
    try {
      this.md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ex) {
    }
    if (nodes != null) {
      for (T node : nodes) {
        add(node);
      }
    }
  }

  private long bytesToLong(byte[] in) {
    long ret = 0;
    for (int i = 0; i < 8; i++) {
      ret <<= 8;
      ret ^= (long) in[i] & 0xFF;
    }
    return ret;
  }

  private Long hash(String key) {
    byte[] ret;
    synchronized (md5) {
      md5.reset();
      md5.update(key.getBytes());
      ret = md5.digest();
    }
    return bytesToLong(ret);
  }

  private long getHash(String key) {
    long hash = hash(key);
    if (!circle.containsKey(hash)) {
      SortedMap<Long, T> tailMap = circle.tailMap(hash);
      hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
    }
    return hash;
  }

  private long hashForNode(T node, int i) {
    return hash(i + node.toString());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.slard.filerepository.NewBetterCHT#add(T)
   */
  @Override
  public void add(T node) {
    for (int i = 0; i < numberOfReplicas; i++) {
      circle.put(hashForNode(node, i), node);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.slard.filerepository.NewBetterCHT#remove(T)
   */
  @Override
  public void remove(T node) {
    for (int i = 0; i < numberOfReplicas; i++) {
      circle.remove(hashForNode(node, i));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.slard.filerepository.NewBetterCHT#get(java.lang.String)
   */
  @Override
  public T get(String key) {
    if (circle.isEmpty()) {
      return null;
    }
    return circle.get(getHash(key));
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.slard.filerepository.NewBetterCHT#getPreviousNodes(java.lang.String,
   * int)
   */
  @Override
  public List<T> getPreviousNodes(String key, int count) {
    List<T> result = new ArrayList<T>();
    long startingPoint = getHash(key);
    SortedMap<Long, T> headMap = circle.headMap(startingPoint);
    long currHash = headMap.isEmpty() ? circle.lastKey() : headMap.lastKey();
    T startingNode = get(key);
    while (result.size() < count && currHash != startingPoint) {
      T curNode = circle.get(currHash);
      if (!curNode.equals(startingNode) && !result.contains(curNode)) {  // count small so ok.
        result.add(circle.get(currHash));
      }
      headMap = circle.headMap(currHash);
      currHash = headMap.isEmpty() ? circle.lastKey() : headMap.lastKey();
    }
    return result;
  }

  @Override
  public boolean contains(T node) {
    // Will check only first hash
    return circle.containsKey(hashForNode(node, 0));  // faster than scanning values
  }

  @Override
  public List<T> getAllValues() {
    return new ArrayList<T>(new HashSet<T>(circle.values()));  // quick dedup.
  }

}