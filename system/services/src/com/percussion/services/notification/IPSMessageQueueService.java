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
