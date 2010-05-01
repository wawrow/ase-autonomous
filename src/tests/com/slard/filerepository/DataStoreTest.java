package tests.com.slard.filerepository;


import java.util.Arrays;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.slard.filerepository.DataObject;
import com.slard.filerepository.DataObjectImpl;
import com.slard.filerepository.DataStore;
import com.slard.filerepository.DataStoreImpl;

public class DataStoreTest {

  String TESTFILENAME = "testName";
  byte[] TESTDATA  = new byte[] { 0, 1, 1 };  
  DataStore dataStore;
  
  @Before
  public void setUp() throws Exception {
    dataStore = new DataStoreImpl();
    DataObject dataObject= new DataObjectImpl(TESTFILENAME, TESTDATA);
    this.dataStore.storeDataObject(dataObject);
  }

  @Test
  public void GetDataObject() throws Exception {
    Assert.assertNotNull(dataStore.getDataObject(TESTFILENAME));
  }
  
  @Test
  public void StoreFileContains() throws Exception{
    Assert.assertTrue(this.dataStore.contains(TESTFILENAME));
  }

  @Test
  public void StoreFileData() throws Exception{
    DataObject dao = this.dataStore.getDataObject(TESTFILENAME);
    Assert.assertTrue(Arrays.equals(dao.getData(), TESTDATA));
  }

  
  @After
  public void tearDown() throws Exception {
    
  }

}
