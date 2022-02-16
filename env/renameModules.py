#!/usr/bin/env python3

import sys
import re

finder = re.compile(r"\b{}\b".format(sys.argv[2]))
output = ""
with open(sys.argv[1], "r") as fh:
    for fd in fh:
        mobj = finder.search(fd)
        if (mobj):
            fd = re.sub(r"({})".format(sys.argv[2]), sys.argv[3]+sys.argv[2], fd)
        output += fd

with open(sys.argv[1], "w") as fh:
    fh.write(output)
