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
package com.percussion.services.notification.filemonitor.impl;

import com.percussion.cms.IPSConstants;
import com.percussion.services.notification.PSNotificationHelper;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Class used to implement a Configuration File Listener for the File
 * Monitor Notification Service.
 *
 * NOTE that in its current implementation, this class is "called back" 
 * by all file changes within a monitored directory
 * and therefore must use the file's name to differentiate between instances of
 * this class.
 */
public class PSConfigFileListener extends PSBaseListener 
   implements IPSFileListener 
{

   /**
    * Full path name of the file to be monitored.
    */
   String m_configFileFullPath = null;
   
    /**
     * Constructors
     */
   private PSConfigFileListener() 
   {
      super();
   }

   public PSConfigFileListener(String fullPath)
   {
      super();
      if (StringUtils.isBlank(fullPath))
         throw new IllegalArgumentException("fullPath may not be null or empty string");
      m_configFileFullPath = new String(fullPath);
   }

   /**
    * Called whenever the File Monitor Notification Service starts monitoring
    * a file.
    * 
    * @param monitoredResource The resource being monitored. Should be an 
    * instance of a File object. Can be <code>null</code>.
    */
   public void onStart(Object monitoredResource)
    {
        // On startup
        if (monitoredResource instanceof File)
        {
            File resource = (File) monitoredResource;
            String absPath = resource.getAbsolutePath();
            if (resource.isFile() && absPath.equals(m_configFileFullPath)) 
            {
                log.info("Starting to monitor {} ..." , absPath);
            }
        }
    }

   /**
    * Called whenever the File Monitor Notification Service stops monitoring
    * a file.
    * 
    * @param notMonitoredResource The resource being monitored. Should be an
    * instance of a File object. Can be <code>null</code>.
    */
    public void onStop(Object notMonitoredResource)
    {
       if (notMonitoredResource instanceof File)
       {
          File resource = (File) notMonitoredResource;
          String absPath = resource.getAbsolutePath();
          if (resource.isFile() && absPath.equals(m_configFileFullPath)) 
          {
              log.info("Halting the monitoring of {}" , absPath);
          }
      }
    }

    /**
     * Called whenever the File Monitor Notification Service detects a file
     * being added to the monitored directory.
     * 
     * @param newResource The resource being monitored. Should be an
     * instance of a File object. Can be <code>null</code>.
     */
    public void onAdd(Object newResource)
    {
        if (newResource instanceof File)
        {
            File file = (File) newResource;
            String absPath = file.getAbsolutePath();
            if (file.isFile() && absPath.equals(m_configFileFullPath)) 
            {
                log.info("{} is now monitored",absPath);
            }
        }
    }

    /**
     * Called whenever the File Monitor Notification Service detects a change
     * to a file in the monitored directory.
     * 
     * @param changedResource The resource being monitored. Should be an
     * instance of a File object. Can be <code>null</code>.
     */
    public void onChange(Object changedResource)
    {
       if (log.isDebugEnabled())
          log.debug("onChange");
       
        if (changedResource instanceof File)
        {
            File file = (File) changedResource;
            String absPath = file.getAbsolutePath();

            if (log.isDebugEnabled())
               log.debug("onChange for file with file-path: {}" , absPath);

            if (file.isFile() && absPath.equals(m_configFileFullPath)) 
            {
               if (log.isDebugEnabled())
                  log.debug("notifyFile file-path: {}" , absPath);

               PSNotificationHelper.notifyFile((File) changedResource);                  
            }

        }
    }

    /**
     * Called whenever the File Monitor Notification Service detects a deletion
     * of a file in the monitored directory.
     * 
     * @param deletedResource The resource being monitored. Should be an
     * instance of a File object. Can be <code>null</code>.
     */
    public void onDelete(Object deletedResource)
    {
        if (deletedResource instanceof String)
        {
            String deletedFile = (String) deletedResource;
            log.info( "Resource: {} was deleted.", deletedFile);
        }
    }
    
    /**
     * Logger for this class.
     */
    private static final Logger log = LogManager.getLogger(IPSConstants.SERVER_LOG);
}

