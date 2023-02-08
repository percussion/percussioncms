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
package com.percussion.services.notification.impl;

import com.percussion.error.PSExceptionUtils;
import com.percussion.services.notification.IPSNotificationListener;
import com.percussion.services.notification.IPSNotificationService;
import com.percussion.services.notification.PSNotificationEvent;
import com.percussion.services.notification.PSNotificationEvent.EventType;
import com.percussion.util.PSBaseBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Synchronized notification service implementation
 */
@PSBaseBean("sys_notificationService")
public class PSNotificationService implements IPSNotificationService
{
   /**
    * The log to use
    */
   private static final Logger log = LogManager.getLogger(PSNotificationService.class);

   /**
    * The listeners to call
    */
   // See following for info on constructor params https://ria101.wordpress.com/2011/12/12/concurrenthashmap-avoid-a-common-misuse/
   private Map<EventType, Collection<IPSNotificationListener>> m_queue = new ConcurrentHashMap<>(8, 0.9f, 1);
   
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
            log.error("Problem in notification: {}",
                    PSExceptionUtils.getMessageForLog(e));
            log.debug(PSExceptionUtils.getDebugMessageForLog(e));
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
               queue = new ArrayList<>();
               m_queue.put(type, queue);
            }
         }
         
      }
      return queue;
   }
   
}
