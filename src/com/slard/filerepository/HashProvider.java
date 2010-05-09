package com.slard.filerepository;

/**
 * The Interface HashProvider.
 */
public interface HashProvider {
  
  /**
   * Returns Hashed key in form of Long.
   *
   * @param key the key
   * @return the long
   */
  Long hash(String key);
}
