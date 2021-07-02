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
