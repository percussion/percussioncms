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
package com.percussion.install;

import static org.junit.Assert.assertEquals;

import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSWorkflow;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.percussion.util.PSResourceUtils;
import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.xml.sax.SAXException;

/**
 * Loads a test workflow from xml and runs the plugin against it to ensure expected
 * changes are made.
 * 
 */
@Category(IntegrationTest.class)
public class PSUpgradePluginUpdateWFNotificationTest
{
   @Test
   public void test() throws IOException, SAXException
   {
      PSUpgradePluginUpdateWFNotification plugin = new PSUpgradePluginUpdateWFNotification();
      plugin.setLogger(System.out);
      
      File wfFile = PSResourceUtils.getFile(PSUpgradePluginFixWFNotificationTest.class,
              "/com/percussion/rxupgrade/FixWFNotificationWorkflow.xml",
              null);

      PSWorkflow testWF = new PSWorkflow();
      testWF.fromXML(FileUtils.readFileToString(wfFile));
      plugin.updateNotification(testWF);
      checkWorkflowNotification(testWF);

   }

   private void checkWorkflowNotification(PSWorkflow testWF)
   {
      List<PSNotificationDef> notifsDef = testWF.getNotificationDefs();
      PSNotificationDef notifDef = notifsDef.get(0);
      assertEquals(notifDef.getSubject(),PSUpgradePluginUpdateWFNotification.NOTIFICATION_SUBJECT);
      assertEquals(notifDef.getBody(),PSUpgradePluginUpdateWFNotification.NOTIFICATION_BODY);
   }
}
