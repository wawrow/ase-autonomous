package com.slard.filerepository;

import org.jgroups.Address;

import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public enum FileRepositoryClientCommand {
  HELP(new Action() {
    @Override
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      c.printf("File Repository Client v1.0%n%n");
      c.printf("   help                 Output this help text%n");
      c.printf("   quit                 exit the client%n");
      c.printf("   cluster              displays the current cluster membership%n");
      c.printf("   capacity             returns the total capacity and free space of the repository%n");
      c.printf("   list <regex>         lists all files in the repository, optionally matching regex%n");
      c.printf("   cat <file name>      dumps the named file's contents on the console%n");
      c.printf("   store <file name>    dumps the named file's contents on the console%n");
      c.printf("   replace <file name>  replaces the named file in the repository with the version in the local directory%n");
      c.printf("   retrieve <file name> retrieves the named file from the repository into the local directory%n");
      c.printf("   delete <file name>   deletes the named file from the repository%n%n");
    }

    @Override
    public String[] aliases() {
      return new String[]{"f1", "commands"};
    }
  }),

  QUIT(new Action() {
    @Override
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) {
      System.exit(0);
    }

    @Override
    public String[] aliases() {
      return new String[]{"exit"};
    }
  }),

  CLUSTER(new Action() {
    @Override
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      ArrayList<Address> clients = new ArrayList<Address>();
      ArrayList<Address> servers = new ArrayList<Address>();
      fileRepositoryClient.listNodes(clients, servers);

      c.printf("%d client nodes%n", clients.size());
      for (Address address : clients) {
        c.printf("   %s%n", address.toString());
      }
      c.printf("%d server nodes%n", servers.size());
      for (Address address : servers) {
        c.printf("   %s%n", address.toString());
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"nodes", "members"};
    }
  }),

  LIST(new Action() {
    @Override
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {

      // Ask any node for the list
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();

      String regex = ".*";
      if (args != null && args.length > 0 && args[0] != null) {
        regex = args[0];
      }
      List<String> files = userCommsClient.getFileNames(regex);
      if (files == null)
        throw new Exception("No files found in repository");
      Collections.sort(files);
      for (String file : files) {
        c.printf("%s%n", file);
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"ls", "dir"};
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
      if (dataObject == null) {
        throw new Exception("Retrieve of " + args[0] + " failed ");
      }
      BufferedOutputStream bos = new BufferedOutputStream(System.out);
      bos.write(dataObject.getData());
      bos.flush();
    }

    @Override
    public String[] aliases() {
      return new String[]{"spool", "less"};
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

    @Override
    public String[] aliases() {
      return new String[]{"get", "pull"};
    }
  }),

  DELETE(new Action() {
    @Override
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception {
      if (args == null || args[0] == null || args.length != 1) {
        throw new Exception("Please specify a single file name to delete");
      }

      // Send the delete command to any node - if we miss the master, no big deal, just one more hop
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();
      if (userCommsClient == null)
        throw new Exception("No repository node could be found to service request");
      if (userCommsClient.delete(args[0]) == false)
        throw new Exception("Delete of " + args[0] + " failed");
    }

    @Override
    public String[] aliases() {
      return new String[]{"del", "rm"};
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

    @Override
    public String[] aliases() {
      return new String[]{"repl", "overwrite"};
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
      DataObjectImpl dataObject = new DataObjectImpl(file.getName(), fileSystemHelper.readFile(file));

      // Ask any node who the master for this file is
      Address address = fileRepositoryClient.getMaster(file.getName());
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

    @Override
    public String[] aliases() {
      return new String[]{"put", "push"};
    }
  }),

  CAPACITY(new Action() {
    @Override
    public void exec(Console c, String[] args,
                     FileRepositoryClient fileRepositoryClient) throws Exception {
      UserCommsClientImpl userCommsClient = fileRepositoryClient.createUserCommsClient();
      UserOperations.DiskSpace space = userCommsClient.getDiskSpace();
      c.printf("%s%n", space.toString());
    }

    @Override
    public String[] aliases() {
      return new String[]{"df", "space"};
    }
  });

  private interface Action {
    public void exec(Console c, String[] args, FileRepositoryClient fileRepositoryClient) throws Exception;

    public String[] aliases();
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

  public String[] aliases() {
    return action.aliases();
  }
}
