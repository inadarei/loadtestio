package io.loadtest.crawler;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */


import io.loadtest.config.ConfigParser;
import io.loadtest.config.CookieManager;
import io.loadtest.threads.MonitorThread;
import io.loadtest.util.DashBoard;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to fetch an HTML of a page from a given URL.
 */
public class UrlFetcher {

  private static Log log = LogFactory.getLog(UrlFetcher.class);
  
  //Used in parse() for the initial capacity of a set
  private static final int AVERAGE_NUM_OF_LINKS = 200;
  private static Pattern pattern;
  public static MultiThreadedHttpConnectionManager connectionManager;

  static {
    final int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL |
        Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.CANON_EQ;

    // Match groups 1 and 3 are just for debugging, we are only interested in group 2nd.
    //String regexp = "<a.*?\\shref\\s*=\\s*([\\\"\\']*)(.*?)([\\\"\\'\\s]).*?>";
    String regexp =
        "<a.*?\\shref\\s*=\\s*([\\\"\\']*)(.*?)([\\\"\\'\\s].*?>|>)";

    log.debug("Regular Expression to catch all anchors: " + regexp);
    UrlFetcher.pattern = Pattern.compile(regexp, flags);

    //-- Reusable connection manager.
    connectionManager = new MultiThreadedHttpConnectionManager();
  }

  /**
   * Method fetching a url and returning HTML output.
   *
   * @param urlString String
   * @throws NullPointerException
   * @return String
   */
  public static String fetch(String urlString) throws UrlFetchException {

    log.debug("Fetching URL " + urlString);

    Crawler.getUrls().add( urlString );

    String content = "";

    // Prepare HTTP client instance
    HttpClient httpclient = new HttpClient(connectionManager);
    httpclient.setConnectionTimeout(ConfigParser.getSettings().getConnectionTimeout());
    httpclient.setState(CookieManager.getHttpState(urlString));

    // Prepare HTTP GET method
    GetMethod httpget = null;
    try {
      httpget = new GetMethod(urlString);
    }
    catch (IllegalArgumentException ex1) {
      throw new UrlFetchException();
    }

    Map headers = ConfigParser.getSettings().getHeaders();
    if (headers != null) {
      Set headerKeys = headers.keySet();
      if (headerKeys != null) {
        Iterator itHeaders = headerKeys.iterator();
        String key, value;

        while (itHeaders.hasNext()) {
          key = (String) itHeaders.next();
          value = (String) headers.get(key);
          //log.debug( key + "=" + value);
          httpget.addRequestHeader(key, value);
        }
      }
    }

    // Execute HTTP GET
    int result = 0;
    try {

      long startTime = System.nanoTime();
      result = httpclient.executeMethod(httpget);

      // Save the cookies
      Cookie[] cookies = httpclient.getState().getCookies();
      synchronized(CookieManager.cookies) {
        for (int i = 0; i < cookies.length; i++) {
          CookieManager.addCookie(cookies[i]);
        }
      }

      String redirectLocation = null;
      Header locationHeader = httpget.getResponseHeader("location");
      if (locationHeader != null) {
        redirectLocation = locationHeader.getValue();
        log.info("-------- Redirect Location: " + redirectLocation);

        if (redirectLocation != null) {
          // Perform Redirect!
          content = fetch(redirectLocation);
        }
        else {
          // The response is invalid and did not provide the new location for
          // the resource.  Report an error or possibly handle the response
          // like a 404 Not Found error.
          log.error("Error redirecting");
        }

      } else {
        content = httpget.getResponseBodyAsString();
      }
      long elapsed = System.nanoTime() - startTime;
      DashBoard.add( urlString, elapsed / 1000000L);

      MonitorThread.calculateCurrentSpeed();

      // Redirects do not count towards fetched urls!!!
      if (redirectLocation != null) {
        Crawler.fetchedCounter++;
        log.info("FETCHED " + Crawler.fetchedCounter + "th URL: " + urlString);
      }

      //log.debug ( "Response code: " + result );

      //CookieManager.printAllCookies();

    }
    catch (Exception ex) {
      throw (new UrlFetchException(ex));
    }
    finally {
      // Release current connection to the connection pool once you are done
      httpget.releaseConnection();
    }

    /*log.debug( " C O O K I E S: " );
    CookieManager.printAllCookies(); */
    return content;

  }

  /**
   * Method parsing HTML and returning a set of links found in there.
   *
   * @param url String needed to compute the absolute pathes from the relative pathes in HTML.
   * @param html String
   * @return java.util.Set or null if no URLs found after parse.
   * @todo In the current implementation HTML FRAME-based sites are not parsed.
   */

  public static Set parse(String url, String html) {

    Set anchors = new HashSet(UrlFetcher.AVERAGE_NUM_OF_LINKS);

    Matcher matcher = UrlFetcher.pattern.matcher(html);

    // Debug code
    //matcher.find();
    //  for ( int i=0; i<=matcher.groupCount(); i++) {
    //    log.debug("#"+matcher.group(i)+"#");
    //  }

    String domain = null;
    try {
      domain = HttpUtil.getDomainFromUrl(url);
    }
    catch (MalformedURLException ex) {
      // We can not parse URL that is malformed.
      return new HashSet();
    }

    String baseURI = null;
    try {
      baseURI = HttpUtil.getBaseUriFromUrl(url);
    }
    catch (MalformedURLException ex1) {
      // We can not parse URL that is malformed.
      return new HashSet();
    }

    String currentUrl = "";
    boolean wrongURL = false;

    while (matcher.find()) {

/*      System.out.println("----------");
      System.out.print( domain + "   " );
      System.out.print( baseURI + "   " );
      System.out.println( matcher.group(2));      */

      currentUrl = HttpUtil.canonizeURL(domain, baseURI, matcher.group(2));

      wrongURL = false;
      try {
        URL javaCurrentURL = new URL(currentUrl);
      }
      catch (MalformedURLException ex2) {
        wrongURL = true;
      }

      //log.debug (  currentUrl );

        if (Crawler.crawlOrNot(currentUrl) == true && wrongURL == false) {
            anchors.add(currentUrl);
        }
    }

    return anchors;
  }

}
