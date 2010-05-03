package com.slard.filerepository;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileSystemHelper {
  
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

  public void writeFile(File file, byte[] fileContents) throws IOException {
    FileOutputStream fos = new FileOutputStream(file);
    BufferedOutputStream bos = new BufferedOutputStream(fos);
    try {
      bos.write(fileContents);
    } finally {
      bos.flush();
      bos.close();
    }
  }

}
