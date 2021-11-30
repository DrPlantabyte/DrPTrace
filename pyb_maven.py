#!/usr/bin/python3
from subprocess import call
from os import path
import sys

import pyb_util as util

def error(msg):
	print(str(msg), file=sys.stderr)
	exit(1)

def fetch(repositories_list, download_dir, maven_exec='mvn', temp_dir='tmp'):
	"""
	fetch(repositories_list, download_dir, maven_exec='mvn')
	
	Downloads specified maven dependencies, along with their dependencies, to the 
	specified download_dir.
	
	repositories_list - list of strings in gradle format 'groupId:artifactId:version' 
		(eg 'info.picocli:picocli:4.1.4')
	
	download_dir - folder where to download the maven .jar files
	
	maven_exec - maven command to use for this operation (default = 'mvn')
	
	temp_dir - folder to use for temporarily created files, such as the 
		pom.xml file (default = 'tmp')
	
	"""
	if(type(repositories_list) == str):
		repo_list = [repositories_list]
	else:
		repo_list = repositories_list
	if(len(repo_list) <= 0):
		# no deps, do nothing
		return
	pom_file = path.join(temp_dir, 'mock-pom.xml')
	util.make_parent_dir(pom_file)
	with open(pom_file, 'w') as pout:
		pout.write('<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd"> <modelVersion>4.0.0</modelVersion> <groupId>project</groupId> <artifactId>dependency</artifactId> <version>list</version> \n<dependencies>\n')
		for lib_str in repositories_list:
			tokens = lib_str.strip().split(':')
			if(len(tokens) != 3):
				error('invalid maven dependency string "%s"\nmaven dependencies must be in format of "groupId:artifactId:version"' % lib_str)
				return False
			pout.write(' <dependency>')
			pout.write('  <groupId>%s</groupId>' % tokens[0])
			pout.write('  <artifactId>%s</artifactId>' % tokens[1])
			pout.write('  <version>%s</version>' % tokens[2])
			pout.write(' </dependency>')
		pout.write('</dependencies> </project>')
	print()
	print('Fetching Maven dependencies:\n\t', end='')
	util.make_dir(download_dir)
	print('\n\t'.join(repositories_list))
	util.shell_command([maven_exec, '-f', str(path.abspath(pom_file)), 'dependency:copy-dependencies', '-DoutputDirectory=%s' % path.abspath(download_dir)])
	print('Maven fetch complete')
	print()

