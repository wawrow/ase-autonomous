package com.slard.filerepository;

import java.util.List;

public interface NewBetterCHT<T> {

  public abstract void add(T node);

  public abstract void remove(T node);

  public abstract T get(String key);

  public abstract List<T> getPreviousNodes(String key, int count);

  List<T> getAllValues();

}