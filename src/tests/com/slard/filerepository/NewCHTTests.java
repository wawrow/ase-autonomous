package tests.com.slard.filerepository;

import java.io.File;
import java.util.List;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.slard.filerepository.DataStoreImpl;
import com.slard.filerepository.NewBetterCHT;
import com.slard.filerepository.NewBetterCHTImpl;

public class NewCHTTests {
  @Before
  public void setUp() throws Exception {
    
  }

  @Test
  public void testReplicasGetting() throws Exception {
    NewBetterCHT<String> ch = new NewBetterCHTImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    Assert.assertEquals(ch.get("test1"), "Node1");
    List<String> result = ch.getPreviousNodes("test1", 3);
    System.out.println(result.size());
    Assert.assertEquals(result.size(), 1);    
  }

  @Test
  public void testReplicasGetting1() throws Exception {
    NewBetterCHT<String> ch = new NewBetterCHTImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    ch.add("Node3");
    ch.add("Node4");
    ch.add("Node5");
    ch.add("Node6");
    Assert.assertEquals(ch.get("test1"), "Node5");
    List<String> result = ch.getPreviousNodes("test1", 3);
    Assert.assertEquals(result.size(), 3);    
  }

  @Test
  public void testReplicasGetting2() throws Exception {
    NewBetterCHT<String> ch = new NewBetterCHTImpl<String>(4, null);
    ch.add("Node1");
    ch.add("Node2");
    ch.add("Node3");
    Assert.assertEquals(ch.get("test1"), "Node1");
    List<String> result = ch.getPreviousNodes("test1", 3);
    Assert.assertEquals(result.size(), 2);    
  }

  
  @After
  public void tearDown() throws Exception {
    
  }
}
