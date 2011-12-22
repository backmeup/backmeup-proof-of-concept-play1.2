package org.backmeup.connectors;

public class DatasourceException extends Exception {
	
	public DatasourceException(String msg) {
		super(msg);
	}
	
	public DatasourceException(Throwable t) {
		super(t);
	}
	
}
