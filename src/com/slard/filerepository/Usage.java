package com.slard.filerepository;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: kbrady
 * Date: 16-May-2010
 * Time: 23:18:40
 * To change this template use File | Settings | File Templates.
 */
public class Usage implements Serializable {
  private String hostname;
  private long free;
  private long total;

  public Usage(String hostname, long free, long total) {
    this.hostname = hostname;
    this.free = free;
    this.total = total;
  }

  public String getHostname() {
    return hostname;
  }

  public long getFree() {
    return free;
  }

  public long getTotal() {
    return total;
  }
}
