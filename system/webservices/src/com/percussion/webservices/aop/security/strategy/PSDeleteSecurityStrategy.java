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
package com.percussion.webservices.aop.security.strategy;



import com.percussion.services.security.PSPermissions;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Implements a security strategy for delete methods.  The strategy is as follows:
 * <ul>
 * <li>
 * Accepts method names that start with "delete", ignores non-design methods
 * based on the interface name of the invoking class.
 * </li>
 * <li>
 * Filters the results using the DELETE permission.
 * </li> 
 * <li>
 * If the first argument is a guid, has a getGUID() method (case-insensitive),
 * or is a collection of either one of those, it filters the incoming values 
 * against the appropriate permission.  
 * </li>
 * <li>
 * Errors and results (if any) are appropriately thrown in a 
 * {@link PSErrorsException} if the signature allows this.  If a single 
 * object is returned, or if the method does not throw that exception then a 
 * {@link PSErrorException} is thrown if allowed, otherwise a runtime exception
 * is thrown.
 * </li>
 * </ul>
 * See the {@link com.percussion.webservices.aop.security.IPSWsMethod}, 
 * {@link com.percussion.webservices.aop.security.IPSWsStrategy}, and 
 * {@link com.percussion.webservices.aop.security.IPSWsPermission} annotations
 * for more information.
 */
public class PSDeleteSecurityStrategy extends PSSecurityStrategy
{
   @Override
   protected boolean accept(MethodInvocation invocation)
   {
      return isDesignService(invocation) && processAccept(invocation, "delete");
   }

   @Override
   public void preProcess()
   {
      logDebugMsg("preProcess by " + getClass().getName());
      m_failedGuids = filterArg(0, PSPermissions.DELETE);
   }

   @Override
   public void postProcess(Object result) throws PSErrorResultsException, 
      PSErrorException, PSErrorsException
   {
      // merge
      if (m_failedGuids != null)
      {
         if (m_failedGuids.isEmpty())
            return;
         
         // create exception and add errors and results
         handleException(m_failedGuids, result);
      }
      else
      {
         handleBadSecurityConfig();
      }      
   }

   @Override
   public void processException(Exception e)
   {
      processErrorException(e, PSPermissions.DELETE);
   }
}

