package org.backmeup.storage;

import java.util.Iterator;

import models.DatasinkProfile;

import org.backmeup.DataObject;
import org.backmeup.datasinks.Datasink;

import play.Play;

public abstract class StorageReader {
	
	private static Class<? extends StorageReader> clazz = null;
	
	public static StorageReader configuredRuntimeInstance() {
		try {
			if (clazz == null)
				clazz = 
					Class.forName(Play.configuration.getProperty("backmeup.storage.reader.impl") + 
							"Reader")
					.asSubclass(StorageReader.class);			
			
			return clazz.newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public abstract void open(String path) throws StorageException;
	
	public abstract Iterator<DataObject> getDataObjects() throws StorageException;
			
	public abstract void close() throws StorageException;
}
