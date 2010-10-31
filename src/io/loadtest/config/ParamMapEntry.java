package io.loadtest.config;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */
public class ParamMapEntry {
  private String key;
  private String value;

  public ParamMapEntry() { }

  public ParamMapEntry(String key, String value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

}
