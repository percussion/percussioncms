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
package com.percussion.services.security.loginmods;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator for security services.
 * @author dougrand
 */
public class PSLoginMgrLocator extends PSBaseServiceLocator
{
   private static volatile IPSLoginMgr lmgr=null;
   /**
    * Get an instance of the login manager
    * @return the login manager, never <code>null</code>
    * @throws PSMissingBeanConfigurationException if the login manager is not found
    */
   public static IPSLoginMgr getLoginManager() throws PSMissingBeanConfigurationException
   {
       if (lmgr==null)
       {
           synchronized (PSLoginMgrLocator.class)
           {
               if (lmgr==null)
               {
                   lmgr = (IPSLoginMgr) getBean("sys_loginMgr");
               }
           }
       }
      return lmgr;
   }
}
