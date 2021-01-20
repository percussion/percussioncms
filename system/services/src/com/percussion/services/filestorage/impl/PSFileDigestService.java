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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.filestorage.impl;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.services.filestorage.IPSFileDigestService;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PSFileDigestService implements IPSFileDigestService
{
   /**
    * See {@link #getAlgorithm()}, {@link #setAlgorithm(String)}.
    */
   private String algorithm = "SHA-1";

   public String createChecksum(InputStream fis)
         throws NoSuchAlgorithmException, IOException
   {
      notNull(fis);
      
      String hexDigest = "";
      try
      {
         byte[] buffer = new byte[1024];
         MessageDigest digest = MessageDigest.getInstance(algorithm);
         int numRead;
         do
         {
            numRead = fis.read(buffer);
            if (numRead > 0)
            {
               digest.update(buffer, 0, numRead);
            }
         }
         while (numRead != -1);

         byte messageDigest[] = digest.digest();

         StringBuffer hexString = new StringBuffer();

         for (int i = 0; i < messageDigest.length; i++)
         {
            String hex = Integer.toHexString(0xFF & messageDigest[i]);
            if (hex.length() == 1)
            {
               hexString.append('0');
            }
            hexString.append(hex);
         }
         hexDigest = hexString.toString();
         return hexDigest;
      }
      finally
      {
         fis.close();
      }
   }

   /**
    * Used to configure the checksum generation algorithm.
    * 
    * @param algorithm
    */
   public void setAlgorithm(String algorithm)
   {
      this.algorithm = algorithm;
   }

   public String getAlgorithm()
   {
      return algorithm;
   }
}
