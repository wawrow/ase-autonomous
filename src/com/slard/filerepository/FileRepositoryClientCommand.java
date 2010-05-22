package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.util.FutureListener;
import org.jgroups.util.NotifyingFuture;
import org.jgroups.util.Tuple;
import org.slf4j.Logger;

import java.io.BufferedOutputStream;
import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public enum FileRepositoryClientCommand {
  HELP(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      System.out.printf("File Repository Client v1.0%n%n");
      System.out.printf("   help                 Output this help text%n");
      System.out.printf("   quit                 exit the client%n");
      System.out.printf("   cluster              displays the current cluster membership%n");
      System.out.printf("   capacity             returns the total capacity and free space of the repository%n");
      System.out.printf("   list <regex>         lists all files in the repository, optionally matching regex%n");
      System.out.printf("   cat <file name>      dumps the named file's contents on the console%n");
      System.out.printf("   store <file name>    dumps the named file's contents on the console%n");
      System.out.printf("   replace <file name>  replaces the named file in the repository with the version in the local directory%n");
      System.out.printf("   retrieve <file name> retrieves the named file from the repository into the local directory%n");
      System.out.printf("   delete <file name>   deletes the named file from the repository%n%n");
    }

    @Override
    public String[] aliases() {
      return new String[]{"f1", "commands"};
    }
  }),

  QUIT(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      System.exit(0);
    }

    @Override
    public String[] aliases() {
      return new String[]{"exit"};
    }
  }),

  CLUSTER(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      Tuple<Collection<Address>, Collection<Address>> nodes = userComms.listNodes();
      System.out.printf("%d client nodes%n", nodes.getVal1().size());
      for (Address address : nodes.getVal1()) {
        System.out.printf("   %s%n", address.toString());
      }
      System.out.printf("%d server nodes%n", nodes.getVal2().size());
      for (Address address : nodes.getVal2()) {
        System.out.printf("   %s%n", address.toString());
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"nodes", "members", "status"};
    }
  }),

  LIST(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      String regex = ".*";
      if (args != null && args.size() > 0 && args.get(0) != null) {
        regex = args.get(0);
      }
      List<String> files = new ArrayList<String>(userComms.getFileNames(regex));
      if (files.isEmpty()) {
        logger.warn("no files in repository");
      }
      Collections.sort(files);
      for (String file : files) {
        System.out.printf("%s%n", file);
        logger.debug(file);
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"ls", "dir"};
    }
  }),

  CAT(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() != 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;
      }

      // Broadcast request for anyone with the file, then ask the first to respond for the file
      Address address = userComms.getQuickestFileLocation(args.get(0));
      if (address == null) {
        logger.warn("No repository node has file {}", args.get(0));
        return;
      }
      System.out.printf("Retrieving from node %s%n", address.toString());
      DataObject dataObject = userComms.retrieve(args.get(0), address);

      // Write the retrieved file to console
      if (dataObject == null) {
        logger.warn("Retrieve of {} failed.", args.get(0));
        return;
      }
      BufferedOutputStream bos = new BufferedOutputStream(System.out);
      try {
        byte[] data = dataObject.getData();
        if (data == null) {
          throw new IOException("file has no data");
        }
        bos.write(data);
        bos.flush();
      } catch (IOException e) {
        logger.warn("unable to get content of {}", args.get(0));
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"spool", "less"};
    }
  }),

  RETRIEVE(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() < 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;
      }
      // Broadcast request for anyone with the file, then ask the first to respond for the file
      Address address = userComms.getQuickestFileLocation(args.get(0));
      if (address == null) {
        logger.warn("No repository node has file {}", args.get(0));
        return;
      }
      System.out.printf("Retrieving from node %s%n", address.toString());
      DataObject dataObject = userComms.retrieve(args.get(0), address);

      // Write the retrieved file to the local file system
      if (dataObject == null) {
        logger.warn("Retrieve of {} failed.", args.get(0));
      }
      FileSystemHelper fs = new FileSystemHelperImpl(new File("."));
      String target = (args.size() >= 2) ? args.get(1) : dataObject.getName();
      File file = new File(target);
      try {
        fs.writeFile(file, dataObject.getData());
      } catch (IOException e) {
        logger.warn("Failed to save file contents to {}", target);
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"get", "pull"};
    }
  }),

  DELETE(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() != 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;
      }
      Address master = userComms.getMaster(args.get(0));
      if (master == null) {
        logger.error("No node is master for {}", args.get(0));
      }
      if (!userComms.delete(args.get(0), master)) {
        logger.warn("Delete of {} failed.", args.get(0));
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"del", "rm"};
    }
  }),

  REPLACE(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() != 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;

      }
      // Read the replacement file from the local file system
      FileSystemHelper fs = new FileSystemHelperImpl(new File("."));
      File file = new File(args.get(0));
      DataObject dataObject = null;
      try {
        dataObject = new DataObjectImpl(args.get(0), fs.readFile(file));
      } catch (IOException e) {
        logger.warn("Failed to load file {}", args.get(0));
        return;
      }

      // Ask any node who the master for this file is
      Address master = userComms.getMaster(args.get(0));
      if (master == null) {
        logger.error("No node is master for {}", args.get(0));
      }
      System.out.printf("Directing request to node %s%n", master.toString());

      // Send the replace command to the returned master address
      if (!userComms.replace(dataObject, master)) {
        logger.warn("Replace of {} failed.", args.get(0));
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"repl", "overwrite"};
    }
  }),

  STORE(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() < 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;

      }
      // Read the new file from the local file system
      FileSystemHelper fs = new FileSystemHelperImpl(new File("."));
      File file = new File(args.get(0));
      DataObject dataObject = null;
      String target = (args.size() < 2) ? args.get(0) : args.get(1);
      if (!file.canRead()) {
        logger.warn("can't read file {}", file.getName());
        return;
      }
      try {
        dataObject = new DataObjectImpl(target, fs.readFile(file));
      } catch (IOException e) {
        logger.warn("unable to load file {}", args.get(0));
      }

      Address master = userComms.getMaster(target);
      if (master == null) {
        logger.error("No node is master for {}", target);
      }
      System.out.printf("Directing request to node %s%n", master.toString());

      // Send the file to the returned master address
      if (!userComms.store(dataObject, master)) {
        logger.warn("store of {} as {} failed.", args.get(0), target);
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"put", "push"};
    }
  }),

  STOREALL(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() < 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;

      }
      // Read the new file from the local file system
      FileSystemHelper fs = new FileSystemHelperImpl(new File("."));
      File file = new File(args.get(0));
      DataObject dataObject = null;
      String target = (args.size() < 2) ? args.get(0) : args.get(1);
      if (!file.canRead()) {
        logger.warn("can't read file {}", file.getName());
        return;
      }
      try {
        dataObject = new DataObjectImpl(target, fs.readFile(file));
      } catch (IOException e) {
        logger.warn("unable to load file {}", args.get(0));
      }

      Address master = userComms.getMaster(target);
      if (master == null) {
        logger.error("No node is master for {}", target);
      }
      System.out.printf("Directing request to node %s%n", master.toString());

      // Send the file to the returned master address
      if (!userComms.storeAll(dataObject, master)) {
        logger.warn("store of {} as {} failed.", args.get(0), target);
      }
    }

    @Override
    public String[] aliases() {
      return new String[]{"safe", "sync"};
    }
  }),

  ASYNCSTORE(new Action() {
    @Override
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l) {
      if (args == null || args.get(0) == null || args.size() < 1) {
        logger.warn("Please specify a single file name to retrieve");
        return;

      }
      // Read the new file from the local file system
      FileSystemHelper fs = new FileSystemHelperImpl(new File("."));
      File file = new File(args.get(0));
      final DataObject dataObject;
      String target = (args.size() < 2) ? args.get(0) : args.get(1);
      if (!file.canRead()) {
        logger.warn("can't read file {}", file.getName());
        return;
      }
      try {
        dataObject = new DataObjectImpl(target, fs.readFile(file));
      } catch (IOException e) {
        logger.warn("unable to load file {}", args.get(0));
        return;
      }
      Address master = userComms.getMaster(target);
      if (master == null) {
        logger.error("No node is master for {}", target);
      }
      System.out.printf("Directing request to node %s%n", master.toString());

      // Send the file to the returned master address
      class AsyncFuture implements FutureListener<Object> {
        Listener listener;

        public AsyncFuture(Listener listener) {
          this.listener = listener;
        }

        public void futureDone(Future<Object> future) {
          try {
            listener.updateStatus("async put of " + dataObject.getName(), future.get());
          } catch (InterruptedException e) {
            logger.warn("async excecution interrupted");
          } catch (ExecutionException e) {
            logger.warn("async execution raised exception: ", e);
          }
        }
      }
      AsyncFuture theFuture = new AsyncFuture(l);
      l.updateStatus("files loaded and ready to send ", null);
      NotifyingFuture<Object> future = userComms.storeAllAsync(dataObject, master);
      future.setListener(theFuture);
    }

    @Override
    public String[] aliases() {
      return new String[]{"storeasync", "async", "stash"};
    }
  }),


  CAPACITY(new Action() {
    private String ToHuman(Long in) {
      String[] sizes = new String[]{"bytes", "KB", "MB", "GB", "TB", "EB"};
      float real = in;
      int index = 0;
      while (real / 1000 > 1) {
        real /= 1000;
        index++;
      }
      return String.format("%,.3f %s", real, sizes[index]);
    }

    @Override
    public void exec(Console c, List<String> args,
                     UserOperations userComms, Listener l) {
      Usage space = userComms.getDiskSpace();
      if (space == null) {
        logger.warn("unable to get the clusters disk usage");
      }
      System.out.printf("Cluster %s: total used %s, Free space %s%n", space.getHostname(),
          ToHuman(space.getTotal()), ToHuman(space.getFree()));
    }

    @Override
    public String[] aliases() {
      return new String[]{"df", "space"};
    }
  });


  private interface Action {
    public void exec(Console c, List<String> args, UserOperations userComms, Listener l);

    public String[] aliases();
  }

  public interface Listener {
    public void exception(Exception e);

    public void updateStatus(String comment, Object result);
  }

  private Action action;
  static
  @InjectLogger
  Logger logger;

  private FileRepositoryClientCommand(Action a) {
    this.action = a;
  }

  public void exec(final Console c, final List<String> args, UserOperations commsClient,
                   final Listener l) {
    try {
      action.exec(c, args, commsClient, l);
    } catch (Exception e) {
      l.exception(e);
    }
  }

  public String[] aliases() {
    return action.aliases();
  }
}
