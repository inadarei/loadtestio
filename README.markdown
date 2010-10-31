## Installation

LoadTest.io is ideal for load-tests that target breadth. You give the app
some initial URLs and it will crawl the rest of your website, auto-discovering
links as it goes. It will also try to not repeat already-tested URLs and reach
maximum number of unique URLs in the shortest time possible.

Due to this characteristic, LoadTest.io is a very good tool for testing websites
that employ caching. Such websites can not adequately be tested by stress-testing
tools that only hit limited number of URLs.

## Installation

 * Properly install Jakarta Ant on your system.
 * Run "ant dist" or "ant build" from console
 * cd dist
 * Edit conf/crawlerconfig.xml to customize load-test for your URLs
   and configuration settings.
   (This file has inline comments so you should not have a problem
   to figure-out how things work).

## Running
 * ./run.sh or run.bat (for Unix or Windows). On Unix you probably will
    need to make the shell script executable by: "chmod u+x run.sh"
 * Watch the console output and "tail -f monitor.log" for status.
   Program can be stopped, at any time, by pressing "Enter" or Ctrl-C in
   the console window.
