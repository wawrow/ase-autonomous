package com.slard.filerepository;

import org.jgroups.View;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.Console;
import java.util.*;

public class FileRepositoryClient {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private static final String PROMPT = "> ";
  private Map<String, FileRepositoryClientCommand> aliasMap = new HashMap<String, FileRepositoryClientCommand>();
  private UserCommsClient commsClient;

  public static void main(String[] args) {
    new FileRepositoryClient().start();
  }

  public void start() {
    SLF4JBridgeHandler.install();
    commsClient = new UserCommsClient();
    for (FileRepositoryClientCommand cmd : FileRepositoryClientCommand.values()) {
      aliasMap.put(cmd.name(), cmd);
      for (String alias : cmd.aliases()) {
        aliasMap.put(alias.toUpperCase(), cmd);
      }
    }
    try {
      System.out.println("File Repository Client Starting");
      System.setProperty("jgroups.udp.mcast_port", UserCommsInterface.CLIENT_PORT);

      // If we're the first then no good - no cluster
      View initialView = commsClient.getChannel().getView();
      if (initialView.size() == 1) {
        System.out.println("Warning, no file repository nodes detected");
      }
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
          final FileRepositoryClientCommand command = aliasMap.get(commandName);
          if (command == null) {
            throw new IllegalArgumentException("unknown command " + commandName);
          }
          List<String> params = new LinkedList<String>();
          while (scanner.hasNext()) {
            params.add(scanner.next());
          }
          long start = System.nanoTime();

          command.exec(console, params, commsClient, new FileRepositoryClientCommand.Listener() {
            @Override
            public void exception(Exception e) {
              System.out.printf("Command error [%1$s]: [%2$s]%n", command, e.getMessage());
              logger.warn("exec threw exception ", e);
            }
          });
          System.out.printf("Elapsed time %,.3f ms%n", (System.nanoTime() - start) / 1000000.0);
        } catch (IllegalArgumentException e) {
          System.out.printf("Unknown command [%1$s]%n", commandName);
          logger.warn("exec threw exception ", e);
        }
      }
      scanner.close();
    }
  }
}
