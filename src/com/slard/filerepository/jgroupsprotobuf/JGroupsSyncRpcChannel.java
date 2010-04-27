package com.slard.filerepository.jgroupsprotobuf;

import com.google.protobuf.*;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.FutureListener;
import org.jgroups.util.NotifyingFuture;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 26-Apr-2010
 * Time: 19:51:38
 * Class to implement a protobuf.RpcChannel using JGroups as an RPC transport.
 * Should probably be templated on exact request and response messages.
 */
public class JGroupsSyncRpcChannel implements BlockingRpcChannel {
  private static RequestOptions options = null;

  private static RpcDispatcher dispatcher;
  private static Address target;

  private JGroupsSyncRpcChannel(Channel jgroupsChannel, Address target) {
    this.dispatcher = new RpcDispatcher(jgroupsChannel, null, null, this);  // using wrong object at end here
  }

  private void setRequestOptions(RequestOptions options) {
    this.options = options;
  }

  @Override
  public Message callBlockingMethod(Descriptors.MethodDescriptor desc, final RpcController rpcController, Message request,
                         Message responseProto) {

    MethodCall call = new MethodCall(desc.getName(), new Message[]{request}, new Class[]{Message.class});
//    NotifyingFuture<Message> future = null;
//    try {
//      future = dispatcher.callRemoteMethodWithFuture(target, call, options);
//    } catch (Throwable throwable) {
//      rpcController.setFailed(throwable.getLocalizedMessage());
//      return;
//    }
//    future.setListener(new FutureListener<Message>() {
//            @Override
//            public void futureDone(Future<Message> future) {
//              try {
//                if (future.isCancelled()){
//                  throw new InterruptedException();
//                } else if (future.get() == null) {
//                  throw new ExecutionException(new Throwable("invalid future received"));
//                }
//                messageRpcCallback.run(future.get());
//              } catch (InterruptedException e) {
//                rpcController.setFailed(e.getLocalizedMessage());
//              } catch (ExecutionException e) {
//                rpcController.setFailed(e.getLocalizedMessage());
//              }
//            }
//          });
    Message ret = null;
    try {
      ret = (Message) dispatcher.callRemoteMethod(target, call, options);
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }
    return ret;
  }

  public static BlockingRpcChannel newUnicastRpcChannel(Channel jgroupsChannel, Address target) {
    JGroupsSyncRpcChannel ret = new JGroupsSyncRpcChannel(jgroupsChannel, target);
    RequestOptions options = new RequestOptions();
    options.setMode(org.jgroups.blocks.Request.GET_FIRST | org.jgroups.Message.NO_FC | org.jgroups.Message.DONT_BUNDLE);
    ret.setRequestOptions(options);
    return ret;    
  }
}
