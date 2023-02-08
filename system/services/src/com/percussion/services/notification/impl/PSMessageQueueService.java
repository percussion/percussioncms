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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.server.PSRequest;
import com.percussion.services.jms.IPSQueueSender;
import com.percussion.services.notification.IPSMessageQueueListener;
import com.percussion.services.notification.IPSMessageQueueService;
import com.percussion.utils.request.PSRequestInfo;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 
 * Dispatches to an implementation of {@link IPSMessageQueueListener} based
 * on the instance class of the message.
 * 
 * @author adamgent
 * @see #addListener(Class, IPSMessageQueueListener)
 */
public final class PSMessageQueueService implements MessageListener, IPSMessageQueueService
{

   /**
    * Logger to use, never <code>null</code>.
    */
   private static final Logger ms_logger = LogManager.getLogger(
         PSMessageQueueService.class);
   
   private IPSQueueSender m_queueSender;
   private ConcurrentHashMap<String, IPSMessageQueueListener<?>> queueMap = new ConcurrentHashMap<>();
   
   /**
    * Associates a Class with a single listener replacing any existing listener bound for that message
    * class.
    * 
    * @param <T> The type of message.
    * @param messageType objects of this type will be sent to 
    *  listener {@link IPSMessageQueueListener#onMessage(Serializable)}, not null.
    * @param listener the listener that will be called for objects of messageType, not null.
    * {@inheritDoc}
    */
   public <T extends Serializable> void addListener(Class<T> messageType, IPSMessageQueueListener<T> listener) {
      notNull(listener);
      queueMap.put(messageType.getCanonicalName(), listener);
   }
   
   /**
    * {@inheritDoc}
    */
   public <T extends Serializable> void removeListener(Class<T> messageType) {
      queueMap.remove(messageType.getCanonicalName());
   }
   
   /**
    * {@inheritDoc}
    */
   public <T extends Serializable> void sendMessage(T message, Integer priority)
   {
      if (priority == null) {
         getQueueSender().sendMessage(message);
      }
      else {
         getQueueSender().sendMessage(message, priority);
      }
   }
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public void onMessage(Message message)
   {
      try
      {
         PSRequest req = PSRequest.getContextForRequest();
         PSRequestInfo.initRequestInfo((Map) null);
         PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);

         if (message instanceof ObjectMessage)
         {
            ObjectMessage om = (ObjectMessage) message;
            Serializable object = om.getObject();
            String name = object.getClass().getCanonicalName();
            IPSMessageQueueListener<Serializable> ql = (IPSMessageQueueListener<Serializable>) 
               queueMap.get(name);
            if (ql == null) {
               ms_logger.error("No listener for type: " + name);
            }
            else {
               ql.onMessage(om.getObject());   
            }
         }
      }
      catch (JMSException e)
      {
         ms_logger.error("Cannot handle jms message", e);
      }
      finally
      {
         PSRequestInfo.resetRequestInfo();

         try
         {
            message.acknowledge();
         }
         catch (JMSException e)
         {
            ms_logger.error("Problem acknowledging message", e);
         }
      }
   }
   
   /**
    *  See setter.
    * @return never null.
    */
   public IPSQueueSender getQueueSender()
   {
      return m_queueSender;
   }
   /**
    * The queue wrapper to send messages.
    * @param queueSender never null.
    */
   public void setQueueSender(IPSQueueSender queueSender)
   {
      m_queueSender = queueSender;
   }

}
