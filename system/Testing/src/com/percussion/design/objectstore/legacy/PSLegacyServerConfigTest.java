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

