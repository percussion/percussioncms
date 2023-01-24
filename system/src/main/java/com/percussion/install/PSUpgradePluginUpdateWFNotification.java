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

import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSNotificationDef;
import com.percussion.services.workflow.data.PSWorkflow;
import org.w3c.dom.Element;

import java.io.PrintStream;
import java.util.List;

/**
 * Updates the workflow notification subject and body for all workflows.
 *
 */
public class PSUpgradePluginUpdateWFNotification extends PSSpringUpgradePluginBase
{
   private IPSWorkflowService wfService;
   private PrintStream logger;
   
   
   public PSUpgradePluginUpdateWFNotification()
   {
      super();
      wfService = PSWorkflowServiceLocator.getWorkflowService();
   }
   /*
    * (non-Javadoc)
    * @see com.percussion.install.IPSUpgradePlugin#process(com.percussion.install.IPSUpgradeModule, org.w3c.dom.Element)
    */
   public PSPluginResponse process(IPSUpgradeModule config, Element elemData)
   {
      logger = config.getLogStream();      
      logger.println("Started " + PSUpgradePluginUpdateWFNotification.class.getName() + "process.");
      try
      {
         List<PSWorkflow> allWorkflows = wfService.findWorkflowsByName(null);
         for (PSWorkflow workflow : allWorkflows)
         {
            updateNotification(workflow);
            wfService.saveWorkflow(workflow);         
         }
      }
      catch (Exception e)
      {
         return new PSPluginResponse(PSPluginResponse.EXCEPTION,
               e.getLocalizedMessage());
      }
      logger.println("Finished " + PSUpgradePluginUpdateWFNotification.class.getName() + "process.");

      return new PSPluginResponse(PSPluginResponse.SUCCESS, "");
   }

   
   /**
    * Set logger, only used by unit test, otherwise set by process() method
    * 
    * @param out print stream to log to.
    */
   void setLogger(PrintStream out)
   {
      logger = out;
   }

   /**
    * Updates the workflow notification's body and subject.
    * @param workflow must not be <code>null</code>.
    */
   public void updateNotification(PSWorkflow workflow)
   {
      if(workflow == null)
         throw new IllegalArgumentException("workflow must not be null");
      List<PSNotificationDef> notifsDef = workflow.getNotificationDefs();
      PSNotificationDef notifDef = notifsDef.get(0);
      logger.println("Updating notification subject.");
      notifDef.setSubject(NOTIFICATION_SUBJECT);
      logger.println("Updating notification body.");
      notifDef.setBody(NOTIFICATION_BODY);
   }
   
   /**
    * Updated workflow notification subject
    */
   public static final String NOTIFICATION_SUBJECT = "Content awaiting your attention";
   
   /**
    * Updated workflow notification body
    */
   public static final String NOTIFICATION_BODY = "Content has been sent to you and is awaiting your attention.\r\n\r\n"
         + "Sent by: ${sys_contentlastmodifier}\r\nComment: $wfcomment\r\n\r\n"
         + "Please click the following link to access your content:";
}
