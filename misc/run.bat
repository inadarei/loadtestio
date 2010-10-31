@echo off
java -cp ".\" -jar  -XX:+ForceTimeHighResolution  -Xms512m -Xmx1024m  loadtestio.jar
