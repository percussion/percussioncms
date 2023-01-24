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
 * Provides lookup service for 
 * {@link com.percussion.services.security.IPSRoleMgr} and 
 * {@link com.percussion.services.security.IPSBackEndRoleMgr}.
 */
public class PSRoleMgrLocator extends PSBaseServiceLocator
{
   private static volatile IPSRoleMgr rml=null;
   private static volatile IPSBackEndRoleMgr berml=null;
   /**
    * Get an instance of the role manager
    * @return the role manager, never <code>null</code>
    * @throws PSMissingBeanConfigurationException if the role manager is not found
    */
   public static IPSRoleMgr getRoleManager() 
      throws PSMissingBeanConfigurationException
   {
       if (rml==null)
       {
           synchronized (PSRoleMgrLocator.class)
           {
               if (rml==null)
               {
                   rml = (IPSRoleMgr) getBean("sys_roleMgr");
               }
           }
       }
      return rml;
   }
   
   /**
    * Get an instance of the backend role manager
    * @return the backend role manager, never <code>null</code>
    * @throws PSMissingBeanConfigurationException if the backend role manager is 
    * not found
    */
   public static IPSBackEndRoleMgr getBackEndRoleManager() 
      throws PSMissingBeanConfigurationException
   {
       if (berml==null)
       {
           synchronized (PSRoleMgrLocator.class)
           {
               if (berml==null)
               {
                   berml = (IPSBackEndRoleMgr) getBean("sys_backEndRoleMgr");
               }
           }
       }
      return berml;
   }   
}

