package org.backmeup;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.backmeup.datasinks.Datasink;
import org.backmeup.datasinks.zipfile.ZipFileDatasink;
import org.backmeup.datasources.Datasource;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageReader;
import org.backmeup.storage.StorageWriter;
import org.backmeup.storage.filesystem.LocalFilesystemStorageReader;
import org.backmeup.storage.filesystem.LocalFilesystemStorageWriter;

import models.BackupStatus;
import models.Workflow;
import play.Logger;
import play.Play;
import play.jobs.Job;
import sun.util.logging.resources.logging;

/**
 * An executable Job based on the 'blueprint' provided in the {@link Workflow} instance.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class BackupJob extends Job {
	
	private Workflow workflow;
	
	private Long jobID;
	
	public BackupJob(Workflow workflow) {
		this.workflow = workflow;
		
		BackupStatus status = new BackupStatus(workflow, JobStatus.SCHEDULED);
		status.save();
		jobID = status.getId();
	}
	
	public Long getJobID() {
		return jobID;
	}
	
	public void doJob() {
		Logger.info("Running workflow " + workflow.id + " NOW - job ID is " + jobID);
		
		Datasource source = Datasource.forProfile(workflow.sourceProfile);
		Datasink sink = null;
		if (workflow.sinkProfile != null)
			sink = Datasink.forProfile(workflow.sinkProfile);
		else
			sink = new ZipFileDatasink("public/" + workflow.name + "_" + System.currentTimeMillis() + ".zip");
		
		try {
			// 1. Retrieve data from the source and store to storage backend

			// TODO make switchable between local FS/HDFS
			String storagePath = Play.configuration.getProperty("backmeup.storage.root.path");
			
			StorageWriter storageWriter = StorageWriter.configuredRuntimeInstance();
			storageWriter.open(storagePath);			
			source.downloadAll(storageWriter);
			storageWriter.close();
			Logger.info("Job " + jobID + ": download from source complete");
	
			// TODO pipe data through processing actions, if any
			
			// 2. Move the data from storage backend to sink
			StorageReader storageReader = StorageReader.configuredRuntimeInstance();
			storageReader.open(storagePath);
			String location = sink.upload(storageReader);
			storageReader.close();
			Logger.info("Job " + jobID + ": upload to sink complete");
			
			BackupStatus t = BackupStatus.findById(jobID);
			t.status = JobStatus.COMPLETED;
			t.location = location;
			t.save(); 
		} catch (Exception e) {
			Logger.error("ERROR! Job " + jobID + " failed: " + e.getClass() + ", " + e.getMessage());
			e.printStackTrace();
			
			BackupStatus status = BackupStatus.findById(jobID);
			status.status = JobStatus.FAILED;
			status.message = e.getMessage();
			status.save();
		}
	}

}
