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
