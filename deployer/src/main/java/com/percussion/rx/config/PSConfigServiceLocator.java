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
package com.percussion.rx.config;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * Locator for getting the config service.
 * @author bjoginipally
 *
 */
public class PSConfigServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSConfigService csr = null;
   /**
    * Find and return the config service
    * 
    * @return config service, never <code>null</code>
    * @throws PSMissingBeanConfigurationException if bean is missing
    */
   public static IPSConfigService getConfigService()
         throws PSMissingBeanConfigurationException
   {
       if (csr==null)
       {
           synchronized (PSConfigServiceLocator.class)
           {
               if (csr==null)
               {
                   csr = (IPSConfigService) getBean("sys_configService");
               }
           }
       }
      return csr; 
   }

}
