#!/usr/bin/env bash
javac -cp ./lib/javax.json-1.0.jar:./lib/javax.json-api-1.0.jar ./src/VenmoGraph.java
java -cp ./lib/javax.json-1.0.jar:./lib/javax.json-api-1.0.jar:./src VenmoGraph
