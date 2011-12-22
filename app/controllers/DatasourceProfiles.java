package controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

import org.backmeup.connectors.Datasource;
import org.backmeup.connectors.base.FilesystemLikeDatasource;
import org.backmeup.connectors.impl.dropbox.DropboxDatasource;
import org.backmeup.storage.DataObject;

import com.dropbox.client2.DropboxAPI.Entry;

import models.BackupStatus;
import models.DatasourceProfile;
import models.User;
import models.UserDownload;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class DatasourceProfiles extends Controller {
	
	public static void list() {
		User user = Security.connectedUser();
		List<DatasourceProfile> profiles = user.listDatasourceProfiles();
		render(user, profiles);
	}
	
	public static void createNew() {
		User user = Security.connectedUser();
		render(user);
	}
	
	public static void show(Long id) throws ClassNotFoundException, IllegalArgumentException,
		SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException,
		NoSuchMethodException {
		
		User user = Security.connectedUser();
		
		DatasourceProfile profile = DatasourceProfile.findById(id);
		if (profile != null) {			
			Datasource datasource = Datasource.forProfile(profile);
			render(user, datasource, id);
		}
	}
	
	public static void delete(Long id) {
		DatasourceProfile profile = DatasourceProfile.findById(id);
		profile.delete();
		redirect("DatasourceProfiles.list");
	}
	
	/*
	
	 * TODO this will eventually be removed (downloads et al will be handled by BackupTasks)
	 * @throws IOException 
	 
	public static void startDownload(Long id) throws IOException {
		User user = Security.connectedUser();
		DatasourceProfile profile = DatasourceProfile.findById(id);
		if (profile != null) {
			AbstractDatasource datasource = AbstractDatasource.forProfile(profile);
			String outputFile = user.username + "/" + profile.name + "_" + System.currentTimeMillis() + ".seq";
			// String outputFile = "public/" + profile.name + "_" + System.currentTimeMillis() + ".zip";
			if (datasource instanceof FilesystemLikeDatasource)
				// ((FilesystemLikeDatasource) datasource).startBackup(outputFile);
				((FilesystemLikeDatasource) datasource).startHdfsTransfer(user, outputFile);			
			render(user, id, datasource, outputFile);
		}
	}
	*/
	
}
