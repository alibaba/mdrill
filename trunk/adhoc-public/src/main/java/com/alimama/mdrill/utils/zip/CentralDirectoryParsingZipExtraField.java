/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.alimama.mdrill.utils.zip;

import java.util.zip.ZipException;

/**
 * {@link ZipExtraField ZipExtraField} that knows how to parse central
 * directory data.
 *
 * @since Ant 1.8.0
 */
public interface CentralDirectoryParsingZipExtraField extends ZipExtraField {
    /**
     * Populate data from this array as if it was in central directory data.
     * @param data an array of bytes
     * @param offset the start offset
     * @param length the number of bytes in the array from offset
     *
     * @throws ZipException on error
     */
    void parseFromCentralDirectoryData(byte[] data, int offset, int length)
        throws ZipException;
}
