package com.alimama.mdrill.hdfsDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.lucene.store.BufferedIndexInput;
import org.apache.lucene.store.BufferedIndexOutput;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.LinkFSDirectory;
import org.apache.lucene.store.SingleInstanceLockFactory;

import com.alimama.mdrill.buffer.BlockBufferInput;


public class FileSystemDirectory extends Directory {
    private static final Log logger = LogFactory.getLog(FileSystemDirectory.class);

    private final FileSystem fs;
    public final Path directory;
    private final int ioFileBufferSize;
    private boolean isAllowMove=false;
    
    private boolean isUsedBlockBuffer=false;
    
    
    public boolean isUsedBlockBuffer() {
		return isUsedBlockBuffer;
	}
	public void setUsedBlockBuffer(boolean isUsedBlockBuffer) {
		this.isUsedBlockBuffer = isUsedBlockBuffer;
	}

	private HashMap<String,Path> links=new HashMap<String,Path>();

    public boolean isAllowMove() {
        return isAllowMove;
    }
    public void setAllowLinks(boolean isAllowMove) {
        this.isAllowMove = isAllowMove;
    }
    public void copy(Directory to, String src, String dest) throws IOException {
	if(to instanceof FileSystemDirectory)
	{
	    FileSystemDirectory fsto=(FileSystemDirectory)to;
	    if(fsto.isAllowMove())
	    {
		Path discfile = new Path(fsto.directory, dest);
		Path srcfile = new Path(directory, src);
		logger.info("used links dest:"+getShortPath(discfile)+",src:"+getShortPath(srcfile));

		fsto.addLinks(discfile.getName(), srcfile);
		return ;
	    }
	}
	
	
	if(to instanceof LinkFSDirectory)
	{
		LinkFSDirectory fsto=(LinkFSDirectory)to;
	    if(fsto.isAllowMove())
	    {
	    	File discfile = new File(fsto.directory, dest);
	    	Path srcfile = new Path(directory, src);
	    	fsto.addhdfsLinks(discfile.getName(), srcfile);
	    	return ;
	    }
	}
	
	super.copy(to, src, dest);
    }
    
    private String getShortPath(Path p)
    {
	StringBuffer buff=new StringBuffer();
	buff.append(p.getParent().getName());
	buff.append("/");
	buff.append(p.getName());
	return buff.toString();
    }
    
    public void addLinks(String name,Path p)
    {
	this.links.put(name, p);
    }
    
    public FileSystemDirectory(FileSystem fs, Path directory, boolean create,
	    Configuration conf) throws IOException {
    	
    	try {
    	      setLockFactory(new SingleInstanceLockFactory());
    	    } catch (IOException e) {
    	      // Cannot happen
    	    }
	this.fs = fs;
	this.directory = directory;
	this.ioFileBufferSize = conf.getInt("io.file.buffer.size", 4096);

	if (create) {
	    create();
	}

	boolean isDir = false;
	try {
	    FileStatus status = fs.getFileStatus(directory);
	    if (status != null) {
		isDir = status.isDir();
	    }
	} catch (IOException e) {
	    // file does not exist, isDir already set to false
	}
	if (!isDir) {
	    throw new IOException(directory + " is not a directory");
	}
    }

    private void create() throws IOException {
	if (!fs.exists(directory)) {
	    fs.mkdirs(directory);
	}

	boolean isDir = false;
	try {
	    FileStatus status = fs.getFileStatus(directory);
	    if (status != null) {
		isDir = status.isDir();
	    }
	} catch (IOException e) {
	    // file does not exist, isDir already set to false
	}
	if (!isDir) {
	    throw new IOException(directory + " is not a directory");
	}

	// clear old index files
	FileStatus[] fileStatus = fs.listStatus(directory,
	        LuceneIndexFileNameFilter.getFilter());
	
	this.links.clear();
	for (int i = 0; i < fileStatus.length; i++) {
	    if (!fs.delete(fileStatus[i].getPath(), true)) {
		throw new IOException("Cannot delete index file " + fileStatus[i].getPath());
	    }
	}
    }

    public String[] list() throws IOException {
	FileStatus[] fileStatus = fs.listStatus(directory,
	        LuceneIndexFileNameFilter.getFilter());
	ArrayList<String> rtn=new ArrayList<String>();
	for (int i = 0; i < fileStatus.length; i++) {
	    rtn.add(fileStatus[i].getPath().getName());
	}
	
	for(java.util.Map.Entry<String, Path> e:this.links.entrySet())
	{
	    rtn.add(e.getKey());
	}
	String[] result = new String[rtn.size()];
	return rtn.toArray(result);
    }

    public boolean fileExists(String name) throws IOException {
	return fs.exists(new Path(directory, name))||this.links.containsKey(name);
    }

    public long fileModified(String name) {
	throw new UnsupportedOperationException();
    }

    public void touchFile(String name) {
	throw new UnsupportedOperationException();
    }

    public long fileLength(String name) throws IOException {
	Path f=this.links.get(name);
	if(f==null)
	{
	    f=new Path(directory, name);
	}
	return fs.getFileStatus(f).getLen();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.store.Directory#deleteFile(java.lang.String)
     */
    public void deleteFile(String name) throws IOException {
	this.links.remove(name);
	if (!fs.delete(new Path(directory, name))) {
	    throw new IOException("Cannot delete index file " + name);
	}
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.store.Directory#renameFile(java.lang.String,
     * java.lang.String)
     */
    public void renameFile(String from, String to) throws IOException {
	Path l=this.links.remove(from);
	if(l!=null)
	{
	    this.links.put(to, l);
	}
	fs.rename(new Path(directory, from), new Path(directory, to));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.store.Directory#createOutput(java.lang.String)
     */
    public IndexOutput createOutput(String name) throws IOException {
	Path file = new Path(directory, name);
	this.links.remove(name);
	if (fs.exists(file) && !fs.delete(file, true)) {
	    // delete the existing one if applicable
	    throw new IOException("Cannot overwrite index file " + file);
	}

	return new FileSystemIndexOutput(file, ioFileBufferSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.store.Directory#openInput(java.lang.String)
     */
    public IndexInput openInput(String name) throws IOException {
	return openInput(name, ioFileBufferSize);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.store.Directory#openInput(java.lang.String, int)
     */
    public IndexInput openInput(String name, int bufferSize) throws IOException {
	Path f=this.links.get(name);
	if(f==null)
	{
	    f=new Path(directory, name);
	}else{
	    logger.info("openInput->usedlinks:"+name+"@"+getShortPath(f));
	    
	}
	
	
	FileSystemIndexInput rtn= new FileSystemIndexInput(f, bufferSize);
	if(this.isUsedBlockBuffer()&&name.indexOf("frq")>=0)
	{
		return new BlockBufferInput(rtn,f.toString(),this.getP());
	}
	
	return rtn;
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.apache.lucene.store.Directory#makeLock(java.lang.String)
//     */
//    public Lock makeLock(final String name) {
//	return new Lock() {
//	    public boolean obtain() {
//		return true;
//	    }
//
//	    public void release() {
//	    }
//
//	    public boolean isLocked() {
//		throw new UnsupportedOperationException();
//	    }
//
//	    public String toString() {
//		return "Lock@" + new Path(directory, name);
//	    }
//	};
//    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.store.Directory#close()
     */
    public void close() throws IOException {
	// do not close the file system
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
	return this.getClass().getName() + "@" + directory;
    }
    
  

private class FileSystemIndexInput extends BufferedIndexInput {
	private final Path filePath; // for debugging
	private final Descriptor descriptor;
	private long length=-1;
	private boolean isOpen;
	private boolean isClone;
	
	

    public FileSystemIndexInput(final Path path, final int ioFileBufferSize)
	        throws IOException {
	    filePath = path;
	    descriptor = new Descriptor(fs,path, ioFileBufferSize);
	    isOpen = true;
	}

	protected void readInternal(byte[] b, int offset, int len)
	        throws IOException {
		long t0=System.currentTimeMillis();
		long t1=0l;
	    synchronized (descriptor) {
			t1=System.currentTimeMillis();
			long position = getFilePointer();
			if (position != descriptor.Positon()) {
			    descriptor.Stream().seek(position);
			    descriptor.setPositon(position);
			}
			int total = 0;
			do {
			    int i = descriptor.Stream().read(b, offset + total, len - total);
			    if (i == -1) {
			    	throw new IOException("Read past EOF");
			    }
			    descriptor.addPositon(i);
			    total += i;
			} while (total < len);
	    }
	    
	    long t2=System.currentTimeMillis();
	    long tl=t2-t1;
	    long tl2=t2-t0;
	    
	    synchronized (descriptor) {
	    	descriptor.Stat(tl, tl2, len);
	    }

	   
	}

	public void close() throws IOException {
	    if (!isClone) {
		if (isOpen) {
		    synchronized (descriptor) {
		    	descriptor.close();
		    }
		    isOpen = false;
		} else {
		    throw new IOException("Index file " + filePath   + " already closed");
		}
	    }
	}

	public long length() {
		if(length<0)
		{
			try {
				length = fs.getFileStatus(this.filePath).getLen();
			} catch (IOException e) {
    			logger.error("getFileStatus "+filePath.getName()+" timetaken ",e);

			}
		}
	    return length;
	}

	protected void finalize() throws IOException {
	    if (!isClone && isOpen) {
		close(); // close the file
	    }
	}

	public Object clone() {
	    FileSystemIndexInput clone = (FileSystemIndexInput) super.clone();
	    clone.isClone = true;
	    return clone;
	}

	@Override
        protected void seekInternal(long pos) throws IOException {

        }

	
    }

    private class FileSystemIndexOutput extends BufferedIndexOutput {

	private final Path filePath; // for debugging
	private final FSDataOutputStream out;
	private boolean isOpen;


	public FileSystemIndexOutput(Path path, int ioFileBufferSize)
	        throws IOException {
	    filePath = path;
	    out = fs.create(path, true, ioFileBufferSize);
	    isOpen = true;
	}

	public void flushBuffer(byte[] b, int offset, int size)
	        throws IOException {
	    out.write(b, offset, size);
	}

	public void close() throws IOException {
	    if (isOpen) {
		super.close();
		out.close();
		isOpen = false;
	    } else {
		throw new IOException("Index file " + filePath
		        + " already closed");
	    }
	}

	    @Override
	    public void seek(long pos) {
	      throw new RuntimeException("not allowed");    
	    }

	public long length() throws IOException {
	    return out.getPos();
	}

	protected void finalize() throws IOException {
	    if (isOpen) {
		close(); // close the file
	    }
	}
    }

    @Override
    public String[] listAll() throws IOException {
	return this.list();
    }

}
