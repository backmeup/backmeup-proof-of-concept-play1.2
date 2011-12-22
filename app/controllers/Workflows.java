package controllers;

import java.util.List;

import org.backmeup.connectors.Datasink;
import org.backmeup.connectors.Datasource;
import org.backmeup.connectors.base.FilesystemLikeDatasource;

import models.BackupStatus;
import models.DatasinkProfile;
import models.DatasourceProfile;
import models.User;
import models.UserDownload;
import models.Workflow;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class Workflows extends Controller {
	
	public static void list(Long source, Long sink) {
		User user = Security.connectedUser();
		List<Workflow> workflows;
		
		DatasourceProfile sourceProfile = null;
		DatasinkProfile sinkProfile = null;
		
		if (source == null && sink == null) {
			workflows = user.listWorkflows();
		} else if (source != null) {
			sourceProfile = DatasourceProfile.findById(source);
			workflows = Workflow.find("byUserAndSourceProfile", user, sourceProfile).fetch();
		} else {
			sinkProfile = DatasinkProfile.findById(sink);
			workflows = Workflow.find("byUserAndSinkProfile", user, sinkProfile).fetch();
		}
		
		render(user, workflows, sourceProfile, sinkProfile);
	}
	
	public static void defineNew() {
		User user = Security.connectedUser();
		List<DatasourceProfile> sources = DatasourceProfile.find("byUser", user).fetch();
		render(user, sources);
	}
	
	public static void saveNew(String name, Long source, Long sink) {
		User user = Security.connectedUser();
		user.addWorkflow(name, source, sink);
		redirect("Workflows.list");
	}
	
	public static void delete(Long id) {
		Workflow workflow = Workflow.findById(id);
		workflow.delete();
		redirect("Workflows.list");
	}
	
	public static void run(Long id) {
		User user = Security.connectedUser();
		Workflow workflow = Workflow.findById(id);
		
		Long jobId = null;
		if (workflow != null)
			jobId = workflow.runNow();
		render(user, workflow, jobId);
	}
	
	public static void queryStatus(Long jobId) {
		BackupStatus status = BackupStatus.findById(jobId);
		if (status != null) {
			renderJSON("{\"status\":\"" + status.status + "\",\"location\":\"" + status.location + "\"}");
		}
		renderJSON(null);
	}

}
