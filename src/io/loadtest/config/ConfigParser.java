package io.loadtest.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Authors: George Kvizhinadze, Irakli Nadareishvili.
 */
public class ConfigParser {
  private static Log log = LogFactory.getLog(ConfigParser.class);
  private static Settings settings = null;

  public static Settings getSettings() {
    return settings;
  }

  static {
    try {
      settings = parseConfiguration();
      autoGeneratePatterns();      
      //-- Compile regexpes to improve performance.
      compileUrlPatterns();

    }
    catch (IOException ex) {
      ex.printStackTrace();
    }
    catch (SAXException ex) {
      ex.printStackTrace();
    }

  }

  /**
   *  auto-generate whitelist of patterns from crawl URLs if permission=TRUE.
   */
  private static void autoGeneratePatterns() {
    //-- Do not auto-generate anything if patterns permission = false.
    if (!ConfigParser.getSettings().getCrawlPermission()) {
      return;      
    }
    
    Set urls = ConfigParser.getSettings().getCrawlUrls(); 
    Set urlPatterns = ConfigParser.getSettings().getUrlPatterns();

    if (urls != null) {
      Iterator itUrls  = urls.iterator();

      while (itUrls.hasNext()) {
        try {
          URL url = new URL((String) itUrls.next());
          String host = url.getHost();
          if (host.startsWith("www.")) {
            host = host.substring(4);
          }
          String path = url.getPath();
          String pattern = ".*?" + host.replace(".", "\\.") + path.replace(".", "\\.") + ".*";
          ConfigParser.getSettings().getUrlPatterns().add(pattern);
        } catch (MalformedURLException e) {
          e.printStackTrace();
        }
      }
    }
  }

  /**
   * Create a set of pre-compiled URL patterns to improve performance.
   */
  private static void compileUrlPatterns() {
    //-- Compile regexpes to improve performance.
    Set urlPatterns = ConfigParser.getSettings().getUrlPatterns();
    Iterator itPatterns = urlPatterns.iterator();
    final int flags = Pattern.CASE_INSENSITIVE | Pattern.DOTALL |
            Pattern.MULTILINE | Pattern.UNICODE_CASE | Pattern.CANON_EQ;
    while (itPatterns.hasNext()) {
      String currRegexp = (String) itPatterns.next();
      Pattern pattern = Pattern.compile(currRegexp, flags);
      try {
        ConfigParser.getSettings().getUrlPatternsCompiled().add(pattern);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

  }
  
  private static Settings parseConfiguration() throws SAXException,
          IOException {

    Settings sett;
    String configFileName = "conf" + File.separator +
            "config.xml";
    File configFile = new File(configFileName);

    log.debug("Loading configuration from: " + configFile.getAbsolutePath());

    Digester digester = new Digester();
    digester.clear();
    digester.setValidating(false);
    digester.setUseContextClassLoader(true);

    digester.addObjectCreate("settings", Settings.class);

    digester.addBeanPropertySetter("settings/hitsPerSecond", "hitsPerSecond");
    digester.addBeanPropertySetter("settings/monitorInterval", "monitorInterval");

    digester.addBeanPropertySetter("settings/connectionTimeout", "connectionTimeout");

    digester.addSetProperties("settings/url-patterns", "permission", "crawlPermission");
    digester.addSetProperties("settings/url-patterns", "autoGeneratePatterns", "autoGeneratePatterns");    

    digester.addObjectCreate("settings/crawl-urls/url", ParamMapEntry.class);
    digester.addSetNext("settings/crawl-urls/url", "addCrawlUrl");
    digester.addBeanPropertySetter("settings/crawl-urls/url", "key");

    digester.addObjectCreate("settings/url-patterns/pattern", ParamMapEntry.class);
    digester.addSetNext("settings/url-patterns/pattern", "addUrlPattern");
    digester.addBeanPropertySetter("settings/url-patterns/pattern", "key");

    digester.addObjectCreate("settings/headers/header", ParamMapEntry.class);
    digester.addSetNext("settings/headers/header", "addHeader");
    digester.addSetProperties("settings/headers/header",
            "name", "key");
    digester.addBeanPropertySetter("settings/headers/header",
            "value");

    sett = (Settings) digester.parse(configFile);
    return sett;
  }
}
