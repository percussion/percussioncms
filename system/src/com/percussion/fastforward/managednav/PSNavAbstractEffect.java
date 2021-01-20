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
package com.percussion.fastforward.managednav;

import org.apache.log4j.Logger;

import com.percussion.extension.PSDefaultExtension;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.relationship.PSEffectResult;
import com.percussion.server.IPSRequestContext;

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
   protected Logger m_log = Logger.getLogger(PSNavAbstractEffect.class);

   /**
    * The exclusion flag is used to prevent item operations inside the effect
    * from triggering another effect. That is, to prevent recursive execution of
    * the effect. This value is used as a request private object of type
    * <code>Boolean</code>
    */
   private static final String EXCLUSION_FLAG = 
      "com.percussion.consulting.nav.PSExclusionFlag";
}
