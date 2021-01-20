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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.locking;

import com.percussion.services.PSBaseServiceLocator;
import com.percussion.services.PSMissingBeanConfigurationException;

/**
 * Locator to get the locking service.
 */
public class PSObjectLockServiceLocator extends PSBaseServiceLocator
{
   private static volatile IPSObjectLockService ols=null;
   /**
    * Find and return the locking service.
    * 
    * @return the locking service, never <code>null</code>.
    * @throws PSMissingBeanConfigurationException if the bean is missing for 
    *    the requested service.
    */
   public static IPSObjectLockService getLockingService() 
      throws PSMissingBeanConfigurationException
   {
       if (ols==null)
       {
           synchronized (PSObjectLockServiceLocator.class)
           {
               if (ols==null)
               {
                   ols = (IPSObjectLockService) getCtx().getBean("sys_lockingService");
               }
           }
       }
      return ols;
   }
}

