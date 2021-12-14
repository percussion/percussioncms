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
