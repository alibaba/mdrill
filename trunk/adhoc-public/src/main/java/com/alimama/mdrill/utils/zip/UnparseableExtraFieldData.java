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

/**
 * Wrapper for extra field data that doesn't conform to the recommended format of header-tag + size + data.
 *
 * <p>The header-id is artificial (and not listed as a known ID in
 * {@link <a href="http://www.pkware.com/documents/casestudies/APPNOTE.TXT">
 * APPNOTE.TXT</a>}).  Since it isn't used anywhere except to satisfy the
 * ZipExtraField contract it shouldn't matter anyway.</p>
 *
 * @since Ant 1.8.1
 */
public final class UnparseableExtraFieldData
    implements CentralDirectoryParsingZipExtraField {

    private static final ZipShort HEADER_ID = new ZipShort(0xACC1);

    private byte[] localFileData;
    private byte[] centralDirectoryData;

    /**
     * The Header-ID.
     *
     * @return a completely arbitrary value that should be ignored.
     */
    public ZipShort getHeaderId() {
        return HEADER_ID;
    }

    /**
     * Length of the complete extra field in the local file data.
     *
     * @return The LocalFileDataLength value
     */
    public ZipShort getLocalFileDataLength() {
        return new ZipShort(localFileData == null ? 0 : localFileData.length);
    }

    /**
     * Length of the complete extra field in the central directory.
     *
     * @return The CentralDirectoryLength value
     */
    public ZipShort getCentralDirectoryLength() {
        return centralDirectoryData == null
            ? getLocalFileDataLength()
            : new ZipShort(centralDirectoryData.length);
    }

    /**
     * The actual data to put into local file data.
     *
     * @return The LocalFileDataData value
     */
    public byte[] getLocalFileDataData() {
        return ZipUtil.copy(localFileData);
    }

    /**
     * The actual data to put into central directory.
     *
     * @return The CentralDirectoryData value
     */
    public byte[] getCentralDirectoryData() {
        return centralDirectoryData == null
            ? getLocalFileDataData() : ZipUtil.copy(centralDirectoryData);
    }

    /**
     * Populate data from this array as if it was in local file data.
     *
     * @param buffer the buffer to read data from
     * @param offset offset into buffer to read data
     * @param length the length of data
     */
    public void parseFromLocalFileData(byte[] buffer, int offset, int length) {
        localFileData = new byte[length];
        System.arraycopy(buffer, offset, localFileData, 0, length);
    }

    /**
     * Populate data from this array as if it was in central directory data.
     *
     * @param buffer the buffer to read data from
     * @param offset offset into buffer to read data
     * @param length the length of data
     */
    public void parseFromCentralDirectoryData(byte[] buffer, int offset,
                                              int length) {
        centralDirectoryData = new byte[length];
        System.arraycopy(buffer, offset, centralDirectoryData, 0, length);
        if (localFileData == null) {
            parseFromLocalFileData(buffer, offset, length);
        }
    }

}
