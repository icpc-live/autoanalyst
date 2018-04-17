#!/bin/bash

# run!
java -jar target/katalyzer-1.9.0.jar -config ../config.yaml $@

# wait until input
read x
