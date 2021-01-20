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

import com.percussion.design.objectstore.PSConditional;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.design.objectstore.PSRule;
import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extension.PSExtensionException;
import com.percussion.server.PSRequest;
import com.percussion.util.PSCollection;
import junit.framework.TestCase;

/**
 * Unit test for the <code>PSRuleListEvaluator</code>.
 */
public class PSRuleListEvaluatorTest extends TestCase
{
   /**
    * Test AND rule evaluations.
    */
   public void testAndEvaluation() throws PSNotFoundException, 
      PSExtensionException
   {
      PSRuleListEvaluator evaluator = null;
      PSCollection rules = new PSCollection(PSRule.class);
      PSExecutionData data = new PSExecutionData(null, null, 
         new PSRequest(null, null, null, null));
      
      // true AND true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // false AND true = false
      rules.clear();
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // false AND false = false
      rules.clear();
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // true AND true AND true AND true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND true AND true AND false = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // true AND true AND false AND true = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // true AND false AND true AND true = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // false AND true AND true AND true = false
      rules.clear();
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
   }
   
   /**
    * Test OR rule evaluations.
    */
   public void testOrEvaluation() throws PSNotFoundException, 
      PSExtensionException
   {
      PSRuleListEvaluator evaluator = null;
      PSCollection rules = new PSCollection(PSRule.class);
      PSExecutionData data = new PSExecutionData(null, null, 
         new PSRequest(null, null, null, null));
      
      // true OR true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true OR false = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // false OR true = true
      rules.clear();
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // false OR false = false
      rules.clear();
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // true OR true OR true OR true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true OR true OR true OR false = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true OR false OR true OR false = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // false OR false OR false OR true = true
      rules.clear();
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
   }
   
   /**
    * Test mixed rule evaluations.
    */
   public void testMixedEvaluation() throws PSNotFoundException, 
      PSExtensionException
   {
      PSRuleListEvaluator evaluator = null;
      PSCollection rules = new PSCollection(PSRule.class);
      PSExecutionData data = new PSExecutionData(null, null, 
         new PSRequest(null, null, null, null));

      // empty rules = true
      rules.clear();
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND true AND true OR false = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false AND true OR false = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // true AND false AND true OR true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false AND true OR false OR false OR true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false OR true AND false = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      // true AND true OR true AND false OR false AND true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false OR true AND true OR false AND true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false OR false AND true OR true AND true = true
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      // true AND false OR true AND false OR false AND true = false
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      /*
       * (true AND true AND false AND true) OR false OR (false AND true) OR 
       * (false AND true AND false) = false
       */
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(!evaluator.isMatch(data));
      
      /*
       * (true AND true AND false AND true) OR true OR (false AND true) OR 
       * (false AND true AND false) = true
       */
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      /*
       * (true AND true AND true AND true) OR false OR (false AND true) OR 
       * (false AND true AND false) = true
       */
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      /*
       * (true AND true AND false AND true) OR false OR (true AND true) OR 
       * (false AND true AND false) = true
       */
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
      
      /*
       * (true AND true AND false AND true) OR false OR (false AND true) OR 
       * (true AND true AND true) = true 
       */
      rules.clear();
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(false, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_OR));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, PSRule.BOOLEAN_AND));
      rules.add(getConditionalRule(true, -1));
      evaluator = new PSRuleListEvaluator(rules);
      assertTrue(evaluator.isMatch(data));
   }
   
   /**
    * Constructs a simple rule that produces the requested evaluation result.
    * 
    * @param evaluationResult <code>true</code> will return a rule which 
    *    evaluates to <code>true</code>, <code>false</code> will return a 
    *    rule which evaluates to <code>false</code>.
    * @param operator the rule operator, assumed one of 
    *    <code>PSRule.OPBOOL_AND</code> or <code>PSRule.OPBOOL_OR</code>, 
    *    defaults to <code>PSRule.OPBOOL_AND</code> if -1 is supplied.
    * @return the rule that produces the requested evaluation result, 
    *    never <code>null</code>.
    */
   private PSRule getConditionalRule(boolean evaluationResult, int operator)
   {
      PSTextLiteral literal_1 = new PSTextLiteral("1");
      PSTextLiteral literal_2 = new PSTextLiteral("2");
      
      // create an AND codition that produces the requested result
      PSConditional conditional = null;
      if (evaluationResult)
         conditional = new PSConditional(literal_1, 
            PSConditional.OPTYPE_EQUALS, literal_1);
      else
         conditional = new PSConditional(literal_1, 
            PSConditional.OPTYPE_EQUALS, literal_2);
      
      PSCollection conditionals = new PSCollection(PSConditional.class);
      conditionals.addElement(conditional);
      
      PSRule rule = new PSRule(conditionals);
      if (operator > 0)
         rule.setOperator(operator);
      
      return rule;
   }
}

