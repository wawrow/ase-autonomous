package com.slard.filerepository;

import com.google.inject.ImplementedBy;

/**
 * The Interface HashProvider.
 */
@ImplementedBy(MD5HashProvider.class)
public interface HashProvider {

  /**
   * Returns Hashed key in form of Long.
   *
   * @param key the key
   * @return the long
   */
  Long hash(String key);
}
