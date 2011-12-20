package org.backmeup.storage;

public class StorageException extends Exception {
	
	public StorageException(String msg) {
		super(msg);
	}
	
	public StorageException(Throwable t) {
		super(t);
	}

}
