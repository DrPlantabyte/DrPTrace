#!/usr/bin/python3

import urllib.request
import zipfile
import os
from os import path

import pyb_util as util
import pyb_java as java
import config

os.chdir(config.root_dir)

#
util.make_dir(config.temp_dir)
util.make_dir(config.build_dir)
util.make_dir(config.run_dir)
util.make_dir(config.module_dir)

with open('.gitignore', 'w', newline='\n') as fout:
	fout.write("""__pycache__/*
%s/*
%s/*
%s/*
%s/*
""" % (config.temp_dir, config.build_dir, config.run_dir, config.maven_dep_dir) )
with open('.hgignore', 'w', newline='\n') as fout:
	fout.write("""syntax:glob
__pycache__/*
%s/*
%s/*
%s/*
%s/*
""" % (config.temp_dir, config.build_dir, config.run_dir, config.maven_dep_dir) )

prev_modules = []
for m in config.module_list:
	package_name = java.to_package_name(m)
	package_path = package_name.replace('.','/')
	if m == config.main_module:
		main_class_path = path.join(config.module_dir, m, config.sources_dirname,config.main_class.replace('.','/')+'.java' )
		util.make_parent_dir(main_class_path)
		with open(main_class_path, 'w') as fout:
			fout.write("""package %(package)s;
public class %(class)s {
	public static void main(String[] args){
		System.out.println("Starting...");
		for(int i = 0; i < args.length; i++){
			System.out.println(i+": "+args[i]);
		}
		System.out.println("...Done");
	}
}""" % {'package':package_name, 'class':path.basename(main_class_path).replace('.java','')})
	else:
		sample_class = path.join(config.module_dir, m, config.sources_dirname, package_path, 'Sample.java')
		sample_resource_dir = path.join(config.module_dir, m, config.resources_dirname, package_path)
		util.make_parent_dir(sample_class)
		util.make_dir(sample_resource_dir)
		with open(sample_class, 'w') as fout:
			fout.write("""package %(package)s;
public class %(class)s {}""" % {'package':package_name, 'class':'Sample'})
	module_info_path = path.join(config.module_dir, m, config.sources_dirname, 'module-info.java')
	with open(module_info_path, 'w') as fout:
		fout.write("""module %(module)s {
exports %(package)s;

""" % {'module':m,'package':package_name})
		for other_mod in prev_modules:
			fout.write('\trequires %s;\n' % other_mod);
		fout.write('}')
	prev_modules.append(m)






