package com.slard.filerepository;

import java.io.*;
import java.util.Scanner;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.MembershipListener;
import org.jgroups.MessageListener;
import org.jgroups.View;
import org.jgroups.Message;
import org.jgroups.blocks.RpcDispatcher;

public class FileRepositoryClient implements MessageListener, MembershipListener {
  private JChannel userChannel;
  private static final String USER_CHANNEL_NAME = "FileRepositoryClusterClient";
  private static final String PROMPT = "> ";
  
  public static void main(String[] args) {
    new FileRepositoryClient().start();
  }

  public void start() {
    try {
      System.out.println("File Repository Client Starting");
      System.setProperty("jgroups.udp.mcast_port", "45589");
      userChannel = new JChannel();
      userChannel.connect(USER_CHANNEL_NAME);
      
      // If we're the first then no good - no cluster
      View initialView = userChannel.getView();
      if (initialView.size() == 1) {
        System.out.println("Failed to connect to file repository, no nodes are alive");
        userChannel.close();
        return;
      }
      
      // Start the console interaction
      Console console = System.console(); 
      if (console != null){ 
        commandLoop(console);
      } else { 
        throw new RuntimeException("No console could be created"); 
      }
    } catch (Exception e){
        e.printStackTrace();
    }
  }
  
  private void commandLoop(final Console console) {
    while (true) {  
      String commandLine = console.readLine(PROMPT); 
      Scanner scanner = new Scanner(commandLine); 
      if (scanner.hasNext()) { 
        final String commandName = scanner.next().toUpperCase(); 
        try { 
          final FileRepositoryClientCommand command = Enum.valueOf(FileRepositoryClientCommand.class, commandName); 
          String param = scanner.hasNext() ? scanner.next() : null; 
          command.exec(console, new String[]{param}, this, new FileRepositoryClientCommand.Listener() { 
            @Override
            public void exception(Exception e) {
              console.printf("Command error [%1$s]: [%2$s]%n", command, e.getMessage()); 
            } 
          }); 
        } catch (IllegalArgumentException e){ 
          console.printf("Unknown command [%1$s]%n", commandName); 
        } 
      } 
      scanner.close();    
    }
  }

  public UserCommsClientImpl createUserCommsClient() {
    // Look for the first member that isn't us
    View view = userChannel.getView();
    for(Address address: view.getMembers()){
      if(!address.equals(userChannel.getAddress())) {
        UserCommsClientImpl userCommsClient = UserCommsClientImpl.getUserCommsClient(
            new RpcDispatcher(userChannel, this, this, this), address);    
        return userCommsClient;        
      }
    }
    return null;
  }

  @Override
  public void viewAccepted(View newView) {
  }

  @Override
  public void receive(Message message) {
  }

  @Override
  public byte[] getState() {
    return null;
  }

  @Override
  public void setState(byte[] bytes) {
  }

  @Override
  public void suspect(Address address) {
  }
  
  @Override
  public void block() {
  }
}
