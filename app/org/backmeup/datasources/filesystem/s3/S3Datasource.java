package org.backmeup.datasources.filesystem.s3;

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

import org.jets3t.service.S3Service;
import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.jets3t.service.impl.rest.httpclient.RestS3Service;
import org.jets3t.service.model.S3Bucket;
import org.jets3t.service.model.S3Object;
import org.jets3t.service.security.AWSCredentials;

import play.Logger;

import controllers.Security;
import controllers.datasources.S3;

public class S3Datasource extends FilesystemLikeDatasource {

	public static final String AWS_ACCESS_KEY_PROPERTY_NAME = "awsAccessKey";
	public static final String AWS_SECRET_KEY_PROPERTY_NAME = "awsSecretKey";
	public static final String AWS_BUCKET_PROPERTY_NAME = "awsTopBucket";
	public static final String AWS_FILTER_PROPERTY_NAME = "awsFilter";
	public static final String AWS_S3_ENDPOINT = "https://www.amazonaws.com/";
	public static final String S3_FOLDER_DESIGNATION = "_$folder$";

	private AWSCredentials awsCredentials;
	private String bucket;
	private String filter;
	private S3Service s3Service;
	
	public S3Datasource(DatasourceProfile profile) {
		super(profile);

		awsCredentials = null;
		s3Service = null;

		String accessKey = profile.getProperty(AWS_ACCESS_KEY_PROPERTY_NAME).propertyValue;
		String secretKey = profile.getProperty(AWS_SECRET_KEY_PROPERTY_NAME).propertyValue;
		bucket = profile.getProperty(AWS_BUCKET_PROPERTY_NAME).propertyValue;
		filter = profile.getProperty(AWS_FILTER_PROPERTY_NAME).propertyValue;
		
		awsCredentials = new AWSCredentials(accessKey, secretKey);

		if (awsCredentials != null) {
			try {
				s3Service = new RestS3Service(awsCredentials);

			} catch (S3ServiceException e) {
				Logger.error("S3 service exception - unable to connect with given credentials.");
				Logger.error(e.getMessage());
			}
		}
	}

	public List<FilesystemURI> list(FilesystemURI pdURI) {
		
		if (s3Service==null) {
			Logger.error("S3Datasource: This class failed to connect to an S3Service, so you cannot call List!");
			return null;
		}

		ArrayList<FilesystemURI> alldo = new ArrayList<FilesystemURI>();
		
		if (pdURI==null) {//list all content recursively

			if (!bucket.equals("")) {// if a bucket has been specified...
				try {
					URI bucketURI = new URI(AWS_S3_ENDPOINT+bucket+filter);
					FilesystemURI dou = new FilesystemURI(bucketURI, true);
					alldo.addAll(listBucket(dou));
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else { // or else list all buckets
				try {
					S3Bucket[] myBuckets = s3Service.listAllBuckets();
					for (int i=0; i<myBuckets.length; i++) {
						String bucketName = myBuckets[i].getName();
						URI bucketURI = new URI(AWS_S3_ENDPOINT+bucketName);
						FilesystemURI dou = new FilesystemURI(bucketURI, true);
						alldo.addAll(listBucket(dou));
					}
				} catch (S3ServiceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} else { // list a specific sub-filter
			return listBucket(pdURI);
		}
		
		return alldo;
	}

	private List<FilesystemURI> listBucket(FilesystemURI pdURI) {
		Logger.debug("Called listBucket with DataObjectURI: " + pdURI);
		
		ArrayList<FilesystemURI> aldo = new ArrayList<FilesystemURI>();
		String path = pdURI.getUri().getPath();
		if (path.equals("")) {
			Logger.error("Tried to list an S3 URI with no bucket: " + pdURI);
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
		Logger.debug("Bucket for this URI is: " + theBucket);
		
		S3Object[] filteredObjects = null;
		if (theFilter.equals("")) {
			try {
				filteredObjects = s3Service.listObjects(theBucket);
			} catch (S3ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				filteredObjects = s3Service
						.listObjects(theBucket, theFilter, null);
			} catch (S3ServiceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (filteredObjects != null) {
			for (int o = 0; o < filteredObjects.length; o++) {
				String key = filteredObjects[o].getKey();
				Logger.debug("Found S3 key: " + key);
				String modkey = key;
				if (!theFilter.equals("")) {
					int k = key.indexOf(theFilter) + theFilter.length();
					modkey = key.substring(k+1);
					Logger.debug("Found modified key: " + modkey);
				}
				if (modkey.length() > 0) {
					if (modkey.endsWith(S3_FOLDER_DESIGNATION)) {
						int k = modkey.length()
								- S3_FOLDER_DESIGNATION.length();
						if (k > 0) {
							// String modModKey = modkey.substring(0, k);
							// it is a directory
							String dirUrl = AWS_S3_ENDPOINT + theBucket + "/" + key + "/";
							Logger.debug("Found a directory at this level: "
									+ dirUrl);
							/* Don't add these to the list
							try {
								aldo.add(new DataObjectURI(new URI(dirUrl), true));
							} catch (URISyntaxException e) {
								Logger.error("Failed to add URI to list: " + dirUrl);
								e.printStackTrace();
							}
							*/
						}
					} else { // it should be a file
						String sUrl = AWS_S3_ENDPOINT + theBucket + "/" + key;
						Logger.debug("Found a file at this level: " + sUrl);
						try {
							aldo.add(new FilesystemURI(new URI(sUrl), false));
						} catch (URISyntaxException e) {
							Logger.error("Failed to add URI to list: " + sUrl);
							e.printStackTrace();
						}
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
			Logger.error("Tried to retrieve an S3 object with no bucket: " + uri);
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
		
		S3Object filteredObject = null;
		
		try {
			filteredObject = s3Service.getObject(theBucket, theKey);
		} catch (S3ServiceException e) {
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
	
	public static boolean validateCredentials(String awsAccessKey, String awsSecretKey, String awsTopBucket) {

		S3Service localS3Service = null;
		S3Bucket localBucket = null;
		if (awsTopBucket==null) awsTopBucket="";
		
		AWSCredentials localAwsCredentials = new AWSCredentials(awsAccessKey, awsSecretKey);

		if (localAwsCredentials != null) {
			try {
				localS3Service = new RestS3Service(localAwsCredentials);

			} catch (S3ServiceException e) {
				Logger.error("S3 service exception - unable to connect with given credentials.");
				Logger.error(e.getMessage());
				return false;
			}
		} else {
			Logger.error("S3DataSource: AWSCredentials are null.");
			return false;
		}
		
		if (localS3Service != null) {
			if (awsTopBucket.equals("")) {
				try {
					S3Bucket[] myBuckets = localS3Service.listAllBuckets();
					if (myBuckets.length<1) {
						Logger.error("S3DataSource: no buckets in S3 account.");
						return false;
					}
				} catch (S3ServiceException e) {
					Logger.error("S3DataSource: Unable to list buckets in S3 account.");
					e.printStackTrace();
					return false;
				}
			} else {
				try {
					localBucket = localS3Service.getBucket(awsTopBucket);
					if (localBucket==null) {
						Logger.error("S3DataSource: Bucket is null: "
								+ awsTopBucket);
						return false;
					}
				} catch (S3ServiceException e) {
					Logger.error("S3DataSource: unable to load bucket: "
							+ awsTopBucket);
					return false;
				}
			}
		} else {
			Logger.error("S3DataSource: S3Service is null.");
			return false;
		}
		
		return true;
	}

}
