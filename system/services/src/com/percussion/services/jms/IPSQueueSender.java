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
package com.percussion.services.jms;

import com.percussion.services.publisher.IPSEdition;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * A JMS client to send messages to its message queue destination.
 */
public interface IPSQueueSender
{
   /**
    * The highest priority used for the out of bound messages, such as
    * start / end publishing jobs. 
    */
   int PRIORITY_HIGHEST = IPSEdition.Priority.HIGHEST.getValue() + 1;

   /**
    * The lowest priority, which is equivalent of 
    * {@link IPSEdition.Priority#LOWEST}
    */
   int PRIORITY_LOWEST = IPSEdition.Priority.LOWEST.getValue();
   
   /**
    * Sends a message to the queue with the specified priority.
    * 
    * @param msg the message to send, never <code>null</code>.
    * @param priority the priority for the messages. It should be a value
    *    between {@link #PRIORITY_LOW} and {@link #PRIORITY_HIGH}.
    */
   void sendMessage(Serializable msg, int priority);
   

   /**
    * Sends a message to the queue with {@link #PRIORITY_LOW} priority.
    * 
    * @param msg the message to send, never <code>null</code>.
    */
   void sendMessage(Serializable msg);
   
   /**
    * Sends a list of messages to the queue with the specified priority.
    * 
    * @param msgs the list of messages to send, never <code>null</code>, but
    *    may be empty.
    * @param priority the priority for the messages. It should be a value
    *    between {@link #PRIORITY_LOW} and {@link #PRIORITY_HIGH}.
    */
   void sendMessages(List<? extends Serializable> msgs, int priority);
   
   /**
    * Sends a sequence of messages to the queue with the specified priority.
    * 
    * @param msgs the list of messages to send, never <code>null</code>, but
    *    may be empty.
    * @param priority the priority for the messages. It should be a value
    *    between {@link #PRIORITY_LOW} and {@link #PRIORITY_HIGH}.
    */
   void sendMessages(Iterator<? extends Serializable> msgs, int priority);
   
   /**
    * Sends a list of messages to the queue with {@link #PRIORITY_LOW} 
    * priority.
    * 
    * @param msgs the list of messages to send, never <code>null</code>, but
    *    may be empty. Each element of the list must be a 
    *    <code>Serializable</code> Java object.
    */
   void sendMessages(List<? extends Serializable> msgs);
}
