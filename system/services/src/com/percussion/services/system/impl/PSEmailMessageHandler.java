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
package com.percussion.services.system.impl;

import com.percussion.workflow.PSWorkFlowUtils;
import com.percussion.workflow.mail.IPSMailMessageContext;
import com.percussion.workflow.mail.IPSMailProgram;
import com.percussion.workflow.mail.PSMailException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

/**
 * This class contains functionality that was embedded in the workflow system. 
 * This listens for email messages to be delivered and uses the configured
 * mail plugin to deliver them. The plugin is resolved on construction, so
 * changes to the configuration require a server restart.
 * 
 * @author dougrand
 *
 */
public class PSEmailMessageHandler implements MessageListener
{
   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger ms_logger = LogManager.getLogger(
         PSEmailMessageHandler.class);
   
   /**
    * Mailing program to use. Resolved during contruction. If <code>null</code>
    * after construction, email cannot be delivered and errors will be logged
    * to the console.
    */
   private IPSMailProgram m_mailPlugin = null;
   
   /**
    * Ctor
    */
   public PSEmailMessageHandler()
   {
      /*
       * Load the mail plugin class and create the plugin object.
       */
      try
      {
         /*
          * Get the registered plugin (java class name) for the custom mail
          * program from the properties file. The default is
          * 'com.percussion.workflow.mail.PSJavaxMailProgram'
          */
         String sClassName = PSWorkFlowUtils.properties.getProperty(
            "CUSTOM_MAIL_CLASS",
            "com.percussion.workflow.mail.PSSecureMailProgram");

         m_mailPlugin = (IPSMailProgram)Class
               .forName(sClassName).newInstance();
         m_mailPlugin.init();
      }
      catch(Exception e)
      {
         ms_logger.error("Cannot instantiate email plugin" , e);
      }
   }
   
   public void onMessage(Message message)
   {
      try
      {
         if (message instanceof ObjectMessage)
         {
            ObjectMessage om = (ObjectMessage) message;
            
            IPSMailMessageContext email = (IPSMailMessageContext) om.getObject();
         
            if (m_mailPlugin == null)
            {
               ms_logger.error("Plugin not configured. " +
                       "Cannot send email message " + email);
            }
            else
            {
               m_mailPlugin.sendMessage(email);
            }
         }
      }
      catch (PSMailException e)
      {
         ms_logger.error("Cannot deliver email", e);
      }
      catch (JMSException e)
      {
         ms_logger.error("Cannot deliver email - jms failure",
               e);
      }
      finally
      {
         try
         {
            message.acknowledge();
         }
         catch (JMSException e)
         {
            ms_logger.error("Problem acknowledging message", e);
         }
      }
   }
   
   /**
    * When this bean is being destroyed, Spring will call this method. Cleanup
    * the mail plugin here if it is present.
    */
   public void destroy()
   {
      if (m_mailPlugin != null)
      {
         try
         {
            m_mailPlugin.terminate();
         }
         catch (PSMailException e)
         {
            ms_logger.error("Problem cleaning up plugin", e);
         }
      }
   }
}
