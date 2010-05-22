package com.slard.filerepository;

import junit.framework.Assert;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Set;

public class CHTTests {

  HashProvider hash;
  ConsistentHashTable<String> ch;

  @Before
  public void setUp() throws Exception {
    int repl_count = 4;
    hash = EasyMock.createMock(HashProvider.class);
    for (Long i = 0L; i < 400; i++) {
      EasyMock.expect(hash.hash(i.toString())).andReturn(i).anyTimes();
    }
    for (int i = 0; i < repl_count; i++) {
      for (int j = 1; j <= 4; j++) {
        EasyMock.expect(hash.hash(i + "node" + j)).andReturn((i * 10 + j) * 10L).anyTimes();
      }
    }
    EasyMock.replay(hash);
    this.ch = new ConsistentHashTableImpl<String>(this.hash, repl_count);
    ch.add("node1");
    ch.add("node2");
    ch.add("node3");
    ch.add("node4");
  }

  @Test
  public void testLookupGivesExpectedResults() throws Exception {
    Assert.assertEquals("node4", ch.get("2"));
    Assert.assertEquals("node4", ch.get("82"));
    Assert.assertEquals("node1", ch.get("110"));
    Assert.assertEquals("node2", ch.get("220"));
    Assert.assertEquals("node1", ch.get("310"));
    Assert.assertEquals("node4", ch.get("350"));
    Assert.assertEquals("node4", ch.get("40"));
  }

  @Test
  public void testWeCanGetSomeReplicaNodes() throws Exception {
    List<String> result = ch.getPreviousNodes("93", 2);
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("node3", result.get(0));
    Assert.assertEquals("node2", result.get(1));
  }

  @Test
  public void testReplicaNodesRollAround() throws Exception {
    List<String> result = ch.getPreviousNodes("25", 3);
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("node1", result.get(0));
    Assert.assertEquals("node4", result.get(1));
    Assert.assertEquals("node3", result.get(2));
  }

  @Test
  public void testCanGetReplicasOfLastNode() throws Exception {
    List<String> result = ch.getPreviousNodes("320", 1);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("node1", result.get(0));
  }

  @Test
  public void testCanGetReplicasOfFirstNode() throws Exception {
    List<String> result = ch.getPreviousNodes("10", 1);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("node4", result.get(0));
  }

  @Test
  public void testGetNumberOfPossibleValues() throws Exception {
    Set<String> values = ch.getAllValues();
    Assert.assertEquals(4, values.size());
  }
}
