package org.backmeup.storage.hdfs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.backmeup.DataObject;
import org.backmeup.storage.StorageWriter;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.hdfs.util.HdfsManager;

public class HdfsStorageWriter extends StorageWriter {
	
	private String outputPath;
	private SequenceFile.Writer output;

	@Override
	public void open(String path) throws StorageException {
		this.outputPath = path;
		try {
			this.output = openOutputFile();
		} catch (Exception e) {
			throw new StorageException(e);
		}
		
	}

	@Override
	public void addFile(InputStream is, String path)
			throws StorageException {
        Text key = new Text(path);
		byte[] data;
		try {
			data = IOUtils.toByteArray(is);
	        BytesWritable value = new BytesWritable(data);
	        output.append(key, value);
		} catch (IOException e) {
			throw new StorageException(e);
		}
	}

	@Override
	public void close() throws StorageException {
		try {
			output.close();
		} catch (IOException e) {
			throw new StorageException(e);
		}
		
	}

	private SequenceFile.Writer openOutputFile() throws Exception {
		Path thePath = new Path(outputPath);
		HdfsManager manager = HdfsManager.getInstance();
		return SequenceFile.createWriter(manager.getFileSystem(), manager
				.getConfig(), thePath, Text.class, BytesWritable.class,
				SequenceFile.CompressionType.BLOCK);
	}
}
