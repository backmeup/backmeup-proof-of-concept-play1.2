package models;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

import org.backmeup.BackupJob;
import org.backmeup.datasinks.Datasink;
import org.backmeup.datasinks.zipfile.ZipFileDatasink;
import org.backmeup.datasources.Datasource;

import play.db.jpa.Model;

/**
 * A Workflow acts as a blueprint for backup jobs. It connects a Datasink with a 
 * Datasource, with optional Processing Actions in between.
 *  
 * TODO Processing Actions are not implemented yet!
 *  
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@Entity
public class Workflow extends Model {
	
	@ManyToOne
	public User user;
	
	public String name;
	
	@ManyToOne
	public DatasourceProfile sourceProfile;
	
	@ManyToOne
	public DatasinkProfile sinkProfile;

	public Workflow(User user, String name, DatasourceProfile sourceProfile, DatasinkProfile sinkProfile) {
		this.user = user;
		this.name = name;
		this.sourceProfile = sourceProfile;
		this.sinkProfile = sinkProfile;
	}
	
	public Long runNow() {
		BackupJob job = new BackupJob(this);
		job.now();
		return job.getJobID();
	}

}
