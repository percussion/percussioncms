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
