package org.backmeup.storage.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.backmeup.DataObject;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageReader;

import play.Logger;

public class LocalFilesystemStorageReader extends StorageReader {

	private File directory;
	
	@Override
	public void open(String path) {
		this.directory = new File(path);
		if (!this.directory.exists())
			this.directory.mkdir();
	}

	@Override
	public Iterator<DataObject> getDataObjects() throws StorageException {				
		final List<DataObject> flatList = new ArrayList<DataObject>(); 
		addToList(directory, "/", flatList);
		
		return new Iterator<DataObject>() {
			
			private int idx = 0;
			
			@Override
			public boolean hasNext() {
				return idx < flatList.size();
			}

			@Override
			public DataObject next() {
				DataObject obj = flatList.get(idx);
				Logger.info("Retrieving from Storage: " + obj.getPath());
				idx++;
				return obj;
			}

			@Override
			public void remove() {
				// Do nothing				
			}
			
		};
	}
	
	private void addToList(File file, String path, List<DataObject> objects) {
		if (file.isDirectory()) {
			for (File child : file.listFiles()) {
				addToList(child, file.getName(), objects);
			}
		} else {
			objects.add(new FileDataObject(file, path + "/" + file.getName()));
		}
	}

	@Override
	public void close() throws StorageException {
		// TODO Auto-generated method stub
		
	}

}
