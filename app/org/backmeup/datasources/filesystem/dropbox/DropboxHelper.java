package org.backmeup.datasources.filesystem.dropbox;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import com.dropbox.client2.session.WebAuthSession;

public class DropboxHelper {
	
	private static DropboxHelper instance = null;
	
	private String appKey;
	
	private String appSecret;
	
	private DropboxHelper() {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream("dropbox.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: dropbox.properties not found");

		try {
			properties.load(is);
		} catch (IOException e) {
			throw new RuntimeException("Fatal error: could not load dropbox.properties: " + e.getMessage());
		}
		
		appKey = properties.getProperty("app.key");
		appSecret = properties.getProperty("app.secret");
	}
	
	public static DropboxHelper getInstance() {
		if (instance == null)
			instance = new DropboxHelper();
		
		return instance;
	}

	public WebAuthSession getWebAuthSession() {
		AppKeyPair appKeys = new AppKeyPair(appKey, appSecret);
		return new WebAuthSession(appKeys, AccessType.DROPBOX);
	}
	
}