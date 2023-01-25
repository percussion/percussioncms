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

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.filestorage.IPSFileDigestService;
import com.percussion.util.PSPurgableTempFile;

import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.FileUtils;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSFileDigestServiceTest extends ServletTestCase
{
   /**
    * @throws Exception
    */
   public void testCreateChecksum() throws Exception
   {
      IPSFileDigestService fdsvc = getFileDigestService();
         
      FileInputStream fin = null;
      try
      {
         PSPurgableTempFile testXml = createFile("This is a test xml file");
         PSPurgableTempFile testTxt = createFile("This is a test txt file");
         
         assertFalse(FileUtils.contentEquals(testXml, testTxt));
        
         fin = new FileInputStream(testXml);
         String xmlChecksum1 = fdsvc.createChecksum(fin);
         assertNotNull(xmlChecksum1);
         
         fin = new FileInputStream(testXml);
         String xmlChecksum2 = fdsvc.createChecksum(fin);
         assertNotNull(xmlChecksum2);
         assertEquals(xmlChecksum1, xmlChecksum2);
         
         fin = new FileInputStream(testTxt);
         String txtChecksum = fdsvc.createChecksum(fin);
         assertNotNull(txtChecksum);
         assertFalse(txtChecksum.equals(xmlChecksum1));
      }
      finally
      {
         if (fin != null)
         {
            fin.close();
         }
      }
   }

   /**
    * @throws Exception
    */
   public void testGetAlgorithm() throws Exception
   {
      IPSFileDigestService fdsvc = getFileDigestService();
   
      assertNotNull(fdsvc.getAlgorithm());
   }

   private PSPurgableTempFile createFile(String content) throws IOException
   {
      PSPurgableTempFile f = new PSPurgableTempFile("tmp", "tmp", null);
      
      FileWriter fw = new FileWriter(f);
      fw.write(content);
      fw.close();
      
      return f;
   }
   
   private IPSFileDigestService getFileDigestService()
   {
      return (IPSFileDigestService) PSBaseServiceLocator.getBean(
            "sys_digestService");      
   }
      
}
