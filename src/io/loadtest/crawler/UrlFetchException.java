package io.loadtest.crawler;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */


public class UrlFetchException extends Exception {

  public UrlFetchException() {
  }

  public UrlFetchException(String message) {
    super(message);
  }

  public UrlFetchException(String message, Throwable cause) {
    super(message, cause);
  }

  public UrlFetchException(Throwable cause) {
    super(cause);
  }
}
