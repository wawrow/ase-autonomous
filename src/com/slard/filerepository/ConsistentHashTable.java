package com.slard.filerepository;

import java.util.List;
import java.util.Set;

/**
 * The Interface ConsistentHashTable.
 *
 * @param <T> the generic type of the elements in the HashTable
 */
public interface ConsistentHashTable<T> {

  /**
   * Adds the Node.
   *
   * @param node the node
   */
  void add(T node);

  /**
   * Removes the Node.
   *
   * @param node the node
   */
  void remove(T node);

  /**
   * Gets the node for given key.
   *
   * @param key the key
   * @return the t
   */
  T get(String key);

  /**
   * Gets the previous nodes to the node that owns the key.
   *
   * @param key   the key
   * @param count the count
   * @return the previous nodes
   */
  List<T> getPreviousNodes(String key, int count);

  /**
   * Check whether node is in the HashTable
   *
   * @param node the node
   * @return true, if successful
   */
  boolean contains(T node);

  /**
   * Gets the all nodes that are in HashTable
   *
   * @return the all values
   */
  Set<T> getAllValues();

  interface Changes<T> {
    Set<T> getRemoved();

    Set<T> getAdded();
  }

  Changes<T> update(Set<T> currentMembers);

  /**
   * Created by IntelliJ IDEA.
   * User: kbrady
   * Date: 21-May-2010
   * Time: 14:05:30
   * To change this template use File | Settings | File Templates.
   */
  interface ConsistentHashTableFactory<T> {
    ConsistentHashTable<T> create(int numReplicas);
  }
}
