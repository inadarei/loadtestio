#!/bin/sh
java -cp "./:loadtestio.jar" -XX:+ForceTimeHighResolution  -Xms512m -Xmx512m -jar loadtestio.jar
