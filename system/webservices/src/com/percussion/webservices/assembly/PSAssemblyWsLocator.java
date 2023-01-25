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
package com.percussion.webservices.assembly;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator to get the assembly and assembly design webservices.
 */
public class PSAssemblyWsLocator extends PSBaseServiceLocator
{
   private static volatile IPSAssemblyWs aws=null;
   private static volatile IPSAssemblyDesignWs adws=null;
   /**
    * Find and return the assembly webservice.
    * 
    * @return the assembly webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSAssemblyWs getAssemblyWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (aws==null)
       {
           synchronized (PSAssemblyWsLocator.class)
           {
               if (aws==null)
               {
                   aws = (IPSAssemblyWs) getCtx().getBean("sys_assemblyWs");
               }
           }
       }
      return aws;
   }

   /**
    * Find and return the assembly design webservice.
    * 
    * @return the assembly design webservice, never <code>null</code>. 
    * @throws PSMissingBeanConfigurationException if the requested bean is 
    *    missing.
    */
   public static IPSAssemblyDesignWs getAssemblyDesignWebservice() 
      throws PSMissingBeanConfigurationException
   {
       if (adws==null)
       {
           synchronized (PSAssemblyWsLocator.class)
           {
               if (adws==null)
               {
                   adws = (IPSAssemblyDesignWs) getCtx().getBean("sys_assemblyDesignWs");
               }
           }
       }
      return adws;
   }
}

