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
package com.percussion.fastforward.managednav;

import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An abstract base class for effects. This extends IPSDefaultExtension by
 * providing default behavior for the 3 methods specified in IPSEffect.
 * 
 * @author DavidBenua
 */
public abstract class PSNavAbstractEffect extends PSDefaultExtension
      implements
         IPSEffect
{
   /**
    * Always returns SUCCESS.
    * 
    * @see com.percussion.relationship.IPSEffect#test(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext,
    *      com.percussion.relationship.IPSExecutionContext,
    *      com.percussion.relationship.PSEffectResult)
    */
   public void test(Object[] params, IPSRequestContext req,
         IPSExecutionContext excontext, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      m_log.debug("Test() Method");
      logExecution(excontext);
      result.setSuccess();
      return;
   }

   /**
    * Does nothing and returns SUCCESS.
    * 
    * @see com.percussion.relationship.IPSEffect#attempt(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext,
    *      com.percussion.relationship.IPSExecutionContext,
    *      com.percussion.relationship.PSEffectResult)
    */
   public void attempt(Object[] params, IPSRequestContext req,
         IPSExecutionContext excontext, PSEffectResult result)
         throws PSExtensionProcessingException, PSParameterMismatchException
   {
      m_log.debug("Attempt() Method");
      logExecution(excontext);
      result.setSuccess();
      return;
   }

   /**
    * Does nothing and returns SUCCESS.
    * 
    * @see com.percussion.relationship.IPSEffect#recover(java.lang.Object[],
    *      com.percussion.server.IPSRequestContext,
    *      com.percussion.relationship.IPSExecutionContext,
    *      com.percussion.extension.PSExtensionProcessingException,
    *      com.percussion.relationship.PSEffectResult)
    */
   public void recover(Object[] params, IPSRequestContext req,
         IPSExecutionContext excontext,
         PSExtensionProcessingException exception, PSEffectResult result)
         throws PSExtensionProcessingException
   {
      m_log.debug("Recover() Method");
      logExecution(excontext);
      result.setSuccess();
      return;
   }

   /**
    * Logs an effect execution context. *
    * 
    * @param ex the execution context to log.
    */
   protected void logExecution(IPSExecutionContext ex)
   {
      this.m_log.debug(String.valueOf(ex.getContextType()));
   }

   /**
    * set the exclusion flag.
    * 
    * @param req the request context of the caller.
    * @param b the new exclusion value. <code>true</code> means that
    *           subsequent effects should not interfere with event processing.
    */
   protected void setExclusive(IPSRequestContext req, boolean b)
   {
      req.setPrivateObject(EXCLUSION_FLAG, b);
   }

   /**
    * tests if the exclusion flag is on.
    * 
    * @param req the parent request context.
    * @return <code>true</code> if the exclusion flag is set.
    */
   protected boolean isExclusive(IPSRequestContext req)
   {
      Boolean b = (Boolean) req.getPrivateObject(EXCLUSION_FLAG);
      if (b == null)
         return false;
      return b.booleanValue();
   }

   /**
    * Writes the log.
    */
   protected static final Logger m_log = LogManager.getLogger(PSNavAbstractEffect.class);

   /**
    * The exclusion flag is used to prevent item operations inside the effect
    * from triggering another effect. That is, to prevent recursive execution of
    * the effect. This value is used as a request private object of type
    * <code>Boolean</code>
    */
   private static final String EXCLUSION_FLAG = 
      "com.percussion.consulting.nav.PSExclusionFlag";
}
