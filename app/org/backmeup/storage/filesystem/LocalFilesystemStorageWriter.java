package org.backmeup.storage.filesystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.backmeup.storage.DataObject;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageWriter;

import play.Logger;

/**
 * An implementation of {@link Storage} based on a directory on the local file
 * system.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class LocalFilesystemStorageWriter extends StorageWriter {
	
	private File directory;
	
	@Override
	public void open(String path) {
		this.directory = new File(path);
		if (!this.directory.exists())
			this.directory.mkdir();
	}
	
	@Override
	public void addFile(InputStream is, String path) throws StorageException {
		try {
			File out = new File(directory, path);
			out.getParentFile().mkdirs();
			
			OutputStream os = new FileOutputStream(out);
						
			byte buf[] = new byte[1024];
			int len;
			while((len = is.read(buf)) > 0)
				os.write(buf, 0, len);
			
			os.close();
			is.close();
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}
	
	@Override
	public void close() throws StorageException {
		// Do nothing
	}

}
