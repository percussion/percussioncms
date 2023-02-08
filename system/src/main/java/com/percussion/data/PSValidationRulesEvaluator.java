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

import com.percussion.design.objectstore.PSConditionalExit;
import com.percussion.error.PSNotFoundException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.PSApplicationHandler;
import org.w3c.dom.Document;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * This validator runs all item level validation exits.
 */
public class PSValidationRulesEvaluator
{
   /** 
    * Creates a new item validator for the provided item level validation
    * rules.
    *
    * @param rules the item level validation rules to be processed, not
    *   <code>null</code>, might be empty.
    * @param ah the application handler, not <code>null</code>.
    * @throws IllegalArgumentException if the provided rules is 
    *   <code>null</code>.
    */
   public PSValidationRulesEvaluator(Iterator rules, PSApplicationHandler ah)
      throws PSNotFoundException, PSExtensionException
   {
      if (rules == null || ah == null)
         throw new IllegalArgumentException("parameters cannot be null");
      
      while (rules.hasNext())
         m_evals.add(new PSConditionalExitEvaluator(
            (PSConditionalExit) rules.next(), ah, 
            IPSResultDocumentProcessor.class.getName()));
   }
   
   /**
    * Checks if this item is valid against the provided item document.
    *
    * @param data the execution data to perform the validation with,
    *    not <code>null</code>.
    * @param item the item document based on which the validation will be
    *    performed, not <code>null</code>.
    * @param errorCollector the error collector to be updated in case of a
    *    validation error, not <code>null</code>.
    * @return <code>true</code> if the validation succeeded, 
    *    <code>false</code> otherwise.
    * @throws IllegalArgumentException if any of the provided parameters is
    *    <code>null</code>.
    * @throws @link IPSResultDocumentProcessor#processResultDocument for
    *    exception description.
    */
   public boolean isValid(PSExecutionData data, Document item, 
      PSErrorCollector errorCollector)
      throws PSExtensionProcessingException, PSDataExtractionException,
         PSParameterMismatchException
   {
      if (data == null || item == null || errorCollector == null)
         throw new IllegalArgumentException("parameters cannot be null");
      
      for (int i=0; i<m_evals.size(); i++)
      {
         PSConditionalExitEvaluator eval = 
            (PSConditionalExitEvaluator) m_evals.get(i);

         if (eval.isMatch(data))
         {
            Iterator exits = eval.getExits().iterator();
            while (exits.hasNext())
            {
               PSExtensionRunner runner = (PSExtensionRunner) exits.next();
               Document doc = runner.processResultDoc(data, item);
               if (doc != null)
               {
                  errorCollector.add(doc);

                  if (errorCollector.maxErrorsExceeded())
                     return false;
               }
            }
         }
      }

      return !errorCollector.hasErrors();
   }
   
   /**
    * A list of conditional exit evaluators, initialized during construction,
    * never <code>null</code> after that.
    */
   private List m_evals = new ArrayList();
}
