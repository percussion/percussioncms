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
