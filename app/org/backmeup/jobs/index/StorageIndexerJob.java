package org.backmeup.jobs.index;

import java.io.IOException;

import org.backmeup.index.StorageIndexer;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageReader;

import play.Logger;
import play.jobs.Job;

public class StorageIndexerJob extends Job {
	
	private StorageReader storage;
	
	public StorageIndexerJob(StorageReader storage) {
		this.storage = storage;
	}
	
	public void doJob() {
		try {
			StorageIndexer indexer = new StorageIndexer(storage);
			indexer.run();
		} catch (Throwable t) {
			// TODO Jobs need to report their status in the database somehow
			Logger.error(t.getMessage());
		} 
	}

}
