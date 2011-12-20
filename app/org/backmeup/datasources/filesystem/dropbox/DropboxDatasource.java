package org.backmeup.datasources.filesystem.dropbox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.backmeup.DataObject;
import org.backmeup.datasources.filesystem.FilesystemLikeDatasource;
import org.backmeup.datasources.filesystem.FilesystemURI;

import play.jobs.Job;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Account;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.WebAuthSession;

import controllers.Security;

import models.BackupStatus;
import models.DatasourceProfile;

public class DropboxDatasource extends FilesystemLikeDatasource {
	
	public static final String PROPERTY_TOKEN = "token";
		
	public static final String PROPERTY_SECRET = "secret";
	
	private DropboxAPI<WebAuthSession> api;
	
	public DropboxDatasource(DatasourceProfile profile) {
		super(profile);
		String token = profile.getProperty(PROPERTY_TOKEN).propertyValue;
		String secret = profile.getProperty(PROPERTY_SECRET).propertyValue;
		
		WebAuthSession session = DropboxHelper.getInstance().getWebAuthSession();
		session.setAccessTokenPair(new AccessTokenPair(token, secret));

		// TODO throw an exception on fail
		session.isLinked();
		
		api = new DropboxAPI<WebAuthSession>(session);
	}
	
	public Account getAccountInfo() throws DropboxException {
		return api.accountInfo();
	}
	
	public Entry getTopLevelDir() throws DropboxException {
		return api.metadata("/", 100, null, true, null);
	}

	@Override
	public List<FilesystemURI> list(FilesystemURI uri) {
		String path = (uri == null) ? "/" : uri.toString();
		
		List<FilesystemURI> uris = new ArrayList<FilesystemURI>();
		
		try {
			for (Entry e : api.metadata(path, 100, null, true, null).contents) {
				uris.add(new FilesystemURI(new URI(e.path), e.isDir));
			}
		} catch (DropboxException e) {
			// TODO Create human-readable error output in view
		} catch (URISyntaxException e) {
			// TODO Create human-readable error output in view
		}
		
		return uris;
	}

	@Override
	public InputStream getFile(FilesystemURI uri) {
		try {
			return api.getFileStream(uri.toString(), null);
		} catch (DropboxException e) {
			// TODO Create human-readable error output in view
		}
		return null;
	}

	@Override
	public String getStatistics() {
		StringBuffer html = new StringBuffer();
		html.append("<ul>");
		for (FilesystemURI uri : list()) {
			html.append("<li>" + uri.toString() + "</li>");
		}
		html.append("</ul>");
		return html.toString();
	}

}
