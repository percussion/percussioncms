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
package com.percussion.services.notification;

import com.percussion.services.PSBaseServiceLocator;

/**
 * Find and return the notification service
 * 
 * @author dougrand
 */
public class PSNotificationServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSNotificationService nsr=null;
   /**
    * Find and return the notification service
    * @return the notification service, never <code>null</code>
    */
   public static IPSNotificationService getNotificationService()
   {
       if (nsr==null)
       {
           synchronized (PSNotificationServiceLocator.class)
           {
               if (nsr==null)
               {
                   nsr = (IPSNotificationService) getBean("sys_notificationService");
               }
           }
       }
      return nsr;
   }
}
