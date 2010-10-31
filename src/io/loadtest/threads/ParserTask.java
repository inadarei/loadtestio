package io.loadtest.threads;

import io.loadtest.crawler.Crawler;
import io.loadtest.crawler.UrlFetcher;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Iterator;
import java.util.Set;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */
public class ParserTask extends Thread {

  private static Log log = LogFactory.getLog(ParserTask.class);

  private String urlString;
  private String resultedHTML;

  public ParserTask( String urlString, String resultedHTML) {
    this.urlString = urlString;
    this.resultedHTML = resultedHTML;
  }

  public void run() {

    Set urls = UrlFetcher.parse(urlString, resultedHTML);
    if (urls == null) {
      log.debug("Parse result is null (no links from this page to another one) for url " + urlString);
      return;
    }
    log.debug("Found " + urls.size() + " URLs when processing " + urlString);
    Iterator it = urls.iterator();
    while (it.hasNext()) {
      String currUrl = (String) it.next();
      try {
        Crawler.rawURLs.add(currUrl);
      } catch (Exception ex) {
        log.error("Could not mark: " + currUrl + " as fetched.", ex);        
      }
    }
    
  }
    
}
