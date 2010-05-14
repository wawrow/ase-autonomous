package com.slard.filerepository;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 10-May-2010
 * Time: 10:33:51
 * To change this template use File | Settings | File Templates.
 */
public interface DirectoryTrie {
  void add(String filename);

  DirectoryTrie getSubTree(String startDir);

  interface Accumulator<T> {
    void add(String name);

    void push(String child);

    void pop();

    T result();
  }

  <T> void postOrder(Accumulator<T> result, String prefix);

  //DirectoryTrie fromString(String toParse);

  String toString();
}
