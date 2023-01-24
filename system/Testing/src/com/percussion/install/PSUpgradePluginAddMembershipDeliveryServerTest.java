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

package com.percussion.install;

import static org.junit.Assert.assertEquals;

import com.percussion.utils.tools.PSBaseXmlConfigTest;
import com.percussion.xml.PSXmlDocumentBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class PSUpgradePluginAddMembershipDeliveryServerTest
{

   @Test
   public void testUpgradeConfig() throws Exception
   {
      File srcNoMemberhipFile = new File("UnitTestResources/com/percussion/rxupgrade/deliveryServicesNoMembership.xml");
      File noMemberhipFile = File.createTempFile("TestMembershipUpgradePlugin", ".xml");
      File withMemberhipFile = new File("UnitTestResources/com/percussion/rxupgrade/deliveryServicesWithMembership.xml");

      File bak = File.createTempFile("TestMembershipUpgradePlugin", ".xml");
      
      // create copy of test src we can modify
      FileUtils.copyFile(srcNoMemberhipFile, noMemberhipFile);
      
      // upgrade the file
      PSUpgradePluginAddMembershipDeliveryServer plugin = new PSUpgradePluginAddMembershipDeliveryServer();
      plugin.upgradeConfig(noMemberhipFile, bak);
      
      // compare
      PSBaseXmlConfigTest.compareXmlDocs(withMemberhipFile, noMemberhipFile, true);
   }
}
