package com.slard.filerepository;

import com.google.protobuf.*;
import com.slard.filerepository.jgroupsprotobuf.JGroupsRpcController;
import com.slard.filerepository.jgroupsprotobuf.JGroupsSyncRpcChannel;
import org.jgroups.Address;
import org.jgroups.Channel;
import org.jgroups.ChannelException;
import org.jgroups.JChannel;
import org.jgroups.blocks.RpcDispatcher;

import java.util.Map;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 26-Apr-2010
 * Time: 23:30:43
 * To change this template use File | Settings | File Templates.
 */
public class SystemCommunicationImpl implements SystemCommunication {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private static final String CHANNEL_NAME = "FileRepositoryCluster";
  private JChannel jgChannel = null;
  private RpcController controller;

  @Override
  public void initialise() throws ChannelException {
    this.controller = JGroupsRpcController.newController();
    this.jgChannel = new JChannel();

    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.FINEST);
    logger.addHandler(ch);

    jgChannel.connect(CHANNEL_NAME);
  }

  @Override
  public Map<Long, NodeDescriptor> getNodelist() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public Vector<String> syncFilelist() {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public boolean store(DataObject dataObject, Address target) {
    BlockingRpcChannel channel = JGroupsSyncRpcChannel.newUnicastRpcChannel(this.jgChannel, target);
    SystemCommunications.RepositoryNode.BlockingInterface service = SystemCommunications.RepositoryNode
        .newBlockingStub(channel);
    SystemCommunications.FileToStore request = SystemCommunications.FileToStore.newBuilder()
        .setSync(true)
        .setContent(dataObject.toMessageFileContents())
        .build();

    try {
      controller.reset();
      service.store(controller, request);
    } catch (ServiceException e) {
      logger.log(Level.WARNING, "rpc call failed", e);
      return false;
    }
    return true;
  }

  @Override
  public DataObject retrieve(String name) {
    return null;  //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void nodeLeft() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void nodeJoined() {
    //To change body of implemented methods use File | Settings | File Templates.
  }
}
