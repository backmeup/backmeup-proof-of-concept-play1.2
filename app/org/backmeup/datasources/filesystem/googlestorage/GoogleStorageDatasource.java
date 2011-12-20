package org.backmeup.datasources.filesystem.googlestorage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import models.DatasourceProfile;
import models.User;

import org.backmeup.DataObject;
import org.backmeup.datasources.filesystem.FilesystemLikeDatasource;
import org.backmeup.datasources.filesystem.FilesystemURI;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.GoogleStorageService;
import org.jets3t.service.model.GSBucket;
import org.jets3t.service.model.GSObject;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.GSCredentials;

import play.Logger;

import controllers.Security;

public class GoogleStorageDatasource extends FilesystemLikeDatasource {

	public static final String GS_ACCESS_KEY_PROPERTY_NAME = "gsAccessKey";
	public static final String GS_SECRET_KEY_PROPERTY_NAME = "gsSecretKey";
	public static final String GS_BUCKET_PROPERTY_NAME = "gsTopBucket";
	public static final String GS_FILTER_PROPERTY_NAME = "gsFilter";
	public static final String GS_ENDPOINT = "https://www.googlestorage.com/";

	private GSCredentials gsCredentials;
	private String bucket;
	private String filter;
	private GoogleStorageService gsService;
	
	public GoogleStorageDatasource(DatasourceProfile profile) {
		super(profile);

		gsCredentials = null;
		gsService = null;

		String accessKey = profile.getProperty(GS_ACCESS_KEY_PROPERTY_NAME).propertyValue;
		String secretKey = profile.getProperty(GS_SECRET_KEY_PROPERTY_NAME).propertyValue;
		bucket = profile.getProperty(GS_BUCKET_PROPERTY_NAME).propertyValue;
		filter = profile.getProperty(GS_FILTER_PROPERTY_NAME).propertyValue;
		
		gsCredentials = new GSCredentials(accessKey, secretKey);

		if (gsCredentials != null) {
			try {
				gsService = new GoogleStorageService(gsCredentials);

			} catch (ServiceException e) {
				Logger.error("GS service exception - unable to connect with given credentials.");
				Logger.error(e.getMessage());
			}
		}
	}

	public List<FilesystemURI> list(FilesystemURI pdURI) {
		
		if (gsService==null) {
			Logger.error("GSDatasource: This class failed to connect to an GSService, so you cannot call List!");
			return null;
		}

		ArrayList<FilesystemURI> alldo = new ArrayList<FilesystemURI>();
		
		if (pdURI==null) {//list all content recursively

			if (!bucket.equals("")) {// if a bucket has been specified...
				try {
					URI bucketURI = new URI(GS_ENDPOINT+bucket+filter);
					FilesystemURI dou = new FilesystemURI(bucketURI, true);
					alldo.addAll(listBucket(dou));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else { // or else list all buckets
				try {
					GSBucket[] myBuckets = gsService.listAllBuckets();
					for (int i=0; i<myBuckets.length; i++) {
						String bucketName = myBuckets[i].getName();
						URI bucketURI = new URI(GS_ENDPOINT+bucketName);
						FilesystemURI dou = new FilesystemURI(bucketURI, true);
						alldo.addAll(listBucket(dou));
					}
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} else { // list a specific sub-filter
			alldo.addAll(listBucket(pdURI));
		}
		
		return alldo;
	}

	private List<FilesystemURI> listBucket(FilesystemURI pdURI) {
		Logger.debug("Called listBucket with DataObjectURI: " + pdURI);

		ArrayList<FilesystemURI> aldo = new ArrayList<FilesystemURI>();
		String path = pdURI.getUri().getPath();
		if (path.equals("")) {
			Logger.error("Tried to list an GoogleStorage URI with no bucket: " + pdURI);
			return aldo;
		}
		// remove leading slash from path
		path = path.substring(1);
		Logger.debug("The path for this URI is: " + path);
		// separate the bucket from the filter
		int ii = path.indexOf("/");
		String theBucket = "";
		String theFilter = "";
		if (ii>0) {
			theBucket = path.substring(0,ii);
			int i = path.indexOf(theBucket);
			if (i + theBucket.length() + 1 < path.length()) {
				theFilter = path.substring(i + theBucket.length() + 1);
			}
			Logger.debug("Filter for this URI is: " + theFilter);
		} else {
			theBucket = path;
		}

		GSObject[] filteredObjects = null;
		if (theFilter.equals("")) {
			try {
				filteredObjects = gsService.listObjects(theBucket);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				filteredObjects = gsService
						.listObjects(theBucket, theFilter, null);
			} catch (ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (filteredObjects != null) {
			for (int o = 0; o < filteredObjects.length; o++) {
				String key = filteredObjects[o].getKey();
				Logger.debug("Found GS key: " + key);
				String modkey = key;
				if (!theFilter.equals("")) {
					int k = key.indexOf(theFilter) + theFilter.length();
					modkey = key.substring(k+1);
				}
				Logger.debug("Found modified key: " + modkey);
				if (modkey.length() > 0) {
					String sUrl = GS_ENDPOINT + theBucket +"/" + key;
					Logger.debug("Found a file at this level: " + sUrl);
					try {
						aldo.add(new FilesystemURI(new URI(sUrl), false));
					} catch (URISyntaxException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		return aldo;
	}
	
	@Override
	public String getStatistics() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public InputStream getFile(FilesystemURI uri) {
		InputStream ios = null;
		Logger.debug("Called getFile with DataObjectURI: " + uri);
		
		String path = uri.getUri().getPath();
		if (path.equals("")) {
			Logger.error("Tried to retrieve an GS object with no bucket: " + uri);
			return null;
		}
		// remove leading slash from path
		path = path.substring(1);
		Logger.debug("The path for this object is: " + path);
		// separate the bucket from the filter
		int ii = path.indexOf("/");
		String theBucket = "";
		String theKey = "";
		if (ii>0) {
			theBucket = path.substring(0,ii);
			int i = path.indexOf(theBucket);
			if (i + theBucket.length() + 1 < path.length()) {
				theKey = path.substring(i + theBucket.length() + 1);
			}
			Logger.debug("Key for this Object is: " + theKey);
		} else {
			theBucket = path;
		}
		Logger.debug("Bucket for this URI is: " + theBucket);
		
		GSObject filteredObject = null;
		
		try {
			filteredObject = gsService.getObject(theBucket, theKey);
		} catch (ServiceException e) {
			Logger.error("Unable to retrieve S3 object with bucket: " + theBucket + ", and key: " + theKey);
			e.printStackTrace();
		}

		try {
			ios = filteredObject.getDataInputStream();
		} catch (ServiceException e) {
			Logger.error("Unable to get data input stream from S3 object!");
			e.printStackTrace();
			return null;
		}
		return ios;
	}
	
	public static boolean validateCredentials(String gsAccessKey, String gsSecretKey, String gsTopBucket) {

		GoogleStorageService localGsService = null;
		if (gsTopBucket==null) gsTopBucket="";
		
		GSCredentials localGsCredentials = new GSCredentials(gsAccessKey, gsSecretKey);

		if (localGsCredentials != null) {
			try {
				localGsService = new GoogleStorageService(localGsCredentials);

			} catch (ServiceException e) {
				Logger.error("GS service exception - unable to connect with given credentials.");
				Logger.error(e.getMessage());
				return false;
			}
		} else {
			Logger.error("GoogleStorageDataSource: AWSCredentials are null.");
			return false;
		}
		
		if (localGsService != null) {
			if (gsTopBucket.equals("")) {
				try {
					GSBucket[] myBuckets = localGsService.listAllBuckets();
					if (myBuckets.length<1) {
						Logger.error("GoogleStorageDataSource: no buckets in GS account.");
						return false;
					}
				} catch (ServiceException e) {
					Logger.error("GoogleStorageDataSource: Unable to list buckets in GS account.");
					e.printStackTrace();
					return false;
				}
			} else {
				try {
					GSObject[] gsObjects = localGsService.listObjects(gsTopBucket);
					if (gsObjects.length < 1) {
						Logger.error("GoogleStorageDataSource: Bucket is empty: "
								+ gsTopBucket);
						return false;
					}
				} catch (ServiceException e) {
					Logger.error("GoogleStorageDataSource: unable to load objects from bucket: "
							+ gsTopBucket);
					return false;
				}
			}
		} else {
			Logger.error("GoogleStorageDataSource: GSService is null.");
			return false;
		}
		
		return true;
	}
	
}
