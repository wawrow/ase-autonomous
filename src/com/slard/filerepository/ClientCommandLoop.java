package com.slard.filerepository;

import com.google.inject.ImplementedBy;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 17/01/2011
 * Time: 21:23
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(ClientCommandLoopImpl.class)
public interface ClientCommandLoop {
  void start();
}
