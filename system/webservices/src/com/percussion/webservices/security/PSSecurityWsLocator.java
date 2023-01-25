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
package com.percussion.webservices.security;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator to get the security and security design webservices.
 */
public class PSSecurityWsLocator extends PSBaseServiceLocator
{
   private static volatile IPSSecurityWs sws=null;
   private static volatile IPSSecurityDesignWs sdws=null;
   /**
    * Find and return the security webservice.
    * 
    * @return the security webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSecurityWs getSecurityWebservice() 
      throws PSMissingBeanConfigurationException
   {
      if (sws==null)
      {
         synchronized (PSSecurityWsLocator.class)
         {
            if (sws==null)
            {
                return (IPSSecurityWs) getCtx().getBean("sys_securityWs");
            }
         }
      }
      return sws;
   }

   /**
    * Find and return the security design webservice.
    * 
    * @return the security design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSSecurityDesignWs getSecurityDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (sdws==null)
       {
           synchronized (PSSecurityWsLocator.class)
           {
               if (sdws==null)
               {
                   sdws = (IPSSecurityDesignWs) getCtx().getBean("sys_securityDesignWs");
               }
           }
       }
       return sdws;
   }
}

