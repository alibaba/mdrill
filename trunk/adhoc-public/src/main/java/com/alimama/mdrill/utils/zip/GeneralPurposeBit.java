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

/**
 * Parser/encoder for the "general purpose bit" field in ZIP's local
 * file and central directory headers.
 *
 * @since Ant 1.9.0
 */
public final class GeneralPurposeBit {
    /**
     * Indicates that the file is encrypted.
     */
    private static final int ENCRYPTION_FLAG = 1 << 0;

    /**
     * Indicates that a data descriptor stored after the file contents
     * will hold CRC and size information.
     */
    private static final int DATA_DESCRIPTOR_FLAG = 1 << 3;

    /**
     * Indicates strong encryption.
     */
    private static final int STRONG_ENCRYPTION_FLAG = 1 << 6;

    /**
     * Indicates that filenames are written in utf-8.
     *
     * <p>The only reason this is public is that {@link
     * ZipOutputStream#EFS_FLAG} was public in several versions of
     * Apache Ant and we needed a substitute for it.</p>
     */
    public static final int UFT8_NAMES_FLAG = 1 << 11;

    private boolean languageEncodingFlag = false;
    private boolean dataDescriptorFlag = false;
    private boolean encryptionFlag = false;
    private boolean strongEncryptionFlag = false;

    public GeneralPurposeBit() {
    }

    /**
     * whether the current entry uses UTF8 for file name and comment.
     */
    public boolean usesUTF8ForNames() {
        return languageEncodingFlag;
    }

    /**
     * whether the current entry will use UTF8 for file name and comment.
     */
    public void useUTF8ForNames(boolean b) {
        languageEncodingFlag = b;
    }

    /**
     * whether the current entry uses the data descriptor to store CRC
     * and size information
     */
    public boolean usesDataDescriptor() {
        return dataDescriptorFlag;
    }

    /**
     * whether the current entry will use the data descriptor to store
     * CRC and size information
     */
    public void useDataDescriptor(boolean b) {
        dataDescriptorFlag = b;
    }

    /**
     * whether the current entry is encrypted
     */
    public boolean usesEncryption() {
        return encryptionFlag;
    }

    /**
     * whether the current entry will be encrypted
     */
    public void useEncryption(boolean b) {
        encryptionFlag = b;
    }

    /**
     * whether the current entry is encrypted using strong encryption
     */
    public boolean usesStrongEncryption() {
        return encryptionFlag && strongEncryptionFlag;
    }

    /**
     * whether the current entry will be encrypted  using strong encryption
     */
    public void useStrongEncryption(boolean b) {
        strongEncryptionFlag = b;
        if (b) {
            useEncryption(true);
        }
    }

    /**
     * Encodes the set bits in a form suitable for ZIP archives.
     */
    public byte[] encode() {
        return 
            ZipShort.getBytes((dataDescriptorFlag ? DATA_DESCRIPTOR_FLAG : 0)
                              |
                              (languageEncodingFlag ? UFT8_NAMES_FLAG : 0)
                              |
                              (encryptionFlag ? ENCRYPTION_FLAG : 0)
                              |
                              (strongEncryptionFlag ? STRONG_ENCRYPTION_FLAG : 0)
                              );
    }

    /**
     * Parses the supported flags from the given archive data.
     * @param data local file header or a central directory entry.
     * @param offset offset at which the general purpose bit starts
     */
    public static GeneralPurposeBit parse(final byte[] data, final int offset) {
        final int generalPurposeFlag = ZipShort.getValue(data, offset);
        GeneralPurposeBit b = new GeneralPurposeBit();
        b.useDataDescriptor((generalPurposeFlag & DATA_DESCRIPTOR_FLAG) != 0);
        b.useUTF8ForNames((generalPurposeFlag & UFT8_NAMES_FLAG) != 0);
        b.useStrongEncryption((generalPurposeFlag & STRONG_ENCRYPTION_FLAG)
                              != 0);
        b.useEncryption((generalPurposeFlag & ENCRYPTION_FLAG) != 0);
        return b;
    }

    @Override
    public int hashCode() {
        return 3 * (7 * (13 * (17 * (encryptionFlag ? 1 : 0)
                               + (strongEncryptionFlag ? 1 : 0))
                         + (languageEncodingFlag ? 1 : 0))
                    + (dataDescriptorFlag ? 1 : 0));
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof GeneralPurposeBit)) {
            return false;
        }
        GeneralPurposeBit g = (GeneralPurposeBit) o;
        return g.encryptionFlag == encryptionFlag
            && g.strongEncryptionFlag == strongEncryptionFlag
            && g.languageEncodingFlag == languageEncodingFlag
            && g.dataDescriptorFlag == dataDescriptorFlag;
    }
}
