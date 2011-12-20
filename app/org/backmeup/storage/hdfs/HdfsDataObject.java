package org.backmeup.storage.hdfs;

import java.io.File;
import java.io.IOException;

import org.backmeup.DataObject;

public class HdfsDataObject implements DataObject {
	
	private String sequenceFilePath;
	private String path;
	private int length;
	private byte[] bytes;

	public HdfsDataObject(String seqPath, String filePath, byte[] barray, int length) {
		this.sequenceFilePath = seqPath;
		this.path = filePath;
		this.bytes = barray;
		this.length = length;
	}

	@Override
	public byte[] getBytes() throws IOException {
		// TODO Auto-generated method stub
		return bytes;
	}
	
	@Override
	public int getLength() {
		return length;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String getProperty(String name) {
		// there are no additional metadata properties associated with HDFS objects
		return null;
	}

}
