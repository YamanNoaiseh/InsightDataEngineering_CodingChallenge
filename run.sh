#!/usr/bin/env bash
cd src
javac -cp '../lib/javax.json-1.0.jar:../lib/javax.json-api-1.0.jar' VenmoGraph.java
java -cp '.:../lib/javax.json-1.0.jar:../lib/javax.json-api-1.0.jar' VenmoGraph
