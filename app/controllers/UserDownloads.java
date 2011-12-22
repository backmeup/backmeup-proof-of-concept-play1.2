package controllers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.backmeup.connectors.Datasource;
import org.backmeup.connectors.base.FilesystemLikeDatasource;
import org.backmeup.connectors.impl.dropbox.DropboxDatasource;
import org.backmeup.storage.DataObject;
import org.backmeup.storage.hdfs.util.HdfsManager;
import org.backmeup.storage.hdfs.util.SequenceFileUtil;

import com.dropbox.client2.DropboxAPI.Entry;

import models.BackupStatus;
import models.DatasourceProfile;
import models.User;
import models.UserDownload;
import play.mvc.Controller;
import play.mvc.With;

@With(Secure.class)
public class UserDownloads extends Controller {
	
	public static void list() {
		User user = Security.connectedUser();
		List<UserDownload> downloads = user.listDownloads();
		render(user, downloads);
	}
	
	public static void delete(Long id) {
		User user = Security.connectedUser();
		user.getDownloadById(id).delete();
		redirect("UserDownloads.list");
	}
	
	public static void download(Long id) throws IOException {
		User user = Security.connectedUser();
		UserDownload download = user.getDownloadById(id);
		String pathString = download.path;
		if (download != null) {
			String outputFile = user.username + "_" + System.currentTimeMillis() + ".zip";
			Path path = new Path(pathString);
			Configuration config = HdfsManager.getInstance().getConfig();
			FileSystem fs = HdfsManager.getInstance().getFileSystem();
			SequenceFileUtil sfu = new SequenceFileUtil(fs, path, config);
			sfu.SequenceToZip(user.username, outputFile);
			redirect("UserDownloads.list");
		}
	}
		
	public static void unpack(Long id) throws IOException {
		User user = Security.connectedUser();
		UserDownload download = user.getDownloadById(id);
		String pathString = download.path;
		if (download != null) {
			String outputDir = user.username;
			Path path = new Path(pathString);
			Configuration config = HdfsManager.getInstance().getConfig();
			FileSystem fs = HdfsManager.getInstance().getFileSystem();
			SequenceFileUtil sfu = new SequenceFileUtil(fs, path, config);
			sfu.SequenceToLocal(outputDir);
			redirect("UserDownloads.list");
		}
	}
	
	public static String queryDownloadStatus(Long id) {
		User user = Security.connectedUser();
		UserDownload download = user.getDownloadById(id);
		if (download != null) {
			BackupStatus task = user.getActiveTaskForDownload(id);
			if (task != null)
				return task.status.toString(); 
		}
		return "no task scheduled";
	}
			
}
