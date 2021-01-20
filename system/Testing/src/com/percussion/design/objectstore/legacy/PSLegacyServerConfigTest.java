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
package com.percussion.design.objectstore.legacy;

import com.percussion.design.objectstore.PSSecurityProviderInstance;
import com.percussion.design.objectstore.PSServerConfiguration;
import com.percussion.util.PSCollection;
import com.percussion.utils.testing.PSTestResourceUtils;
import com.percussion.utils.tools.PSBaseXmlConfigTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.Test;
import org.w3c.dom.Document;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Test case for the {@link PSLegacyServerConfig} class.
 */
public class PSLegacyServerConfigTest extends PSBaseXmlConfigTest
{
   /**
    * Constant for the test legacy server config file.
    */
   public static final String TEST_LEGACY_CONFIG_FILE = 
      "/com/percussion/design/objectstore/legacy/config.xml";

   /**
    * Test the legacy server config.
    * 
    * @throws Exception if the test fails.
    */
   @Test
   public void testConfig() throws Exception
   {
      File srcConfig = getTempXmlFile();
      File tmpConfig1 = getTempXmlFile();
      
      // copy source to that file and to test file
      File srcFile = PSTestResourceUtils.getFile(PSLegacyServerConfigTest.class,TEST_LEGACY_CONFIG_FILE,null);
      copyXmlFile(srcFile, srcConfig);
      copyXmlFile(srcFile, tmpConfig1);
      compareXmlDocs(srcConfig, tmpConfig1);
      
      // load the legacy config and test
      Document srcDoc = PSXmlDocumentBuilder.createXmlDocument(
         new FileInputStream(srcConfig), false);
      PSLegacyServerConfig serverConfig = new PSLegacyServerConfig(srcDoc);
      assertTrue(!serverConfig.getBackEndConnections().isEmpty());
      assertTrue(!serverConfig.getBackEndCredentials().isEmpty());
      
      // save and reload, be sure the legacy objects are gone
      FileOutputStream fos =  new FileOutputStream(tmpConfig1);
      try {
         PSXmlDocumentBuilder.write(serverConfig.toXml(),fos);
      } finally {
         if (fos!=null) 
            try { fos.close();} catch (Exception e) {/*ignore*/ }
      }
      Document tmpDoc = PSXmlDocumentBuilder.createXmlDocument(
         new FileInputStream(tmpConfig1), false);
      PSLegacyServerConfig tmpConfig = new PSLegacyServerConfig(tmpDoc);
      assertTrue(tmpConfig.getBackEndConnections().isEmpty());
      assertTrue(tmpConfig.getBackEndCredentials().isEmpty());
      
      // now test loading as new config, must exclude security providers as they
      // are legacy in one case, and not in another
      PSServerConfiguration newConfig = new PSServerConfiguration(tmpDoc);
      tmpConfig.setSecurityProviderInstances(
         new PSCollection(PSSecurityProviderInstance.class));
      newConfig.setSecurityProviderInstances(
         new PSCollection(PSSecurityProviderInstance.class));      
      assertEquals(tmpConfig, newConfig);
   }
   
   @Override
   protected String getFilePrefix()
   {
      return "legacy-";
   }

}

