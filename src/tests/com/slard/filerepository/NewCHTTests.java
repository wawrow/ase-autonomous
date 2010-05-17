package com.slard.filerepository;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.Set;

public class NewCHTTests {

  HashProvider hash;
  ConsistentHashTable<String> ch;

  @Before
  public void setUp() throws Exception {
    hash = Mockito.mock(HashProvider.class);
    for (Long i = 0L; i < 300; i++) {
      Mockito.when(hash.hash(i.toString())).thenReturn(i);
    }
    Mockito.when(hash.hash("0file1")).thenReturn(0L);
    Mockito.when(hash.hash("0file2")).thenReturn(20L);
    Mockito.when(hash.hash("0file3")).thenReturn(40L);
    Mockito.when(hash.hash("1file1")).thenReturn(50L);
    Mockito.when(hash.hash("0file4")).thenReturn(60L);
    Mockito.when(hash.hash("1file2")).thenReturn(70L);
    Mockito.when(hash.hash("1file3")).thenReturn(90L);
    //93 down
    Mockito.when(hash.hash("2file1")).thenReturn(100L);

    Mockito.when(hash.hash("1file4")).thenReturn(110L);
    Mockito.when(hash.hash("2file2")).thenReturn(120L);
    Mockito.when(hash.hash("2file3")).thenReturn(140L);
    Mockito.when(hash.hash("3file1")).thenReturn(150L);
    Mockito.when(hash.hash("2file4")).thenReturn(160L);
    Mockito.when(hash.hash("3file2")).thenReturn(170L);
    Mockito.when(hash.hash("3file3")).thenReturn(190L);
    Mockito.when(hash.hash("3file4")).thenReturn(210L);

    this.ch = new ConsistentHashTableImpl<String>(4, null, this.hash);
    ch.add("file1");
    ch.add("file2");
    ch.add("file3");
    ch.add("file4");
  }

  @Test
  public void testLookupGivesExpectedResults() throws Exception {
    Assert.assertEquals(ch.get("2"), "file1");
    Assert.assertEquals(ch.get("82"), "file2");
    Assert.assertEquals(ch.get("90"), "file3");
    Assert.assertEquals(ch.get("220"), "file4");
    Assert.assertEquals(ch.get("93"), "file3");
    Assert.assertEquals(ch.get("194"), "file3");
    Assert.assertEquals(ch.get("175"), "file2");
  }

  @Test
  public void testWeCanGetSomeReplicaNodes() throws Exception {
    List<String> result = ch.getPreviousNodes("93", 2);
    Assert.assertEquals(2, result.size());
    Assert.assertEquals("file2", result.get(0));
    Assert.assertEquals("file4", result.get(1));
  }

  @Test
  public void testReplicaNodesRollAround() throws Exception {
    List<String> result = ch.getPreviousNodes("45", 3);
    Assert.assertEquals(3, result.size());
    Assert.assertEquals("file2", result.get(0));
    Assert.assertEquals("file1", result.get(1));
    Assert.assertEquals("file4", result.get(2));
  }

  @Test
  public void testCanGetReplicasOfLastNode() throws Exception {
    List<String> result = ch.getPreviousNodes("220", 1);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("file3", result.get(0));
  }

  @Test
  public void testCanGetReplicasOfFirstNode() throws Exception {
    List<String> result = ch.getPreviousNodes("10", 1);
    Assert.assertEquals(1, result.size());
    Assert.assertEquals("file4", result.get(0));
  }

  @Test
  public void testGetNumberOfPossibleValues() throws Exception {
    Set<String> values = ch.getAllValues();
    Assert.assertEquals(values.size(), 4);
  }

  @After
  public void tearDown() throws Exception {

  }
}
