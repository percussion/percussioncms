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
package com.percussion.services.notification.filemonitor.impl;

import java.io.File;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.percussion.services.notification.PSNotificationHelper;

/**
 * Class used to implement a Configuration File Listener for the File
 * Monitor Notification Service.
 *
 * NOTE that in its current implementation, this class is "called back" 
 * by all file changes within a monitored directory {@link #PSDirectoryWatcher.java}
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
                //System.out.println("Starting to monitor " + absPath);
            }
        }
    }

   /**
    * Called whenever the File Monitor Notification Service stops monitoring
    * a file.
    * 
    * @param monitoredResource The resource being monitored. Should be an 
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
              //System.out.println("Halting the monitoring of " + absPath);
          }
      }
    }

    /**
     * Called whenever the File Monitor Notification Service detects a file
     * being added to the monitored directory.
     * 
     * @param monitoredResource The resource being monitored. Should be an 
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
                //System.out.println(absPath + " is added");
            }
        }
    }

    /**
     * Called whenever the File Monitor Notification Service detects a change
     * to a file in the monitored directory.
     * 
     * @param monitoredResource The resource being monitored. Should be an 
     * instance of a File object. Can be <code>null</code>.
     */
    public void onChange(Object changedResource)
    {
       if (ms_log.isDebugEnabled())
          ms_log.debug("onChange");
       
        if (changedResource instanceof File)
        {
            File file = (File) changedResource;
            String absPath = file.getAbsolutePath();

            if (ms_log.isDebugEnabled())
               ms_log.debug("file-path: " + absPath);

            if (file.isFile() && absPath.equals(m_configFileFullPath)) 
            {
               if (ms_log.isDebugEnabled())
                  ms_log.debug("notifyFile file-path: " + absPath);

               PSNotificationHelper.notifyFile((File) changedResource);                  
            }

        }
    }

    /**
     * Called whenever the File Monitor Notification Service detects a deletion
     * of a file in the monitored directory.
     * 
     * @param monitoredResource The resource being monitored. Should be an 
     * instance of a File object. Can be <code>null</code>.
     */
    public void onDelete(Object deletedResource)
    {
        if (deletedResource instanceof String)
        {
            String deletedFile = (String) deletedResource;
            System.out.println(deletedFile + " is deleted");
        }
    }
    
    /**
     * Logger for this class.
     */
    private static final Logger ms_log = LogManager.getLogger("PSConfigFileListener");
}

