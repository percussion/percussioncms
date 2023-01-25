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

import org.aopalliance.intercept.MethodInvocation;

/**
 * Implements a security strategy for save methods.  The strategy is as follows:
 * <ul>
 * <li>
 * Accepts method names that start with "save", ignores non-design methods
 * based on the interface name of the invoking class.   
 * </li>
 * <li>
 * Filters the results using the UPDATE permission.
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
public class PSSaveSecurityStrategy extends PSSecurityStrategy
{
   @Override
   protected boolean accept(MethodInvocation invocation)
   {
      return isDesignService(invocation) && processAccept(invocation, "save");
   }

   @Override
   public void preProcess()
   {
      logDebugMsg("preProcess by " + getClass().getName());
      m_failedGuids = filterArg(0, PSPermissions.UPDATE);
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
      processErrorException(e, PSPermissions.UPDATE);
   }
}

