package com.slard.filerepository;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

public class NewCHTTests {
  @Before
  public void setUp() throws Exception {

  }

  @Test
  public void testReplicasGetting() throws Exception {
    ConsistentHashTable<String> ch = new ConsistentHashTableImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    Assert.assertEquals(ch.get("test1"), "Node1");
    List<String> result = ch.getPreviousNodes("test1", 3);
    System.out.println(result.size());
    Assert.assertEquals(result.size(), 1);
  }

  @Test
  public void testReplicasGetting1() throws Exception {
    ConsistentHashTable<String> ch = new ConsistentHashTableImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    ch.add("Node3");
    ch.add("Node4");
    ch.add("Node5");
    ch.add("Node6");
    Assert.assertEquals(ch.get("test1"), "Node3");
    List<String> result = ch.getPreviousNodes("test1", 3);
    Assert.assertEquals(result.size(), 3);
  }

  @Test
  public void testReplicasGetting2() throws Exception {
    ConsistentHashTable<String> ch = new ConsistentHashTableImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    ch.add("Node3");
    Assert.assertEquals(ch.get("test1"), "Node3");
    List<String> result = ch.getPreviousNodes("test1", 3);
    Assert.assertEquals(result.size(), 2);
  }

  @Test
  public void testGetValues() throws Exception {
    ConsistentHashTable<String> ch = new ConsistentHashTableImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    ch.add("Node3");
    ch.add("Node4");
    ch.add("Node5");
    ch.add("Node6");
    List<String> values = ch.getAllValues();
    Assert.assertEquals(values.size(), 6);
  }

  @After
  public void tearDown() throws Exception {

  }
}
