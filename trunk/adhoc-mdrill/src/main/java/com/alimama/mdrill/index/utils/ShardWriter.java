package com.alimama.mdrill.index.utils;

import java.io.IOException;
import java.util.Date;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.Trash;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.KeepOnlyLastCommitDeletionPolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.store.Directory;

import com.alimama.mdrill.hdfsDirectory.FileSystemDirectory;


public class ShardWriter {
    private static Logger logger = Logger.getLogger(ShardWriter.class);
    private Path perm;
    private FileSystemDirectory dir;
    public FileSystemDirectory getDir() {
        return dir;
    }

    private IndexWriter writer;
    private long numForms = 0;
    private long numDocs=0;

    public long getNumDocs() {
        return numDocs;
    }

    public ShardWriter(FileSystem fs, String indexOutputPathStr,Configuration iconf) throws IOException {
	logger.info("Construct a shard writer " + indexOutputPathStr);
	perm = new Path(indexOutputPathStr);
	if (!fs.exists(perm)) {
	    fs.mkdirs(perm);
	} else {
	    moveToTrash(iconf, perm);
	    fs.mkdirs(perm);
	}

	this.dir = new FileSystemDirectory(fs, perm, true, iconf);
	writer = new IndexWriter(dir, null,
	        new KeepOnlyLastCommitDeletionPolicy(),
	        MaxFieldLength.UNLIMITED);
	writer.setMergeFactor(256);
	writer.setTermIndexInterval(128);
	writer.setUseCompoundFile(false);


    }
    
    public void setUseCompoundFile()
    {
    	writer.setUseCompoundFile(true);
    }
    
    public void addEmptyDoc() throws CorruptIndexException, IOException
    {
    	Document empty=new Document();
    	empty.add(new Field("higoempty_emptydoc_s", "", Field.Store.YES, Field.Index.NO));
    	writer.addDocument(empty);
    }

    public void process(RamWriter form) throws IOException {
	int docs=form.getNumDocs();
	numDocs+=docs;
	numForms++;
	this.process(form.getDirectory());
    }
    public void process(Directory dir) throws CorruptIndexException, IOException
    {
	Date d1=new Date();
	writer.addIndexesNoOptimize(new Directory[] { dir });
	Date d2=new Date();
	logger.info("process time "+(d2.getTime()-d1.getTime()));
    }

    public void close() throws IOException {
	logger.info("Closing the shard writer, processed " + numForms + " forms "+numDocs +" docs ");
	try {

		
		writer.getReader();
	    writer.close();
	    
	    

	} finally {
	    this.writer = null;
//	    IndexReader reader=IndexReader.open(this.dir);
//	    TermEnum te=reader.terms(new Term("thedate"));
//	    StringBuffer buff=new StringBuffer();
//	    int index=0;
//	    for (;;) {
//	    	index++;
//			Term t = te.term();
//			if (t == null||!t.field().equals("thedate")||index>100) {
//				break;
//			}
//			buff.append(t.toString());
//			buff.append(",");
//			te.next();
//		}
//	    reader.close();
//	    
//	    System.out.println("thedate:"+buff.toString());
	    this.dir.close();
	    this.dir = null;
	    logger.info("Closed Lucene index writer");
	}
	logger.info("Moved new index files to " + perm);
    }

    public String toString() {
	return this.getClass().getName() + "@" + perm + "&";
    }

	public void optimize() throws IOException {
		Throwable error=null;
		for(int i=0;i<10;i++)
		{
			error=null;
			try {
				writer.optimize();
				return ;
			} catch (CorruptIndexException e) {
				logger.error("optimize Corrupt Index error. ", e);
				error=e;
			} catch (IOException e) {
				logger.error("optimize IOException . ", e);
				error=e;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				logger.error(e);
			}
			
		}
		if(error!=null)
		{
			throw new IOException(error);
		}
	}

    public static void moveToTrash(Configuration conf, Path path)
	    throws IOException {
	Trash t = new Trash(conf);
	boolean isMoved = t.moveToTrash(path);
	t.expunge();
	if (!isMoved) {
	    logger.error("Trash is not enabled or file is already in the trash.");
	}
    }
}