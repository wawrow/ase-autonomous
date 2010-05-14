package com.slard.filerepository;

import java.io.File;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 10-May-2010
 * Time: 10:36:55
 * To change this template use File | Settings | File Templates.
 */
public class DirectoryTrieImpl implements DirectoryTrie {
  private Map<String, DirectoryTrieImpl> children = null;
  private DirectoryTrieImpl parent;

  public DirectoryTrieImpl(DirectoryTrieImpl parent) {
    this.parent = parent;
  }

  public static DirectoryTrie fromString(String path) {
    DirectoryTrieImpl ret = new DirectoryTrieImpl(null);
    StringTokenizer st = new StringTokenizer(path, "{}", true);
    DirectoryTrieImpl next = ret;
    while (st.hasMoreTokens()) {
      String tok = st.nextToken();
      if (tok.equals("}")) {
        if (next == null) {
          return null;  // malformed
        }
        next = next.parent;
      } else if (!tok.equals("{") || next == null || !st.hasMoreTokens()) {
        return null;   // malformed
      } else {
        if (next.children == null) {
          next.children = new HashMap<String, DirectoryTrieImpl>();
        }
        DirectoryTrieImpl child = new DirectoryTrieImpl(next);
        next.children.put(st.nextToken(), child);
        next = child;
      }
    }
    return ret;
  }

  @Override
  public void add(String content) {
    DirectoryTrieImpl next = this;
    StringTokenizer st = new StringTokenizer(content, "/");
    while (st.hasMoreTokens()) {
      String word = st.nextToken();
      if (next.children == null) {
        next.children = new HashMap<String, DirectoryTrieImpl>();
      }
      if (!next.children.containsKey(word)) {
        next.children.put(word, new DirectoryTrieImpl(this));
      }
      next = next.children.get(word);
    }
  }

  @Override
  public void postOrder(DirectoryTrie.Accumulator ret, String prefix) {
    if (children == null) {
      return;
    }
    prefix += File.separator;
    List<String> elems = new ArrayList<String>(children.keySet());
    Collections.sort(elems);
    for (String elem : elems) {
      ret.push(elem);
      DirectoryTrieImpl dt = children.get(elem);
      if (dt.children != null) {
        children.get(elem).postOrder(ret, prefix + elem);
      }
      ret.pop();
    }
  }

  @Override
  public DirectoryTrie getSubTree(String startDir) {
    DirectoryTrieImpl ret = this;
    for (String dir : startDir.split("/")) {
      if (dir.length() < 1) {
        continue;
      }
      if (!ret.children.containsKey(dir)) {
        ret = null;
        break;  // entry not in tree.
      }
      ret = ret.children.get(dir);
    }
    return ret;
  }

  public String toString() {
    class BuildPath implements Accumulator<String> {
      StringBuilder sbuf;

      BuildPath() {
        sbuf = new StringBuilder();
      }

      public void push(String elem) {
        sbuf.append('{').append(elem);
      }

      public void pop() {
        sbuf.append('}');
      }

      public void add(String elem) {
        if (sbuf == null) {
          sbuf = new StringBuilder(elem);
        } else {
          sbuf.append(":").append(elem);
        }
      }

      public String result() {
        return sbuf.toString();
      }
    }
    BuildPath store = new BuildPath();
    postOrder(store, "");
    return store.result();
  }

  public static void main(String[] args) {
    final String[] test = {"/home/build/static/projects/", "/home/build/static/projects2",
        "/home/build/static/projects3", "/home/local/static/projects", "/home/jim/static/projects",
        "/home/local/static/wibble", "/home/local/static/jim", "/home/build/static3/projects"};
    DirectoryTrie dt = new DirectoryTrieImpl(null);
    for (String path : test) {
      dt.add(path);
    }
    String res = dt.toString();
    System.out.println(res);
    int c1 = 0, c2 = 0;
    for (int i = 0; i < res.length(); i++) {
      if (res.charAt(i) == '{') {
        c1++;
      } else if (res.charAt(i) == '}') {
        c2++;
      }
    }
    System.out.println(c1 + " vs " + c2);
    System.out.println(dt.getSubTree("/home/build").toString());
    if (dt.getSubTree("/home/build/static/projects") == null) {
      System.out.println("didn't find a tree");
    }
    System.out.println(DirectoryTrieImpl.fromString(res).toString());
  }
}
