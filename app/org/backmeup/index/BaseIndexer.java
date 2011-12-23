package org.backmeup.index;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Random;

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
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public abstract class BaseIndexer {
	
	private Tika tika = new Tika();
	
	protected IndexWriter createIndexWriter(String indexDirectory) throws IOException {
		String shardName = System.currentTimeMillis() + "-" + new Random().nextInt();
		File file = new File(indexDirectory, shardName);		
		Directory dir = FSDirectory.open(file);
		
		Analyzer analyzer = new SimpleAnalyzer(Version.LUCENE_35);
		IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_35, analyzer);
		return new IndexWriter(dir, conf);
	}
	
	protected void index(IndexWriter writer, String filename, ByteArrayInputStream is)
		throws IOException, SAXException, TikaException {
		
		System.out.println("Indexing: " + filename);
		
		String mimeType = (is.markSupported()) ? tika.detect(is) : null;

		Metadata metadata = new Metadata();
		if (mimeType != null) 
			metadata.set(Metadata.CONTENT_TYPE, mimeType);
		
		ContentHandler handler = new BodyContentHandler();
		Parser parser = tika.getParser();
		parser.parse(is, handler, metadata, new ParseContext());

		Document doc = new Document();
		
		// Add the file path to the Lucene index document
		doc.add(new Field("filename", filename, Field.Store.YES, Field.Index.NO));
		
		// Add the content to Lucene index document
		doc.add(new Field("body", handler.toString(), Field.Store.NO, Field.Index.ANALYZED));
		
		// Add tika metadata
		for (String name : metadata.names()) {
			String theValue = metadata.get(name);
			if (theValue != null)
				doc.add(new Field(name, theValue, Field.Store.YES, Field.Index.ANALYZED));
		}
		
		writer.addDocument(doc);
	}

}
