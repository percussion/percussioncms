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
package com.percussion.services.notification.filemonitor;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Find and return the File Monitor Notification service.
 * 
 */
public class PSFileMonitorServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSFileMonitorService fms=null;
   /**
    * Find and return the notification service
    * @return the notification service, never <code>null</code>
    */
   public static IPSFileMonitorService getFileMonitorService()
   {
       if (fms==null)
       {
           synchronized (PSFileMonitorServiceLocator.class)
           {
               if (fms==null)
               {
                   fms = (IPSFileMonitorService) getBean("sys_fileMonitorService");
               }
           }
       }
      return fms;
   }
}
