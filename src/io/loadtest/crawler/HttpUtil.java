package io.loadtest.crawler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.net.MalformedURLException;
import java.net.URL;


public class HttpUtil {

  private static Log log = LogFactory.getLog(UrlFetcher.class);

  public static String getBaseUriFromUrl(String url) throws MalformedURLException {
    URL javaURL = new URL(url);
    String path = javaURL.getPath();
    int index = path.lastIndexOf("/");
    if (index == -1) {
      return "";
    }
    else {
      return path.substring(0, index);
    }
  }

  /**
   * URLs, in anchors, can come in three flavours:
   * <li> Canonical (begining with "http://")
   * <li> Absolute, non-canonical (begining with "/")
   * <li> Relative (not begining with either "http" or "/")
   * @param domain
   * @param baseUrl
   * @param link
   * @return
   */
  public static String canonizeURL(String domain, String baseUrl, String link) {
    link = link.trim();
    String ret = "";

    if (link.startsWith("javascript") || link.startsWith("mailto:")) {
      ret = ""; //Illegal URL
    }
    else if (link.startsWith("http")) {
      ret = link;
    }
    else if (link.startsWith("www.")) {
      ret = "http://"+link;
    }
    else if (link.startsWith("/")) {
      int indx = 0;
      if (domain.endsWith("/")) {
        indx = 1;
      }
      ret = domain.substring(indx) + link;
    }
    else {
      String slash2 = "/";

      if ( ! domain.endsWith("/")) domain = domain + "/";
      if ( baseUrl.startsWith ( "/") ) baseUrl = baseUrl.substring(1);
      if ( link.startsWith ( "/") ) link = link.substring(1);
      if ( baseUrl.equals("")) { slash2="";}
      if (baseUrl.endsWith("/")) { slash2 = "";  }
      if (link.equals("")) { slash2 = "";  }

      //System.out.println( domain + "%1%" + baseUrl + "%3%" +  slash2 + "%4%" +  link );

      ret = domain + baseUrl + slash2 + link;

    }
    return ret;
  }

  public static String getDomainFromUrl(String url) throws
      MalformedURLException {
    URL javaURL = new URL(url);
    return javaURL.getProtocol() + "://" + javaURL.getHost()+(javaURL.getPort()!=-1?":"+javaURL.getPort():"");
  }

}
