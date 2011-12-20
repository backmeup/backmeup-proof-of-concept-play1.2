package org.backmeup;

public enum JobStatus {
	
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
