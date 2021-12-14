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
