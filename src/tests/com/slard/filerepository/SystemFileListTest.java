package com.slard.filerepository;

import junit.framework.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class SystemFileListTest extends FileListDataObjectImpl {

  public SystemFileListTest() {
    super(Mockito.mock(DataStore.class));
  }

  @Test
  public void passTest() throws Exception {
    Assert.assertTrue(true);
  }

}
