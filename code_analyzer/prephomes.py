#!/usr/bin/python

import os, sys, inspect

# Include this scripts home directory in the search path.
cmd_folder = os.path.abspath(os.path.dirname(inspect.getfile( inspect.currentframe())))
if cmd_folder not in sys.path:
    sys.path.insert(0, cmd_folder)

from githomes import GitHomes

if __name__ == '__main__':
    gitHomes = GitHomes()
    gitHomes.prep()
    
