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
package com.percussion.data;

import com.percussion.design.objectstore.PSConditionalExit;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSResultDocumentProcessor;
import com.percussion.extension.PSExtensionException;
import com.percussion.extension.PSExtensionProcessingException;
import com.percussion.extension.PSParameterMismatchException;
import com.percussion.server.PSApplicationHandler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Document;

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
