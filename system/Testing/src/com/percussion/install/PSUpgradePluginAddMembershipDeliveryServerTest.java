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
