package org.backmeup.connectors.base;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import models.BackupStatus;
import models.DatasourceProfile;
import models.User;

import org.backmeup.connectors.Datasource;
import org.backmeup.jobs.BackupJobStatus;
import org.backmeup.storage.DataObject;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageWriter;

import play.Logger;

/**
 * An abstract base class for datasources following a filesystem-like paradigm.
 * These datasources are arranged in a hierarchical structure of folders and files. 
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public abstract class FilesystemLikeDatasource extends Datasource {
		
	public FilesystemLikeDatasource(DatasourceProfile profile) {
		super(profile);
	}
	
	public void downloadAll(StorageWriter storage) throws StorageException {
		for (FilesystemURI uri : list()) {
			download(uri, storage);
		}
	}
	
	private void download(FilesystemURI uri, StorageWriter storage) throws StorageException {
		if (uri.isDirectory()) {
			Logger.info("Downloading contents of directory " + uri);
			for (FilesystemURI child : list(uri)) {
				download(child, storage);
			}
		} else {
			Logger.info("Downloading file " + uri);
			InputStream is = getFile(uri);
			if (is == null) {
				Logger.warn("Got a null input stream for " + uri.getUri().getPath().toString());
			} else {
				storage.addFile(is, uri.getUri().getPath().toString());
			}
		}
	}
	
	public List<FilesystemURI> list() {
		return list(null);
	}
	
	public abstract List<FilesystemURI> list(FilesystemURI uri);
	
	public abstract InputStream getFile(FilesystemURI uri);
	
}
