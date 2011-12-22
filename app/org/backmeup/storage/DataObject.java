package org.backmeup.storage;

import java.io.IOException;
import java.net.URI;

public interface DataObject {

	public byte[] getBytes() throws IOException;

	public int getLength();
	
	public String getPath();
	
	public String getProperty(String name);
	
}
