package org.apache.lucene.index;

/**
 * Copyright 2004 The Apache Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.IOException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.document.NumericField;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.store.RAMOutputStream;
import org.apache.lucene.util.IOUtils;

import com.alimama.mdrill.fdtBlockCompress.FdtCompressIndexInput;
import com.alimama.mdrill.fdtBlockCompress.FdtCompressIndexOutput;

public class FieldsWriter {
    private static final Log LOG = LogFactory.getLog(FieldsWriter.class);

  static final int FIELD_IS_TOKENIZED = 1 << 0;
  static final int FIELD_IS_BINARY = 1 << 1;

  /** @deprecated Kept for backwards-compatibility with <3.0 indexes; will be removed in 4.0 */
  @Deprecated
  static final int FIELD_IS_COMPRESSED = 1 << 2;

  private static final int _NUMERIC_BIT_SHIFT = 3;
  static final int FIELD_IS_NUMERIC_MASK = 0x07 << _NUMERIC_BIT_SHIFT;

  static final int FIELD_IS_NUMERIC_INT = 1 << _NUMERIC_BIT_SHIFT;
  static final int FIELD_IS_NUMERIC_LONG = 2 << _NUMERIC_BIT_SHIFT;
  static final int FIELD_IS_NUMERIC_FLOAT = 3 << _NUMERIC_BIT_SHIFT;
  static final int FIELD_IS_NUMERIC_DOUBLE = 4 << _NUMERIC_BIT_SHIFT;
  // currently unused: static final int FIELD_IS_NUMERIC_SHORT = 5 << _NUMERIC_BIT_SHIFT;
  // currently unused: static final int FIELD_IS_NUMERIC_BYTE = 6 << _NUMERIC_BIT_SHIFT;

  // the next possible bits are: 1 << 6; 1 << 7
  
  // Original format
  static final int FORMAT = 0;

  // Changed strings to UTF8
  static final int FORMAT_VERSION_UTF8_LENGTH_IN_BYTES = 1;
  
  // Lucene 3.0: Removal of compressed fields
  static final int FORMAT_LUCENE_3_0_NO_COMPRESSED_FIELDS = 2;

  // Lucene 3.2: NumericFields are stored in binary format
  static final int FORMAT_LUCENE_3_2_NUMERIC_FIELDS = 3;

  // NOTE: if you introduce a new format, make it 1 higher
  // than the current one, and always change this if you
  // switch to a new format!
  static final int FORMAT_CURRENT = FORMAT_LUCENE_3_2_NUMERIC_FIELDS;
  
  private FieldInfos fieldInfos;

  // If null - we were supplied with streams, if notnull - we manage them ourselves
  private Directory directory;
  private String segment;
  private IndexOutput fieldsStream;
  private IndexOutput indexStream;
  
  

  FieldsWriter(Directory directory, String segment, FieldInfos fn) throws IOException {
    this.directory = directory;
    this.segment = segment;
    fieldInfos = fn;

    boolean success = false;
    try {

    	IndexOutput fdt = directory.createOutput(IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_EXTENSION));
      indexStream = directory.createOutput(IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_INDEX_EXTENSION));

      if(FieldsWriterCompress.isFdtCompress()&&!(directory instanceof RAMDirectory))
      {
          indexStream.writeInt(FieldsWriterCompress.FORMAT_CURRENT);
          fieldsStream=new FdtCompressIndexOutput(fdt,1024*512);
          fieldsStream.writeInt(FieldsWriterCompress.FORMAT_CURRENT);

      }else{
          fdt.writeInt(FORMAT_CURRENT);
          indexStream.writeInt(FORMAT_CURRENT);
          fieldsStream=fdt;
          fieldsStream.writeInt(FieldsWriterCompress.FORMAT_CURRENT);
      }

      success = true;
    } finally {
      if (!success) {
        abort();
      }
    }
  }

  FieldsWriter(IndexOutput fdx, IndexOutput fdt, FieldInfos fn) {
    directory = null;
    segment = null;
    fieldInfos = fn;
    fieldsStream = fdt;
    indexStream = fdx;
  }

  void setFieldsStream(IndexOutput stream) {
    this.fieldsStream = stream;
  }

  // Writes the contents of buffer into the fields stream
  // and adds a new entry for this document into the index
  // stream.  This assumes the buffer was already written
  // in the correct fields format.
  void flushDocument(int numStoredFields, RAMOutputStream buffer) throws IOException {
	  long pos=fieldsStream.getFilePointer();
//	  LOG.info("flushDocument:"+pos+","+numStoredFields);
    indexStream.writeLong(pos);
    fieldsStream.writeVInt(numStoredFields);
    buffer.writeTo(fieldsStream);
  }

  void skipDocument() throws IOException {
	  long pos=fieldsStream.getFilePointer();
//	  LOG.info("skipDocument:"+pos);
    indexStream.writeLong(pos);
    fieldsStream.writeVInt(0);
  }

  void close() throws IOException {
    if (directory != null) {
      try {
        IOUtils.close(fieldsStream, indexStream);
      } finally {
        fieldsStream = indexStream = null;
      }
    }
  }

  void abort() {
    if (directory != null) {
      try {
        close();
      } catch (IOException ignored) {
      }
      try {
        directory.deleteFile(IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_EXTENSION));
      } catch (IOException ignored) {
      }
      try {
        directory.deleteFile(IndexFileNames.segmentFileName(segment, IndexFileNames.FIELDS_INDEX_EXTENSION));
      } catch (IOException ignored) {
      }
    }
  }

  final void writeField(FieldInfo fi, Fieldable field) throws IOException {
      fieldsStream.writeVInt(fi.number);
    int bits = 0;
    if (field.isTokenized())
      bits |= FIELD_IS_TOKENIZED;
    if (field.isBinary())
      bits |= FIELD_IS_BINARY;
    if (field instanceof NumericField) {
      switch (((NumericField) field).getDataType()) {
        case INT:
          bits |= FIELD_IS_NUMERIC_INT; break;
        case LONG:
          bits |= FIELD_IS_NUMERIC_LONG; break;
        case FLOAT:
          bits |= FIELD_IS_NUMERIC_FLOAT; break;
        case DOUBLE:
          bits |= FIELD_IS_NUMERIC_DOUBLE; break;
        default:
          assert false : "Should never get here";
      }
    }
    fieldsStream.writeByte((byte) bits);

    if (field.isBinary()) {
      final byte[] data;
      final int len;
      final int offset;
      data = field.getBinaryValue();
      len = field.getBinaryLength();
      offset =  field.getBinaryOffset();

      fieldsStream.writeVInt(len);
      fieldsStream.writeBytes(data, offset, len);
    } else if (field instanceof NumericField) {
      final NumericField nf = (NumericField) field;
      final Number n = nf.getNumericValue();
      switch (nf.getDataType()) {
        case INT:
            fieldsStream.writeVVInt(n.intValue()); break;
        case LONG:
            fieldsStream.writeVVLong(n.longValue()); break;
        case FLOAT:
            fieldsStream.writeVVVInt(Float.floatToIntBits(n.floatValue())); break;
        case DOUBLE:
            fieldsStream.writeVVVLong(Double.doubleToLongBits(n.doubleValue())); break;
        default:
          assert false : "Should never get here";
      }
    } else {
	fieldsStream.writeString(field.stringValue());
    }
  }

  final void addRawDocuments(final IndexInput stream, long[] lengthsstart,long[] lengthsend, int numDocs) throws IOException {
	  if(stream instanceof FdtCompressIndexInput)
	  {
		  FdtCompressIndexInput inputstream=(FdtCompressIndexInput)stream;
		    for(int i=0;i<numDocs;i++) {
			  long position = fieldsStream.getFilePointer();
		      indexStream.writeLong(position);
//		      LOG.info("addRawDocuments 1 "+position+","+lengthsstart[i]+","+lengthsend[i]);
		      inputstream.writeToPos(fieldsStream,lengthsend[i]);
		    }
		  return ;
	  }
	  
	  if(fieldsStream instanceof FdtCompressIndexOutput)
	  {
		    for(int i=0;i<numDocs;i++) {
				  long position = fieldsStream.getFilePointer();
			      indexStream.writeLong(position);
//			      LOG.info("addRawDocuments 2 "+position+","+lengthsstart[i]+","+lengthsend[i]);
			      long end=lengthsend[i];
			      if(end==-1)
			      {
			    	  end=stream.length();
			      }
				  fieldsStream.copyBytes(stream, end-lengthsstart[i]);
		    }
		  
		  return ;
	  }
	  
	    long position = fieldsStream.getFilePointer();
	    long start = position;
	    for(int i=0;i<numDocs;i++) {
	      indexStream.writeLong(position);
//	      LOG.info("addRawDocuments 3 "+position+","+lengthsstart[i]+","+lengthsend[i]);

	      long end=lengthsend[i];
	      if(end==-1)
	      {
	    	  end=stream.length();
	      }
	      position += end-lengthsstart[i];
	    }
	    fieldsStream.copyBytes(stream, position-start);
	  }

  final void addDocument(Document doc) throws IOException {
	  long pos=fieldsStream.getFilePointer();
    indexStream.writeLong(pos);

//    LOG.info("addDocument "+pos);

    int storedCount = 0;
    List<Fieldable> fields = doc.getFields();
    for (Fieldable field : fields) {
      if (field.isStored())
          storedCount++;
    }
    fieldsStream.writeVInt(storedCount);

    for (Fieldable field : fields) {
      if (field.isStored())
      {
        writeField(fieldInfos.fieldInfo(field.name()), field);
      }
    }
    
  }
  

  
}
