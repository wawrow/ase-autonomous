package tests.com.slard.filerepository;

import java.util.Arrays;
import java.util.Properties;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.slard.filerepository.CHT;
import com.slard.filerepository.DataObject;
import com.slard.filerepository.DataStore;
import com.slard.filerepository.DataStoreImpl;

public class DataStoreTest {

  String TESTFILENAME = "testName";
  byte[] TESTDATA = new byte[] { 0, 1, 1 };
  String TESTSTOREDIR = "teststore";
  DataStore dataStore;
  DataObject mockedDataObject;
  CHT mockedCHT;

  @Before
  public void setUp() throws Exception {
    // Set Up Mocks - possibly will be refactored to common place for other
    // tests
    mockedDataObject = Mockito.mock(DataObject.class);
    Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
    Mockito.when(mockedDataObject.getName()).thenReturn(TESTFILENAME);

    mockedCHT = Mockito.mock(CHT.class);

    String curDir = System.getProperty("user.dir");
    Properties options = new Properties();
    options.put("datastore.dir", curDir + TESTSTOREDIR);
    // Prepare test objects
    dataStore = new DataStoreImpl(options);
    this.dataStore.storeDataObject(mockedDataObject);
  }

  @Test
  public void GetDataObject() throws Exception {
    Assert.assertNotNull(dataStore.getDataObject(TESTFILENAME));
  }

  @Test
  public void StoreFileContains() throws Exception {
    Assert.assertTrue(this.dataStore.contains(TESTFILENAME));
  }

  @Test
  public void StoreFileData() throws Exception {
    DataObject dao = this.dataStore.getDataObject(TESTFILENAME);
    Assert.assertTrue(Arrays.equals(dao.getData(), TESTDATA));
  }

  @Test
  public void VerifyMocks() throws Exception {
    Mockito.verify(mockedDataObject, Mockito.atLeast(1)).getName();
    Mockito.verify(mockedDataObject, Mockito.atLeast(1)).getData();
  }

  @After
  public void tearDown() throws Exception {
  }

}
