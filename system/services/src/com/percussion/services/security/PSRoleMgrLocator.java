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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.security;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

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

