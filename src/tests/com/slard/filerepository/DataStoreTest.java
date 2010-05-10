package com.slard.filerepository;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.Arrays;
import java.util.Properties;

public class DataStoreTest {

  private final byte[] TESTDATA = new byte[]{0, 1, 1};
  private final String TESTSTOREDIR = "teststore";
  private String dataStoreLocation;
  private DataStore dataStore;

  @Before
  public void setUp() throws Exception {
    String currentDirectory = System.getProperty("user.dir");
    Properties options = new Properties();
    dataStoreLocation = currentDirectory + File.pathSeparator + TESTSTOREDIR;
    options.put("datastore.dir", dataStoreLocation);
    dataStore = new DataStoreImpl(options);
    dataStore.initialise();
  }

  @Test
  public void testGetStoreLocation() throws Exception {
    Assert.assertEquals(this.dataStoreLocation, this.dataStore.getStoreLocation());
  }

  @Test
  public void testStoreDataObject() throws Exception {
    String name = new String("testStore");
    DataObject mockedDataObject = Mockito.mock(DataObject.class);
    Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
    Mockito.when(mockedDataObject.getName()).thenReturn(name);

    try {
      this.dataStore.store(mockedDataObject);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    try {
      Assert.assertFalse("Erroneously succeeded in adding the same file twice", dataStore.store(mockedDataObject));
    } catch (Exception e) {
    }
  }

  @Test
  public void testReplaceDataObject() throws Exception {
    String name = new String("testReplace");
    DataObject mockedDataObject = Mockito.mock(DataObject.class);
    Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
    Mockito.when(mockedDataObject.getName()).thenReturn(name);
    Mockito.when(mockedDataObject.getCRC()).thenReturn(123L);

    this.dataStore.store(mockedDataObject);
    Assert.assertTrue(this.dataStore.replace(mockedDataObject));
    //Assert.fail(e.getMessage());
  }

  @Test
  public void testGetDataObject() throws Exception {
    try {
      String name = new String("testGet");
      DataObject mockedDataObject = Mockito.mock(DataObject.class);
      Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
      Mockito.when(mockedDataObject.getName()).thenReturn(name);

      this.dataStore.store(mockedDataObject);
      DataObject dataObject = this.dataStore.retrieve(name);
      Assert.assertNotNull(dataObject);
      Assert.assertTrue(Arrays.equals(dataObject.getData(), TESTDATA));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testDeleteDataObject() throws Exception {
    try {
      String name = new String("testDelete");
      DataObject mockedDataObject = Mockito.mock(DataObject.class);
      Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
      Mockito.when(mockedDataObject.getName()).thenReturn(name);

      this.dataStore.store(mockedDataObject);
      this.dataStore.delete(name);
      Assert.assertFalse(this.dataStore.hasFile(name));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testgetAllDataObjects() throws Exception {
    String name = new String("testGetAll");
    DataObject mockedDataObject = Mockito.mock(DataObject.class);
    Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
    Mockito.when(mockedDataObject.getName()).thenReturn(name);

    try {
      this.dataStore.store(mockedDataObject);
      Assert.assertFalse(this.dataStore.getAllDataObjects().isEmpty());
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testContains() throws Exception {
    String name = new String("testContains");
    DataObject mockedDataObject = Mockito.mock(DataObject.class);
    Mockito.when(mockedDataObject.getData()).thenReturn(TESTDATA);
    Mockito.when(mockedDataObject.getName()).thenReturn(name);

    try {
      this.dataStore.store(mockedDataObject);
      Assert.assertTrue(this.dataStore.hasFile(name));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @After
  public void tearDown() throws Exception {
    File directory = new File(this.dataStoreLocation);
    File[] files = directory.listFiles();
    for (File file : files) {
      file.delete();
    }
    directory.delete();
  }
}
