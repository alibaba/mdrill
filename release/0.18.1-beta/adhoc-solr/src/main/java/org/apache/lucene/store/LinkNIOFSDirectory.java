package org.apache.lucene.store;

import java.io.File;
import java.io.IOException;

public class LinkNIOFSDirectory extends LinkFSDirectory{

	public LinkNIOFSDirectory(File path, LockFactory lockFactory) throws IOException {
	    super(path, lockFactory);
	  }

	  /** Create a new NIOFSDirectory for the named location and {@link NativeFSLockFactory}.
	   *
	   * @param path the path of the directory
	   * @throws IOException
	   */
	  public LinkNIOFSDirectory(File path) throws IOException {
	    super(path, null);
	  }
	  
	  
	  public IndexInput openInput(String name, int bufferSize) throws IOException {
		  File file=null;
		  if (!this.links.containsKey(name)) {
			  file=new File(directory, name);
		  }else{
			file = this.links.get(name);
		  }
		  return new NIOFSDirectory.NIOFSIndexInput(file, bufferSize, getReadChunkSize());
	  }

}
