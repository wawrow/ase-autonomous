package com.slard.filerepository;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.List;

/**
 * The Interface UserOperations.
 */
public interface UserOperations {

  /**
   * Checks if node is server.
   *
   * @return the boolean
   */
  Boolean isServer();

  /**
   * Gets the file names.
   *
   * @return the file names
   */
  List<String> getFileNames();

  /**
   * Store a data object.
   *
   * @param dataObject the data object
   * @return true, if successful
   */
  Boolean store(DataObject dataObject);

  /**
   * Retrieve a data object.
   *
   * @param name the name
   * @return the data object
   */
  DataObject retrieve(String name);

  /**
   * Replace a data object.
   *
   * @param dataObject the data object
   * @return true, if successful
   */
  boolean replace(DataObject dataObject);

  /**
   * Deletes a file by name.
   *
   * @param name the name
   * @return true, if successful
   */
  boolean delete(String name);

  /**
   * Class that will wrap up capacity state for a node.
   */
  class DiskSpace implements Serializable {
    public final long free;
    public final long total;
    public final String hostname;

    DiskSpace(String hostname, long free, long total) {
      this.hostname = hostname;
      this.free = free;
      this.total = total;
    }

    public String toString() {
      return new StringBuilder(hostname)
          .append(": ").append(NumberFormat.getNumberInstance().format(free / 1000000))
          .append(" GB of ").append(NumberFormat.getNumberInstance().format(total / 1000000))
          .append(" GB").toString();
    }
  }

  /**
   * Find the free and total disk space of a node (or the cluster).
   *
   * @return disk usage and hostname of node.
   */
  DiskSpace getDiskSpace();
}

