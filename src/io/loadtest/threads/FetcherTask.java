package io.loadtest.threads;

import io.loadtest.Main;
import io.loadtest.crawler.Crawler;
import io.loadtest.crawler.UrlFetchException;
import io.loadtest.crawler.UrlFetcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */
public class FetcherTask extends Thread {

  private static Log log = LogFactory.getLog(FetcherTask.class);

  public FetcherTask() {
  }

  public void run() {
    log.debug("Starting FetcherTask");

    if (Crawler.rawURLs.isEmpty()) return;
    if (Crawler.stopCrawler) return;
    
    String urlString = (String) Crawler.rawURLs.remove(0);

    if (urlString == null) {
      log.warn("URL is null");
      return;
    }

    //-- Store just hashcodes of already-processed URLs to minimize memory footprint.
    String urlHashCode = this.hashcode(urlString);
    //String urlHashCode = urlString;
    synchronized(Crawler.rawURLs) {
      if (Crawler.getUrls().contains(urlHashCode)) {
        return;
      }
    }
    Crawler.getUrls().add(urlHashCode);

    Crawler.numOfActiveThreads++;
    log.debug("Begin processing URL: " + urlString);

    String resultedHTML = null;
    try {
      resultedHTML = UrlFetcher.fetch(urlString);
    }
    catch (UrlFetchException ex1) {
      log.warn("Could not fetch URL: " + urlString + " \n" + ex1.getMessage());
    }

    if (resultedHTML != null) {
      Main.parserPool.execute(new ParserTask(urlString, resultedHTML));
    } else {
      log.warn("resultedHTML is null for url " + urlString);
    }

    log.debug("FetcherTask Completed");
    Crawler.numOfActiveThreads--;
  }

  /**
   * Compute hash code of a String. Currently using md5, but could be changed to something
   * that ensures more collision resistance, if necessary.
   */
  private String hashcode(String s) {
    MessageDigest m = null;
    try {
      m = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    m.update(s.getBytes(), 0, s.length());
    return new BigInteger(1, m.digest()).toString(16);
  }
}
