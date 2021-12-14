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
