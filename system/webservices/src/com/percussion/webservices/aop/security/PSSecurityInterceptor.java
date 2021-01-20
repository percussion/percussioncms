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
package com.percussion.webservices.aop.security;

import com.percussion.webservices.aop.security.strategy.PSSecurityStrategy;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * Inteceptor class to handle automatically processing security around web 
 * service calls.
 */
public class PSSecurityInterceptor implements MethodInterceptor
{
   public Object invoke(MethodInvocation invocation) throws Throwable
   {
      PSSecurityStrategy strategy = null;
      
      if (!ignore(invocation))
         strategy = PSSecurityStrategy.getStrategy(invocation);
      
      if (strategy != null)
         strategy.preProcess();
      
      Object result = null;
      try
      {
         if (strategy == null || strategy.shouldProceed())
            result = invocation.proceed();
      }
      catch (Exception e)
      {
         if (strategy != null)
            strategy.processException(e);
         throw e;
      }
      
      if (strategy != null)
         strategy.postProcess(result);
      
      return result;
   }

   /**
    * Determine if the specified method should be processed at all
    * 
    * @param invocation The invocation specifying the method, assumed not 
    * <code>null</code>.
    * 
    * @return <code>true</code> to ignore, <code>false</code> otherwise.
    */
   private boolean ignore(MethodInvocation invocation)
   {
      IPSWsMethod ws = invocation.getMethod().getAnnotation(IPSWsMethod.class);
      if (ws != null)
         return ws.ignore();
      else
         return false;
   }
}

