package com.slard.filerepository;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.bridge.SLF4JBridgeHandler;

public class FileRepositoryClient {
  public static void main(String[] args) {
    SLF4JBridgeHandler.install();

    Injector injector = Guice.createInjector(new FileRepositoryModule());
    ClientCommandLoop loop = injector.getInstance(ClientCommandLoop.class);
    loop.start();
  }
}
