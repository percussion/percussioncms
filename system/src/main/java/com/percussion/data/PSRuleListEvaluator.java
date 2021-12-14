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

import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRule;
import com.percussion.extension.PSExtensionException;
import com.percussion.util.PSCollection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A new concept known as a PSRule was added when the Content Editors were
 * first created. A rule is either a collection of conditionals or an
 * extension. Rules can be combined with boolean 'and' and 'or' operators
 * to create complex expressions. For the given list of rules, 'and' operations
 * will be considered to have higher precedence, which means that rules 
 * seperated by 'and' operators will be grouped and evaluated prior to 
 * applying 'or' operations.
 *
 * This class constructs an appropriate representation of the definitions
 * that can be executed repeatedly at run time using the {@link
 * #isMatch(PSExecutionData) isMatch} method.
 */
public class PSRuleListEvaluator
{
   /**
    * Constructs a rule list evaluator, building an appropriate
    * internal representation of the supplied list of rules as defined
    * by their operators.
    *
    * @param rules a collection of <code>PSRule</code> objects. If 
    *    <code>null</code> or empty, <code>isMatch</code> will always return 
    *    <code>true</code>.
    * @throws PSNotFoundException if a specified extension cannot be found.
    * @throws PSExtensionException if any errors occur while preparing a
    *    runnable version of an extension.
    */
   public PSRuleListEvaluator(PSCollection rules)
      throws PSNotFoundException, PSExtensionException
   {
      this(rules == null ? null : rules.iterator());
   }

   /*
    * Convenience method for supplying an iterator pointing to a list of
    * <code>PSRules</code>, see the <code>PSCollection</code> constructor for 
    * more information.
    */
   public PSRuleListEvaluator(Iterator rules)
      throws PSNotFoundException, PSExtensionException
   {
      List andRules = null;
      if (rules != null)
      {
         while (rules.hasNext())
         {
            PSRule rule = (PSRule) rules.next();
            PSRuleEvaluator evaluator = new PSRuleEvaluator(rule);
            
            if (rules.hasNext())
            {
               if (rule.getOperator() == PSRule.BOOLEAN_AND)
               {
                  if (andRules == null)
                  {
                     andRules = new ArrayList();
                     m_andGroups.add(andRules);
                  }
                  
                  andRules.add(evaluator);
               }
               else
               {
                  if (andRules != null)
                  {
                     andRules.add(evaluator);
                     andRules = null;
                  }
                  else
                     m_orRules.add(evaluator);
               }
            }
            else
            {
               if (andRules == null)
                  m_orRules.add(evaluator);
               else
                  andRules.add(evaluator);
            }
         }
      }
   }
   
   /**
    * Evaluates this rule list against the supplied execution data.
    * <p>
    * This evaluator may use the request context hash tables, the input
    * XML document and the result set(s) for processing.
    * <P>
    * When using multiple rules (chaining) a boolean operator must be 
    * specified on all but the last one. The boolean operators currently 
    * supported are AND and OR. AND is the default boolean operator. The 
    * rules are processed in the order they were supplied in the collection, 
    * with AND operations having higher precedence than OR operations.
    * <P>
    * A 'short-circuit' algorithm is used, meaning as soon as the result is
    * known, the rest of the rules will not be processed.
    *
    * @param data the execution data the evaluator will be applied to.
    *    The row data will be obtained by calling getCurrentResultRowData() on
    *    this parameter.
    * @return  <code>true</code> if the conditional criteria is met,
    *    <code>false</code> otherwise.
    * @throws PSEvaluationException if a data extraction or conversion exception
    *    occurs (for extension-based rules) or if a evaluation exception occurs
    *    in the underlying base class (for conditional-based rules).
    */
   public boolean isMatch(PSExecutionData data)
   {
      // no evaluators evaluate to true
      if (m_andGroups.isEmpty() && m_orRules.isEmpty())
         return true;
      
      boolean isMatch = true;

      // first evaluate all AND groups and collect the results
      List andResults = new ArrayList();
      for (int i=0; i<m_andGroups.size(); i++)
      {
         isMatch = true;
         Iterator evaluators = ((List) m_andGroups.get(i)).iterator();
         while (isMatch && evaluators.hasNext())
         {
            PSRuleEvaluator evaluator = (PSRuleEvaluator) evaluators.next();
            isMatch = evaluator.isMatch(data);
         }
         
         andResults.add(isMatch);
      }
      
      // then OR all group results
      isMatch = false;
      Iterator results = andResults.iterator();
      while (!isMatch && results.hasNext())
      {
         Boolean result = (Boolean) results.next();
         isMatch = result.booleanValue();
      }
      
      // finally evaluate all OR evaluators
      Iterator evaluators = m_orRules.iterator();
      while (!isMatch && evaluators.hasNext())
      {
         PSRuleEvaluator evaluator = (PSRuleEvaluator) evaluators.next();
         isMatch = evaluator.isMatch(data);
      }
      
      return isMatch;
   }

   /**
    * This list contains a list of list of <code>PSRuleEvaluator</code> 
    * objects. All <code>PSRuleEvaluator</code> objects in these lists are 
    * ANDed together. The results of all lists are ORed together. Initialized in 
    * the constructor and never changed after that, never <code>null</code>, 
    * may be empty. 
    */
   private List m_andGroups = new ArrayList();
   
   /**
    * A list with <code>PSRuleEvaluator</code> objects ORd together. 
    * Initialized in the constructor and never changed after that, never 
    * <code>null</code>, may be empty.
    */
   private List m_orRules = new ArrayList();
}
