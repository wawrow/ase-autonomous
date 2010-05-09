package com.slard.filerepository;

public class DataObjectExistsException extends java.lang.Exception {
  private static final long serialVersionUID = 1L;

  public DataObjectExistsException(String errorText) {
      super(errorText);
    }
}
