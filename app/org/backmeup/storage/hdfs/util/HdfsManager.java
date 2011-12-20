package org.backmeup.storage.hdfs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class HdfsManager {
	
	private static HdfsManager instance = null;
	
	public static final String HADOOP_CORESITE_KEY_NAME = "coresite.path";
	public static final String HADOOP_HDFSSITE_KEY_NAME = "hdfssite.path";
	public static final String HADOOP_MAPREDSITE_KEY_NAME = "mapredsite.path";
	private FileSystem fileSystem;
	private Configuration config;
	
	private HdfsManager() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("hdfs.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: hdfs.properties not found");

		try {
			properties.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load hdfs.properties: " + e.getMessage());
		}
		
		String sitePath = properties.getProperty(HADOOP_CORESITE_KEY_NAME);
		String hdfsPath = properties.getProperty(HADOOP_HDFSSITE_KEY_NAME);
		String mapredPath = properties.getProperty(HADOOP_MAPREDSITE_KEY_NAME);

		config = new Configuration();
		config.addResource(new Path(sitePath));
		config.addResource(new Path(hdfsPath));
		config.addResource(new Path(mapredPath));
		
		try {
			fileSystem = FileSystem.get(config);
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not mount HDFS: " + e.getMessage());
		}
	}
	
	public FileSystem getFileSystem() {
		return fileSystem;
	}
	
	public Configuration getConfig() {
		return config;
	}
	
	public static HdfsManager getInstance() {
		if (instance == null)
			instance = new HdfsManager();
		
		return instance;
	}

}
