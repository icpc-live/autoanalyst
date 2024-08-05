#!/bin/bash

# run!
$(dirname $0)/build/install/katalyzer/bin/katalyzer -config local.yaml $@

# wait until input
read x
