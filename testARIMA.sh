#!/bin/sh


mvn clean; mvn assembly:assembly;

rm -rf logs/*

java -classpath target/arima-1.0-SNAPSHOT-jar-with-dependencies.jar org.alidata.odps.udf.arima.ARIMATest;


