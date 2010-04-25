package com.slard.filerepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;

public class NodeImpl extends ReceiverAdapter {
    JChannel channel;
  
    public void start() throws Exception {
        channel=new JChannel();
        channel.setReceiver(this);
        channel.connect("FileRepositoryCluster");
        eventLoop();
        channel.close();
    }
    
    private void eventLoop() {
        BufferedReader in=new BufferedReader(new InputStreamReader(System.in));
        while(true) {
        	try {
                System.out.print("> "); System.out.flush();
                String line=in.readLine().toLowerCase();
                if(line.startsWith("quit") || line.startsWith("exit")) {
                    break;
                }
                Message msg=new Message(null, null, line);
                channel.send(msg);
            } catch(Exception e) {
                System.out.println("Exception encountered e=" + e.toString());	
            }
        }
    }
    
    public void viewAccepted(View new_view) {
        System.out.println("** view: " + new_view);
    }

    public void receive(Message msg) {
        System.out.println(msg.getSrc() + ": " + msg.getObject());
    }
}
     