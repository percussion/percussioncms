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
 * A basic strategy with some default behaviors, allows definition of custom
 * strategies for methods not conforming to the pattern of the pre-defined
 * strategies.  Implementers should only need to be concerned with the abstract
 * methods defined by this class.
 *
 * See the {@link com.percussion.webservices.aop.security.IPSWsMethod}, 
 * {@link com.percussion.webservices.aop.security.IPSWsStrategy}, and 
 * {@link com.percussion.webservices.aop.security.IPSWsPermission} annotations
 * for more information.
 */
public abstract class PSCustomSecurityStrategy extends PSSecurityStrategy
{
   /**
    * Determine if this strategy should handle the name of the method being 
    * invoked.
    * 
    * @param name The name of the method, never <code>null</code> or empty.
    * 
    * @return <code>true</code> to accept this invocation for processing, 
    * <code>false</code> if this strategy will not handle the method call.
    */
   protected abstract boolean acceptName(String name);
   
   /**
    * Get the index of the argument to filter during pre-process.  See 
    * {@link PSSecurityStrategy#filterArg(int, PSPermissions)} for
    * details on how arguments are filtered.
    * 
    * @return The index, or -1 to avoid preprocessing.
    */
   protected abstract int getFilterArg();

   /**
    * Get the permission to use for filtering.  Applied to either the argument 
    * specified by {@link #getFilterArg()}, or the return value from the method
    * invocation if {@link #shouldPostProcess()} is <code>true</code>.
    * 
    * @return The permission, never <code>null</code>.
    */
   protected abstract PSPermissions getRequiredPermission();
   
   /**
    * Determine if post processing should be done.  If not, 
    * {@link #postProcess(Object)} simply returns.  Otherwise if inputs were
    * not filtered, the results are filtered.  If any objects were filtered 
    * either from arguments or results, {@link #shouldReturnResults()} is called
    * to determine if the resulting return value is simply returned, or if
    * an exception should be thrown.  
    * 
    * @return <code>true</code> to post process, <code>false</code> otherwise.
    */
   protected abstract boolean shouldPostProcess();

   /** 
    * Determine if filtered results are simply returned, or if an exeption is
    * to be thrown.  If the latter, see 
    * {@link PSSecurityStrategy#handleException(java.util.Map, Object) 
    * super.handleException} for details on exception handling.  Note that if
    * this returns <code>true</code> and the result is not a collection, an
    * exception will be thrown.
    * 
    * @return <code>true</code> to return the results, <code>false</code> to 
    * throw an exception.
    */
   protected abstract boolean shouldReturnResults();
   
   @Override
   protected boolean accept(MethodInvocation invocation)
   {
      String name = invocation.getMethod().getName(); 
      if (acceptName(name)) 
      {
         logAccept(invocation);
         
         return true;
      }
      return false;
   }

   @Override
   public void preProcess()
   {
      logDebugMsg("preProcess by " + getClass().getName());
      int filterArg = getFilterArg();
      if (filterArg >= 0)
         m_failedGuids = filterArg(filterArg, getRequiredPermission());
   }

   @Override
   public void postProcess(Object result) throws PSErrorResultsException,
      PSErrorsException, PSErrorException
   {
      if (!shouldPostProcess())
         return;
      
      logDebugMsg("postProcess by " + getClass().getName());
      
      if (m_failedGuids == null)
         m_failedGuids = filterObject(result, getRequiredPermission());
      
      if (m_failedGuids != null)
      {
         if (m_failedGuids.isEmpty())
            return;
         
         if (shouldReturnResults() && result instanceof Collection)
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


   @Override
   public void processException(Exception e)
   {
      processErrorException(e, getRequiredPermission());
   }
}

