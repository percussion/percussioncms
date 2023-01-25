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

/**
 * The notification service allows the notification of change events to
 * propagate from services to interested parties without tight coupling of the
 * parties.
 * 
 * @author dougrand
 * 
 */
public interface IPSNotificationService
{
   /**
    * Add a new listener to the notification queue. The queue's order should be
    * considered indeterminate and listeners should not rely on the order being
    * maintained over time. If a listener is already on the queue, this call is
    * a no-op.
    * 
    * @param type the type of event to register the listener for, never
    *           <code>null</code>
    * @param listener the listener to add to the queue, never <code>null</code>
    */
   void addListener(EventType type, IPSNotificationListener listener);

   /**
    * Remove a listener from the notification queue. If the listener is not on
    * the queue, this call is a no-op.
    * 
    * @param type the type of event to register the listener for, never
    *           <code>null</code>
    * @param listener the listener to remove to the queue, never
    *           <code>null</code>
    */
   void removeListener(EventType type, IPSNotificationListener listener);

   /**
    * Notify all listeners that the passed event has "happened". The listeners
    * should respond quickly. Listeners should not rely on queue order as
    * there's no guarantee of event arrival order.
    * 
    * @param event notification event, never <code>null</code>
    */
   void notifyEvent(PSNotificationEvent event);

}
