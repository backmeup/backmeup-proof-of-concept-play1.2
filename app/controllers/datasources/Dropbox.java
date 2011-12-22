package controllers.datasources;

import java.util.Arrays;
import java.util.List;

import models.DatasourceProfile;
import models.ProfileProperty;
import models.User;

import org.backmeup.connectors.impl.dropbox.DropboxDatasource;
import org.backmeup.connectors.impl.dropbox.DropboxHelper;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.RequestTokenPair;
import com.dropbox.client2.session.WebAuthSession;
import com.dropbox.client2.session.WebAuthSession.WebAuthInfo;

import controllers.Secure;
import controllers.Security;

import play.Play;
import play.mvc.Controller;
import play.mvc.Http.Request;
import play.mvc.With;
import play.mvc.Scope.Flash;
import play.mvc.Scope.Session;

@With(Secure.class)
public class Dropbox extends Controller {
	
	private static String FLASH_SCOPE_KEY = "id";
	
	public static void authorize() {
		User user = Security.connectedUser();

		try {
			WebAuthInfo authInfo = DropboxHelper.getInstance().getWebAuthSession().getAuthInfo();
			RequestTokenPair rtp = authInfo.requestTokenPair;
			
			// Store auth session info in DB
			DatasourceProfile p = 
				user.addDatasourceProfile("", "org.backmeup.datasources.filesystem.dropbox.DropboxDatasource");
			p.setProperty(DropboxDatasource.PROPERTY_TOKEN, rtp.key);
			p.setProperty(DropboxDatasource.PROPERTY_SECRET, rtp.secret);
			
			flash.put(FLASH_SCOPE_KEY, p.getId());
			
			String url = 
				authInfo.url + "&oauth_callback=" +
				Request.current.get().getBase() +
				"/datasources/new/dropbox/auth";
			
			redirect(url);
		} catch (DropboxException e) {
			// TODO proper error handling in GUI
			e.printStackTrace();
		}
	}
	
	public static void postAuthorize() throws DropboxException {	
		// Retrieve auth info from DB
		DatasourceProfile profile = DatasourceProfile.findById(Long.parseLong(flash.get(FLASH_SCOPE_KEY)));		
		ProfileProperty token = profile.getProperty(DropboxDatasource.PROPERTY_TOKEN);
		ProfileProperty secret = profile.getProperty(DropboxDatasource.PROPERTY_SECRET);
		
		WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();
		session.setAccessTokenPair(new AccessTokenPair(token.propertyValue, secret.propertyValue));
		session.retrieveWebAccessToken(new RequestTokenPair(token.propertyValue, secret.propertyValue));
		
		// Update access token in DB
		AccessTokenPair atp = session.getAccessTokenPair();
		profile.setProperty(DropboxDatasource.PROPERTY_TOKEN, atp.key);
		profile.setProperty(DropboxDatasource.PROPERTY_SECRET, atp.secret);
		
		DropboxDatasource dropbox = new DropboxDatasource(profile);
		profile.name = "Dropbox [" + dropbox.getAccountInfo().displayName + "]";
		profile.save();
		
		redirect("DatasourceProfiles.list");
	}
	
}
