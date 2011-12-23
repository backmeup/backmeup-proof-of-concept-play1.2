package org.backmeup.jobs.index;

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
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
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

import play.jobs.Job;

/**
 * Illustrates how to implement a indexer as hadoop map reduce job.
 */
public class HadoopIndexerJob extends Job {

	private String sequenceFilePath;
	private String indexFileDestination;
	private String indexJarPath;
	private int numberOfShards;
	public static final String INDEX_JARPATH_KEY_NAME = "index.jar.path";

	public HadoopIndexerJob(String path, String finalDestination, int numOfShards) {
		Properties properties = new Properties();
		InputStream is = getClass().getClassLoader().getResourceAsStream(
				"hdfs.properties");
		if (is == null)
			throw new RuntimeException("Fatal error: hdfs.properties not found");

		try {
			properties.load(is);
		} catch (IOException e) {
			throw new RuntimeException(
					"Fatal error: could not load hdfs.properties: "
							+ e.getMessage());
		}

		this.indexJarPath = properties.getProperty(INDEX_JARPATH_KEY_NAME);
		this.sequenceFilePath = path;
		this.indexFileDestination = finalDestination;
		this.numberOfShards = numOfShards;
	}

	public void doJob() {
		// create job conf with class pointing into job jar.
		JobConf jobConf = new JobConf();
		jobConf.setJobName("indexer");
		jobConf.setMapRunnerClass(org.backmeup.index.HadoopIndexer.class);
		jobConf.setJar(indexJarPath);
		// alternative use a text file and a TextInputFormat
		jobConf.setInputFormat(SequenceFileInputFormat.class);

		Path input = new Path(sequenceFilePath);
		SequenceFileInputFormat.setInputPaths(jobConf, input);
		// we just set the output path to make hadoop happy.
		TextOutputFormat.setOutputPath(jobConf, new Path(indexFileDestination));
		// setting the folder where lucene indexes will be copied when finished.
		jobConf.set("finalDestination", indexFileDestination);
		jobConf.set("fs.default.name", "hdfs://localhost:9000");

		// important to switch spec exec off.
		// We dont want to have something duplicated.
		jobConf.setSpeculativeExecution(false);

		try {
			// The num of map tasks is equal to the num of input splits.
			// The num of input splits by default is equal to the num of hdf
			// blocks
			// for the input file(s). To get the right num of shards we need to
			// calculate the best input split size.
			FileSystem fs = FileSystem.get(input.toUri(), jobConf);
			FileStatus[] status = fs.globStatus(input);
			long size = 0;
			for (FileStatus fileStatus : status) {
				size += fileStatus.getLen();
			}
			long optimalSplisize = size / numberOfShards;
			jobConf.set("mapred.min.split.size", "" + optimalSplisize);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		try {
			// give more mem to lucene tasks.
			jobConf.set("mapred.child.java.opts", "-Xmx2G");
			jobConf.setNumMapTasks(1);
			jobConf.setNumReduceTasks(0);
			JobClient.runJob(jobConf);
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
