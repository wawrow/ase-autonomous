package com.slard.filerepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashTableImpl<T> implements ConsistentHashTable<T> {

  private final int numberOfReplicas;
  private final SortedMap<Long, T> circle = new TreeMap<Long, T>();

  public ConsistentHashTableImpl(int numberOfReplicas, Collection<T> nodes) {

    this.numberOfReplicas = numberOfReplicas;

    if (nodes != null) {
      for (T node : nodes) {
        add(node);
      }
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.slard.filerepository.NewBetterCHT#add(T)
   */
  public void add(T node) {
    for (int i = 0; i < numberOfReplicas; i++) {
      circle.put(this.hash(node.toString() + i), node);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.slard.filerepository.NewBetterCHT#remove(T)
   */
  public void remove(T node) {
    for (int i = 0; i < numberOfReplicas; i++) {
      circle.remove(this.hash(node.toString() + i));
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.slard.filerepository.NewBetterCHT#get(java.lang.String)
   */
  public T get(String key) {
    if (circle.isEmpty()) {
      return null;
    }
    long hash = this.getHash(key);
    return circle.get(hash);
  }

  private long getHash(String key) {
    long hash = this.hash(key);
    if (!circle.containsKey(hash)) {
      SortedMap<Long, T> tailMap = circle.tailMap(hash);
      hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
    }
    return hash;
  }

  private synchronized Long hash(String key) {
    MessageDigest md5 = null;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ex) {

    }
    md5.reset();
    md5.update(key.getBytes());
    return this.bytesToLong(md5.digest());
  }

  private long bytesToLong(byte[] in) {
    long ret = 0;
    for (int i = 0; i < 8; i++) {
      ret <<= 8;
      ret ^= (long) in[i] & 0xFF;
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.slard.filerepository.NewBetterCHT#getPreviousNodes(java.lang.String,
   * int)
   */
  public List<T> getPreviousNodes(String key, int count) {
    List<T> result = new ArrayList<T>();
    long startingPoint = this.getHash(key);
    SortedMap<Long, T> headMap = circle.headMap(startingPoint - 1);
    long currHash = headMap.isEmpty() ? circle.lastKey() : headMap.lastKey();
    T startingNode = this.get(key);
    while (result.size() < count && currHash != startingPoint) {
      T curNode = circle.get(currHash);
      if (!curNode.equals(startingNode) && !result.contains(curNode)) {
        result.add(circle.get(currHash));
      }
      headMap = circle.headMap(currHash - 1);
      currHash = headMap.isEmpty() ? circle.lastKey() : headMap.lastKey();
    }
    return result;
  }

  public boolean contains(T node) {
    // Will check only first hash
    return circle.containsValue(node);
  }
  
  @Override
  public List<T> getAllValues(){
    ArrayList<T> result = new ArrayList<T>();
    for(T node:this.circle.values()){
      if(!result.contains(node)) result.add(node);
    }
    return result;
  }

}