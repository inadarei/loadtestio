package io.loadtest.threads;

import io.loadtest.crawler.Crawler;

import java.util.concurrent.ThreadFactory;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */
public class FetcherFactory implements ThreadFactory {

  public Thread newThread(Runnable r) {
    Thread myT = new Thread(r);
    myT.setPriority(Thread.MIN_PRIORITY);

    Crawler.incCounter();

    myT.setName("THREAD#" + Crawler.getCounter() + " CREATED " + Crawler.getCurrentTime());
    myT.setDaemon(true);

    return myT;
  }
  
}
