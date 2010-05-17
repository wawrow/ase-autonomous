package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.blocks.RspFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CommsPrep {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
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

  interface Calls {
    String method();
  }

  public CommsPrep(RpcDispatcher dispatcher, int timeout) {
    this.dispatcher = dispatcher;
    this.timeout = timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  private MethodCall getMethodCall(Calls call, Object... args) {
    Class[] types = new Class[args.length];
    for (int i = 0; i < args.length; i++) {
      types[i] = args[i].getClass();
    }
    return new MethodCall(call.method(), args, types);
  }

  List<Object> issueRpcs(Calls toCall, Collection<Address> addresses, int gatherOption, Object... obj) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(gatherOption, timeout, false,
        new NullFilter((addresses != null) ? addresses.size() : dispatcher.getChannel().getView().size()));
    List<Object> ret = null;
    try {
      ret = Collections.list(dispatcher.callRemoteMethods(addresses, call, options).getResults().elements());
    } catch (Throwable throwable) {
      logger.warn("{} rpc failed: {}", toCall.method(), throwable);
    }
    return ret;
  }

  Object issueRpc(Calls toCall, Address address, int gatherOption, Object... obj) {
    MethodCall call = getMethodCall(toCall, obj);
    RequestOptions options = new RequestOptions(gatherOption, timeout, false,
        new NullFilter((address != null) ? 1 : dispatcher.getChannel().getView().size()));
    Object ret = null;
    try {
      ret = dispatcher.callRemoteMethod(address, call, options);
    } catch (Throwable throwable) {
      logger.warn("{} rpc failed {}", toCall.method(), throwable);
    }
    return ret;
  }

  Object issueRpc(Calls toCall, Address address, int gatherOptions) {
    return issueRpc(toCall, address, gatherOptions, new Object[0]);
  }
}