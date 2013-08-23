package com.alimama.mdrill.buffer;

import java.io.File;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alimama.mdrill.utils.TryLockFile;

public class TryLock {
	public static Logger log = LoggerFactory.getLogger(TryLock.class);

	Directory cacheDir;

	public TryLock(Directory cacheDir) {
		this.cacheDir = cacheDir;
	}

	TryLockFile lockf=null;

	public void unlock() {

		try {
			if (lockf != null) {
				lockf.unlock();
			}
		} catch (Exception e) {
		}

	}

	public void tryLock() {

		try {
			if (cacheDir != null) {
				if (cacheDir instanceof FSDirectory) {
					FSDirectory localdir = (FSDirectory) cacheDir;
					this.lockf=new TryLockFile((new File(localdir.getDirectory(), "mdrill_uni_lock")).getAbsolutePath());
					this.lockf.trylock();
				}

			}
		}

		catch (Throwable e) {
		}

	}

}
