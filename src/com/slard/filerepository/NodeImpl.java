package com.slard.filerepository;

import org.jgroups.*;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.Request;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;
import org.jgroups.util.FutureListener;
import org.jgroups.util.NotifyingFuture;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.Future;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Dictionary;
import java.util.Vector;

public class NodeImpl extends ReceiverAdapter implements Node {
  private static final String CHANNEL_NAME = "FileRepositoryCluster";
  private static final int RPC_TIMEOUT = 90;


  private final Logger logger = Logger.getLogger(this.getClass().getName());

	private DataStore dataStore;
	private SystemCommunication systemComm;
	private ClientCommunication clientComm;
	private CHTHelper chtHelper;
	private long[] ids;
	private Dictionary<Long, NodeDescriptor> nodes;	
	
	//Constructor
	public NodeImpl(DataStore _dataStore, SystemCommunication _systemComm, ClientCommunication _clientComm, CHTHelper _chtHelper){
		this.dataStore = _dataStore;
		this.systemComm = _systemComm;
		this.clientComm = _clientComm;
		this.chtHelper = _chtHelper;
	}


  private JChannel channel;
  private RpcDispatcher rpcDispatcher;


  public void initialise() {
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.FINEST);
    logger.addHandler(ch);
  }
	@Override
	public void joinTheNetwork() {
		// Find the network
		this.nodes = this.systemComm.getNodelist();
		// TODO For each file in dataStore if any, check if I'll become a replica or master and perform actions
		
	}

  public long[] getIds() {
    return new long[0];  //To change body of implemented methods use File | Settings | File Templates.
  }

  public void start() throws Exception {
    this.channel = new JChannel();
    this.rpcDispatcher = new RpcDispatcher(channel, this, this, this);
    
    channel.connect(CHANNEL_NAME);
    logger.fine("dispatcher created and channel connected");
    eventLoop();
    logger.fine("eventloop finished, closing channel");
    channel.close();
  }

	@Override
	public void JoinTheNetwork() {
		// Find the network
		this.nodes = this.systemComm.GetNodelist();
		//
		    Vector<DataObject> ownedObjects = this.chtHelper.getOwnedObjects(this.nodes, this.ids, this.dataStore.GetAllDataObjects());
		    for(DataObject ownedObject: ownedObjects){
		    	//Get previous master Id
		    	//Match the CRC
		    	//Retrieve if required
		    	//Check if previous master will become replica
		    	//if not then delete
		    	//Fire Replicate Event on the file
		    }
		
		// TODO For each file in dataStore if any, check if I'll become a replica or master and perform actions
    //To change body of implemented methods use File | Settings | File Templates.
  }
  public void replicaGuard() {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void nodeLeft(long[] nodeIds) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  @Override
  public void nodeJoined(long[] nodeIds) {
    //To change body of implemented methods use File | Settings | File Templates.
  }

  private void eventLoop() {
    BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    MethodCall call = new MethodCall("print", null, new Class[]{String.class});
    while (true) {
      RequestOptions options = new RequestOptions(Request.GET_ALL, RPC_TIMEOUT);
      try {
        System.out.print("> ");
        System.out.flush();
        String line = in.readLine().toLowerCase().trim();
        if (line.startsWith("quit") || line.startsWith("exit")) {
          break;
        }        
        call.setArgs(new String[]{line});
        Address target = channel.getView().getMembers().firstElement();
        try {
          rpcDispatcher.callRemoteMethodWithFuture(target, call, options).setListener(new FutureListener<Object>() {
            @Override
            public void futureDone(Future<Object> booleanFuture) {
             System.out.println("Got my callback");
            }
          });
        } catch (Throwable throwable) {
          throwable.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        rpcDispatcher.callRemoteMethods(null, call, options);
      } catch (Exception e) {
        System.out.println("Exception encountered e=" + e.toString());
      }
    }
  }

  public Boolean print(String message) {
    System.out.println("Got the message: " + message);

    return true;
  }

  public void viewAccepted(View new_view) {
    System.out.println("** view: " + new_view);
  }

  public void receive(Message msg) {
    System.out.println("received message: " + msg.getSrc() + ": " + msg.getObject());
  }

 
}
     