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
