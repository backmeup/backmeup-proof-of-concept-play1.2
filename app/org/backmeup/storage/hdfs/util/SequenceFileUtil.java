package org.backmeup.storage.hdfs.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipOutputStream;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;

import play.Play;

public class SequenceFileUtil {
	
	private FileSystem fileSystem;
	private Path thePath;
	private Configuration config;
	private File tempDir;

	/**
	* Allows the instantiation of a SequenceFile.Reader for a given HDFS SequenceFile
	* and the export of the SequenceFile contents to a local zip file or a local directory
	*
	* @param fs An instance of the HDFS Filesystem
	* @param path The HDFS path of the SequenceFile
	* @param conf The configuration of the HDFS instance
	*/
	public SequenceFileUtil(FileSystem fs, Path path, Configuration conf) {
		this.fileSystem = fs;
		this.thePath = path;
		this.config = conf;
		tempDir = new File(Play.configuration.getProperty("backmeup.temp.dir"));
		if (!tempDir.exists())
			tempDir.mkdir();

	}
	
	/**
	* Export the contents of a HDFS SequenceFile to a zip file on the local file system.
	* It is assumed that the SequenceFile is of type <Text, BytesWritable>
	* and that the intended path of the binary file is stored in the Text key.
	*
	* @param path The local filesystem path of the directory to which the zip file should be stored
	* @param filename The name of the zip file
	*
	* @return The file handle of the resulting zip file; null if the action failed
	*/
	public File SequenceToZip(String path, String filename) throws IOException {
		System.out.println("Called SequenceToZip with path: " + path + ", filename: " + filename);
		FileOutputStream fOut = null;
		BufferedOutputStream bOut = null;
		ZipOutputStream zipOut = null;
		File zipFile = null;
		Text key = null;
		SequenceFile.Reader reader = null;
		BytesWritable value = null;
		try {
			zipFile = new File(tempDir, filename);
			zipFile.createNewFile();
			fOut = new FileOutputStream(zipFile);
			bOut = new BufferedOutputStream(fOut);
			zipOut = new ZipOutputStream(bOut);
			reader = new SequenceFile.Reader(fileSystem, thePath, config);
			key = (Text) reader.getKeyClass().newInstance();
			value = (BytesWritable) reader.getValueClass().newInstance();
			while (reader.next(key, value)) {
				File f = new File(key.toString());
				String entryName = f.getName();
				ZipArchiveEntry tarEntry = new ZipArchiveEntry(f, entryName);
				zipOut.putNextEntry(tarEntry);
				zipOut.write(value.getBytes(), 0, value.getLength());
				zipOut.closeEntry();
			}
			reader.close();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			zipOut.flush();
			zipOut.close();
			bOut.close();
			fOut.close();
		}
		
		return zipFile;
	}
	
	/**
	* Export the contents of a HDFS SequenceFile to a directory on the local file system.
	* It is assumed that the SequenceFile is of type <Text, BytesWritable>
	* and that the intended path of the binary file is stored in the Text key.
	*
	* @param dirname The local directory to which the binary files should be stored
	*
	* @return The local root directory of the resulting file tree; null if the action failed
	*/
	public File SequenceToLocal(String dirname){
		System.out.println("Called SequenceLocal with directory name: " + dirname);
		File localDir = new File(tempDir, dirname);
		Text key = null;
		SequenceFile.Reader reader = null;
		BytesWritable value = null;
		try {
			reader = new SequenceFile.Reader(fileSystem, thePath, config);
			key = (Text) reader.getKeyClass().newInstance();
			value = (BytesWritable) reader.getValueClass().newInstance();
			while (reader.next(key, value)) {
				  // perform some operation
				File f = new File(localDir, key.toString());
				String parentDir = f.getParent();
				File dir = new File(parentDir);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(f));
				os.write(value.getBytes(), 0, value.getLength());
				os.flush();
				os.close();
			}
			reader.close();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		return localDir;
	}
		
}