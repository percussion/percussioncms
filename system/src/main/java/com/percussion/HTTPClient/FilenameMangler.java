/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.percussion.HTTPClient;


/**
 * {@link Codecs#mpFormDataDecode(byte[], java.lang.String, java.lang.String,
 * HTTPClient.FilenameMangler) Codecs.mpFormDataDecode} and {@link
 * Codecs#mpFormDataEncode(HTTPClient.NVPair[], HTTPClient.NVPair[],
 * HTTPClient.NVPair[], HTTPClient.FilenameMangler) Codecs.mpFormDataEncode}
 * may be handed an instance of a class which implements this interface in
 * order to control names of the decoded files or the names sent in the encoded
 * data.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @since	V0.3-1
 */
@Deprecated
public interface FilenameMangler
{
    /**
     * This is invoked by {@link Codecs#mpFormDataDecode(byte[],
     * java.lang.String, java.lang.String, HTTPClient.FilenameMangler)
     * Codecs.mpFormDataDecode} for each file found in the data, just before
     * the file is created and written. If null is returned then the file is
     * not created or written. This allows you to control which files are
     * written and the names of the resulting files.
     *
     * <P>For {@link Codecs#mpFormDataEncode(HTTPClient.NVPair[],
     * HTTPClient.NVPair[], HTTPClient.NVPair[], HTTPClient.FilenameMangler)
     * Codecs.mpFormDataEncode} this is also invoked on each filename, allowing
     * you to control the actual name used in the <var>filename</var> attribute
     * of the Content-Disposition header. This does not change the name of the
     * file actually read. If null is returned then the file is ignored.
     *
     * @param filename  the original filename in the Content-Disposition header
     * @param fieldname the name of the this field, i.e. the value of the
     *                  <var>name</var> attribute in Content-Disposition
     *                  header
     * @return the new file name, or null if the file is to be ignored.
     */
    public String mangleFilename(String filename, String fieldname);
}
