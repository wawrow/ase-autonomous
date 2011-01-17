package com.slard.filerepository;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.jgroups.Address;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.RspFilter;
import org.jgroups.util.NotifyingFuture;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CommsPrepImpl implements CommsPrep {
  @InjectLogger
  Logger logger;
  private final RpcDispatcher dispatcher;
  private int timeout;

  private class NullFilter implements RspFilter {
    private int expected;

    private NullFilter(int expected) {
      this.expected = expected;
    }

    @Override
    public boolean isAcceptable(Object response, Address source) {
      expected--;
      logger.trace("got response from {}", source.toString());
      return (response != null);
    }

    @Override
    public boolean needMoreResponses() {
      return (expected > 0);
    }
  }

  @Inject
  public CommsPrepImpl(@Assisted RpcDispatcher dispatcher, @Assisted int timeout) {
    this.dispatcher = dispatcher;
    this.timeout = timeout;
  }

  @Override
  public void setDefaultTimeout(int timeout) {
    this.timeout = timeout;
  }

  private MethodCall getMethodCall(Calls call, Object... args) {
    Class[] types = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      types[i] = args[i].getClass();
    }
    return new MethodCall(call.method(), args, types);
  }

  private int timeout(Calls call, int requested) {
    if (requested >= 0) {
      return requested;
    }
    if (call.timeout() >= 0) {
      return call.timeout();
    }
    return timeout;
  }

  @Override
  public List<Object> issueRpcs(Calls toCall, Collection<Address> addresses, int gatherOption, int timeout, Object... obj) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(gatherOption, timeout(toCall, timeout), true,
        new NullFilter((addresses != null) ? addresses.size() : dispatcher.getChannel().getView().size()));
    List<Object> ret = null;
    try {
      ret = Collections.list(dispatcher.callRemoteMethods(addresses, call, options).getResults().elements());
    } catch (Throwable throwable) {
      logger.warn("{} rpc failed: {}", toCall.method(), throwable);
    }
    return ret;
  }

  @Override
  public Object issueRpc(Calls toCall, Address address, int gatherOption, int timeout, Object... obj) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(gatherOption, timeout(toCall, timeout), true,
        new NullFilter((address != null) ? 1 : dispatcher.getChannel().getView().size()));
    Object ret = null;
    try {
      ret = dispatcher.callRemoteMethod(address, call, options);
    } catch (Throwable throwable) {
      logger.warn("{} rpc failed {}", toCall.method(), throwable);
    }
    return ret;
  }

  @Override
  public Object issueRpc(Calls toCall, Address address, int gatherOptions, int timeout) {
    return issueRpc(toCall, address, gatherOptions, timeout, new Object[0]);
  }

  @Override
  public NotifyingFuture<Object> issueAsyncRpc(Calls toCall, Address address, int timeout, int gatherOption, Object... obj) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(gatherOption, timeout(toCall, timeout), true,
        new NullFilter((address != null) ? 1 : dispatcher.getChannel().getView().size()));
    NotifyingFuture<Object> ret = null;
    try {
      ret = dispatcher.callRemoteMethodWithFuture(address, call, options);
    } catch (Throwable throwable) {
      logger.warn("{} rpc failed {}", toCall.method(), throwable);
    }
    return ret;
  }
}