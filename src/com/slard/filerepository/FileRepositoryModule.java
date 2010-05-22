/*
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 21-May-2010
 * Time: 10:56:57
 */
package com.slard.filerepository;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;
import com.google.inject.assistedinject.FactoryProvider;
import com.google.inject.matcher.Matchers;
import org.jgroups.Address;
import org.jgroups.blocks.Cache;

import java.util.Timer;

public class FileRepositoryModule extends AbstractModule {

  public FileRepositoryModule() {
  }

  protected void configure() {
    bind(new TypeLiteral<ConsistentHashTable.ConsistentHashTableFactory<Address>>() {
    }).toProvider(
        FactoryProvider.newFactory(new TypeLiteral<ConsistentHashTable.ConsistentHashTableFactory<Address>>() {
        },
            new TypeLiteral<ConsistentHashTableImpl<Address>>() {
            }));

    bind(SystemCommsClient.SystemCommsFactory.class).toProvider(
        FactoryProvider.newFactory(SystemCommsClient.SystemCommsFactory.class, SystemComms.class));

    bind(UserCommsInterface.UserCommsFactory.class).toProvider(
        FactoryProvider.newFactory(UserCommsInterface.UserCommsFactory.class, UserCommsServer.class));

    bind(DataObject.DataObjectFactory.class).toProvider(
        FactoryProvider.newFactory(DataObject.DataObjectFactory.class, DataObjectImpl.class));

    bind(FSDataObject.FSDataObjectFactory.class).toProvider(
        FactoryProvider.newFactory(FSDataObject.FSDataObjectFactory.class, FSDataObjectImpl.class));

    bind(FileSystemHelper.FileSystemHelperFactory.class).toProvider(
        FactoryProvider.newFactory(FileSystemHelper.FileSystemHelperFactory.class, FileSystemHelperImpl.class));

    bindListener(Matchers.any(), new Slf4jTypeListener());
  }

  @Provides
  @Singleton
  Cache<String, FSDataObject> provideCache() {
    return new Cache<String, FSDataObject>();
  }

  @Provides
  Timer provideTimer() {
    return new Timer("ReplicaGuardTimer");
  }
}
