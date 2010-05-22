package com.slard.filerepository;

import com.google.inject.Provider;
import org.easymock.EasyMock;
import org.jgroups.blocks.Cache;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

public class DataStoreTest {

  private final byte[] TESTDATA = new byte[]{0, 1, 1};
  private final String TESTSTOREDIR = "teststore";
  private DataStore dataStore;

  FileSystemHelper.FileSystemHelperFactory mockFsh;
  DataObject.DataObjectFactory mockDof;
  FSDataObject.FSDataObjectFactory mockFsd;
  Provider<Cache<String, FSDataObject>> cacheProv;
  DataObject data;
  FileSystemHelper fsh;

  Logger logger = LoggerFactory.getLogger("tests");

  Properties options;

  @Before
  public void setUp() throws Exception {
    options = new Properties();
    String dataStoreLocation = TESTSTOREDIR;
    options.put("datastore.dir", dataStoreLocation);

    fsh = EasyMock.createMock(FileSystemHelper.class);

    fsh.mkdirs();
    EasyMock.expectLastCall().once();

    EasyMock.expect(fsh.readFile(EasyMock.anyObject(String.class))).andReturn(TESTDATA).anyTimes();

    fsh.writeFile("a_file", TESTDATA);
    EasyMock.expectLastCall().anyTimes();
    fsh.writeFile("banana", TESTDATA);
    EasyMock.expectLastCall().anyTimes();

    EasyMock.expect(fsh.canRead("a_file")).andReturn(true);
    EasyMock.expect(fsh.canRead("banana")).andReturn(true);

    List<String> allfiles = Arrays.asList("a_file", "banana");
    EasyMock.expect(fsh.list()).andReturn(allfiles).anyTimes();

    EasyMock.expect(fsh.delete(EasyMock.anyObject(String.class))).andReturn(true).anyTimes();
    EasyMock.expect(fsh.rename("a_file", "a_file.tmp")).andReturn(true).anyTimes();

    mockFsh = EasyMock.createMock(FileSystemHelper.FileSystemHelperFactory.class);
    EasyMock.expect(mockFsh.create(new File("teststore"))).andReturn(fsh).once();
    EasyMock.replay(mockFsh);

    data = new DataObjectImpl("a_file", TESTDATA);
    DataObject data2 = new DataObjectImpl("banana", TESTDATA);

    mockDof = EasyMock.createMock(DataObject.DataObjectFactory.class);
    EasyMock.expect(mockDof.create("a_file", TESTDATA)).andReturn(data).anyTimes();
    EasyMock.expect(mockDof.create("banana", TESTDATA)).andReturn(data2).anyTimes();
    EasyMock.replay(mockDof);

    //FSDataObject fsdata = new FSDataObjectImpl(data, fsh);
    //FSDataObject fsdata2 = new FSDataObjectImpl(data2, fsh);

    mockFsd = new FSDataObject.FSDataObjectFactory() {
      public FSDataObject create(DataObject datum, FileSystemHelper fs) {
        return new FSDataObjectImpl(datum, fs);
      }
    };

    //mockFsd = EasyMock.createMock(FSDataObject.FSDataObjectFactory.class);
    //EasyMock.expect(mockFsd.create(data, fsh)).andReturn(fsdata);
    //EasyMock.expect(mockFsd.create(data2,fsh)).andReturn(fsdata2);
    //EasyMock.replay(mockFsd);

    final Cache<String, FSDataObject> cache = new Cache<String, FSDataObject>();
    cacheProv = new Provider<Cache<String, FSDataObject>>() {
      public Cache<String, FSDataObject> get() {
        return cache;
      }
    };

    dataStore = new DataStoreImpl(mockFsh, mockDof, mockFsd, cacheProv);
    dataStore.setLogger(logger);
  }


  @Test
  public void testCanOnlyStoreDataObjectOnce() throws Exception {
    EasyMock.expect(fsh.exists("a_file")).andReturn(false);
    EasyMock.expect(fsh.exists("a_file")).andReturn(true);
    EasyMock.replay(fsh);
    dataStore.initialise(options);
    try {
      dataStore.store(data);
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }

    try {
      Assert.assertFalse("Erroneously succeeded in adding the same file twice",
          dataStore.store(data));
    } catch (Exception e) {
    }
  }

  @Test
  public void testReplaceDataObjectWorks() throws Exception {
    EasyMock.expect(fsh.exists("a_file")).andReturn(false).once();
    EasyMock.expect(fsh.exists("a_file")).andReturn(true).once();
    EasyMock.expect(fsh.exists("a_file")).andReturn(false);
    EasyMock.replay(fsh);
    dataStore.initialise(options);

    DataObject data2 = new DataObjectImpl("a_file", TESTDATA);

    dataStore.store(data);
    Assert.assertTrue(dataStore.replace(data2));
  }

  @Test
  public void testRetrieveSameDataObjectWeStore() throws Exception {
    EasyMock.expect(fsh.exists("a_file")).andReturn(false).anyTimes();
    EasyMock.replay(fsh);
    dataStore.initialise(options);
    try {
      dataStore.store(data);
      DataObject dataObject = dataStore.retrieve("a_file");
      Assert.assertNotNull(dataObject);
      Assert.assertTrue(Arrays.equals(dataObject.getData(), TESTDATA));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testWhenWeDeleteDataObjectHasFileIsFalse() throws Exception {
    EasyMock.expect(fsh.exists("a_file")).andReturn(false);
    EasyMock.expect(fsh.exists("a_file")).andReturn(true);
    EasyMock.expect(fsh.exists("a_file")).andReturn(false);
    EasyMock.expect(fsh.canRead("a_file")).andReturn(false);
    EasyMock.replay(fsh);
    dataStore.initialise(options);
    try {
      dataStore.store(data);
      dataStore.delete(data.getName());
      Assert.assertFalse(dataStore.hasFile(data.getName()));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testGetExpectedNumberOfObjectsFromGetAllDataObjects() throws Exception {
    EasyMock.expect(fsh.exists("banana")).andReturn(false);
    EasyMock.expect(fsh.exists("a_file")).andReturn(true);
    EasyMock.expect(fsh.exists("banana")).andReturn(false);
    EasyMock.expect(fsh.exists("a_file")).andReturn(true);
    EasyMock.replay(fsh);
    dataStore.initialise(options);
    try {
      dataStore.store(data);
      dataStore.store(new DataObjectImpl("banana", TESTDATA));
      Collection<DataObject> files = dataStore.getAllDataObjects();
      Assert.assertEquals(2, files.size());
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @Test
  public void testWhenWeStoreFileHasFileIsTrue() throws Exception {
    EasyMock.expect(fsh.exists("a_file")).andReturn(false);
    EasyMock.expect(fsh.exists("a_file")).andReturn(true);
    EasyMock.replay(fsh);
    dataStore.initialise(options);
    try {
      dataStore.store(data);
      Assert.assertTrue(dataStore.hasFile("a_file"));
    } catch (Exception e) {
      Assert.fail(e.getMessage());
    }
  }

  @After
  public void tearDown() throws Exception {
  }
}
