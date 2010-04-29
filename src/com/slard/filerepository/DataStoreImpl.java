package com.slard.filerepository;

import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 28-Apr-2010
 * Time: 09:25:37
 * To change this template use File | Settings | File Templates.
 */
public class DataStoreImpl implements DataStore {
  @Override
  public DataObject GetDataObject(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Boolean Contains(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void Delete(String name) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void StoreDataObject(DataObject dataObject) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Vector<DataObject> GetAllDataObjects() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }
  
  public DataStoreImpl(String path){
	//Construct with path as working path    
  }
}
