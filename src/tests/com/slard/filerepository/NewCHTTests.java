package tests.com.slard.filerepository;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.slard.filerepository.ConsistentHashTable;
import com.slard.filerepository.ConsistentHashTableImpl;
import com.slard.filerepository.HashProvider;

import java.util.List;

public class NewCHTTests {
  
  HashProvider hash;
  ConsistentHashTable<String> ch;
  
  @Before
  public void setUp() throws Exception {
    hash = Mockito.mock(HashProvider.class);
    for(Long i = 0L; i < 300 ; i++){
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
  public void testForfNodeGet() throws Exception {
    Assert.assertEquals(ch.get("2"),"file2");
    Assert.assertEquals(ch.get("82"),"file3");
    Assert.assertEquals(ch.get("90"),"file3");
    Assert.assertEquals(ch.get("220"),"file1");
    Assert.assertEquals(ch.get("93"),"file1");
    Assert.assertEquals(ch.get("194"),"file4");
    Assert.assertEquals(ch.get("175"),"file3");
  }

  @Test
  public void testReplicasGetting() throws Exception {
    List<String> result = ch.getPreviousNodes("93", 2);
    Assert.assertEquals(result.size(), 2);
    Assert.assertEquals("file4", result.get(0));
    Assert.assertEquals("file2", result.get(1));
  }

  @Test
  public void testReplicasGetting2() throws Exception {
    List<String> result = ch.getPreviousNodes("175", 10);
    Assert.assertEquals(result.size(), 3);
    Assert.assertEquals("file4", result.get(0));
    Assert.assertEquals("file1", result.get(1));
    Assert.assertEquals("file2", result.get(2));
  }

  @Test
  public void testGetValues() throws Exception {
    List<String> values = ch.getAllValues();
    Assert.assertEquals(values.size(), 4);
  }

  @After
  public void tearDown() throws Exception {

  }
}
