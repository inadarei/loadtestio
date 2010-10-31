package io.loadtest.config;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * Author: Irakli Nadareishvili. Copyright (C) 2010.
 */
public class Settings {

  public static final boolean CRAWL_ALLOWED = true;
  public static final boolean CRAWL_DENIED = false;

  private double interval;
  private double hitsPerSecond;
  
  private int monitorInterval;
  private int connectionTimeout;
  private Map headers;
  private Set crawlUrls;

  private boolean crawlPermission;
  private boolean autoGeneratePatterns;


  private Set urlPatterns;
  private Set urlPatternsCompiled;

  public Settings() {

    // crawlPermission = Settings.CRAWL_DENIED;

    this.headers = new HashMap();
    this.urlPatterns = new HashSet();
    this.urlPatternsCompiled = Collections.synchronizedSet(new HashSet());
    this.crawlUrls = new HashSet();

  }

  public boolean getCrawlPermission() {
    return crawlPermission;
  }

  public void setCrawlPermission( boolean permission ) {
    crawlPermission = permission;
  }
  
  public boolean isAutoGeneratePatterns() {
    return autoGeneratePatterns;
  }

  public void setAutoGeneratePatterns(boolean autoGeneratePatterns) {
    this.autoGeneratePatterns = autoGeneratePatterns;
  }

  public void addCrawlUrl(ParamMapEntry url) {
    this.crawlUrls.add(url.getKey());
  }

  public void addHeader(ParamMapEntry entry) {
    this.headers.put(entry.getKey(), entry.getValue());
  }

  public void addUrlPattern(ParamMapEntry pattern) {
    this.urlPatterns.add(pattern.getKey());
  }

  public int getConnectionTimeout() {
    return connectionTimeout;
  }

  public Set getCrawlUrls() {
    return crawlUrls;
  }

  public Map getHeaders() {
    return headers;
  }

  public double getInterval() {
    return interval;
  }

  public double getHitsPerSecond() {
    return hitsPerSecond;
  }  

  public int getMonitorInterval() {
    return monitorInterval;
  }

  public Set getUrlPatterns() {
    return urlPatterns;
  }

  public Set getUrlPatternsCompiled() {
    return urlPatternsCompiled;
  }

  public void setConnectionTimeout(int connectionTimeout) {
    this.connectionTimeout = connectionTimeout;
  }

  public void setCrawlUrls(Set crawlUrls) {
    this.crawlUrls = crawlUrls;
  }

  public void setHeaders(Map headers) {
    this.headers = headers;
  }

  public void setHitsPerSecond(double hitsPerSecond) {
    this.hitsPerSecond = hitsPerSecond;
    this.interval = 1000.0/hitsPerSecond;
  }

  public void setMonitorInterval(int interval) {
    this.monitorInterval = interval;
  }

  public void setUrlPatterns(Set urlPatterns) {
    this.urlPatterns = urlPatterns;
  }
}