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

import com.percussion.design.objectstore.PSResultPage;
import com.percussion.error.PSEvaluationException;
import com.percussion.server.PSRequest;
import com.percussion.util.PSCollection;

import java.net.URL;
import java.util.Collection;

/**
 * The PSSetStyleSheetEvaluator class implements exit handling for the
 * setStyleSheet simple action exit. This exit can be used against context
 * information and the XML document. If the conditional evaluates to true,
 * the style sheet associated with this exit will be used.
 *
 * @author     Tas Giakouminakis
 * @version    1.0
 * @since      1.0
 */
public class PSSetStyleSheetEvaluator extends PSConditionalEvaluator
{
   /**
    * Build an evaluator which will check the specified conditions and set
    * the style sheet if they conditions are met.
    *
    * @param styleSheet The style sheet associated with this evaluator
    * @param resultPage
    * @param conditionals The collection of PSConditional objects
    *
    */
   public PSSetStyleSheetEvaluator(URL styleSheet,
      PSResultPage resultPage,
      PSCollection conditionals
      )
   {
      super(conditionals);
      m_styleSheet = styleSheet;
      m_resultPage = resultPage;
   }

   /**
    * Get the style sheet associated with this evaluator handler.
    *
    * @return      the style sheet URL
    */
   public java.net.URL getStyleSheet()
   {
      return m_styleSheet;
   }

   /** Is this condition met?
    *
    * Override conditional exit handler's isMatch() method to
    * check if the request URL's extension is supported by the
    * result page associated with this exit handler.
    *
    * @param data The execution data associated with the current request.
    *
    * @throws  PSEvaluationException   if an error occurs doing the
    *          underlying isMatch().
    */
   public boolean isMatch(PSExecutionData data)
      throws PSEvaluationException
   {
      if ((m_resultPage != null) &&
         m_resultPage.requestIsSupported(data.getRequest().getRequestFileURL()))
      {
        return super.isMatch(data);
      }
      else
      {
        return false;
      }
   }

   /**
    *
    * Determine whether the result page associated with this evaluator
    * has specific extensions to which it refers.
    *
    * @retun   <code>true</code> if it does, or <code>false</code> if
    *          this style sheet will accept any extensions (from the requestor)
    *
    */
   public boolean hasExplicitExtensionList()
   {
      Collection c = m_resultPage.getExtensions();

      return ((c != null) && (!c.isEmpty()));
   }

   /* ************  IPSExecutionStep Interface Implementation ************ */

   /**
    * Execute the evaluator as a step in the execution plan.
    *
    * @param   data     the execution data the evaluator will be applied to
    */
   public void execute(PSExecutionData data)
   {
   }

   public String evaluate(PSRequest request, Object [] backEndColumnData)
   {
      return null;
   }

   public String[] getBackEndColumnList(PSRequest request)
   {
      return null;
   }

   private java.net.URL m_styleSheet;
   private PSResultPage m_resultPage;
}

