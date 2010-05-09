package com.slard.filerepository;

import java.util.List;

public interface ConsistentHashTable<T> {
  void add(T node);
  void remove(T node);
  T get(String key);
  List<T> getPreviousNodes(String key, int count);
  boolean contains(T node);
  List<T> getAllValues();
  
}
