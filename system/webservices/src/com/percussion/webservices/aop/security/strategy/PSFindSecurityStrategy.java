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
package com.percussion.webservices.aop.security.strategy;

import com.percussion.services.security.PSPermissions;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSErrorsException;

import java.util.Collection;

import org.aopalliance.intercept.MethodInvocation;

/**
 * Implements a security strategy for load methods.  The strategy is as follows:
 * <ul>
 * <li>
 * Accepts method names that start with "find", ignores non-design methods
 * based on the interface name of the invoking class.      
 * </li>
 * <li>
 * Filters results based on the READ permission.  If a collection is returned,
 * objects that fail are removed from the collection.  If a single object is
 * returned (unlikely), if the check fails then a runtime exception is thrown.
 * </li>
 * <li>
 * Objects that will be returned in the results which are instances of 
 * {@link com.percussion.services.catalog.data.PSObjectSummary} have the user's
 * {@link com.percussion.services.security.data.PSUserAccessLevel} set on the 
 * summary. 
 * </li>
 * <li>
 * Exceptions thrown by the method are simply allowed to propagate unmodified.
 * </li>
 * </ul>
 * See the {@link com.percussion.webservices.aop.security.IPSWsMethod}, 
 * {@link com.percussion.webservices.aop.security.IPSWsStrategy}, and 
 * {@link com.percussion.webservices.aop.security.IPSWsPermission} annotations
 * for more information.
 */
public class PSFindSecurityStrategy extends PSSecurityStrategy
{
   @Override
   protected boolean accept(MethodInvocation invocation)
   {
      return isDesignService(invocation) && processAccept(invocation, "find");
   }

   @Override
   public void preProcess()
   {
      // noop
   }

   @Override
   public void postProcess(Object result) throws PSErrorResultsException, 
      PSErrorException, PSErrorsException
   {
      logDebugMsg("postProcess by " + getClass().getName());
      if (result != null)
      {
         m_failedGuids = filterObject(result, PSPermissions.READ);
         if (m_failedGuids != null)
         {
            if (m_failedGuids.isEmpty())
               return;
            
            if (result instanceof Collection)
            {
               return;
            }

            // throw appropriate exception
            handleException(m_failedGuids, result);
         }
         else
         {
            handleBadSecurityConfig();
         }
      }
   }

   @Override
   public void processException(Exception e)
   {
      // noop
      if (e == null);
   }

}

