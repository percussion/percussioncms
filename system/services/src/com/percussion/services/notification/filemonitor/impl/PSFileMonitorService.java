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

import com.percussion.services.notification.filemonitor.IPSFileMonitorService;
import com.percussion.services.notification.filemonitor.impl.PSFileMonitorService;
import com.percussion.services.notification.filemonitor.impl.PSDirectoryWatcher;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

/**
 * File Modification notification service implementation.
 * <p>
 * Note, currently there is one PSDirectoryWatcher per directory
 * and one PSConfigFileListener for each file being tracked. Each 
 * PSDirectoryWatcher object invokes one thread.
 * This implementation is not suitable for monitoring files which have huge
 * amount of non-monitored sibling files. It would be overkill to use one
 * thread to monitor one file (or a few files), where all files are in different
 * directories, and the program has to use one PSDirectoryWatcher for each 
 * directory.
 * </p>
 */
public class PSFileMonitorService implements IPSFileMonitorService
{
   /**
    * Map of Directory Path Names to PSDirectoryWatcher objects.
    * Used by the service to track the active PSDirectoryWatchers.
    */
   static Map<String,PSDirectoryWatcher> m_dirWatcherMap = 
      new HashMap<>();
   
   /**
    * Map of File Path Names to PSConfigFileListener objects.
    * Used by the service to track the active PSDirectoryWatchers.
    */
   static Map<String,PSConfigFileListener> m_fileListenerMap = 
      new HashMap<>();

   /**
    * The polling interval in seconds for monitoring file changes.
    */
   private int m_defIntervalSec = 5;

   
   /**
    * Start a monitor for the file represented by the file object (using default
    * polling interval)
    *
    * @param fileObject The object representing file to monitor, never <code>null</code>
    * 
    */
   public void monitorFile(File fileObject)
   {
      monitorFile( fileObject, m_defIntervalSec);
   }
      
  /**
    * Start a monitor for the file represented by the file object
    *
    * @param fileObject The object representing file to monitor, never <code>null</code>
    * 
    * @param intervalSec The time between file checks (polling interval) in seconds.
    */
   public synchronized void monitorFile(File fileObject, int intervalSec)
   {

      if (fileObject == null)
         throw new IllegalArgumentException("fileObject may not be null");

      if (fileObject.isDirectory()) 
         throw new IllegalArgumentException("fileObject may not be a directory");
      
      String directoryName = fileObject.getParent();
      String fullName      = fileObject.getAbsolutePath();
      
      PSDirectoryWatcher dirWatcher = m_dirWatcherMap.get(directoryName);
      if (dirWatcher == null)
      {
        dirWatcher = new PSDirectoryWatcher(directoryName, intervalSec);
        m_dirWatcherMap.put( directoryName, dirWatcher);
        dirWatcher.start();
      }
      PSConfigFileListener configFileListener = 
         new PSConfigFileListener(fullName);
      dirWatcher.addListener(configFileListener);
      
      m_fileListenerMap.put(fullName, configFileListener);
   }

   
  /**
    * Start a monitor for the file represented by the file object
    *
    * @param fileObject the object representing file to monitor, never <code>null</code>
    */
   public synchronized void unmonitorFile(File fileObject)
   { 
      if (fileObject == null)
         throw new IllegalArgumentException("fileObject may not be null");

      if (fileObject.isDirectory()) 
         throw new IllegalArgumentException("fileObject may not be a directory");
      
      String directoryName = fileObject.getParent();
      String fullName      = fileObject.getAbsolutePath();
      
      PSDirectoryWatcher dirWatcher = m_dirWatcherMap.get( directoryName);
      if (dirWatcher == null)
         return;
      
      PSConfigFileListener configFileListener = m_fileListenerMap.get(fullName);

      dirWatcher.removeListener(configFileListener);
      if (dirWatcher.getListenerCount() == 0)
      {
         dirWatcher.stop();
         m_dirWatcherMap.remove(directoryName);
      }
      
      m_fileListenerMap.remove(fullName);
   }

   /**
    * Gets the number of directory watcher. 
    * 
    * @return the number of directory wa
    */
   public int getDirWatcherCount()
   {
      return m_dirWatcherMap.size();
   }
}

