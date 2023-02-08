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
package com.percussion.webservices.system;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator to get the system and system design webservices.
 */
public class PSSystemWsLocator extends PSBaseServiceLocator
{
   private static volatile IPSSystemWs sws=null;
   private static volatile IPSSystemDesignWs sdws=null;
   /**
    * Find and return the system webservice.
    * 
    * @return the system webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSystemWs getSystemWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (sws==null)
       {
           synchronized (PSSystemWsLocator.class)
           {
               if (sws==null)
               {
                   sws = (IPSSystemWs) getCtx().getBean("sys_systemWs");
               }
           }
       }
      return sws;
   }

   /**
    * Find and return the system design webservice.
    * 
    * @return the system design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSystemDesignWs getSystemDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (sdws==null)
       {
           synchronized (PSSystemWsLocator.class)
           {
               if (sdws==null)
               {
                   sdws = (IPSSystemDesignWs) getCtx().getBean("sys_systemDesignWs");
               }
           }
       }
      return sdws;
   }
}

