/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.alimama.mdrill.utils.zip;

import java.util.zip.ZipException;

import static com.alimama.mdrill.utils.zip.ZipConstants.DWORD;
import static com.alimama.mdrill.utils.zip.ZipConstants.WORD;

/**
 * Holds size and other extended information for entries that use Zip64
 * features.
 *
 * <p>From {@link "http://www.pkware.com/documents/casestudies/APPNOTE.TXT PKWARE's APPNOTE.TXT"}
 * <pre>
 * Zip64 Extended Information Extra Field (0x0001):
 *
 *          The following is the layout of the zip64 extended 
 *          information "extra" block. If one of the size or
 *          offset fields in the Local or Central directory
 *          record is too small to hold the required data,
 *          a Zip64 extended information record is created.
 *          The order of the fields in the zip64 extended 
 *          information record is fixed, but the fields will
 *          only appear if the corresponding Local or Central
 *          directory record field is set to 0xFFFF or 0xFFFFFFFF.
 *
 *          Note: all fields stored in Intel low-byte/high-byte order.
 *
 *          Value      Size       Description
 *          -----      ----       -----------
 *  (ZIP64) 0x0001     2 bytes    Tag for this "extra" block type
 *          Size       2 bytes    Size of this "extra" block
 *          Original 
 *          Size       8 bytes    Original uncompressed file size
 *          Compressed
 *          Size       8 bytes    Size of compressed data
 *          Relative Header
 *          Offset     8 bytes    Offset of local header record
 *          Disk Start
 *          Number     4 bytes    Number of the disk on which
 *                                this file starts 
 *
 *          This entry in the Local header must include BOTH original
 *          and compressed file size fields. If encrypting the 
 *          central directory and bit 13 of the general purpose bit
 *          flag is set indicating masking, the value stored in the
 *          Local Header for the original file size will be zero.
 * </pre></p>
 *
 * <p>Currently Ant doesn't support encrypting the
 * central directory so the note about masking doesn't apply.</p>
 *
 * <p>The implementation relies on data being read from the local file
 * header and assumes that both size values are always present.</p>
 *
 * @since Ant 1.9.0
 */
public class Zip64ExtendedInformationExtraField
    implements CentralDirectoryParsingZipExtraField {

    static final ZipShort HEADER_ID = new ZipShort(0x0001);

    private static final String LFH_MUST_HAVE_BOTH_SIZES_MSG =
        "Zip64 extended information must contain"
        + " both size values in the local file header.";
    private static final byte[] EMPTY = new byte[0];

    private ZipEightByteInteger size, compressedSize, relativeHeaderOffset;
    private ZipLong diskStart;

    /**
     * Stored in {@link #parseFromCentralDirectoryData
     * parseFromCentralDirectoryData} so it can be reused when ZipFile
     * calls {@link #reparseCentralDirectoryData
     * reparseCentralDirectoryData}.
     *
     * <p>Not used for anything else</p>
     */
    private byte[] rawCentralDirectoryData;

    /**
     * This constructor should only be used by the code that reads
     * archives inside of Ant.
     */
    public Zip64ExtendedInformationExtraField() { }

    /**
     * Creates an extra field based on the original and compressed size.
     *
     * @param size the entry's original size
     * @param compressedSize the entry's compressed size
     *
     * @throws IllegalArgumentException if size or compressedSize is null
     */
    public Zip64ExtendedInformationExtraField(ZipEightByteInteger size,
                                              ZipEightByteInteger compressedSize) {
        this(size, compressedSize, null, null);
    }

    /**
     * Creates an extra field based on all four possible values.
     *
     * @param size the entry's original size
     * @param compressedSize the entry's compressed size
     *
     * @throws IllegalArgumentException if size or compressedSize is null
     */
    public Zip64ExtendedInformationExtraField(ZipEightByteInteger size,
                                              ZipEightByteInteger compressedSize,
                                              ZipEightByteInteger relativeHeaderOffset,
                                              ZipLong diskStart) {
        this.size = size;
        this.compressedSize = compressedSize;
        this.relativeHeaderOffset = relativeHeaderOffset;
        this.diskStart = diskStart;
    }

    /** {@inheritDoc} */
    public ZipShort getHeaderId() {
        return HEADER_ID;
    }

    /** {@inheritDoc} */
    public ZipShort getLocalFileDataLength() {
        return new ZipShort(size != null ? 2 * DWORD : 0);
    }

    /** {@inheritDoc} */
    public ZipShort getCentralDirectoryLength() {
        return new ZipShort((size != null ? DWORD : 0)
                            + (compressedSize != null ? DWORD : 0)
                            + (relativeHeaderOffset != null ? DWORD : 0)
                            + (diskStart != null ? WORD : 0));
    }

    /** {@inheritDoc} */
    public byte[] getLocalFileDataData() {
        if (size != null || compressedSize != null) {
            if (size == null || compressedSize == null) {
                throw new IllegalArgumentException(LFH_MUST_HAVE_BOTH_SIZES_MSG);
            }
            byte[] data = new byte[2 * DWORD];
            addSizes(data);
            return data;
        }
        return EMPTY;
    }

    /** {@inheritDoc} */
    public byte[] getCentralDirectoryData() {
        byte[] data = new byte[getCentralDirectoryLength().getValue()];
        int off = addSizes(data);
        if (relativeHeaderOffset != null) {
            System.arraycopy(relativeHeaderOffset.getBytes(), 0, data, off, DWORD);
            off += DWORD;
        }
        if (diskStart != null) {
            System.arraycopy(diskStart.getBytes(), 0, data, off, WORD);
            off += WORD;
        }
        return data;
    }

    /** {@inheritDoc} */
    public void parseFromLocalFileData(byte[] buffer, int offset, int length)
        throws ZipException {
        if (length == 0) {
            // no local file data at all, may happen if an archive
            // only holds a ZIP64 extended information extra field
            // inside the central directory but not inside the local
            // file header
            return;
        }
        if (length < 2 * DWORD) {
            throw new ZipException(LFH_MUST_HAVE_BOTH_SIZES_MSG);
        }
        size = new ZipEightByteInteger(buffer, offset);
        offset += DWORD;
        compressedSize = new ZipEightByteInteger(buffer, offset);
        offset += DWORD;
        int remaining = length - 2 * DWORD;
        if (remaining >= DWORD) {
            relativeHeaderOffset = new ZipEightByteInteger(buffer, offset);
            offset += DWORD;
            remaining -= DWORD;
        }
        if (remaining >= WORD) {
            diskStart = new ZipLong(buffer, offset);
            offset += WORD;
            remaining -= WORD;
        }
    }

    /** {@inheritDoc} */
    public void parseFromCentralDirectoryData(byte[] buffer, int offset,
                                              int length)
        throws ZipException {
        // store for processing in reparseCentralDirectoryData
        rawCentralDirectoryData = new byte[length];
        System.arraycopy(buffer, offset, rawCentralDirectoryData, 0, length);

        // if there is no size information in here, we are screwed and
        // can only hope things will get resolved by LFH data later
        // But there are some cases that can be detected
        // * all data is there
        // * length == 24 -> both sizes and offset
        // * length % 8 == 4 -> at least we can identify the diskStart field
        if (length >= 3 * DWORD + WORD) {
            parseFromLocalFileData(buffer, offset, length);
        } else if (length == 3 * DWORD) {
            size = new ZipEightByteInteger(buffer, offset);
            offset += DWORD;
            compressedSize = new ZipEightByteInteger(buffer, offset);
            offset += DWORD;
            relativeHeaderOffset = new ZipEightByteInteger(buffer, offset);
        } else if (length % DWORD == WORD) {
            diskStart = new ZipLong(buffer, offset + length - WORD);
        }
    }

    /**
     * Parses the raw bytes read from the central directory extra
     * field with knowledge which fields are expected to be there.
     *
     * <p>All four fields inside the zip64 extended information extra
     * field are optional and must only be present if their corresponding
     * entry inside the central directory contains the correct magic
     * value.</p>
     */
    public void reparseCentralDirectoryData(boolean hasUncompressedSize,
                                            boolean hasCompressedSize,
                                            boolean hasRelativeHeaderOffset,
                                            boolean hasDiskStart)
        throws ZipException {
        if (rawCentralDirectoryData != null) {
            int expectedLength = (hasUncompressedSize ? DWORD : 0)
                + (hasCompressedSize ? DWORD : 0)
                + (hasRelativeHeaderOffset ? DWORD : 0)
                + (hasDiskStart ? WORD : 0);
            if (rawCentralDirectoryData.length < expectedLength) {
                throw new ZipException("central directory zip64 extended"
                                       + " information extra field's length"
                                       + " doesn't match central directory"
                                       + " data.  Expected length "
                                       + expectedLength + " but is "
                                       + rawCentralDirectoryData.length);
            }
            int offset = 0;
            if (hasUncompressedSize) {
                size = new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasCompressedSize) {
                compressedSize = new ZipEightByteInteger(rawCentralDirectoryData,
                                                         offset);
                offset += DWORD;
            }
            if (hasRelativeHeaderOffset) {
                relativeHeaderOffset =
                    new ZipEightByteInteger(rawCentralDirectoryData, offset);
                offset += DWORD;
            }
            if (hasDiskStart) {
                diskStart = new ZipLong(rawCentralDirectoryData, offset);
                offset += WORD;
            }
        }
    }

    /**
     * The uncompressed size stored in this extra field.
     */
    public ZipEightByteInteger getSize() {
        return size;
    }

    /**
     * The uncompressed size stored in this extra field.
     */
    public void setSize(ZipEightByteInteger size) {
        this.size = size;
    }

    /**
     * The compressed size stored in this extra field.
     */
    public ZipEightByteInteger getCompressedSize() {
        return compressedSize;
    }

    /**
     * The uncompressed size stored in this extra field.
     */
    public void setCompressedSize(ZipEightByteInteger compressedSize) {
        this.compressedSize = compressedSize;
    }

    /**
     * The relative header offset stored in this extra field.
     */
    public ZipEightByteInteger getRelativeHeaderOffset() {
        return relativeHeaderOffset;
    }

    /**
     * The relative header offset stored in this extra field.
     */
    public void setRelativeHeaderOffset(ZipEightByteInteger rho) {
        relativeHeaderOffset = rho;
    }

    /**
     * The disk start number stored in this extra field.
     */
    public ZipLong getDiskStartNumber() {
        return diskStart;
    }

    /**
     * The disk start number stored in this extra field.
     */
    public void setDiskStartNumber(ZipLong ds) {
        diskStart = ds;
    }

    private int addSizes(byte[] data) {
        int off = 0;
        if (size != null) {
            System.arraycopy(size.getBytes(), 0, data, 0, DWORD);
            off += DWORD;
        }
        if (compressedSize != null) {
            System.arraycopy(compressedSize.getBytes(), 0, data, off, DWORD);
            off += DWORD;
        }
        return off;
    }
}
