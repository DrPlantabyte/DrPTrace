#!/usr/bin/python3

import sys, os, shutil
from os import path
from urllib.request import pathname2url
import subprocess
from subprocess import call
import sys
import re
import zipfile
import json

import config

os.chdir(config.root_dir)

def check_changed_files(dir_list, tmp_file='temp/filestamp_cache.json'):
	"""
	check_changed_files(dir_list, tmp_file='temp/filestamp_cache.json')
	
	Checks all of the files in the listed folders for changes to the file 
	modification time, saving the cache of timestamps in the given tmp_file
	
	dir_list - a single directory path string or a list of directory paths
	tmp_file - file path to save the timestamps for subsequent check 
	           (file will contain json format text)
	
	return True if any files have been added, deleted, or modified based on 
	the list of timestamps in tmp_file, and False if not changes are detected
	"""
	if not path.exists(tmp_file):
		d={}
		make_parent_dir(tmp_file)
		with open(tmp_file, 'w') as fout:
			json.dump(d,fout, indent=1)
	with open(tmp_file, 'r') as fin:
		stamp_cache = json.load(fin)
	new_cache = {}
	no_deletions = True
	no_additions = True
	no_modifications = True
	if type(dir_list) == str:
		dir_list = [dir_list]
	for dir_path in dir_list:
		file_list = list_files(dir_path)
		for file_path in file_list:
			reg_path = path.abspath(file_path)
			timestamp = path.getmtime(reg_path)
			new_cache[reg_path] = timestamp
			if reg_path in stamp_cache:
				# file in cache, is it modified?
				if stamp_cache[reg_path] != timestamp:
					# modified
					no_modifications = False
					#print('modification detected')
			else:
				# file not in cache
				no_additions = False
				#print('addition found')
	# deletions?
	if len(stamp_cache) != len(new_cache):
		no_deletions = False
		#print('deletion found')
	# save new cache
	with open(tmp_file, 'w') as fout:
		json.dump(new_cache,fout, indent=1)
	#
	return not (no_deletions and no_additions and no_modifications)
		

def shell_command(args_list):
	print(' '.join(args_list))
	ret_code = call(args_list)
	if ret_code != 0:
		print('Error: exit code %s' % ret_code,file=sys.stderr)
		exit(ret_code)

def make_dir(dir_path):
	"""
	make_dir(dir_path)

	creates a directory if it does not already exist, including parent 
	directories.

	dir_path - directory to create
	"""
	if not path.exists(dir_path):
		os.makedirs(dir_path)
def make_parent_dir(file_path):
	"""
	make_parent_dir(file_path)
	
	Creates the parent directory for the specified filepath if it does not 
	already exist.
	
	file_path - path to some file
	"""
	parent_dir = path.dirname(file_path)
	if parent_dir == '': # means parent is working directory
		return
	if not path.isdir(parent_dir):
		os.makedirs(parent_dir)
def _del(filepath):
	"""
	Deletes a file or recursively deletes a directory. Use with caution.
	"""
	if(path.isdir(filepath)):
		for f in os.listdir(filepath):
			_del(path.join(filepath,f))
		os.rmdir(filepath)
	elif(path.exists(filepath)):
		os.remove(filepath)
def del_file(filepath):
	"""
	del_file(filepath):
	
	Deletes a file or recursively deletes a directory. Use with caution.
	
	filepath - path to file or directory to delete
	"""
	if(path.isdir(filepath)):
		for f in os.listdir(filepath):
			_del(path.join(filepath,f))
		os.rmdir(filepath)
	elif(path.exists(filepath)):
		os.remove(filepath)
def del_contents(dirpath, exclude=[]):
	""" 
	del_contents(dirpath, exclude=[])
	
	Recursively deletes the contents of a directory, but not the directory itself
	
	dirpath - path to directory to clean-out
	exclude - specific files to NOT delete
	"""
	if(path.isdir(dirpath)):
		for f in os.listdir(dirpath):
			del_path = path.join(dirpath,f)
			if path.normpath(del_path) in [path.normpath(x) for x in exclude]:
				continue
			del_file(del_path)
def list_files(dirpath):
	"""
	list_filetree(dirpath)
	
	Returns a list of all files inside a directory (recursive scan)
	
	dirpath - filepath or list of file paths of directory(s) to scan
	"""
	if(type(dirpath) == str):
		dir_list = [dirpath]
	else:
		dir_list = dirpath
	file_list = []
	for _dir_ in dir_list:
		for base, directories, files in os.walk(_dir_):
			for f in files:
				file_list.append(path.join(base,f))
	return file_list
def list_files_by_extension(dirpath, extension):
	"""
	list_files_by_extension(dirpath, extension)
	
	Returns a list of all files inside a directory (recursive scan) whose
	filenames end with the provided extension (case sensitive)
	
	dirpath - filepath or list of file paths of directory(s) to scan
	
	extension - filename ending or list of endings
	
	returns a list of files
	
	"""
	if(type(dirpath) == str):
		dir_list = [dirpath]
	else:
		dir_list = dirpath
	if(type(extension) == str):
		ext_list = [extension]
	else:
		ext_list = extension
	file_list = list_files(dir_list)
	for ext in ext_list:
		file_list = [f for f in file_list if str(f).endswith(ext)]
	return file_list
def safe_quote_string(text):
	"""
	safe_quote_string(text)
	
	returns the text in quotes, with escapes for any quotes in the text itself
	
	text - input text to quote
	
	returns: text in quotes with escapes
	"""
	if os.sep != '\\':
		text2 = text.replace('\\', '\\\\')
		text3 = text2.replace('"', '\\"')
	else:
		text3 = text.replace('\\', '/')
		# windows does not allow " in file names anyway
	return '"'+text3+'"'
def copy_files(file_list, dest_dir):
	"""
	copy_files(file_list, dest_dir)
	
	Copies all files to dest_dir
	"""
	make_dir(dest_dir)
	for f in file_list:
		shutil.copy(f, dest_dir)
def copy_tree(file_list, src_root, dest_root):
	"""
	copy_tree(file_list, src_root, dest_root)
	
	Copies all files to directory dest_root (creating it if necessary), 
	preserving the folder structure relative to src_root
	"""
	for f in file_list:
		rel_path = path.relpath(f, src_root)
		dst_path = path.join(dest_root, rel_path)
		make_parent_dir(dst_path)
		shutil.copy(f, dst_path)
def zip_dir(dir_path, zip_path):
	print('\nzipping %s to %s\n' % (dir_path, zip_path))
	with zipfile.ZipFile(zip_path, 'w', zipfile.ZIP_DEFLATED) as zipf:
		# zipf is zipfile handle
		for root, dirs, files in os.walk(dir_path):
			for file in files:
				fname = path.basename(dir_path)
				src_file = path.join(root, file)
				dst_file = path.join(fname, path.relpath(src_file, dir_path) )
				zipf.write(src_file, arcname=dst_file)
	# done

