package tests.com.slard.filerepository;


import java.util.Arrays;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.slard.filerepository.DataObject;
import com.slard.filerepository.DataStore;
import com.slard.filerepository.DataStoreImpl;

public class DataStoreTest {

  String TESTFILENAME = "testName";
  byte[] TESTDATA  = new byte[] { 0, 1, 1 };  
  DataStore dataStore;
  DataObject mockedDataObject;
  
  @Before
  public void setUp() throws Exception {
    dataStore = new DataStoreImpl();
    
    //Set Up Mocks
    mockedDataObject = Mockito.mock(DataObject.class);
    Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
    Mockito.when(mockedDataObject.getName()).thenReturn(TESTFILENAME);
    this.dataStore.storeDataObject(mockedDataObject);    
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

  @Test
  public void VerifyMocks() throws Exception{
    Mockito.verify(mockedDataObject, Mockito.atLeast(1)).getName();
    Mockito.verify(mockedDataObject, Mockito.atLeast(1)).getData();
  }
  
  @After
  public void tearDown() throws Exception {
  }

}
