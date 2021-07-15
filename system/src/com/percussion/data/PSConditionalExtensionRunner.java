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
package com.percussion.data;

import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.error.PSIllegalArgumentException;
import com.percussion.error.PSResult;
import com.percussion.extension.IPSExtension;
import com.percussion.extension.IPSRequestPreProcessor;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.IPSUdfProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.relationship.IPSEffect;
import com.percussion.relationship.IPSExecutionContext;
import com.percussion.security.PSAuthorizationException;
import com.percussion.server.PSRequestValidationException;

import java.util.Iterator;

import org.w3c.dom.Document;

/**
 * A conditional extension runner.
 */
public class PSConditionalExtensionRunner extends PSExtensionRunner
{
   /**
    * Constructs a new conditional extension runner that can run the given
    * extension, extracting data based on the params defined in the extension
    * call.
    *
    * @param call the extension call which defines the params to be bound to
    *    the extension for each invocation, must not be <code>null</code>.
    * @param ext the extension instance, must not be <code>null</code>.
    * @param conditions the conditions which must evaluate to <code>true</code>
    *    combined in order to execute this runner, may be <code>null</code> or
    *    empty.
    * @throws PSNotFoundException if an execption is not found.
    * @throws PSExtensionException for any exception that failed initializing.
    * @throws PSIllegalArgumentException if the extension call params do not 
    *    match the extension.
    */
   public PSConditionalExtensionRunner(PSExtensionCall call,
      IPSExtension ext, Iterator conditions) throws PSNotFoundException, 
         PSIllegalArgumentException, PSExtensionException
   {
      super(call);

      if (ext == null)
         throw new IllegalArgumentException("ext cannot be null");

      boolean foundSupported = false;
      if (ext instanceof IPSRequestPreProcessor)
      {
         m_rppExt = (IPSRequestPreProcessor) ext;
         foundSupported = true;
      }

      if (ext instanceof IPSResultDocumentProcessor)
      {
         m_rdpExt = (IPSResultDocumentProcessor) ext;
         foundSupported = true;
      }

      if (ext instanceof IPSUdfProcessor)
      {
         m_udfExt = (IPSUdfProcessor) ext;
         foundSupported = true;
      }

      if (ext instanceof IPSEffect)
      {
         m_effect = (IPSEffect) ext;
         foundSupported = true;
      }

      if (!foundSupported)
         throw new IllegalArgumentException(
            "Unknown extension type for runner: " + ext.getClass().getName());

      if (conditions != null)
         m_evaluator = new PSRuleListEvaluator(conditions);
   }


   /**
    * @see PSExtensionRunner#testEffect(PSExecutionData, IPSExecutionContext)
    */
   public PSResult testEffect(PSExecutionData data,
      IPSExecutionContext context)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      if (m_evaluator != null && !m_evaluator.isMatch(data))
         return new PSResult();

      return super.executeEffect(data, context, 1, null);
   }

   /**
    * @see PSExtensionRunner#attemptEffect(PSExecutionData, IPSExecutionContext)
    */
   public PSResult attemptEffect(PSExecutionData data,
      IPSExecutionContext context)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      if (m_evaluator != null && !m_evaluator.isMatch(data))
         return new PSResult();
      
      return executeEffect(data, context, 2, null);
   }

   /**
    * @see PSExtensionRunner#recoverEffect(PSExecutionData, IPSExecutionContext,
    * PSExtensionProcessingException)
    */
   public PSResult recoverEffect(PSExecutionData data,
      IPSExecutionContext context, PSExtensionProcessingException e)
      throws PSExtensionProcessingException, PSDataExtractionException,
      PSParameterMismatchException
   {
      if (m_evaluator != null && !m_evaluator.isMatch(data))
         return new PSResult();
      
      return executeEffect(data, context, 3, e);
   }

   /**
    * @see IPSRequestPreProcessor#preProcessRequest
    */
   public void preProcessRequest(PSExecutionData data)
      throws PSExtensionProcessingException, PSParameterMismatchException,
         PSRequestValidationException, PSDataExtractionException,
         PSAuthorizationException
   {
      if (m_evaluator != null && !m_evaluator.isMatch(data))
         return;

      super.preProcessRequest(data);
   }

   /**
    * @see IPSResultDocumentProcessor#processResultDocument(Object[], 
    *    IPSRequestContext, Document)
    */
   public Document processResultDoc(PSExecutionData data, Document doc)
      throws PSExtensionProcessingException, PSParameterMismatchException,
         PSDataExtractionException
   {
      if (m_evaluator != null && !m_evaluator.isMatch(data))
         return doc;

      return super.processResultDoc(data, doc);
   }

   /**
    * @see PSUdfCallExtractor#extract
    */
   public Object processUdfCallExtractor(PSExecutionData data)
      throws PSConversionException, PSDataExtractionException
   {
      if (m_evaluator != null && !m_evaluator.isMatch(data))
         return null;

      return super.processUdfCallExtractor(data);
   }

   /**
    * A <code>PSRuleListEvaluator</code> evaluator, initialized in ctor, may
    * be <code>null</code> or empty and is never changed after construction.
    */
   private PSRuleListEvaluator m_evaluator = null;
}
