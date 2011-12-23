package org.backmeup.index;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Random;

import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.BytesWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.MapRunnable;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.RecordReader;
import org.apache.hadoop.mapred.Reporter;
import org.apache.hadoop.mapred.SequenceFileInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;

import play.Logger;

/**
 * A {@link BaseIndexer} subclass that is compatible with Hadoop. Note that this
 * class does not work based on a {@link StorageReader}, but has to work directly
 * on the Hadoop-specific record objects!
 */
public class HadoopIndexer extends BaseIndexer implements MapRunnable<Text, BytesWritable, Text, Text> {

	private JobConf conf;
	
	public void configure(JobConf conf) {
		this.conf = conf;
	}
	
	public void run(RecordReader<Text, BytesWritable> reader, OutputCollector<Text, Text> output,
			Reporter report) throws IOException {
		
		Text key = reader.createKey();
		BytesWritable value = reader.createValue();
		
		IndexWriter indexWriter = createIndexWriter(conf.get("backmeup.index.directory"));
		
		report.setStatus("Adding documents...");
		
		while (reader.next(key, value)) {
			try {
				index(indexWriter, key.toString(), new ByteArrayInputStream(value.getBytes()));
				report.progress();
			} catch (Throwable t) {
				Logger.error(t.getMessage());
			}
		}

		report.setStatus("Done adding documents.");
		report.setStatus("Closing index...");
		indexWriter.close();
		report.setStatus("Closing done!");
	}

}
