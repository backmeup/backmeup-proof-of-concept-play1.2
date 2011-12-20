package controllers;

import java.util.List;

import play.mvc.Controller;

import models.DatasinkProfile;
import models.DatasourceProfile;
import models.User;

public class DatasinkProfiles extends Controller {

	public static void list() {
		User user = Security.connectedUser();
		List<DatasinkProfile> profiles = user.listDatasinkProfiles();
		render(user, profiles);
	}
	
	public static void createNew() {
		User user = Security.connectedUser();
		render(user);
	}
	
}
