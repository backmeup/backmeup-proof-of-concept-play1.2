package org.backmeup.connectors.base;

import java.net.URI;

public class FilesystemURI {
	
	private URI uri;
	
	private boolean isDirectory;
	
	public FilesystemURI(URI uri, boolean isDirectory) {
		this.uri = uri;
		this.isDirectory = isDirectory;
	}
	
	public URI getUri() {
		return uri;
	}
	
	public boolean isDirectory() {
		return isDirectory;
	}
	
	@Override
	public String toString() {
		return uri.toString();
	}

}
