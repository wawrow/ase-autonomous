package com.slard.filerepository;

import java.io.Serializable;

public interface DataObject extends Serializable {

  byte[] getData();
  String getName();
  Long getCRC();

}
