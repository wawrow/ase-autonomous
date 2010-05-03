package tests.com.slard.filerepository;

import org.mockito.Mockito;
import com.slard.filerepository.*;

public class SystemFileListTest extends FileListDataObjectImpl {

  public SystemFileListTest() {
    super(Mockito.mock(DataStore.class));
  }


}
