package org.backmeup.jobs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.backmeup.connectors.base.FilesystemLikeDatasource;
import org.backmeup.connectors.base.FilesystemURI;
import org.backmeup.storage.DataObject;
import org.backmeup.storage.hdfs.util.HdfsManager;

import models.BackupStatus;
import models.User;

import play.Logger;
import play.Play;
import play.jobs.Job;

public class HdfsSequenceFileJob extends Job {

	private FilesystemLikeDatasource datasource;
	private long taskId;
	private String outputPath;
	private SequenceFile.Writer output;
	private User user;

	public HdfsSequenceFileJob(FilesystemLikeDatasource datasource, User user,
			String outputPath) {

		Logger.debug("HdfsFilesystemSourceJob: called with outputPath: "
				+ outputPath);
		this.datasource = datasource;
		this.outputPath = outputPath;
		this.user = user;

		/* Create task in DB
		BackupJob task = new BackupJob(datasource.getProfile(),
				TaskStatus.IN_PROGRESS);
		task.save();
		taskId = task.getId();
		*/
	}

	public void doJob() {
		output = null;
		try {
			output = openOutputFile();
			// Recursively download all files to temp dir
			for (FilesystemURI uri : datasource.list()) {
				downloadFiles(uri);
			}

			// Set the task to completed
			BackupStatus t = BackupStatus.findById(taskId);
			t.status = BackupJobStatus.COMPLETED;
			t.save();

			// TODO we need a policy to clean out old tasks from the DB
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				user.addDownload(outputPath, new Date(System.currentTimeMillis()));
			}
		}
	}

	private void downloadFiles(FilesystemURI uri) throws IOException {
		/*
		String dest = uri.getUri().getPath();
		Logger
				.debug("HdfsFilesystemSourceJob: downloadFiles: destination path: "
						+ dest);
		if (uri.isDirectory()) {
			Logger.debug("It is a directory: " + dest);
			for (DataObject child : datasource.list(uri)) {
				downloadFiles(child);
			}
		} else {
            Text key = new Text(dest);
			InputStream ins = datasource.getFile(uri);
			byte[] data = IOUtils.toByteArray(ins);
            BytesWritable value = new BytesWritable(data);
            output.append(key, value);
		}
		*/
	}

	private SequenceFile.Writer openOutputFile() throws Exception {
		Path thePath = new Path(outputPath);
		HdfsManager manager = HdfsManager.getInstance();
		return SequenceFile.createWriter(manager.getFileSystem(), manager
				.getConfig(), thePath, Text.class, BytesWritable.class,
				SequenceFile.CompressionType.BLOCK);
	}

}
