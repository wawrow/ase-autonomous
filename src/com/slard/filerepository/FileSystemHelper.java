package com.slard.filerepository;

import com.google.inject.ImplementedBy;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 21-May-2010
 * Time: 10:58:29
 * To change this template use File | Settings | File Templates.
 */
@ImplementedBy(FileSystemHelperImpl.class)
public interface FileSystemHelper {
  byte[] readFile(String name) throws IOException;

  byte[] readFile(File file) throws IOException;

  void writeFile(String name, byte[] fileContents) throws IOException;

  void writeFile(File file, byte[] fileContents) throws IOException;

  boolean delete(String name);

  boolean exists(String name);

  boolean rename(String from, String to);

  boolean canRead(String name);

  void mkdirs();

  List<String> list();

  /**
   * Created by IntelliJ IDEA.
   * User: kbrady
   * Date: 21-May-2010
   * Time: 13:05:58
   * To change this template use File | Settings | File Templates.
   */
  interface FileSystemHelperFactory {
    FileSystemHelper create(File directory);
  }
}
