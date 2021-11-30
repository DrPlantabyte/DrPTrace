#!/usr/bin/python3

import os
from os import path
import sys

import pyb_util as util
import config

os.chdir(config.root_dir)

util.del_contents(config.temp_dir, exclude=[config.timestamp_cachefile])
util.del_contents(config.build_dir)
util.del_contents(config.run_dir)

# done
print('\nCLEAN SUCCESFUL!\n')
