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

