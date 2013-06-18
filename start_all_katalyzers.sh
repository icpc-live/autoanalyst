#!/bin/bash

cd katalyze && xterm -e './run_katalyze.sh -port 8079 populateDatabase | tee katalyzer_populate_db.log' &
cd katalyze && xterm -e './run_katalyze.sh -port 22222 tweet | tee katalyzer_tweet_db.log' &
#cd katalyze && xterm -e './run_katalyze.sh createGraphs | tee katalyzer_create_graphs.log' &
