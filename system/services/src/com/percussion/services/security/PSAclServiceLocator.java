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
package com.percussion.services.security;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.error.PSMissingBeanConfigurationException;

/**
 * 
 */
public class PSAclServiceLocator extends PSBaseServiceLocator
{
   
   private static volatile IPSAclService aclService = null;
   /**
    * Get object manager
    * 
    * @return object manager, never <code>null</code>
    * @throws PSMissingBeanConfigurationException
    */
   public static IPSAclService getAclService()
      throws PSMissingBeanConfigurationException
   {
      if (aclService==null)
      {
         synchronized(PSAclServiceLocator.class)
         {
            if (aclService==null)
               aclService = (IPSAclService) getBean("sys_aclService"); //$NON-NLS-1$
         }
      }
      return aclService;
   }
}
