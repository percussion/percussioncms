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
package com.percussion.data;

import com.percussion.design.objectstore.PSExtensionCall;
import com.percussion.error.PSNotFoundException;
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
import org.w3c.dom.Document;

import java.util.Iterator;

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
