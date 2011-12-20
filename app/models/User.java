package models;

import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import controllers.Secure.Security;
import controllers.Workflows;

import play.db.jpa.Model;

/**
 * The user.
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
@Entity
public class User extends Model {

	public String email;

	public String password;

	public String username;

	public User(String email, String password, String username) {
		this.email = email;
		this.password = password;
		this.username = username;
	}

	public static User connect(String username, String password) {
	    return find("byUsernameAndPassword", username, password).first();
	}
	
	/**
	 * Lists this user's stored DatasourceProfiles.
	 * @return the list of DatasourceProfiles
	 */
	public List<DatasourceProfile> listDatasourceProfiles() {
		return DatasourceProfile.find("byUser", this).fetch();
	}
	
	/**
	 * Lists this user's stored DatasinkProfiles.
	 * @return the list of DatasinkProfiles
	 */
	public List<DatasinkProfile> listDatasinkProfiles() {
		return DatasinkProfile.find("byUser", this).fetch();
	}
	
	/**
	 * Lists this user's stored Workflows.
	 * @return the list of Workflows
	 */
	public List<Workflow> listWorkflows() {
		return Workflow.find("byUser", this).fetch();
	}
	
	public DatasourceProfile addDatasourceProfile(String name, String implementationClass) {
		// Remove if exists
		DatasourceProfile old = DatasourceProfile.find("byUserAndName", this, name).first();
		if (old != null)
			old.delete();
		
		DatasourceProfile profile = new DatasourceProfile(this, name, implementationClass);
		profile.save();
		return profile;
	}
	
	public Workflow addWorkflow(String name, Long sourceId, Long sinkId) {
		// Remove if exists
		Workflow old = Workflow.find("byUserAndName", this, name).first();
		if (old != null)
			old.delete();
		
		DatasourceProfile source = DatasourceProfile.findById(sourceId);
		DatasinkProfile sink = DatasinkProfile.findById(sinkId);
		
		if (source != null) {
			Workflow workflow = new Workflow(this, name, source, sink);
			workflow.save();
			return workflow;
		} else {
			// TODO some sort of error handling
			return null;
		}
	}
	
	public BackupStatus getBackupJobsForProfile(Long id) {
		List<BackupStatus> activeTasks = BackupStatus.find("byOwnerAndSourceProfile", this, DatasourceProfile.findById(id)).fetch();
		return activeTasks.get(activeTasks.size() - 1); 
	}

	public List<UserDownload> listDownloads() {
		return UserDownload.find("byOwner", this).fetch();
	}
	
	public UserDownload getDownloadByPath(String path) {
		return UserDownload.find("byOwnerAndPath", this, path).first();
	}
	
	public UserDownload getDownloadById(Long id) {
		return UserDownload.find("byOwnerAndId", this, id).first();
	}
	
	public UserDownload addDownload(String path, Date creationDate) {
		
		UserDownload download = new UserDownload(this, path, creationDate);
		download.save();
		return download;
	}
	
	public BackupStatus getActiveTaskForDownload(Long id) {
		List<BackupStatus> activeTasks = BackupStatus.find("byOwnerAndDownload", this, getDownloadById(id)).fetch();
		return activeTasks.get(activeTasks.size() - 1); 
	}
}
