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

import com.percussion.services.PSBaseServiceLocator;

/**
 * The locator for various Queue Senders. 
 */
public class PSQueueSenderLocator extends PSBaseServiceLocator
{
   private static  IPSQueueSender qsnd=null;
   /**
    * Gets the Queue Sender from the supplied bean name.
    * 
    * @param beanName the name of the Spring Bean of the Queue Sender, never
    *    <code>null</code> or empty.
    *    
    * @return the Queue Sender, never <code>null</code> in a correct 
    *    configuration.
    */
   public static synchronized IPSQueueSender getQueueSender(String beanName)
   {
      if (qsnd==null)
      {
         qsnd = (IPSQueueSender) getBean(beanName);
      }
      return qsnd;
   }   
}
