package com.slard.filerepository;

import java.util.*;

public class ConsistentHashTableImpl<T> implements ConsistentHashTable<T> {

  private final int numberOfReplicas;
  private final SortedMap<Long, T> circle = new TreeMap<Long, T>();
  private HashProvider hashProvider;

  public ConsistentHashTableImpl(int numberOfReplicas, Iterable<T> nodes) {
    this(numberOfReplicas, nodes, new MD5HashProvider());
  }
  
  public ConsistentHashTableImpl(int numberOfReplicas, Iterable<T> nodes, HashProvider hashProvider) {
    this.numberOfReplicas = numberOfReplicas;
    this.hashProvider = hashProvider;
    if (nodes != null) {
      for (T node : nodes) {
        add(node);
      }
    }
  }

  private long getHash(String key) {
    long hash = this.hashProvider.hash(key);
    if (!circle.containsKey(hash)) {
      SortedMap<Long, T> tailMap = circle.tailMap(hash);
      hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
    }
    return hash;
  }

  private long hashForNode(T node, int i) {
    return this.hashProvider.hash(i + node.toString());
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
    SortedMap<Long, T> tailMap = circle.tailMap(startingPoint + 1);
    long currHash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
    T startingNode = get(key);
    while (result.size() < count && currHash != startingPoint) {
      T curNode = circle.get(currHash);
      if (!curNode.equals(startingNode) && !result.contains(curNode)) {  // count small so ok.
        result.add(circle.get(currHash));
      }
      tailMap = circle.tailMap(currHash + 1);
      currHash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
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