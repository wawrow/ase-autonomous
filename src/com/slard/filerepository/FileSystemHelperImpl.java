package com.slard.filerepository;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.slf4j.Logger;

import java.io.*;
import java.util.Arrays;
import java.util.List;

public class FileSystemHelperImpl implements FileSystemHelper {
  @InjectLogger
  Logger logger;
  private final File storeLocation;

  @Inject
  public FileSystemHelperImpl(@Assisted File storeLocation) {
    this.storeLocation = storeLocation;
  }

  @Override
  public byte[] readFile(String name) throws IOException {
    return readFile(new File(storeLocation, name));
  }

  @Override
  public byte[] readFile(File file) throws IOException {
    byte[] fileContents = new byte[(int) file.length()];
    FileInputStream fis = new FileInputStream(file);
    try {
      BufferedInputStream bis = new BufferedInputStream(fis);
      try {
        bis.read(fileContents, 0, fileContents.length);
        return fileContents;
      } finally {
        bis.close();
      }
    } finally {
      fis.close();
    }
  }


  @Override
  public void writeFile(String name, byte[] fileContents) throws IOException {
    writeFile(new File(storeLocation, name), fileContents);
  }

  @Override
  public void writeFile(File file, byte[] fileContents) throws IOException {
    logger.trace("writing file {} of length {}", file.getName(), fileContents.length);
    FileOutputStream fos = new FileOutputStream(file);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    try {
      bos.write(fileContents);
    } finally {
      bos.flush();
      bos.close();
    }
  }

  @Override
  public boolean delete(String name) {
    return new File(storeLocation, name).delete();
  }

  @Override
  public boolean exists(String name) {
    return new File(storeLocation, name).exists();
  }

  @Override
  public boolean canRead(String name) {
    return new File(storeLocation, name).canRead();
  }

  @Override
  public void mkdirs() {
    storeLocation.mkdirs();
  }

  @Override
  public List<String> list() {
    return Arrays.asList(storeLocation.list());
  }

  @Override
  public boolean rename(String from, String to) {
    File fromFile = new File(storeLocation, from);
    File toFile = new File(storeLocation, to);
    return fromFile.renameTo(toFile);
  }
}
