#!/usr/bin/env python3

import sys
import configparser

config = configparser.ConfigParser()
config.read(sys.argv[1])
with open(sys.argv[2], 'w') as f:
    config.write(f)
