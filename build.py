#!/usr/bin/python3

import os
from os import path
import sys

import pyb_java as java
import pyb_util as util
import pyb_maven as maven
import config

os.chdir(config.root_dir)

util.shell_command([config.python_exec, 'clean.py'])

compile_dir = path.join(config.build_dir,'compile')

# maven deps
maven.fetch(
	repositories_list=config.maven_deps, 
	download_dir=config.maven_dep_dir, 
	maven_exec=config.maven_exec, 
	temp_dir=config.temp_dir
)

# module order must be explicit to avoid problems
modules = config.module_list
os.listdir(config.module_dir)

def compile_module(module_name):
	java.compile_module(
		module_name=module_name, 
		source_dir=path.join(config.module_dir, module_name,config.sources_dirname),
		resource_dir=path.join(config.module_dir, module_name,config.resources_dirname),
		module_dependencies = [compile_dir] +
				util.list_files_by_extension(config.jar_dep_dir,'.jar') +
				config.dependency_dirs,
		temp_dir=config.temp_dir,
		output_dir=compile_dir,
		javac_exec=config.javac_exec
	)

for module_name in modules:
	compile_module(module_name)


# jlink
def jlink_compile(target_os_arch):
	java.jlink_module(
		module_name=config.main_module,
		module_locations=[x.replace(config.this_os_arch, target_os_arch) for x in config.dependency_dirs] + 
				[compile_dir] + 
				util.list_files_by_extension(config.dependency_dirs,'.jar'),
		output_dir=path.join(config.build_dir,'image', target_os_arch),
		temp_dir=config.temp_dir,
		main_class = config.main_class,
		jlink_exec=config.jlink_exec
	)
jlink_compile(config.this_os_arch)

# done
util.check_changed_files(dir_list=config.module_dir,tmp_file=config.timestamp_cachefile)
print('\nBUILD SUCCESFUL!\n')
