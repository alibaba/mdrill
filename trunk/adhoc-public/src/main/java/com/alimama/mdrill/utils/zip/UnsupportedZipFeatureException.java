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
 * Exception thrown when attempting to read or write data for a zip
 * entry that uses ZIP features not supported by this library.
 * @since Ant 1.9.0
 */
public class UnsupportedZipFeatureException extends ZipException {

    private final Feature reason;
    private final ZipEntry entry;
    private static final long serialVersionUID = 4430521921766595597L;

    /**
     * Creates an exception.
     * @param reason the feature that is not supported
     * @param entry the entry using the feature
     */
    public UnsupportedZipFeatureException(Feature reason,
                                          ZipEntry entry) {
        super("unsupported feature " + reason +  " used in entry "
              + entry.getName());
        this.reason = reason;
        this.entry = entry;
    }

    /**
     * The unsupported feature that has been used.
     */
    public Feature getFeature() {
        return reason;
    }

    /**
     * The entry using the unsupported feature.
     */
    public ZipEntry getEntry() {
        return entry;
    }

    /**
     * ZIP Features that may or may not be supported.
     */
    public static class Feature {
        /**
         * The entry is encrypted.
         */
        public static final Feature ENCRYPTION = new Feature("encryption");
        /**
         * The entry used an unsupported compression method.
         */
        public static final Feature METHOD = new Feature("compression method");
        /**
         * The entry uses a data descriptor.
         */
        public static final Feature DATA_DESCRIPTOR = new Feature("data descriptor");

        private final String name;

        private Feature(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
