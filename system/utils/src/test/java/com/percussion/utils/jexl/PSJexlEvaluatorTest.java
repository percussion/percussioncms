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
package com.percussion.utils.jexl;

import com.percussion.utils.testing.UnitTest;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Test evaluator for valid and invalid case handling
 * 
 * @author dougrand
 */
@SuppressWarnings(value = "unchecked")
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Category(UnitTest.class)
public class PSJexlEvaluatorTest
{

   public PSJexlEvaluatorTest() {
   }

   /**
    * Test variable binder implementation. The binder creates lists and maps as
    * it encounters the array and dot notation.
    * 
    * @throws Exception
    */
   @Test
   public void test_01_Binder() throws Exception
   {
      PSJexlEvaluator eval = new PSJexlEvaluator();

      eval.bind("$x", 1);
      eval.bind("$y[2]", 2);
      eval.bind("$y[5]", 3);
      eval.bind("$z.a.b.c", "n1");
      eval.bind("$z.a.c[1]", "n2");
      eval.bind("$z.a.c[0]", "n3");
      eval.bind("$z.b.x.y[0].w", "a");
      eval.bind("$z.x", 123);
      eval.evaluate("$c", PSJexlEvaluator.createExpression("$y[2] + $y[5]"));
      eval.evaluate("$d", PSJexlEvaluator.createExpression("$x * $y[5]"));
      eval.evaluate("$e", PSJexlEvaluator.createScript("if ($x > 1) {3;} else {4;}"));

      Map<String, Object> vars = eval.getVars();
      Map z = (Map)vars.get("$z");
      Map a = (Map) z.get("a");
      Map b = (Map) z.get("b");
      Map z_a_b = (Map) a.get("b");
      List y = (List) vars.get("$y");
      List z_a_c = (List) a.get("c");
      Map z_b_x = (Map) b.get("x");
      List z_b_x_y = (List) z_b_x.get("y");
      Map y_0 = (Map) z_b_x_y.get(0);
      assertEquals("n3", z_a_c.get(0));
      assertEquals("n2", z_a_c.get(1));
      assertEquals("n1", z_a_b.get("c"));
      assertEquals(new Integer(2), y.get(2));
      assertEquals(new Integer(3), y.get(5));
      assertEquals(null, y.get(0));
      assertEquals("a", y_0.get("w"));
      assertEquals(new Integer(123), z.get("x"));
      assertEquals(new Integer(5), (vars.get("$c")));
      assertEquals(new Integer(3), (vars.get("$d")));
      assertEquals(new Integer(3), (vars.get("$d")));
   }

   /**
    * Simple evaluation test
    * 
    * @throws Exception
    */
   @Test
   public void test_02_PrebindingAndEvaluation() throws Exception
   {
      Map<String, Object> initial = new HashMap<String, Object>();

      initial.put("a", 1);
      initial.put("b", 2);

      PSJexlEvaluator eval = new PSJexlEvaluator(initial);

      assertEquals(1, eval.evaluate(PSJexlEvaluator.createExpression("a")));
      assertEquals(2, eval.evaluate(PSJexlEvaluator.createExpression("b")));

      eval.bind("$foo.bar.bletch", 3);

      assertEquals(3, eval.evaluate(PSJexlEvaluator
            .createExpression("$foo.bar.bletch")));
   }

   @Test
   public void test_03_Add() throws Exception
   {
      Map<String, Object> initial = new HashMap<String, Object>();
      Map<String, Object> c = new HashMap<String, Object>();
      initial.put("$a", 1);
      initial.put("$b", 2);
      initial.put("$c", c);
      c.put("x", 10);
      c.put("y", 11);

      PSJexlEvaluator eval = new PSJexlEvaluator(initial);

      Map<String, Object> add = new HashMap<String, Object>();
      add.put("y", 12);
      add.put("z", 13);

      eval.add("$c", PSJexlEvaluator.createExpression("$c"), add);


      Map<String, Object> expVarsC = new HashMap<>();
      expVarsC.put("z",13);
      expVarsC.put("y",12);
      expVarsC.put("x",10);
      Map<String, Object> expVars = new HashMap<>();
      expVars.put("$a",1);
      expVars.put("$b",2);
      expVars.put("$c",expVarsC);
      assertEquals(expVars, eval.getVars());
   }

   /**
    * Test a concatenation case that is similar
    * 
    * @throws Exception
    */
   @Test
   public void test_04_Concat() throws Exception
   {
      Map<String, Object> initial = new HashMap<String, Object>();
      initial.put("$c", true);
      initial.put("$val1", "the quick");
      initial.put("$val2", "the slow");
      
      PSJexlEvaluator eval = new PSJexlEvaluator(initial);
      IPSScript exp = eval.createScript("if ($c) {$a = $val1;} else {$a = $val2;}\n$b = $a + ' brown fox'");
      assertEquals("the quick brown fox", eval.evaluate(exp));
   }
   
   /**
    * Test backward compatibility from script to expression
    * @throws Exception
    */
   @Test
   public void test_05_EvalScript() throws Exception
   {
      Map<String, Object> initial = new HashMap<String, Object>();
      initial.put("$c", 2147483647);
      initial.put("$a", 4);
      
      PSJexlEvaluator eval = new PSJexlEvaluator(initial);
      IPSScript exp = eval.createScript("$c");
      assertEquals(new Integer(2147483647), eval.evaluate(exp));
      exp = eval.createScript("$c * $a");
      Object result =eval.evaluate(exp);
      assertEquals(new Long(8589934588l),result);
   }
   
   /**
    * Test various exception cases with the uberspect
    * 
    * @throws Exception
    */
   @Test
   public void test_06_Errors() throws Exception
   {
      Map<String, Object> initial = new HashMap<String, Object>();
      initial.put("$a", 1);
      initial.put("$b", 2);
      initial.put("$c", "the fox in the henhouse");

      PSJexlEvaluator eval = new PSJexlEvaluator(initial);

      doExceptionTest(eval, "$c.foo()");
      doExceptionTest(eval, "$c.xyz");
   }


   /**
    * Do an exception test by evaluating the expression and asserting if an
    * exception is not thrown
    * @param eval evaluator
    * @param expression 
    * @throws Exception 
    */
   private void doExceptionTest(PSJexlEvaluator eval, String expression)
   throws Exception
   {
      try
      {
         IPSScript exp = eval.createExpression(expression);

         exp.setUseDebugMode(true);
         exp.setUseSilentMode(false);
         exp.setUseStrictMode(true);
         exp.reinit(false);
         Object ret = exp.eval(eval.getVars());

         assertFalse("An exception should have been thrown for "
               + expression, true);
      }
      catch (RuntimeException t)
      {
         // OK
         System.out.println(t.getLocalizedMessage());
      }
   }
   
   

}
