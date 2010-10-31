package io.loadtest.crawler;

import io.loadtest.Main;
import io.loadtest.config.ConfigParser;
import io.loadtest.config.Settings;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */
public class Crawler {

  private static Log log = LogFactory.getLog(Crawler.class);

  /**
   * Number of processed URLs. The modulus of this is, actually, used
   * separated by number of threads, to determine which thread should
   * process the current url.
   */
  private static volatile long counter = 0;
  private static long startTime;

  public static volatile boolean stopCrawler = false;
  public static volatile long fetchedCounter = 0;
  public static volatile long numOfActiveThreads = 0;

  /**
   * URLS found by processing HTMLs.
   * crawler threads are getting URLs to process
   * from this FIFO.
   */
  public static List rawURLs = Collections.synchronizedList(new LinkedList());

  /**
   * Set of URLs already parsed. We need to keep track of this so that
   * we do not get into an endless loop due to possible cross-references on sites.
   */
  private static Set processedURLs = Collections.synchronizedSet(new HashSet());

  public static long getCounter() {
    return Crawler.counter;
  }

  /**
   * Increase the counter - number of processed URLs
   */
  public static void incCounter() {
    Crawler.counter++;
  }

  public static Set getUrls() {
    return processedURLs;
  }

  public static long getStartTime() {
    return Crawler.startTime;
  }

  public static void setStartTime(long startTime) {
    Crawler.startTime = startTime;
  }

  public static final Object watch = new Integer(0);
  public static final Object cookieWatch = new Integer(0);


  /**
   * Counts number of iterations for which there are no URLs to process.
   * After ten iterations, crawler will restart. Used by: restartIfNeeded()
   * method.
   */
  private static volatile int idleCounter = 0;

  /**
   * One of the "dangers" of unique crawling is that you will run
   * out of URLs eventually - once all unique ones are crawled.
   * <p/>
   * Once we detect that all unique URLs have been parsed, we try
   * to restart the process.
   */
  public static void restartIfNeeded() {

    if (idleCounter != -1 && Crawler.rawURLs.isEmpty() && Crawler.fetchedCounter > 3) {
      idleCounter++;
    } else {
      idleCounter = 0;
    }

    if (idleCounter > 50) {
      log.info("Crawler Restart requested. \n " +
              "This means all the unique URLs have been " +
              "accessed in this iteration and we are going " +
              "into the next one");

      idleCounter = -1; //-- Do not disturb while we are restarting!
      restartCrawler();
      idleCounter = 0;
    }
  }

  public static void restartCrawler() {
    Crawler.getUrls().clear();
    Main.loadInitialUrls();
  }

  /**
   * Determines if the URL should be crawled down or not.
   *
   * @param url
   * @return
   */
  public static boolean crawlOrNot(String url) {
    boolean flag = false;

//    synchronized (Crawler.watch) {

    //-- Has this URL already been processed? Then do not process.
    if (!Crawler.getUrls().contains(url)) {
      flag = true;
    } else {
      return false;
    }

    //-- Is this URL allowed from the configuration XML file settings?
    boolean matchFlag = false;
    Set urlPatterns = ConfigParser.getSettings().getUrlPatternsCompiled();
    if (urlPatterns != null) {
      Iterator itPatterns = urlPatterns.iterator();
      while (itPatterns.hasNext()) {
        Pattern pattern = (Pattern) itPatterns.next();
        Matcher m = pattern.matcher(url);
        matchFlag = m.matches();

        if (matchFlag) {
          break;
        }

      }

    }

    //-- If permissionMode = Denied, crawl all but that URLS
    if (ConfigParser.getSettings().getCrawlPermission() ==
            Settings.CRAWL_DENIED) {
      if (matchFlag) {
        flag = false;
        log.debug("DENIED URL " + url);
      } else {
        log.debug("NOT DENIED URL " + url);
        flag = true && flag;
      }
    }

    //-- If permissionMode = Allowed, crawl only that URLS
    if (ConfigParser.getSettings().getCrawlPermission() ==
            Settings.CRAWL_ALLOWED) {
      if (matchFlag) {
        flag = true && flag;
        log.debug("ALLOWED URL " + url);
      } else {
        log.debug("NOT ALLOWED URL " + url);
        flag = false;
      }
    }

    //-- enf synch cond  }

    return flag;
  }

  /**
   * Returns current time in a human-readable way with the precision of milliseconds
   *
   * @return
   */
  public static String getCurrentTime() {
    DateFormat sdf = new SimpleDateFormat("HH:mm:ss::SSS");
    Date date = new Date(System.currentTimeMillis());

    return sdf.format(date);
  }

}
