package org.backmeup.index;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Random;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.backmeup.storage.DataObject;
import org.backmeup.storage.StorageException;
import org.backmeup.storage.StorageReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import play.Logger;
import play.Play;

/**
 * A simple {@link BaseIndexer} subclass that creates a Lucene index from a 
 * {@link StorageReader}. Note that this class is not Hadoop-compatible!
 *  
 * @author Rainer Simon <rainer.simon@ait.ac.at>
 */
public class StorageIndexer extends BaseIndexer {
	
	private StorageReader storage;
	
	public StorageIndexer(StorageReader storage) {
		this.storage = storage;
	}
	
	public void run() throws IOException, StorageException, SAXException, TikaException {
		Logger.info("Starting indexing");
		IndexWriter indexWriter = createIndexWriter();
		
		Iterator<DataObject> it = storage.getDataObjects();
		while (it.hasNext()) {
			DataObject next = it.next();
			index(indexWriter, next.getPath(), new ByteArrayInputStream(next.getBytes()));
		}

		indexWriter.close();
		Logger.info("Indexing complete");
	}

}
