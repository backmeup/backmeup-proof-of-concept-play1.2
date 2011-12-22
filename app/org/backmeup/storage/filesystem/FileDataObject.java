package org.backmeup.storage.filesystem;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.backmeup.storage.DataObject;

public class FileDataObject implements DataObject {
	
	private File file;
	
	private String path;
	
	public FileDataObject(File file, String path) {
		this.file = file;
		this.path = path;
	}

	@Override
	public byte[] getBytes() throws IOException {
		return IOUtils.toByteArray(new FileReader(file));
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getProperty(String name) {
		// Metadata properties not supported on filesystem
		return null;
	}
	
	@Override
	public String toString() {
		try {
			return path + " (" + getBytes().length + " bytes)";
		} catch (IOException e) {
			return path + " (corrupt file?)";
		}
	}

	@Override
	public int getLength() {
		// TODO Auto-generated method stub
		return 0;
	}

}
