package org.apache.lucene.store;

import java.io.File;
import java.io.IOException;

public class LinkSimpleFSDirectory extends LinkFSDirectory{

	public LinkSimpleFSDirectory(File path, LockFactory lockFactory) throws IOException {
	    super(path, lockFactory);
	  }
	  public LinkSimpleFSDirectory(File path) throws IOException {
	    super(path, null);
	  }
	  
	  public IndexInput openInput(String name, int bufferSize) throws IOException {
		  File file=null;
		  if (!this.links.containsKey(name)) {
			  file=new File(directory, name);
		  }else{
			file = this.links.get(name);
		  }
		  return new SimpleFSDirectory.SimpleFSIndexInput("LinkSimpleFSDirectory(path=\"" + file.getPath() + "\")", file, bufferSize, getReadChunkSize());
	  }
}
