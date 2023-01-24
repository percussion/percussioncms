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
package com.percussion.utils.spring;

import com.mockrunner.jms.DestinationManager;
import com.mockrunner.mock.jms.JMSMockObjectFactory;
import com.mockrunner.mock.jms.MockConnectionFactory;
import com.percussion.utils.jndi.PSNamingContextHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;
import javax.naming.NamingException;
import java.util.Map;

/**
 * Create the connection factory for the mock JMS system. This bean registers
 * the created factory as <em>jdbc/ConnectionFactory</em>.
 * 
 * @author dougrand
 * 
 */
public class PSMockJmsConnectionFactoryHelper
{

   private static PSMockJmsConnectionFactoryHelper instance = null;
   /**
    * Logger used for publisher service
    */
   private static final Logger ms_log = LogManager.getLogger(PSMockJmsConnectionFactoryHelper.class);

   /**
    * Property binding for the initial JMS connection factory JNDI name.
    * This constant must be the same as the "jndiName" property specified by
    * "sys_jmsConnectionFactory" bean in beans.xml
    */
   private static final String JMS_CONNECTION_FACTORY = "java:comp/env/jms/ConnectionFactory";

   public static JMSMockObjectFactory getMs_mockFactory() {
      return ms_mockFactory;
   }

   /**
    * Mock object factory
    */
   private static JMSMockObjectFactory ms_mockFactory = new JMSMockObjectFactory();

   /**
    * Destinations to configure, set from the spring configuration.
    */
   Map<String, String> m_destinations = null;

   /**
    * The connection factory, never <code>null</code> after ctor.
    */
   private static MockConnectionFactory m_jmsConnectionFactory;


   /**
    * The context helper, set in the ctor
    */
   private PSNamingContextHelper m_helper = null;

   /**
    * Create the instance
    * 
    * @param helper the jndi helper, never <code>null</code>.
    * @throws NamingException
    */
   public PSMockJmsConnectionFactoryHelper(PSNamingContextHelper helper)
   throws NamingException {
      if (helper == null)
      {
         throw new IllegalArgumentException("helper may not be null");
      }
      m_jmsConnectionFactory = ms_mockFactory.createMockConnectionFactory();
      m_helper = helper;
      m_helper.addBareBinding(JMS_CONNECTION_FACTORY, m_jmsConnectionFactory);
   }

   /**
    * @return the destinations
    */
   public Map getDestinations()
   {
      return m_destinations;
   }

   /**
    * @param destinations the destinations to set
    * @throws JMSException
    * @throws NamingException
    */
   @SuppressWarnings("unchecked")
   public void setDestinations(Map destinations)
         throws JMSException, NamingException
   {
      m_destinations = destinations;
      DestinationManager destmgr = ms_mockFactory.getDestinationManager();
      // Add destinations to factory. The key of each entry is the jndi
      // name, the value is the string topic or queue
      for (Map.Entry<String, String> destination : m_destinations.entrySet())
      {
         String jndiname = destination.getKey();
         String type = destination.getValue();
         if (type.equals("topic"))
         {
            Topic t = destmgr.createTopic(jndiname);
            m_helper.addBareBinding(jndiname, t);
         }
         else if (type.equals("queue"))
         {
            Queue q = destmgr.createQueue(jndiname);
            m_helper.addBareBinding(jndiname, q);
         }
         else
         {
            ms_log.warn("Unknown type found " + type);
         }
      }

   }
}
