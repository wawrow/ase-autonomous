package com.slard.filerepository;

import java.util.Vector;

import org.jgroups.Address;
import org.jgroups.View;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 28-Apr-2010
 * Time: 09:26:08
 * To change this template use File | Settings | File Templates.
 */
public class CHTImpl implements CHT {
  @Override
  public long[] GetIdsForNode(Node node) {
    return new long[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void recalculate(View view) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void leave(Address address) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public NodeDescriptor master(DataObject file) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public NodeDescriptor previous(NodeDescriptor node) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

@Override
public Vector<DataObject> getOwnedObjects(Long[] allNodes, long[] myIds,
		Vector<DataObject> myObjects) {
	// TODO Auto-generated method stub
	return null;
}
}
