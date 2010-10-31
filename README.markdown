## What is It?

LoadTest.io is multi-threaded load-tester with auto-discovery. It is ideal for load-tests that 
target breadth. You give loadtest.io some initial URLs and it will crawl the rest of your website,
auto-discovering links as it goes. It will also try to not repeat already-tested URLs and reach
maximum number of unique URLs in the shortest time possible.

Due to this characteristic, LoadTest.io is a very good tool for testing websites
that employ caching. Such websites can not adequately be tested by stress-testing
tools that only hit limited number of URLs.

## Why Do I Care?

The majority of tools currently available do not have auto-discovery feature. Which means they can only test a fixed set of URLs. But any reasonably built web-system has some kind of caching, so after the first hit, any consequent hit to the same URL only tests your cache
not - the web system (application layer, database etc). Such test can be unrealistically 
optimistic and misleading. Real traffic from real users will not just hit 20 hand-picked URLs 
from your web-site.

## Installation

 * Properly install JDK6 and the latest Jakarta Ant on your system.
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

## Analyzes
The output on the screen just displays URLs being currently processed. This output
is only useful to deduce the health of the test run:
 * Are the URLs the ones you expect?
 * Are you getting broken connection attempts?
 * Are there too many redirects?
But it does not tell you much about the system's scalability per se.

## What is being measured?

Since LoadTest.io is a highly CPU-intensive application, running a load itself
requires significant resources. To better utilize your resource, LoadTest.io
only grabs dynamic content (HTML) and tries to avoid static content like images,
css, javascript or videos.

This may sound misleading, but in reality, it is not. Optimizing scalability of static 
content is a well-understood and a solved problem, as such it is much more trivial than 
understanding scalability characteristics of your web system. We deliberately choose 
to concentrate on the hard problem and leave the trivial one alone. 


## The output 

The real "meat" of the application is in its monitor.log output. Every "monitorInterval"
seconds it shows you a snapshot which looks like the following:

<code>
17:09:04::393 ====== Elapsed: 5.42 mins =====
Average Speed: 11.37 pages/second fetched 
Average Page-load: 88.0ms 
Current Speed: 14.15 pages/second fetched 
Active Threads: 400
</code>

Average Speed: is how many page requests per second are served on average.
Average Page Load: is how long it takes on average to return a single request.
Current Speed is similar to Average Speed but calculated for last several requests only.
Active Threads: shows how many threads LoadTest.io had to fire to keep consistent
load. This number has a limit of 400, because too many threads can crash the load-tester
itself.

So, does Average Speed of 11.37 page/second mean that your web system can only
handle 11.37 pages/second? No, it does not, because your web system is not single-threaded
and there's no direct formula to deduce system's capacity from this number. Pretty much,
this number is there only for comparision. If you were getting 10p/s and you optimize
something and you get 20p/s - you know you did something right.

Also, as you can see the number of active threads is at capacity (400). This is bad.
It means that pages are not being processed fast enough by the server, for load-tester
to achieve constant load that you indicated. If we set up configuration file for
20 hits per second and we see high number of active threads - we can be sure that
web system can not handle 20 hits per second.

So that is the best way to find the capacity: start from a small hitsPerSecond number
and make sure the website can handle it for at least 15-20 minutes with low Active Threads
number. Start increasing. If Active Threads keeps at lower than 10 number - you can be
reasonably certain that your system can handle that load. When you find a load-level
at which Active Thread keeps increasing - your web-system is near its capacity.

Happy hunting.
