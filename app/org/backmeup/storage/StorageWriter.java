package org.backmeup.storage;

import java.io.File;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;


import play.Play;

public abstract class StorageWriter {
	
	private static Class<? extends StorageWriter> clazz = null;
	
	public static StorageWriter configuredRuntimeInstance() {
		try {
			if (clazz == null)
				clazz = 
					Class.forName(Play.configuration.getProperty("backmeup.storage.reader.impl") + 
							"Writer")
					.asSubclass(StorageWriter.class);			
			
			return clazz.newInstance();
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}
	
	public abstract void open(String path) throws StorageException;
	
	public abstract void addFile(InputStream is, String path) throws StorageException;
			
	public abstract void close() throws StorageException;
	
}
