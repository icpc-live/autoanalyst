#!/bin/bash

cd katalyze && xterm -e './run_katalyze.sh | tee katalyzer_populate_db.log' &
