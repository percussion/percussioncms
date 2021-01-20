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
package com.percussion.services.jms.impl;

import com.percussion.services.jms.IPSQueueSender;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

/**
 * The implementation of the {@link IPSQueueSender} interface.
 * The (spring bean's) properties of {@link #getJndiConnectionFactory()}
 * and {@link #getJndiQueue()} are expected to be configured (or wired) by 
 * spring framework. 
 */
public class PSQueueSender implements IPSQueueSender
{
   /**
    * The logger for this class.
    */
   private static Logger ms_log = Logger.getLogger(PSQueueSender.class);

   /**
    * The connection factory. It is set by Spring bean framework.
    * It is not <code>null</code> if configured properly.
    */
   private QueueConnectionFactory m_connectionFactory;
   
   /**
    * The JMS point to point queue. It is set by Spring bean framework.
    * It is not <code>null</code> if configured properly.
    */
   private Queue m_queue = null;
   
   /**
    * A wapper class of {@link QueueSender}. It sets delivery mode to
    * {@link javax.jms.DeliveryMode#PERSISTENT} and the send message will be
    * retain indefinitely by the message system. 
    */
   private class PSSender
   {
      /**
       * The queue connection created by the connection factory, never 
       * <code>null</code> or modified after the call of {@link #open()}.
       */
      QueueConnection mi_conn;

      /**
       * The queue session, never <code>null</code> or modified after the call 
       * of {@link #open()}.
       */
      QueueSession mi_session;

      /**
       * The queue sender, never <code>null</code>after the call of 
       * {@link #open()}.
       */
      QueueSender mi_sender;
      
      /**
       * Creates connection, session and sender of the message queue.
       * 
       * @throws JMSException if failed to create sender.
       */
      void open() throws JMSException
      {
         mi_conn = getConnectionFactory().createQueueConnection();
         mi_session = mi_conn.createQueueSession(false,
               QueueSession.AUTO_ACKNOWLEDGE);
         mi_conn.start();
         mi_sender = mi_session.createSender(getDestination());
         
         mi_sender.setDeliveryMode(javax.jms.DeliveryMode.NON_PERSISTENT);
         
         mi_sender.setTimeToLive(0);
      }
      
      /**
       * Sends a specified message to the queue.
       * @param msg the message to send, assumed not <code>null</code>.
       * @throws JMSException if failed to send the message.
       */
      void send(Serializable msg) throws JMSException
      {
         ObjectMessage omsg = mi_session.createObjectMessage();
         omsg.setObject(msg);
         mi_sender.send(omsg);
      }
      
      /**
       * A wrapper method of {@link javax.jms.MessageProducer#setPriority(int)}
       * @param priority the new priority.
       * @throws JMSException if fail to set the priority.
       */
      void setPriority(int priority) throws JMSException
      {
         if (priority < IPSQueueSender.PRIORITY_LOWEST
               || priority > IPSQueueSender.PRIORITY_HIGHEST)
         {
            throw new IllegalArgumentException("priority must be between "
                  + IPSQueueSender.PRIORITY_LOWEST + " and "
                  + IPSQueueSender.PRIORITY_HIGHEST);
         }

         mi_sender.setPriority(priority);
      }

      /**
       * A wrapper method of {@link javax.jms.MessageProducer#getPriority()}
       * @return the message priority for this message sender.
       * @throws JMSException if fail to get the priority.
       */
      int getPriority() throws JMSException
      {
         return mi_sender.getPriority();
      }
      
      /**
       * Close connection/session to the queue and release all resources that
       * hold by the sender.
       */
      void close()
      {
         try
         {
            mi_conn.stop();
            mi_session.close();
            mi_conn.close();
         }
         catch (Exception e)
         {
            ms_log.error("Error close JMS sender", e);
         }
      }
   }
   
   /*
    * //see interface method for details
    */
   public void sendMessage(Serializable msg, int priority)
   {
      if (msg == null)
         throw new IllegalArgumentException("message cannot be null.");
      
      if (priority < IPSQueueSender.PRIORITY_LOWEST
            || priority > IPSQueueSender.PRIORITY_HIGHEST)
      {
         throw new IllegalArgumentException("priority must be between "
               + IPSQueueSender.PRIORITY_LOWEST + " and "
               + IPSQueueSender.PRIORITY_HIGHEST);
      }
      
      PSSender sender = new PSSender();
      try
      {
         sender.open();
         sender.setPriority(priority);
         sender.send(msg);
      }
      catch (Exception e)
      {
         ms_log.error("Error when open JMS connection or send message", e);
      }
      finally
      {
         sender.close();
      }
      
   }

   /*
    * //see interface method for details
    */
   public void sendMessage(Serializable msg)
   {
      sendMessage(msg, IPSQueueSender.PRIORITY_LOWEST);
   }
   
   /*
    * //see interface method for details
    */
   public void sendMessages(List< ? extends Serializable> msgs,
         int priority)
   {
      sendMessages(msgs.iterator(), priority);
   }
   
   public void sendMessages(Iterator< ? extends Serializable> msgs,
         int priority)
   {
      // Do not need to synchronized because each call creates its own PSSender
      // synchronizing this method prevents other messages from being sent until this
      // one has finished, this could take a long time if it is processing all content list
      // items.
      
      if (msgs == null)
         throw new IllegalArgumentException("message's list cannot be null.");

      if (priority < IPSQueueSender.PRIORITY_LOWEST
            || priority > IPSQueueSender.PRIORITY_HIGHEST)
      {
         throw new IllegalArgumentException("priority must be between "
               + IPSQueueSender.PRIORITY_LOWEST + " and "
               + IPSQueueSender.PRIORITY_HIGHEST);
      }
      
      if ( ! msgs.hasNext() )
         return;
      
      PSSender sender = new PSSender();
      try
      {
         sender.open();
         sender.setPriority(priority);
         int count = 0;
         while ( msgs.hasNext() )
         {
            try {
            Serializable msg = msgs.next();
            sender.send(msg);
            } 
            catch (Exception e)
            {
               ms_log.error("Error when open JMS connection or send message", e);
               throw new RuntimeException("Error sending item to Queue: ", e);
            }
            count++;
         }
         ms_log.debug("Finished sending messages for a total count of: " + count);
      }
      catch (JMSException e)
      {
         ms_log.error("Error with JMS connection ", e);
         throw new RuntimeException("Error when open JMS connection ", e);
      }
      finally
      {
         sender.close();
      }
   }

   /*
    * //see interface method for details
    */
   public void sendMessages(List<? extends Serializable> msgs)
   {
      sendMessages(msgs, IPSQueueSender.PRIORITY_LOWEST);
   }
   
   /**
    * Gets the Connection Factory used to connect to the message queue.
    * @return the Connection Factory, never <code>null</code> when it is 
    * properly configured.
    */
   public QueueConnectionFactory getConnectionFactory()
   {
      return m_connectionFactory;
   }

   /**
    * Sets the Connection Factory used to connect to the message queue. This is
    * called by Spring bean framework.
    */
   public void setConnectionFactory(QueueConnectionFactory factory)
   {
      if (factory == null)
         throw new IllegalArgumentException("factory may not be null.");
      
      m_connectionFactory = factory;
   }
   
   /**
    * Gets the message Queue.
    * @return the message queue, never <code>null</code>.
    * @throws NamingException if failed to find the message queue by the
    *    JNDI name of {@link #getJndiQueue()}.
    */
   public Queue getDestination()
   {
      return m_queue;
   }   

   /**
    * Sets the property of JNDI name that references to a point to point queue. 
    * @param jndiName the new JNDI name of the queue, must not be 
    *    <code>null</code> or empty.
    */
   public void setDestination(Queue q)
   {
      if (q == null)
         throw new IllegalArgumentException("q must not be null.");
      
      m_queue = q;
   }   
}
