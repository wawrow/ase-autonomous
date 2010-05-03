package com.slard.filerepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5HashProvider implements HashProvider {

  private MessageDigest md5;

  public MD5HashProvider() {
    try {
      this.md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ex) {
    }
  }

  @Override
  public Long hash(String key) {
    byte[] ret;
    synchronized (md5) {
      md5.reset();
      md5.update(key.getBytes());
      ret = md5.digest();
    }
    return bytesToLong(ret);
  }

  private long bytesToLong(byte[] in) {
    long ret = 0;
    for (int i = 0; i < 8; i++) {
      ret <<= 8;
      ret ^= (long) in[i] & 0xFF;
    }
    return ret;
  }

}
