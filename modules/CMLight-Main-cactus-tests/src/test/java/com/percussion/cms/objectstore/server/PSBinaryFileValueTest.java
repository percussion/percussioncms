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
package com.percussion.cms.objectstore.server;

import com.percussion.cms.objectstore.IPSFieldValue;
import com.percussion.cms.objectstore.PSDateValue;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.PSItemField;
import com.percussion.cms.objectstore.PSTextValue;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.security.PSSecurityToken;
import com.percussion.server.PSRequest;
import com.percussion.server.PSServer;
import com.percussion.services.filestorage.IPSFileStorageService;
import com.percussion.services.filestorage.PSFileStorageServiceLocator;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;
import org.apache.cactus.ServletTestCase;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;

/**
 * Test use of the {@link PSBinaryFileValue} class
 */

@Category(IntegrationTest.class)
public class PSBinaryFileValueTest extends ServletTestCase
{
   /**
    * Test saving a file item to ensure info fields are set by the extractor
    * 
    * @throws Exception If the test fails or there are any errors.
    */
   @Test
   public void testFileUpload() throws Exception
   {
      Path currentRelativePath = Paths.get("");
      String s = currentRelativePath.toAbsolutePath().toString();
      System.out.println("Current relative path is: " + s);

      // login to set community etc
      IPSSecurityWs secWs = PSSecurityWsLocator.getSecurityWebservice();
      secWs.login(request, response, "admin1", "demo", null, "Enterprise_Investments", null);
      PSRequest psReq = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      PSSecurityToken tok = psReq.getSecurityToken();
      
      // create the item
      PSItemDefinition itemDef = PSItemDefManager.getInstance().getItemDef("rffFile", tok);
      PSServerItem item = new PSServerItem(itemDef, null, tok);
      Iterator<PSItemField> fields = item.getAllFields();
      String fileFieldName = "item_file_attachment";
      while (fields.hasNext())
      {
         PSItemField field = fields.next();
         String name = field.getName();

         if (name.equals("sys_title") || name.equals("displaytitle"))
            field.addValue(new PSTextValue("testFile"));
         else if (name.equals("sys_contentstartdate"))
            field.addValue(new PSDateValue(new Date()));
         else if (name.equals(fileFieldName + "_hash"))
         {
            // This is no longer to a real field "item_file_attachment"
            // the content item will store item_file_attachement_hash value
            // and store the file in the binary store

            IPSFileStorageService storage = PSFileStorageServiceLocator.getFileStorageService();
            String hash = storage.store(new File(PSServer.getRxDir(), "rx_resources/images/boxcheck.gif"));
            field.addValue(new PSTextValue(hash));
         } else if (name.equals(fileFieldName + "_filename"))
         {
            field.addValue(new PSTextValue("rx_resources/images/boxcheck.gif"));
         }
      }
      
      item.save(tok);
      
      // load and ensure fields are set
      item = PSServerItem.loadItem(new PSLocator(item.getContentId(), item.getRevision()), tok);
      
      fields = item.getAllFields();
      while (fields.hasNext())
      {
         PSItemField field = fields.next();
         String name =  field.getName();
         if (name.startsWith(fileFieldName + "_"))
         {
            IPSFieldValue val = field.getValue();
            assertTrue(val != null);
            if (val instanceof PSTextValue)
            {
               assertTrue(!StringUtils.isBlank((String) ((PSTextValue) val).getValue()));
            }
         }
      }
   }
}
