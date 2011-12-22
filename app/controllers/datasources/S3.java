package controllers.datasources;

import java.util.ArrayList;
import java.util.List;

import org.backmeup.connectors.impl.s3.S3Datasource;
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
public class S3 extends Controller {

	public static void authorize() {
		
		render();
	}
	
	public static void postAuthorize(@Required String pName, @Required String awsAccessKey, @Required String awsSecretKey, String awsTopBucket) {	
		
		User user = Security.connectedUser();
		String awsFilter = "";
		
		if(validation.hasErrors()) {
			params.flash(); // add http parameters to the flash scope
		    validation.keep(); // keep the errors for the next request
		    authorize();
		}
		
		int i = awsTopBucket.indexOf("/");
		if (i>0) {
			awsFilter = awsTopBucket.substring(i);
			awsTopBucket = awsTopBucket.substring(0,i);
		}
		
		if (S3Datasource.validateCredentials(awsAccessKey, awsSecretKey, awsTopBucket)) {
			
			// Store auth session info in DB
			DatasourceProfile profile = 
				user.addDatasourceProfile("s3", "org.backmeup.datasources.filesystem.s3.S3Datasource");
			profile.setProperty(S3Datasource.AWS_ACCESS_KEY_PROPERTY_NAME, awsAccessKey);
			profile.setProperty(S3Datasource.AWS_SECRET_KEY_PROPERTY_NAME, awsSecretKey);
			profile.setProperty(S3Datasource.AWS_BUCKET_PROPERTY_NAME, awsTopBucket);
			profile.setProperty(S3Datasource.AWS_FILTER_PROPERTY_NAME, awsFilter);
			
			profile.name = pName;
			profile.save();
			
		} else {
			//Error condition, bad credentials
			validation.addError("Credential Failure", "Unable to connect to an S3 bucket of that name with these credentials.", "");
			params.flash(); // add http parameters to the flash scope
		    validation.keep(); // keep the errors for the next request
		    authorize();
		}		
		
		redirect("DatasourceProfiles.list");
	}
	
}
