package com.slard.filerepository;

import java.util.List;

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
   * @param key the key
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
  List<T> getAllValues();
  
}
