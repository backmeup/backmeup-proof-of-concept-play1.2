package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.backmeup.JobStatus;
import org.hibernate.annotations.ManyToAny;

import play.db.jpa.Model;

/**
 * A BackupStatus is a basic 'metadata' object for a currently running
 * (or recently finished) Workflow execution.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at.>
 */
@Entity
public class BackupStatus extends Model {

	@ManyToOne
	public Workflow workflow;
	
	public JobStatus status;
	
	public String message;
	
	public String location;
	
	public BackupStatus(Workflow workflow, JobStatus status) {
		this.workflow = workflow;
		this.status = status;
	}
	
}
