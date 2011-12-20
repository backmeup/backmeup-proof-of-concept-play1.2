package org.backmeup.datasources;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.backmeup.DataObject;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageWriter;

import models.DatasourceProfile;

/**
 * An abstract base class for all datasource implementations
 * 
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public abstract class Datasource {
	
	protected DatasourceProfile profile;
	
	public static Datasource forProfile(DatasourceProfile profile) {
		try {
			Class<? extends Datasource> clazz = 
				Class.forName(profile.implementationClass).asSubclass(Datasource.class);
			
			return clazz.getConstructor(DatasourceProfile.class).newInstance(profile);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public Datasource(DatasourceProfile profile) {
		this.profile = profile;
	}
	
	/**
	 * Returns the profile backing this datasource implementation.
	 * @return the profile
	 */
	public DatasourceProfile getProfile() {
		return profile;
	}
	
	/**
	 * Downloads the entire content of this datasource to the provided
	 * data storage.
	 * @param storage the datastorage
	 * @return a DataObject handle to access the download in the store (e.g. a root directory etc.)
	 */
	public abstract void downloadAll(StorageWriter storage) throws DatasourceException, StorageException;
	
	/**
	 * Returns any type of overview information/statistics that can be
	 * shown to the user as 'detail view' for this datasource. The
	 * returned string can be HTML formatted.
	 * 
	 * @return statistics or any other information about the datasource
	 */
	public abstract String getStatistics();

}
