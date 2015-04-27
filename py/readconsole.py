#!/usr/bin/python

import sys

line = sys.stdin.readline()

while line:
    print line[:-1]
    line = sys.stdin.readline()