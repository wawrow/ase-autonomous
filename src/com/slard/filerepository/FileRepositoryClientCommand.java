package com.slard.filerepository;

import java.io.Console;
import java.io.File;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.jgroups.Address;

public enum FileRepositoryClientCommand {
  HELP(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      c.printf("File Repository Client v1.0%n%n");
      c.printf("   help                 Output this help text%n");
      c.printf("   quit                 exit the client%n");
      c.printf("   cluster              displays the current cluster membership%n");
      c.printf("   list                 lists all files in the repository%n");
      c.printf("   cat <file name>      dumps the named file's contents on the console%n");
      c.printf("   store <file name>    dumps the named file's contents on the console%n");
      c.printf("   replace <file name>  replaces the named file in the repository with the version in the local directory%n");
      c.printf("   retrieve <file name> retrieves the named file from the repository into the local directory%n");
      c.printf("   delete <file name>   deletes the named file from the repository%n%n");
    }
  }), 

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

  LIST(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {

      // Ask any node for the list
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();
      List<String> files = userCommsClient.getFileNames();      
      if (files == null)
        throw new Exception("No files found in repository");
      for (String name : files) {
        c.printf("%s%n", name);        
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
      if (address == null)
        throw new Exception("No repository node has file " + args[0]);
      c.printf("Retrieving from node %s%n", address.toString());
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      DataObject dataObject = userCommsClient.retrieve(args[0]);
      
      // Write the retrieved file to console
      if (dataObject == null)
        throw new Exception("Retrieve of " + args[0] + " failed ");    
      // TODO: Understand encoding issue or else ditch the cat command completely
      //c.printf("%s%n", dataObject.getData().toString());
      PrintStream out = new PrintStream(System.out, true, "UTF-8");
      out.println(dataObject.getData());        
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
      if (address == null)
        throw new Exception("No repository node has file " + args[0]);
      c.printf("Retrieving from node %s%n", address.toString());
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      DataObject dataObject = userCommsClient.retrieve(args[0]);
      
      // Write the retrieved file to the local file system
      if (dataObject == null)
        throw new Exception("Retrieve of " + args[0] + " failed ");
      FileSystemHelper fileSystemHelper = new FileSystemHelper();
      File file = new File(dataObject.getName());
      fileSystemHelper.writeFile(file, dataObject.getData());
    } 
  }), 

  DELETE(new Action() { 
    @Override 
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to delete");
      }

      // Ask all nodes who the master for this file is
      Address address = fileRepositoryClient.getMaster(args[0]);
      if (address == null)
        throw new Exception("No repository node could be found to service request");
      c.printf("Directing request to node %s%n", address.toString());
      
      // Send the delete command to the returned master address
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      if (userCommsClient == null) 
        throw new Exception("No repository node could be found to service request");
      if (userCommsClient.delete(args[0]) == false)
        throw new Exception("Delete of " + args[0] + " failed");
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
      Address address = fileRepositoryClient.getMaster(args[0]);
      if (address == null)
        throw new Exception("No repository node could be found to service request");
      c.printf("Directing request to node %s%n", address.toString());
      
      // Send the replace command to the returned master address
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      if (userCommsClient == null) 
        throw new Exception("No repository node could be found to service request");
      if (userCommsClient.replace(dataObject) == false)
        throw new Exception("Replace of " + args[0] + " failed");
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
      Address address = fileRepositoryClient.getMaster(args[0]);
      if (address == null)
        throw new Exception("No repository node could be found to service request");
      c.printf("Directing request to node %s%n", address.toString());
      
      // Send the file to the returned master address
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient(address);
      if (userCommsClient == null) 
        throw new Exception("No repository node could be found to service request");      
      if (userCommsClient.store(dataObject) == false) {
        throw new Exception("Store of " + args[0] + " failed");
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
