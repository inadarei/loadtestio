package io.loadtest.threads;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */

import io.loadtest.Main;
import io.loadtest.crawler.Crawler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Monitor Thread
 */
public class MainSchedulerThread extends Thread {

  private static Log log = LogFactory.getLog(MainSchedulerThread.class);
  private double launchInterval;

  public MainSchedulerThread(double launchInterval) {
    this.launchInterval = launchInterval;
  }

  public void run() {

    //-- parsers should run as frequently as possible
    while (!Crawler.stopCrawler) {
      Crawler.restartIfNeeded();
      Main.retrieverPool.execute(new FetcherTask());
      halt(launchInterval);
    }

  }

  /**
   * Halt execution with more precision than what can be achieved using:
   *
   * Thread.sleep() or scheduleWithFixedDelay();
   *
   * @param millis
   */
  public static void halt(double millis) {
    long start;

    start = System.nanoTime();
    double nanos = millis * 1000000L;
    while (true) {
      if ((double)(System.nanoTime() - start) > nanos) break;
    }
  }
}
