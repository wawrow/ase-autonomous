package com.slard.filerepository;

import java.io.Console;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;

import org.jgroups.Address;

public enum FileRepositoryClientCommand {
  QUIT(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) {  
      System.exit(0); 
    } 
  }), 

  CLUSTER(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      ArrayList<Address> clients = new ArrayList<Address>();
      ArrayList<Address> servers = new ArrayList<Address>();
      fileRepositoryClient.listNodes(clients, servers);

      c.printf("%d client nodes%n", clients.size());
      for (Address address: clients) {
        c.printf("   %s%n", address.toString());
      }
      c.printf("%d server nodes%n", servers.size());
      for (Address address: servers) {
        c.printf("   %s%n", address.toString());
      }      
    } 
  }), 

  CAT(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to retrieve");
      }

      // Broadcast request for anyone with the file, then ask the first to respond for the file
      Address address = fileRepositoryClient.getQuickestFileLocation(args[0]);
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      DataObject dataObject = userCommsClient.retrieve(args[0]);
      
      // Write the retrieved file to console
      if (dataObject == null) {
        c.printf("Retrieve of %s failed%n", args[0]);        
      } else {
        // TODO: Understand encoding issue or else ditch the cat command completely
        //c.printf("%s%n", dataObject.getData().toString());
        PrintStream out = new PrintStream(System.out, true, "UTF-8");
        out.println(dataObject.getData());        
      }
    } 
  }), 

  RETRIEVE(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to retrieve");
      }
      
      // Broadcast request for anyone with the file, then ask the first to respond for the file
      Address address = fileRepositoryClient.getQuickestFileLocation(args[0]);
      c.printf("Address returned %s%n", address.toString());
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      DataObject dataObject = userCommsClient.retrieve(args[0]);
      
      // Write the retrieved file to the local file system
      if (dataObject == null) {
        c.printf("Retrieve of %s failed%n", args[0]);        
      } else {
        FileSystemHelper fileSystemHelper = new FileSystemHelper();
        File file = new File(dataObject.getName());
        fileSystemHelper.writeFile(file, dataObject.getData());
      }
    } 
  }), 

  REPLACE(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to replace");
      }      
      // Read the replacement file from the local file system
      FileSystemHelper fileSystemHelper = new FileSystemHelper();
      File file = new File(args[0]);
      DataObjectImpl dataObject = new DataObjectImpl(args[0], fileSystemHelper.readFile(file));
      
      // Ask any node who the master for this file is
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();
      Address address = userCommsClient.whoIsMaster(args[0]);      

      // Send the replace command to the returned master address
      userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      if (userCommsClient.replace(dataObject) == false) {
        c.printf("Replace of %s failed%n", args[0]);
      }
    } 
  }), 

  DELETE(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to delete");
      }

      // Ask any node who the master for this file is
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();
      Address address = userCommsClient.whoIsMaster(args[0]);      
      
      // Send the delete command to the returned master address
      userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      if (userCommsClient.delete(args[0]) == false) {
        c.printf("Delete of %s failed%n", args[0]);
      }
    } 
  }), 

  STORE(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to store");
      }

      // Read the new file from the local file system
      FileSystemHelper fileSystemHelper = new FileSystemHelper();
      File file = new File(args[0]);
      DataObjectImpl dataObject = new DataObjectImpl(args[0], fileSystemHelper.readFile(file));

      // Ask any node who the master for this file is
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();
      Address address = userCommsClient.whoIsMaster(args[0]);
      
      // Send the file to the returned master address
      userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      if (userCommsClient.store(dataObject) == false) {
        c.printf("Store of %s failed%n", args[0]);
      }
    } 
  }); 
 
  private interface Action {  
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception; 
  } 
 
  public interface Listener { 
    public void exception(Exception e); 
  } 
 
  private Action action;
 
  private FileRepositoryClientCommand(Action a) {  
    this.action = a; 
  } 
 
  public void exec(final Console c, final String[] args, FileRepositoryClient fileRepositoryClient, final Listener l) {  
    try {      
      action.exec(c, args, fileRepositoryClient); 
    } catch (Exception e) {
      l.exception(e); 
    } 
  } 
}
