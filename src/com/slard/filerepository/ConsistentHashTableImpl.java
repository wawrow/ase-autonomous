package com.slard.filerepository;

import java.util.*;

/**
 * The Class ConsistentHashTable Implementation
 *
 * @param <T> the generic type
 */
public class ConsistentHashTableImpl<T> implements ConsistentHashTable<T> {

  /** The number of replicas. */
  private final int numberOfReplicas;
  
  /** The circle of all of the nodes. */
  private final SortedMap<Long, T> circle = new TreeMap<Long, T>();
  
  /** The hash provider implementation. */
  private HashProvider hashProvider;

  /**
   * Instantiates a new consistent hash table implementation.
   *
   * @param numberOfReplicas the number of replicas
   * @param nodes initial nodes null for none
   */
  public ConsistentHashTableImpl(int numberOfReplicas, Iterable<T> nodes) {
    this(numberOfReplicas, nodes, new MD5HashProvider());
  }
  
  /**
   * Instantiates a new consistent hash table implementation.
   *
   * @param numberOfReplicas the number of replicas
   * @param nodes initial nodes null for none
   * @param hashProvider the hash provider implementation
   */
  public ConsistentHashTableImpl(int numberOfReplicas, Iterable<T> nodes, HashProvider hashProvider) {
    this.numberOfReplicas = numberOfReplicas;
    this.hashProvider = hashProvider;
    if (nodes != null) {
      for (T node : nodes) {
        add(node);
      }
    }
  }

  /**
   * Gets the node id for given key.
   *
   * @param key the key
   * @return the hash
   */
  private long getHash(String key) {
    long hash = this.hashProvider.hash(key);
    if (!circle.containsKey(hash)) {
      SortedMap<Long, T> tailMap = circle.tailMap(hash);
      hash = tailMap.isEmpty() ? circle.firstKey() : tailMap.firstKey();
    }
    return hash;
  }

  /**
   * calculates a hash for given node and replica number.
   *
   * @param node the node
   * @param i the i
   * @return the long
   */
  private long hashForNode(T node, int i) {
    return this.hashProvider.hash(i + node.toString());
  }

  /** {@inheritDoc} */
  @Override
  public void add(T node) {
    for (int i = 0; i < numberOfReplicas; i++) {
      circle.put(hashForNode(node, i), node);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void remove(T node) {
    for (int i = 0; i < numberOfReplicas; i++) {
      circle.remove(hashForNode(node, i));
    }
  }

  /** {@inheritDoc} */
  @Override
  public T get(String key) {
    if (circle.isEmpty()) {
      return null;
    }
    return circle.get(getHash(key));
  }

  /** {@inheritDoc} */
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

  /** {@inheritDoc} */
  @Override
  public boolean contains(T node) {
    // Will check only first hash
    return circle.containsKey(hashForNode(node, 0));  // faster than scanning values
  }

  /** {@inheritDoc} */
  @Override
  public List<T> getAllValues() {
    return new ArrayList<T>(new HashSet<T>(circle.values()));  // quick dedup.
  }

}