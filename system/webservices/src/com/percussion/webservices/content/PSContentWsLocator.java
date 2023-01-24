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
package com.percussion.webservices.content;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator to get the content and content design webservices.
 */
public class PSContentWsLocator extends PSBaseServiceLocator
{
   
   private static volatile IPSContentWs cws = null;
   private static volatile IPSContentDesignWs cdws = null;
   
   /**
    * Find and return the content webservice.
    * 
    * @return the content webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSContentWs getContentWebservice() 
      throws PSMissingBeanConfigurationException
   {
      if (cws==null)
      {
         synchronized (PSContentWsLocator.class)
         {
            if (cws==null)
            {
               cws = (IPSContentWs) getCtx().getBean("sys_contentWs");
            }
         }
      }
      return cws;
   }

   /**
    * Find and return the content design webservice.
    * 
    * @return the content design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSContentDesignWs getContentDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
      if (cdws==null)
      {
         synchronized (PSContentWsLocator.class)
         {
            if (cdws==null)
            {
               cdws=(IPSContentDesignWs) getCtx().getBean("sys_contentDesignWs");
            }
         }
      }
      return cdws;
   }
}

