package tests.com.slard.filerepository;

import java.util.Vector;

import junit.framework.Assert;

import org.junit.Test;
import org.mockito.Mockito;

import com.slard.filerepository.DataStore;
import com.slard.filerepository.FileListDataObjectImpl;

public class SystemFileListTest extends FileListDataObjectImpl {
  
  public SystemFileListTest(){    
    super(Mockito.mock(DataStore.class));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void serializeDeserializeTest() throws Exception {    
    Vector<String> test = new Vector<String>();
    test.add("test1");
    test.add("test2");
    test.add("test3");
    Vector<String> test1 = (Vector<String>)deserialize(serialize(test));
    Assert.assertEquals(test, test1);
    Assert.assertEquals(test.get(0), test1.get(0));
    Assert.assertEquals(test.get(1), test1.get(1));
    Assert.assertEquals(test.get(2), test1.get(2));
  }  
}
