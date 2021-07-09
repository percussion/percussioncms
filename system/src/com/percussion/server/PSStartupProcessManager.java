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
package com.percussion.server;

import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author JaySeletz
 *
 */
public class PSStartupProcessManager implements IPSNotificationListener, IPSStartupProcessManager
{
   private static final Logger log = LogManager.getLogger(PSStartupProcessManager.class);
   
   List<IPSStartupProcess> m_startupProcesses = new ArrayList<IPSStartupProcess>();
   
   private Properties m_startupProperties;
   
   private File m_propFile = null;

   public void setNotificationService(IPSNotificationService notificationService)
   {
      notificationService.addListener(EventType.CORE_SERVER_INITIALIZED, this);
   }
   
   public void addStartupProcess(IPSStartupProcess startupProc)
   {
      Validate.notNull(startupProc);
      m_startupProcesses.add(startupProc);
   }

   public void notifyEvent(PSNotificationEvent notification)
   {
      if (!EventType.CORE_SERVER_INITIALIZED.equals(notification.getType()))
         return;
      log.info("Loading startup properties");
      Properties startupProps = getStartupProperties();
      log.info("Running startup processes");
      try
      {
         runStartupProcesses(startupProps);
      }
      finally
      {
         // write out props we have
         log.info("Saving startup properties");
         saveStartupProperties(startupProps);
         log.info("Finished running startup processes");
      }
   }

   protected void runStartupProcesses(Properties startupProps)
   {
      for (IPSStartupProcess proc : m_startupProcesses)
      {
         try
         {
            proc.doStartupWork(startupProps);
         }
         catch (Exception e)
         {
            // log it, and throw a runtime exception to halt the server init
            String msg = "Error running startup process: " + proc.getClass().getName() + ", : " + e.getLocalizedMessage();
            log.error(msg, e);
            throw new RuntimeException(msg);
         }
      }
   }


   private void saveStartupProperties(Properties startupProps)
   {
      if (m_propFile == null)
         throw new RuntimeException("Startup properties have not been initialized, cannot save");
      
      Writer writer = null;
      try
      {
         writer = new FileWriter(m_propFile);
         startupProps.store(writer, null);
      }
      catch (IOException e)
      {
         throw new RuntimeException("Failed to save startup properties: " + m_propFile, e);
      }
      finally
      {
         IOUtils.closeQuietly(writer);
      }
   }

   private Properties getStartupProperties()
   {
      if (m_startupProperties == null)
      {
         if (m_propFile != null)
            loadStartupProperties();
         else
            m_startupProperties = new Properties();
      }
      
      return m_startupProperties;
   }

   public void setStartupProperties(Properties startupProperties)
   {
      Validate.notNull(startupProperties);
      m_startupProperties = startupProperties;
   }

   /**
    * Set the path to the startup properties and read them in.
    * 
    * @param propFilePath The path, relative to the install root, not <code>null</code>, must exist and
    * reference a valid properties file.
    */
   public void setPropFilePath(String propFilePath)
   {
      m_propFile = new File(PSServer.getRxDir(), propFilePath);
   }

   protected void loadStartupProperties()
   {
      Reader reader = null;
      try
      {
         reader = new FileReader(m_propFile);
         m_startupProperties = new Properties();
         m_startupProperties.load(reader);
      }
      catch (FileNotFoundException e)
      {
         String msg = "Failed to read startup prop file: " + m_propFile.getAbsolutePath();
         throw new IllegalArgumentException(e);
      }
      catch (IOException e)
      {
         String msg = "Failed to load startup properties: " + m_propFile.getAbsolutePath();
         log.error(msg);
         throw new RuntimeException(msg, e);
      }
      finally
      {
         IOUtils.closeQuietly(reader);
      }
   }
}
