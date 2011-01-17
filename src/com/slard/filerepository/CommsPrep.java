package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.NotifyingFuture;

import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 21-May-2010
 * Time: 14:30:39
 * To change this template use File | Settings | File Templates.
 */
public interface CommsPrep {
  void setDefaultTimeout(int timeout);

  List<Object> issueRpcs(Calls toCall, Collection<Address> addresses, int gatherOption, int timeout, Object... obj);

  Object issueRpc(Calls toCall, Address address, int gatherOption, int timeout, Object... obj);

  Object issueRpc(Calls toCall, Address address, int gatherOptions, int timeout);

  NotifyingFuture<Object> issueAsyncRpc(Calls toCall, Address address, int timeout, int gatherOption, Object... obj);

  public interface Calls {
    String method();

    int timeout();
  }

  public interface CommsPrepFactory {
    CommsPrep create(RpcDispatcher dispatcher, int timeout);
  }
}
