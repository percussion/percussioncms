/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
