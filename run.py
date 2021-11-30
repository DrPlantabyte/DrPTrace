#!/usr/bin/python3

import os
from os import path
import sys

import pyb_util as util
import config

os.chdir(config.root_dir)

if util.check_changed_files(dir_list=config.module_dir,tmp_file=config.timestamp_cachefile) == True:
	print('file changes detected, re-compiling...')
	util.shell_command([config.python_exec, 'build.py'])

if config.this_os_arch == 'windows-x64':
	launch_file = path.join(config.build_dir,'image', config.this_os_arch, 'bin','launch.bat')
else:
	launch_file = path.join(config.build_dir,'image', config.this_os_arch, 'bin', 'launch')

launch_file = path.relpath(launch_file, config.run_dir)

util.make_dir(config.run_dir)

os.chdir(config.run_dir)

util.shell_command([launch_file]+sys.argv[1:])


# done
print('\nRUN SUCCESFUL!\n')
