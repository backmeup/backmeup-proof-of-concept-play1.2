package controllers.datasources;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.connectors.impl.googlestorage.GoogleStorageDatasource;
import org.backmeup.storage.DataObject;

import models.DatasourceProfile;
import models.ProfileProperty;
import models.User;

import controllers.Secure;
import controllers.Security;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.With;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Session;

@With(Secure.class)
public class GoogleStorage extends Controller {

	public static void authorize() {
		
		render();
	}
	
	public static void postAuthorize(@Required String pName, @Required String gsAccessKey, @Required String gsSecretKey, String gsTopBucket) {	
		
		User user = Security.connectedUser();
		String gsFilter = "";
		
		if(validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
		    validation.keep(); // keep the errors for the next request
		    authorize();
		}
		
		int i = gsTopBucket.indexOf("/");
		if (i>0) {
			gsFilter = gsTopBucket.substring(i);
			gsTopBucket = gsTopBucket.substring(0,i);
		}
		
		if (GoogleStorageDatasource.validateCredentials(gsAccessKey, gsSecretKey, gsTopBucket)) {
			
			// Store auth session info in DB
			DatasourceProfile profile = 
				user.addDatasourceProfile("GoogleStorage", "org.backmeup.connectors.impl.googlestorage.GoogleStorageDatasource");
			profile.setProperty(GoogleStorageDatasource.GS_ACCESS_KEY_PROPERTY_NAME, gsAccessKey);
			profile.setProperty(GoogleStorageDatasource.GS_SECRET_KEY_PROPERTY_NAME, gsSecretKey);
			profile.setProperty(GoogleStorageDatasource.GS_BUCKET_PROPERTY_NAME, gsTopBucket);
			profile.setProperty(GoogleStorageDatasource.GS_FILTER_PROPERTY_NAME, gsFilter);
			
			profile.name = pName;
			profile.save();
			
		} else {
			//Error condition, bad credentials
			validation.addError("Credential Failure", "Unable to connect to an GoogleStorage bucket of that name with these credentials.", "");
			params.flash(); // add http parameters to the flash scope
		    validation.keep(); // keep the errors for the next request
		    authorize();
		}		
		
		redirect("DatasourceProfiles.list");
	}
	
}
