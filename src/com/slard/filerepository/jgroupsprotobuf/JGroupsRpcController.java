package com.slard.filerepository.jgroupsprotobuf;

import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 26-Apr-2010
 * Time: 23:18:04
 * Simple JGroupsRpcController that actually doesn't really bind to jgroups.
 * TODO: actually bind cancellation to jgroups.
 */
public class JGroupsRpcController implements RpcController {
  private String failed = null;
  private boolean cancelled = false;
  private RpcCallback<Object> cancellationCallback = null;

  @Override
  public void reset() {
    failed = null;
    cancelled = false;
    cancellationCallback = null;
  }

  @Override
  public boolean failed() {
    return failed != null;
  }

  @Override
  public String errorText() {
    return failed;
  }

  @Override
  public void startCancel() {
    cancelled = true;
    if(cancellationCallback != null) {
      cancellationCallback.run(null);
    }
  }

  @Override
  public void setFailed(String failure) {
    failed = failure;
  }

  @Override
  public boolean isCanceled() {
    return cancelled;
  }

  @Override
  public void notifyOnCancel(RpcCallback<Object> objectRpcCallback) {
    cancellationCallback = objectRpcCallback;
  }

  public static JGroupsRpcController newController() {
    JGroupsRpcController ret = new JGroupsRpcController();
    ret.reset();
    return ret;
  }
}
