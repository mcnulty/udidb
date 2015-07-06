#!/usr/bin/python

import requests
import sys
import json

if len(sys.argv) != 2:
    print "Usage: script /path/to/executable"
    sys.exit(1)

data = dict()
data["execPath"] = sys.argv[1]
data["args"] = []

resp = requests.post("http://localhost:8888/debuggeeContexts", data=json.dumps(data), headers={ "Content-Type" : "application/json"})
print(str(resp.text))
