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

import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;
import java.io.Serializable;

/**
 * Helper for notifying events. Wraps the notification service into a series of
 * methods that dispatch specific events.
 * 
 * @author dougrand
 */
public class PSNotificationHelper
{
   /**
    * General method to notify an event
    * 
    * @param type the type of the event, never <code>null</code>
    * @param target the target of the event, see {@link EventType} for details,
    *           never <code>null</code>
    */
   public static void notifyEvent(EventType type, Serializable target)
   {
      if (type == null)
      {
         throw new IllegalArgumentException("type may not be null");
      }
      if (target == null)
      {
         throw new IllegalArgumentException("target may not be null");
      }
      IPSNotificationService nsvc = PSNotificationServiceLocator
            .getNotificationService();
      nsvc.notifyEvent(new PSNotificationEvent(type, target));
   }
   
   /**
    * Notify an object invalidation
    * @param id the id of the object being invalidated, never <code>null</code>
    */
   public static void notifyInvalidation(IPSGuid id)
   {
      notifyEvent(EventType.OBJECT_INVALIDATION, id);
   }
   
   /**
    * Notify a file modification
    * @param file the file being modified, never <code>null</code>
    */
   public static void notifyFile(File file)
   {
      notifyEvent(EventType.FILE, file);
   }  

   /**
    * Notify that Core Server is initialized, but no packages have been
    * installed yet.
    * @param serverRoot The root directory (identifier) of Server,
    * never <code>null</code>
    */
   public static void notifyServerInitComplete(File serverRoot)
   {
      notifyEvent(EventType.CORE_SERVER_INITIALIZED, serverRoot);
      notifyEvent(EventType.CORE_SERVER_POST_INIT, serverRoot);
   }  

   public static void notifyServerShutdown(File serverRoot)
   {
      notifyEvent(EventType.CORE_SERVER_SHUTDOWN, serverRoot);
   }
}
