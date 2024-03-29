package com.slard.filerepository;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;

import java.util.*;

/**
 * The Class ConsistentHashTable Implementation
 *
 * @param <T> the generic type
 */
public class ConsistentHashTableImpl<T> implements ConsistentHashTable<T> {
  @InjectLogger
  Logger logger;
  /**
   * The number of replicas.
   */
  private final int numberOfReplicas;

  /**
   * The circle of all of the nodes.
   */
  private final SortedMap<Long, T> circle = Collections.synchronizedSortedMap(new TreeMap<Long, T>());
  private Map<T, Long[]> members = Collections.synchronizedMap(new HashMap<T, Long[]>());

  /**
   * The hash provider implementation.
   */
  private HashProvider hashProvider;

  /**
   * Instantiates a new consistent hash table implementation.
   *
   * @param numberOfReplicas the number of replicas
   * @param hashProvider     the hash provider implementation
   */
  @Inject
  public ConsistentHashTableImpl(HashProvider hashProvider, @Assisted int numberOfReplicas) {
    this.numberOfReplicas = numberOfReplicas;
    this.hashProvider = hashProvider;
  }

  /**
   * Gets the node id for given key.
   *
   * @param key the key
   * @return the hash
   */
  private long getHash(String key) {
    long hash = hashProvider.hash(key);
    if (!circle.containsKey(hash)) {
      SortedMap<Long, T> headMap = circle.headMap(hash);
      hash = headMap.isEmpty() ? circle.lastKey() : headMap.lastKey();
    }
    return hash;
  }

  /**
   * calculates a hash for given node and replica number.
   *
   * @param node the node
   * @param i    the i
   * @return the long
   */
  private long hashForNode(T node, int i) {
    return hashProvider.hash(i + node.toString());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void add(T node) {
    Long[] hashes = new Long[numberOfReplicas];
    for (int i = 0; i < hashes.length; i++) {
      hashes[i] = hashForNode(node, i);
      circle.put(hashes[i], node);
      members.put(node, hashes);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void remove(T node) {
    Long[] hashes = members.remove(node);
    if (hashes == null) {
      return;
    }
    for (int i = 0; i < hashes.length; i++) {
      circle.remove(hashes[i]);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public T get(String key) {
    if (circle.isEmpty()) {
      logger.warn("cht circle is empty");
      return null;
    }
    return circle.get(getHash(key));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public List<T> getPreviousNodes(String key, int count) {
    List<T> result = new LinkedList<T>();
    long startingPoint = getHash(key);
    T startingNode = circle.get(startingPoint);
    SortedMap<Long, T> headMap = circle.headMap(startingPoint);
    Long currHash;
    do {
      currHash = headMap.isEmpty() ? circle.lastKey() : headMap.lastKey();
      T curNode = circle.get(currHash);
      if (!curNode.equals(startingNode) && !result.contains(curNode)) {  // count small so ok.
        result.add(curNode);
      }
      headMap = circle.headMap(currHash);
    } while (result.size() < count && currHash != startingPoint);
    return result;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean contains(T node) {
    // Will check only first hash
    return members.containsKey(node);  // faster than scanning values
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<T> getAllValues() {
    return members.keySet();
  }

  @Override
  public Changes<T> update(Set<T> currentMembers) {
    final Set<T> added = new HashSet<T>(currentMembers);
    final Set<T> removed = new HashSet<T>(members.keySet());

    removed.removeAll(currentMembers);
    added.removeAll(members.keySet());

    return new Changes<T>() {
      public Set<T> getAdded() {
        return added;
      }

      public Set<T> getRemoved() {
        return removed;
      }
    };
  }

}