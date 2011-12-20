package org.backmeup.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.hadoop.fs.Path;
import org.backmeup.DataObject;
import org.backmeup.JobStatus;
import org.backmeup.datasources.filesystem.FilesystemLikeDatasource;
import org.backmeup.datasources.filesystem.FilesystemURI;
import org.backmeup.storage.hdfs.util.HdfsUtil;

import models.BackupStatus;

import play.Logger;
import play.Play;
import play.jobs.Job;

public class HdfsFilesystemSourceJob extends Job {
	
	private FilesystemLikeDatasource datasource;
	private long taskId;
	private HdfsUtil hdfsutil;
	private Path tempDir;
	private String outputPath;
	
	public HdfsFilesystemSourceJob(FilesystemLikeDatasource datasource, String outputPath) {
		
		Logger.debug("HdfsFilesystemSourceJob: called with outputPath: " + outputPath);
		this.hdfsutil = new HdfsUtil();
		this.datasource = datasource;
		this.outputPath = outputPath;
		
		hdfsutil.open();
		try {
			tempDir = hdfsutil.mkdir(outputPath);
		} catch (IOException e) {
			throw new RuntimeException("Unable to create HDFS path: " + outputPath, e);
		}
		
		if (tempDir==null) throw new RuntimeException("Unable to create HDFS path (path is null): " + outputPath);
		
		/* Create task in DB
		BackupJob task = new BackupJob(datasource.getProfile(), TaskStatus.IN_PROGRESS);
		task.save();
		taskId = task.getId();
		*/
	}
	
	public void doJob() {
		try {
			// Recursively download all files to temp dir
			for (FilesystemURI uri : datasource.list()) {
				downloadFiles(uri);
			}
						
			// Set the task to completed
			BackupStatus t = BackupStatus.findById(taskId);
			t.status = JobStatus.COMPLETED;
			t.save(); 

			// TODO we need a policy to clean out old tasks from the DB
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
    }	
	
	private void downloadFiles(FilesystemURI uri) throws IOException {
		/* String dest = outputPath + uri.getUri().getPath();
		Logger.debug("HdfsFilesystemSourceJob: downlaodFiles: destination path: " + dest);
		
		if (uri.isDirectory()) {
			Logger.debug("It is a directory: " + dest);
			hdfsutil.mkdir(dest);
			
			for (DataObject child : datasource.list(uri)) {
				downloadFiles(child);
			}
		} else {
			InputStream ins = datasource.getFile(uri);
			hdfsutil.addFile(ins, dest);
		//}
		*/
	}

}

