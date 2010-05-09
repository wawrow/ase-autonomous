package com.slard.filerepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Logger;

public class MD5HashProvider implements HashProvider {
  private final Logger logger = Logger.getLogger(this.getClass().getName());
  private MessageDigest md5;

  public MD5HashProvider() {
    try {
      this.md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ex) {
    }
  }

  @Override
  public Long hash(String key) {
    byte[] hash;
    synchronized (md5) {
      md5.reset();
      md5.update(key.getBytes());
      hash = md5.digest();
    }
    return bytesToLong(hash);
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
