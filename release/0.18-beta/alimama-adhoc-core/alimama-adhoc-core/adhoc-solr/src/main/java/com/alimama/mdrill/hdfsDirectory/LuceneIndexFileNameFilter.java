package com.alimama.mdrill.hdfsDirectory;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.lucene.index.IndexFileNameFilter;

/**
 * A wrapper class to convert an IndexFileNameFilter which implements
 * java.io.FilenameFilter to an org.apache.hadoop.fs.PathFilter.
 */
public class LuceneIndexFileNameFilter implements PathFilter {

  private static final LuceneIndexFileNameFilter singleton =
      new LuceneIndexFileNameFilter();

  /**
   * Get a static instance.
   * @return the static instance
   */
  public static LuceneIndexFileNameFilter getFilter() {
    return singleton;
  }

  private final IndexFileNameFilter luceneFilter;

  private LuceneIndexFileNameFilter() {
    luceneFilter = IndexFileNameFilter.getFilter();
  }

  /* (non-Javadoc)
   * @see org.apache.hadoop.fs.PathFilter#accept(org.apache.hadoop.fs.Path)
   */
  public boolean accept(Path path) {
    return luceneFilter.accept(null, path.getName());
  }

}
