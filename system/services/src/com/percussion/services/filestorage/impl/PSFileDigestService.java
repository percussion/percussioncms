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
package com.percussion.services.filestorage.impl;

import com.percussion.services.filestorage.IPSFileDigestService;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.apache.commons.lang.Validate.notNull;

public class PSFileDigestService implements IPSFileDigestService
{
   /**
    * See {@link #getAlgorithm()}, {@link #setAlgorithm(String)}.
    */
   private String algorithm = "SHA-256";

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

         StringBuilder hexString = new StringBuilder();

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
