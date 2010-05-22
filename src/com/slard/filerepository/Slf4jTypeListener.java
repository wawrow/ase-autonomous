package com.slard.filerepository;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import org.slf4j.Logger;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 21-May-2010
 * Time: 13:34:53
 * To change this template use File | Settings | File Templates.
 */
public class Slf4jTypeListener implements TypeListener {
  public <T> void hear(TypeLiteral<T> typeLiteral, TypeEncounter<T> typeEncounter) {
    for (Field field : typeLiteral.getRawType().getDeclaredFields()) {
      if (field.getType() == Logger.class
          && field.isAnnotationPresent(InjectLogger.class)) {
        typeEncounter.register(new Slf4jMembersInjector<T>(field));
      }
    }
  }
}
