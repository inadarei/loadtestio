package io.loadtest;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */

import io.loadtest.config.ConfigParser;
import io.loadtest.config.Settings;
import io.loadtest.crawler.Crawler;
import io.loadtest.crawler.UrlFetcher;
import io.loadtest.threads.MainSchedulerThread;
import io.loadtest.threads.MonitorThread;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Main Class of the application
 */
public class Main {

  private static Log log = LogFactory.getLog(Main.class);

  final public static int FETCHER_THREAD_POOL_SIZE = 400;
  final public static int PARSER_THREAD_POOL_SIZE = 200;

  //public static ScheduledThreadPoolExecutor fetcherPool;
  public static ThreadPoolExecutor retrieverPool;
  public static ThreadPoolExecutor parserPool;

  public static void main(String[] args) {

    Settings setts = ConfigParser.getSettings();
    testConfigparser();

    boolean isAnythingToCrawl = loadInitialUrls();

    if (!isAnythingToCrawl) {
      log.error("No starting URLs given to begin crawling from ");
      return;
    }

    /** My (or Java's?) failed attempt to achieve constant execution intervals :(

     ThreadFactory fetcherFactory = new FetcherFactory();
     fetcherPool = new ScheduledThreadPoolExecutor(FETCHER_THREAD_POOL_SIZE, fetcherFactory);
     Runnable fetcherTask = new FetcherTask();

     fetcherPool.scheduleWithFixedDelay(new FetcherTask(), 0, launchInterval, TimeUnit.MILLISECONDS);
     */

    double launchInterval = ConfigParser.getSettings().getInterval();

    parserPool = new ThreadPoolExecutor(PARSER_THREAD_POOL_SIZE,
            PARSER_THREAD_POOL_SIZE * 2,
            500L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue());

    retrieverPool = new ThreadPoolExecutor(FETCHER_THREAD_POOL_SIZE,
            FETCHER_THREAD_POOL_SIZE * 2,
            500L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue());

    Crawler.setStartTime(System.nanoTime());
    
    MainSchedulerThread scheduler = new MainSchedulerThread(launchInterval);
    scheduler.setDaemon(false);
    scheduler.setPriority(Thread.MAX_PRIORITY);
    scheduler.start();

    MonitorThread monitor = new MonitorThread(ConfigParser.getSettings().getMonitorInterval());
    monitor.setDaemon(false);
    monitor.setPriority(Thread.MIN_PRIORITY);
    monitor.start();

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
      while (true) {
        int c = br.read();
        if (c == 10) {
          log.info(" Program Stopped by user. ");
          Crawler.stopCrawler = true;

          //-- Shutdown connection pool
          UrlFetcher.connectionManager.shutdown();
          return;
        }
      }
    }
    catch (IOException ex) {
    }

  }

  public static boolean loadInitialUrls() {
    Set urls = ConfigParser.getSettings().getCrawlUrls();
    if (urls != null) {
      Iterator it = urls.iterator();
      while (it.hasNext()) {
        String currURL = (String) it.next();
        Crawler.rawURLs.add(currURL);
      }
    } else {
      return false;
    }

    return true;
  }

  /**
   * Debug method to test if the config parser works properly
   */
  private static void testConfigparser() {
    log.info("======= CONFIGURATION: ======");
    log.info("HTTP Connection Timout: " +
            ConfigParser.getSettings().getConnectionTimeout());
    log.info("Hits per second: " + ConfigParser.getSettings().getHitsPerSecond());
    log.info("Monitor Interval: " +
            ConfigParser.getSettings().getMonitorInterval());
    log.info("------- HTTP Headers ----- ");
    printMap(ConfigParser.getSettings().getHeaders());
    log.info("------- Crawl URLS ------- ");
    printSet(ConfigParser.getSettings().getCrawlUrls());

    String permWord = "Banned";
    if (ConfigParser.getSettings().getCrawlPermission()) {
      permWord = "Allowed";
    }

    if (ConfigParser.getSettings().getCrawlPermission()) {
      log.info("----------------------------------------");
      log.info("ATTENTION: Allowed Patterns were augmented by an auto-generated list from Crawl URLs!");
    }
    log.info("------- " + permWord + " URL Patterns ------- ");
    printSet(ConfigParser.getSettings().getUrlPatterns());
    log.info("===== END CONFIGURATION =====");

  }

  public static void printSet(Set set) {
    if (set != null) {
      Iterator it = set.iterator();
      while (it.hasNext()) {
        String element = (String) it.next();
        log.info("   " + element);
      }
    }
  }

  public static void printMap(Map map) {
    if (map != null) {
      Set keyset = map.keySet();
      if (keyset != null) {
        Iterator it = keyset.iterator();
        String key, value;

        while (it.hasNext()) {
          key = (String) it.next();
          value = (String) map.get(key);
          log.info("   " + key + " = " + value);
        }
      }
    }
  }

}