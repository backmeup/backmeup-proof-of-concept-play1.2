package org.backmeup.datasinks;

import java.net.URI;
import java.util.List;

import org.backmeup.DataObject;
import org.backmeup.datasources.Datasource;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageReader;

import models.DatasinkProfile;
import models.DatasourceProfile;

public abstract class Datasink {
	
	protected DatasinkProfile profile;
	
	public static Datasink forProfile(DatasinkProfile profile) {
		try {
			Class<? extends Datasink> clazz = 
				Class.forName(profile.implementationClass).asSubclass(Datasink.class);
			
			return clazz.getConstructor(DatasinkProfile.class).newInstance(profile);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
	}
	
	public Datasink(DatasinkProfile profile) {
		this.profile = profile;
	}
	
	public abstract String upload(StorageReader storage)
		throws DatasinkException, StorageException;

}
