package io.loadtest.config;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */

import io.loadtest.crawler.Crawler;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpState;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 *
 * <p>Non-persistent cookie manager class. Cookies are kept
 * in-memory, only while the Crawler runs.</p>
 *
 */
public class CookieManager {

  private static Log log = LogFactory.getLog(CookieManager.class);

  static {
    cookies = Collections.synchronizedMap(new HashMap());
  }

  /** All cookies. Contains pair: domain - <code>list</code>. Where <code>list</code>
   * is a collection of all cookies for that domain */
  public static Map cookies;

  /** Add a cookie to the cookie manager */
  public static void addCookie(Cookie cookie) {
    synchronized (Crawler.cookieWatch) {
      Collection domainCookies = getDomainCookies(cookie.getDomain());
      if (domainCookies == null) {
        domainCookies = Collections.synchronizedSet(new HashSet());
      }

      domainCookies.add(cookie);

      cookies.put(cookie.getDomain(), domainCookies);
    }
    log.debug("Registering cookie " + cookie + " for domain " + cookie.getDomain());
  }

  /** Return all cookies for a particular domain */
  private static Collection getDomainCookies(String domain) {
    return (Collection) cookies.get(domain);
  }

  public static HttpState getHttpState(String urlString) {

    HttpState httpState = new HttpState();

    // We agreed that cookie domains always begin with a dot.
    String domain = null;
    try {
      domain = new URL(urlString).getHost();

      Collection domainCookies;
      Cookie curCookie;

      synchronized (Crawler.cookieWatch) {
        domainCookies = getDomainCookies(domain);
      }
      if (domainCookies != null) {
        Iterator it = domainCookies.iterator();
        while (it.hasNext()) {
          curCookie = (Cookie) it.next();
          httpState.addCookie(curCookie);
        }
      }

      httpState.setCookiePolicy(CookiePolicy.RFC2109);
    }
    catch (MalformedURLException ex) {
      log.warn("Could not get domain for URL:" + urlString);
    }
    log.debug("HTTP State for URL: " + urlString + "(" + domain + ") is :" + httpState.toString());
    return httpState;
  }

  public static void printAllCookies() {
    synchronized (Crawler.cookieWatch) {
      Set set = cookies.keySet();

      if (set != null) {
        Iterator it = set.iterator();

        while (it.hasNext()) {
          String key = (String) it.next();
          log.debug("Domain: " + key);

          Collection list = (Collection) cookies.get(key);
          Cookie cookie;

          if (list != null) {
            Iterator it2 = list.iterator();
            while (it2.hasNext()) {
              cookie = (Cookie) (it2.next());
              printCookie(cookie);
            }
          }
        }
      }
    }

  }

  public static void printCookie(Cookie cookie) {
    log.debug("   " +
              " Comment " + cookie.getComment() +
              " Domain " + cookie.getDomain() +
              " Date " + cookie.getExpiryDate() +
              " Name " + cookie.getName() +
              " Value " + cookie.getValue() +
              " Path " + cookie.getPath() +
              " Secure " + cookie.getSecure() +
              " Version " + cookie.getVersion() +
              "");

  }

}
