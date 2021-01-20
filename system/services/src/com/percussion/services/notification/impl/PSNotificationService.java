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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.notification.impl;

import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Synchronized notification service implementation
 */
public class PSNotificationService implements IPSNotificationService
{
   /**
    * The log to use
    */
   private static Log ms_log = LogFactory.getLog(PSNotificationService.class);

   /**
    * The listeners to call
    */
   // See following for info on constructor params https://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/
   private Map<EventType, Collection<IPSNotificationListener>> m_queue = new ConcurrentHashMap<EventType, Collection<IPSNotificationListener>>(8, 0.9f, 1);
   
   private Object queueLock = new Object();
   /*
    * //see base class method for details
    */
   public void notifyEvent(PSNotificationEvent event)
   {
      Collection<IPSNotificationListener> queue = getQueue(event.getType());
  
      for (IPSNotificationListener l : queue)
      {
         try
         {
            l.notifyEvent(event);
         }
         catch (Exception e)
         {
            ms_log.error("Problem in notification", e);
         }
      }
 
   }

   /*
    * //see base class method for details
    */
   public void addListener(EventType type,
         IPSNotificationListener listener)
   {
      Collection<IPSNotificationListener> queue = getQueue(type);
      synchronized(queue)
      {
         if (queue.contains(listener))
            return;
         queue.add(listener);
      }
   }

   /*
    * //see base class method for details
    */
   public void removeListener(EventType type,
         IPSNotificationListener listener)
   {
      Collection<IPSNotificationListener> queue = getQueue(type);
      synchronized(queue)
      {
         if (!queue.contains(listener))
            return;
   
         queue.remove(listener);
      }
   }


   /**
    * Get queue. Create it if it doesn't already exist.
    * 
    * @param type the type of the queue, assumed never <code>null</code>
    * @return the queue, never <code>null</code>
    */
   private Collection<IPSNotificationListener> getQueue(EventType type)
   {
      Collection<IPSNotificationListener> queue = m_queue.get(type);
      if (queue == null)
      {
         synchronized (queueLock)
         {
            queue = m_queue.get(type);
            if (queue==null)
            {
               queue = new ArrayList<IPSNotificationListener>();
               m_queue.put(type, queue);
            }
         }
         
      }
      return queue;
   }
   
}
