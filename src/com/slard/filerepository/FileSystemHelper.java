package com.slard.filerepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class FileSystemHelper {
  private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());
  private final File storeLocation;

  public FileSystemHelper(File storeLocation) {
    this.storeLocation = storeLocation;
  }

  public byte[] readFile(String name) throws IOException {
    return readFile(new File(storeLocation, name));
  }

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


  public void writeFile(String name, byte[] fileContents) throws IOException {
    writeFile(new File(storeLocation, name), fileContents);
  }

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

  public boolean delete(String name) {
    return new File(storeLocation, name).delete();
  }

  public boolean exists(String name) {
    return new File(storeLocation, name).exists();
  }

  public boolean rename(String from, String to) {
    File fromFile = new File(storeLocation, from);
    File toFile = new File(storeLocation, to);
    return fromFile.renameTo(toFile);
  }
}
