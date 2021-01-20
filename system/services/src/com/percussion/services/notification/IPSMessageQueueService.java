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

import java.io.Serializable;

/**
 * A service that provides MQ for various message types.
 * Only one listener can listen to a distinct message type (Class).
 * 
 * @author adamgent
 *
 */
public interface IPSMessageQueueService
{
   /**
    * Sends a message to the queue.
    * <p>
    * The exact concrete class of the message is used to determine what
    * {@link IPSMessageQueueListener listener} to dispatch too. 
    * If there is no {@link IPSMessageQueueListener listener} listening for that concrete type 
    * then a warning maybe be logged, may not be null.
    * <p>
    * The call is asynchronous and should return immediately.
    * 
    * @param <T> The type of message.
    * @param message the message to send to the queue it must be {@link Serializable}.
    * @param priority the priority which is based on JMS, null indicates default priority.
    * @see #addQueueListener(Class, IPSMessageQueueListener)
    */
   <T extends Serializable> void sendMessage(T message, Integer priority);
   
   /**
    * Adds a listener to a distinct concrete type (Class).
    * There maybe only one 
    * @param <T> the type of message that the listener will receive.
    * @param messageType the type of message that the listener will receive, should not be a an abstract or interface class, 
    * never null.
    * @param listener not null.
    */
   public <T extends Serializable> void addListener(Class<T> messageType, IPSMessageQueueListener<T> listener);
   
   /**
    * Removes the listener for the given type. If there is no listener registered
    * nothing will happen.
    * @param <T> the type of message that the listener would have received.
    * @param messageType never null.
    */
   public <T extends Serializable> void removeListener(Class<T> messageType);
}
