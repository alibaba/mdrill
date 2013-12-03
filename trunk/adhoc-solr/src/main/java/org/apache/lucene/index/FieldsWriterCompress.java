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


public class FieldsWriterCompress {
	public static final int FORMAT_CURRENT = 9999;
	private static boolean isFdtCompress = true;

	public static boolean isFdtCompress() {
		return FieldsWriterCompress.isFdtCompress;
	}

	public static void setFdtCompress(boolean isFdtCompress) {
		FieldsWriterCompress.isFdtCompress = isFdtCompress;
	}

}
