package org.backmeup.jobs;

public enum BackupJobStatus {
	
	SCHEDULED {
	    public String toString() {
	        return "scheduled";
	    }		
	},

	IN_PROGRESS {
	    public String toString() {
	        return "in progress";
	    }
	},
	
	COMPLETED {
	    public String toString() {
	        return "completed";
	    }
	},
	
	FAILED {
	    public String toString() {
	        return "failed";
	    }
	}
	
}
