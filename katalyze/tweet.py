#!/usr/bin/python3

import pprint
import re
import requests
import sys
import json
from pprint import pprint
import subprocess


r = requests.get('http://analyst.ida.liu.se:8099/AllNotifications', verify=False)
assert r.status_code == 200, "Invalid response: " + str(r.status_code)

data = r.json()

minid = -1
if len(sys.argv) == 2:
    minid = int(sys.argv[1])

for notification in data:
    if notification['importance'] != 1:
        continue
    if int(notification['id']) < minid:
        continue

    tweet = str(notification['message'])
    if 'team' in notification:
        tweet += " #t" + str(notification['team'])

    tweet += str(" #NWERC")
    
    subprocess.call(['echo', tweet])
