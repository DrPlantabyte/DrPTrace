#!/usr/bin/python3

import os
from os import path
import shutil

import pyb_util as util

def _java_command(command, args_list, temp_dir):
	command_args = ' '.join(map(util.safe_quote_string, args_list))
	print()
	print('+'+'-'*78+'+')
	print('|  '+str(path.basename(command)).upper()+' '*(76-len(command)) + '|')
	print('+'+'-'*78+'+')
	print(command, command_args)
	print(' '*10+'-'*60+' '*10)
	args_file = path.join(temp_dir, str(path.basename(command))+'-args.txt')
	util.make_parent_dir(args_file)
	with open(args_file, 'w') as fout:
		fout.write(command_args)
	util.shell_command([command, '@%s' % args_file])
	print('\n%s success!\n' % path.basename(command)) 
	
def compile_module(
	module_name, 
	source_dir,
	output_dir,
	temp_dir,
	resource_dir=None,
	module_dependencies = [],
	javac_exec='javac'
):
	#modular compile example: javac -d out --module-source-path cchall.jmodtest=alt_src_style/cchall.jmodtest/src --module cchall.jmodtest
	util.make_dir(temp_dir)
	util.make_dir(output_dir)
	javac_args = []
	javac_args += ['-d', output_dir]
	if module_dependencies != None and len(module_dependencies) > 0:
		javac_args += ['--module-path', os.pathsep.join(module_dependencies)]
	javac_args += ['--module-source-path', '%s=%s' % (module_name, source_dir)]
	javac_args += ['--module', module_name]
	_java_command(
		command=javac_exec,
		args_list=javac_args,
		temp_dir=temp_dir
	)
	if resource_dir != None:
		util.copy_tree(
			file_list=util.list_files(resource_dir), 
			src_root=resource_dir, 
			dest_root=path.join(output_dir, module_name)
		)
def jlink_module(
	module_name,
	module_locations,
	output_dir,
	temp_dir,
	main_class = None,
	jlink_exec='jlink'
):
	# modular jlink example: jlink --output deploy/windows --module-path jmods/windows:out --add-modules cchall.jmodtest
	util.make_dir(temp_dir)
	util.make_parent_dir(output_dir)
	if path.exists(output_dir):
		# jlink forbids using an already existing dir for some reason
		shutil.rmtree(output_dir)
	jlink_args = []
	jlink_args += ['--output', output_dir]
	jlink_args += ['--module-path', module_name]
	jlink_args += ['--module-path', os.pathsep.join(module_locations)]
	jlink_args += ['--add-modules', module_name]
	if main_class != None:
		jlink_args += ['--launcher', 'launch=%s/%s' % (module_name, main_class)]
	_java_command(
		command=jlink_exec,
		args_list=jlink_args,
		temp_dir=temp_dir
	)
def jar_module(
	module_name,
	module_version,
	compile_dir,
	output_dir,
	temp_dir,
	main_class = None,
	jar_exec='jar'
):
	# modular jar example: 
	## jar --create --file out/jar/cchall.noise.jar -C out/compile/cchall.noise .
	## jar --create --file out/jar/cchall.noisetests.jar --main-class cchall.noisetests.Test1 -C out/compile/cchall.noisetests .
	## java --module-path out/jar/cchall.noise.jar:out/jar/cchall.noisetests.jar --module cchall.noisetests
	util.make_dir(temp_dir)
	util.make_dir(output_dir)
	if module_version != None:
		filename='%s_V%s.jar'%(module_name, module_version)
	else:
		filename='%s.jar'%(module_name)
	jar_args = ['--create']
	jar_args += ['--file', path.join(output_dir, filename)]
	if main_class != None:
		jar_args += ['--main-class', main_class]
	jar_args += ['-C', compile_dir, '.']
	_java_command(
		command=jar_exec,
		args_list=jar_args,
		temp_dir=temp_dir
	)
def to_package_name(text):
	"""
	to_package_name(text)
	
	strips out all invalid characters and converts to lower-case, replacing slashes with periods
	"""
	t = text.strip().lower().replace('/','.').replace('\\','.').replace('-','.')
	clean = ""
	clean_chars = 'QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm1234567890_.'
	number_chars = '1234567890.'
	for i in range(0, len(t)):
		c = t[i]
		if c in clean_chars:
			clean = clean + c
	if len(clean) == 0:
		clean = 'xyz'
	if clean[0] in number_chars:
		clean = '_'+clean
	return clean
		

