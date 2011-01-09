package io.loadtest.threads;

import io.loadtest.crawler.Crawler;
import io.loadtest.util.DashBoard;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;
import java.util.Locale;


public class MonitorThread extends Thread {

  private static Log log = LogFactory.getLog(MainSchedulerThread.class);
  private static final String FILENAME = "monitor.log";
  private BufferedWriter out;

  /** to be able to print records list every 10 output interval **/
  private static volatile int recordsCounter = 0;

  private long interval;

  public MonitorThread(long interval) {

    this.interval = interval;

    try {
      //-- Purge old file and initialize new.
      this.out = new BufferedWriter(new FileWriter(FILENAME));
      this.out.write("Monitor started at: " + Crawler.getCurrentTime() + "\n");
      this.out.close();
      
      this.out = new BufferedWriter(new FileWriter(FILENAME, true));
    }
    catch (IOException e) {
      log.error(" Could not initialize monitor log");
    }

  }

  public void run() {

    try {

      while (!Crawler.stopCrawler) {

        try {
          this.out.write(Crawler.getCurrentTime() + this.displayMonitorInfo());
          this.out.flush();
        }
        catch (IOException e) {
          log.error(" Could not append to monitor log");
        }

        try {
          Thread.sleep(this.interval);
        }
        catch (InterruptedException ex) {
          log.warn("TaskMonitor interrupted");
        }
      }

      this.out.close();

    }
    catch (IOException ex1) {
      log.error(" Could not close monitor log");
    }
  }

  private String displayMonitorInfo() {
    long elapsedSeconds = 0L;
    try {
      elapsedSeconds = Math.round( (float)(System.nanoTime() -
                                    Crawler.getStartTime()) / 1000.0 / 1000000.0);
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
    float minutes = (float)(elapsedSeconds/60.0);
    StringBuilder sb = new StringBuilder();
    Formatter formatter = new Formatter(sb, Locale.US);    
    String display = " ====== Elapsed: " + formatter.format("%.2f", minutes) + " mins =====";
    long numProcessed = Crawler.fetchedCounter;
    try {
      double totalSecondsFetching = totalTimeSpentFetching / 1000.0;
      //double speed = (float)Math.round((float)totalSecondsFetching/(float)numProcessed * 100.0)/100.0;
      double speed = (float)Math.round((float)numProcessed/(float)elapsedSeconds * 100.0)/100.0;
      double pageLoad = (float)Math.round((float)totalSecondsFetching/(float)numProcessed * 10000.0)/10.0;
      display += "\nAverage Speed: " + speed + " pages/second fetched ";
      //display += "\nTime Fetching: " + totalTimeSpentFetching + " elapsed: " + elapsedSeconds;
      display += "\nAverage Page-load length: " + pageLoad + "ms ";
      display += "\nCurrent Speed: " + getCurrentSpeed() + " pages/second fetched ";
      display += "\nActive Threads: " + Crawler.numOfActiveThreads;
    }
    catch (Exception ex1) {
    }

    recordsCounter++;
    if (recordsCounter == 20 ) {
      display += "\n ------------------ RECORD URLs ---------------------------\n ";
      display += DashBoard.print();
      display += "\n ---------------------------------------------------------- ";
      recordsCounter=0;
    }

    return display + "\n";
  }



  /** used to calculate current speed **/
  private static long startTimeCS = 0;

  /** used to calculate current speed **/
  private static long endTimeCS = 0;

  /** used to calculate current speed **/
  private static float speedCS = 0;

  /** used to calculate current speed **/
  private static final long PERIOD_CS = 3;

  /**
   * Used to calculate average page load speeds
   */
  public static volatile long totalTimeSpentFetching;

  /**
   * This is kinda more accurate than the average speed of a multihour process.
   *
   * It counts time needed for 3 iterations.
   *
   * @return int
   */
  private static float getCurrentSpeed() {
    return speedCS;
  }

  public static void calculateCurrentSpeed() {
     float speed = 0;

      if ( Crawler.fetchedCounter == 0 ) {
        startTimeCS = System.currentTimeMillis();
      }

      if ( Crawler.fetchedCounter % PERIOD_CS == 0 && Crawler.fetchedCounter >0  ) {

        endTimeCS = System.currentTimeMillis();

          speed = (float) PERIOD_CS / (float) ( endTimeCS - startTimeCS )*1000;
          speed = (float) Math.round( speed * 100.0 ) / 100;

          startTimeCS = endTimeCS;

        speedCS = (float)speed;
      }

  }

}
