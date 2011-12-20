package org.backmeup.datasinks.zipfile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipOutputStream;

import models.DatasinkProfile;

import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.backmeup.DataObject;
import org.backmeup.datasinks.Datasink;
import org.backmeup.datasinks.DatasinkException;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageReader;

import play.Logger;

public class ZipFileDatasink extends Datasink {

	private String zipPath;
	
	public ZipFileDatasink(String path) {
		super(null);
		this.zipPath = path;
	}

	@Override
	public String upload(StorageReader storage) throws DatasinkException, StorageException {
		try {
			File archive = new File(zipPath);
			archive.createNewFile();
			
			FileOutputStream fOut = new FileOutputStream(archive);
			BufferedOutputStream bOut = new BufferedOutputStream(fOut);
			ZipOutputStream zipOut = new ZipOutputStream(bOut);
			
			Iterator<DataObject> it = storage.getDataObjects();
			while(it.hasNext()) {
				DataObject next = it.next();
				
				String relativePath = next.getPath();
				if (relativePath.startsWith("/"))
					relativePath = relativePath.substring(1);
				
				Logger.info("Adding to ZIP: " + relativePath);
				
				zipOut.putNextEntry(new ZipArchiveEntry(relativePath));
				zipOut.write(next.getBytes(), 0, next.getLength());
				zipOut.closeEntry();
			}
			
			zipOut.flush();
			zipOut.close();
			bOut.close();
			fOut.close();
			
			return zipPath;
		} catch (IOException e) {
			throw new DatasinkException(e);
		}
	}

}
