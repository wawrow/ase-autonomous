package com.slard.filerepository;

import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.View;
import org.jgroups.blocks.RpcDispatcher;

import java.io.Console;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.*;

public class FileRepositoryClient {
  private JChannel userChannel;
  private static final String PROMPT = "> ";
  UserCommsDummyServerImpl userCommsServer = null;

  private static final LogManager logManager = LogManager.getLogManager();

  public static void main(String[] args) {
    logManager.reset();
    try {
      Handler fileHandler = new FileHandler("client.%u.log", 100000, 5, true);

      fileHandler.setFormatter(new Formatter() {
        public String format(LogRecord log) {
          String className = log.getSourceClassName();
          StringBuilder sbuf = new StringBuilder();
          sbuf.append(log.getLevel().getLocalizedName())
              .append("\t[").append(log.getThreadID())
              .append("] ").append(className.substring(className.lastIndexOf('.') + 1))
              .append(".").append(log.getSourceMethodName())
              .append("\t").append(log.getMessage());
          if (log.getThrown() != null) {
            sbuf.append(log.getThrown().toString());
            for (StackTraceElement elem : log.getThrown().getStackTrace()) {
              sbuf.append("\n\t ").append(elem.toString());
            }
          }
          return sbuf.append("\n").toString();
        }
      });

      fileHandler.setLevel(Level.FINEST);
      logManager.getLogger("").addHandler(fileHandler);
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    new FileRepositoryClient().start();
  }

  public void start() {
    try {
      System.out.println("File Repository Client Starting");
      System.setProperty("jgroups.udp.mcast_port", "45589");
      userChannel = new JChannel();
      userChannel.connect(NodeImpl.USER_CHANNEL_NAME);

      // If we're the first then no good - no cluster
      View initialView = userChannel.getView();
      if (initialView.size() == 1) {
        System.out.println("Warning, no file repository nodes detected");
      }

      // Create a dummy user communications server so we can identify ourselves as a client only
      userCommsServer = new UserCommsDummyServerImpl(userChannel, null, null);

      // Start the console interaction
      Console console = System.console();
      if (console != null) {
        commandLoop(console);
      } else {
        throw new RuntimeException("No console could be created");
      }
    } catch (Exception e) {
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
        } catch (IllegalArgumentException e) {
          console.printf("Unknown command [%1$s]%n", commandName);
        }
      }
      scanner.close();
    }
  }

  public void listNodes(ArrayList<Address> clients, ArrayList<Address> servers) throws Exception {
    View view = userChannel.getView();
    for (Address address : view.getMembers()) {
      if (address.equals(userChannel.getAddress())) {
        clients.add(address);
      } else {
        UserCommsClientImpl userCommsClient = UserCommsClientImpl.getUserCommsClient(
            new RpcDispatcher(userChannel, null, null, userCommsServer), address);
        if (userCommsClient.isServer()) {
          servers.add(address);
        } else {
          clients.add(address);
        }
      }
    }
  }

  // Ask everyone are they the master for the named file
  public Address getMaster(String fileName) {
    return (Address) UserCommsClientImpl.getMaster(new RpcDispatcher(userChannel, null, null, userCommsServer), fileName);
  }

  // Ask everyone about a file and return the address of the first to respond
  public Address getQuickestFileLocation(String fileName) {
    return (Address) UserCommsClientImpl.getQuickestFileLocation(new RpcDispatcher(userChannel, null, null, userCommsServer), fileName);
  }

  // Create a user communications client to the specified address
  public UserCommsClientImpl createUserCommsClient(Address address) throws Exception {
    return UserCommsClientImpl.getUserCommsClient(new RpcDispatcher(userChannel, null, null, userCommsServer), address);
  }

  // Create a user communications client to the first server we encounter
  public UserCommsClientImpl createUserCommsClient() throws Exception {
    View view = userChannel.getView();
    for (Address address : view.getMembers()) {
      if (!address.equals(userChannel.getAddress())) {
        UserCommsClientImpl userCommsClient = UserCommsClientImpl.getUserCommsClient(
            new RpcDispatcher(userChannel, null, null, userCommsServer), address);
        if (userCommsClient.isServer()) {
          return userCommsClient;
        }
      }
    }
    throw new Exception("No repository nodes were found to fulfill request");
  }
}
