#!/usr/bin/python3

import urllib.request
import zipfile
import os
from os import path

import pyb_util as util
import pyb_java as java
import config

os.chdir(config.root_dir)

def download_from_url(url_path, file_path):
	print('Downloading %s to %s ...' % (url_path, file_path))
	urllib.request.urlretrieve(url_path, file_path)
	print('... Download complete.')
def unzip_file(zipfile_path, target_dir):
	print('Unzipping %s to %s ...' % (zipfile_path, target_dir))
	with zipfile.ZipFile(zipfile_path, 'r') as zip_ref:
		zip_ref.extractall(target_dir)
	print('... Unzip complete.')
def install_jfx_jmods(download_url, target_os_arch):
	print('\tInstalling JavaFX jmods for platform %s ...' % target_os_arch)
	zip_file = path.join(config.temp_dir,'javafx-11-0-2-jmods-%s.zip' % target_os_arch)
	dest_dir = config.native_jmod_dep_dir.replace(config.this_os_arch, target_os_arch)
	util.make_parent_dir(zip_file)
	download_from_url(download_url, zip_file)
	unzip_file(zip_file, config.temp_dir)
	util.make_dir(dest_dir)
	util.copy_files( 
		util.list_files_by_extension(
			path.join(config.temp_dir,'javafx-jmods-11.0.2'),'.jmod'
		), 
		dest_dir 
	)
	print('\t... Installation of %s JavaFX jmods complete.' % target_os_arch)
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
		controller_class_path = path.join(config.module_dir, m, config.sources_dirname, package_path, 'MainViewController.java')
		fxml_resource = path.join(config.module_dir, m, config.resources_dirname, package_path, 'MainView.fxml')
		bundle_resource = path.join(config.module_dir, m, config.resources_dirname, package_path, 'language_en.properties')
		util.make_parent_dir(main_class_path)
		util.make_parent_dir(controller_class_path)
		util.make_parent_dir(bundle_resource)
		util.make_parent_dir(fxml_resource)
		with open(main_class_path, 'w') as fout:
			fout.write("""package %(package)s;

public class %(class)s extends javafx.application.Application{
	public static void main(String[] args){
		System.out.println("Starting...");
		launch();
		System.out.println("...Finished!");
	}
	
	@Override
	public void start(javafx.stage.Stage stage) throws Exception {
		stage.setTitle("App");
		var loader = new javafx.fxml.FXMLLoader(getClass().getResource("MainView.fxml"));
		loader.setResources(
			java.util.ResourceBundle.getBundle(
				getClass().getPackage().getName()+".language", java.util.Locale.getDefault()
		));
		javafx.scene.Parent root = loader.load();
		var controller = loader.getController();
		var scene  = new javafx.scene.Scene(root,800,600);
		stage.setScene(scene);
		stage.show();
	}
}""" % {'package': package_name, 'class': path.basename(main_class_path).replace('.java','')})
		module_info_path = path.join(config.module_dir, m, config.sources_dirname, 'module-info.java')
		with open(module_info_path, 'w') as fout:
			fout.write("""module %(module)s {
	exports %(package)s;
	
""" % {'module':m,'package':package_name})
			for other_mod in prev_modules:
				fout.write('\trequires %s;\n' % other_mod);
			fout.write("""
	
	requires javafx.base;
	requires javafx.graphics;
	requires javafx.controls;
	requires javafx.fxml;
	opens %(package)s to javafx.base, javafx.graphics, javafx.controls, javafx.fxml;
}""" % {'package':package_name})
		with open(controller_class_path, 'w') as fout:
			fout.write("""package %(package)s;

import javafx.scene.control.*;
import javafx.fxml.FXML;

public class %(class)s implements javafx.fxml.Initializable {
	private java.util.ResourceBundle bundle;
	@FXML private Label label1;
	@FXML private void sayGoodBye(){
		label1.setText(bundle.getString("bye"));
	}
	@Override public void initialize(java.net.URL url, java.util.ResourceBundle resources) {
		bundle = resources;
	} 
}"""%{'package':package_name, 'class':'MainViewController'})
		with open(fxml_resource, 'w') as fout:
			fout.write("""<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.layout.VBox?>
<?import javafx.scene.control.Label?>
<?import javafx.scene.control.Button?>

<VBox xmlns:fx="http://javafx.com/fxml" fx:controller="___PACKAGENAME___.MainViewController">
    <children>
        <Label text="%hello" fx:id="label1"/>
        <Button text="%click" onAction="#sayGoodBye"/>
    </children>
</VBox>
""".replace('___PACKAGENAME___',package_name))
		with open(bundle_resource, 'w') as fout:
			fout.write("""hello=Hello!
bye=Goodbye!
click=Click me.""")
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


#
install_jfx_jmods('https://gluonhq.com/download/javafx-11-0-2-jmods-windows/', 'windows-x64')
install_jfx_jmods('https://gluonhq.com/download/javafx-11-0-2-jmods-linux/', 'linux-x64')
install_jfx_jmods('https://gluonhq.com/download/javafx-11-0-2-jmods-mac/', 'osx-x64')




