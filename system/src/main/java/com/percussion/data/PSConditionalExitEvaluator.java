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
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSApplicationHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used as an aid in evaluating and running a Conditional Exit.
 * Call the <code>isMatch</code> method first. If it evaluates to <code>
 * true</code>, then call <code>getExits</code> method to retrieve the
 * prepared extensions.
 */
public class PSConditionalExitEvaluator extends PSRuleListEvaluator
{
   /**
    * Creates a rule evaluator and prepares the extensions.
    *
    * @param exit The conditional exits to evaluate.  May not be
    * <code>null</code>.
    *
    * @param appHandler The appHandler to use when preparing the exits, never
    * <code>null</code>.
    *
    * @param className the fully qualified class name of the processor
    *    interface to use, not <code>null</code> or empty.
    *
    * @throws PSExtensionException if there is an error preparing any exits.
    * @throws PSNotFoundException if an exit definition cannot be located.
    */
   public PSConditionalExitEvaluator(PSConditionalExit exit,
      PSApplicationHandler appHandler, String className)
   throws PSExtensionException, PSNotFoundException
   {
      super(exit != null ? exit.getCondition(): null);
      if (exit == null || appHandler == null)
         throw new IllegalArgumentException(
            "exit and appHandler may not be null");
      if (className == null || className.trim().length() == 0)
         throw new IllegalArgumentException(
            "className cannot be null orempty");

      try
      {
         m_preparedExits = new ArrayList();
         PSDataHandler.loadExtensions(appHandler, exit.getRules(),
            className, m_preparedExits);
      }
      catch (IllegalArgumentException e)
      {
         throw new IllegalArgumentException(e.getLocalizedMessage());
      }
   }

   /**
    * Returns a list of prepared extensions.  Never <code>null</code>.
    */
   public List getExits()
   {
      return m_preparedExits;
   }

   /**
    * List of prepared exits, initialized in the constructor, never <code>
    * null</code> after that.
    */
   private List m_preparedExits;
}
