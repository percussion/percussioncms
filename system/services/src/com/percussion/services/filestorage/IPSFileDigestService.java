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
package com.percussion.services.filestorage;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

/**
 * The file digest service is used to generate unique hash values
 * for file content.  The hash values are generated using the
 * algorithm specified by {@link #getAlgorithm()}.
 * 
 * @author peterfrontiero
 */
public interface IPSFileDigestService {
   
   /**
    * Generates a checksum for the specified input stream and
    * then closes the stream.
    * 
    * @param stream input stream representing the file content,
    * never <code>null</code>.
    * 
    * @return a unique checksum, never <code>null</code> or
    * empty.
    * @throws NoSuchAlgorithmException if the configured
    * algorithm is not valid.
    * @throws IOException if an error occurs reading from
    * or closing the input stream.
    */
   public String createChecksum(InputStream stream)
      throws NoSuchAlgorithmException, IOException;
   
   /**
    * @return the algorithm which has been configured for
    * checksum generation.
    */
   public String getAlgorithm();
}
