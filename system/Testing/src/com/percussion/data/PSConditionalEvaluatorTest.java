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

import com.percussion.design.objectstore.PSDateLiteral;
import com.percussion.design.objectstore.PSLiteralSet;
import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSTextLiteral;

import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * The PSConditionalEvaluatorTest class is for unit test of PSConditionalEvaluator
 * class.
 *
 * @author    Jian Huang
 * @version   1.0
 * @since     1.0
 */
public class PSConditionalEvaluatorTest extends TestCase
{
   public PSConditionalEvaluatorTest(String name)
   {
      super(name);
   }

   public void testMakeComparable2Number() throws Exception
   {
      // only BigDecimal works for type Number
      java.math.BigDecimal left  = new java.math.BigDecimal(10);
      java.math.BigDecimal right = new java.math.BigDecimal(10);

      testTheseCases(left, right);
      testLikeCase(left, right, false, true);
      testEqualCase(left, right);
      testNormalCases(left, right);

      // let left = BigDecimal, right = numiric set, date set, and text set
      java.text.DecimalFormat format = new java.text.DecimalFormat();
      java.math.BigDecimal numOne  = new java.math.BigDecimal(10);
      java.math.BigDecimal numTwo = new java.math.BigDecimal(10);

      PSNumericLiteral num1 = new PSNumericLiteral(numOne, format);
      PSNumericLiteral num2 = new PSNumericLiteral(numTwo, format);


      PSLiteralSet literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSNumericLiteral.class);
      literalSet.add(num1);
      literalSet.add(num2);

      testLiteralSet(left, literalSet);

      java.util.Date date = new java.util.Date(10);
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
      PSDateLiteral day = new PSDateLiteral(date, dateFormat);

      PSLiteralSet dateLiteralSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSDateLiteral.class);
      dateLiteralSet.add(day);
      dateLiteralSet.add(day);

      testLiteralSet(left, dateLiteralSet);

      String left1 = "10";
      String right1= "10";
      PSTextLiteral text1 = new PSTextLiteral(left1);
      PSTextLiteral text2 = new PSTextLiteral(right1);

      PSLiteralSet textLiteralSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSTextLiteral.class);
      textLiteralSet.add(text1);
      textLiteralSet.add(text2);

      testLiteralSet(left, textLiteralSet);

      // let left = BigDecimal, right = date
      java.util.Date rightDate = new java.util.Date(10);
      testTheseCases(left, rightDate);
      testLikeCase(left, rightDate, false, true);
      testEqualCase(left, rightDate);
      testNormalCases(left, rightDate);

      // let left = BigDecimal, right = String
      String numInString = "10";
      testTheseCases(left, numInString);
      testLikeCase(left, numInString, false, true);
      testEqualCase(left, numInString);
      testNormalCases(left, numInString);

      // let left = BigDecimal, right = PSNumericLiteral
      testTheseCases(left, num1);
      testLikeCase(left, num1, false, true);
      testEqualCase(left, num1);
      testNormalCases(left, num1);

      // let left = BigDecimal, right = PSDateLiteral
      PSDateLiteral dateLiteral = new PSDateLiteral(rightDate, dateFormat);
      testTheseCases(left, dateLiteral);
      testLikeCase(left, dateLiteral, false, true);
      testEqualCase(left, dateLiteral);
      testNormalCases(left, dateLiteral);

      // let left = BigDecimal, right = PSTextLiteral
      PSTextLiteral textLiteral = new PSTextLiteral("10");
      testTheseCases(left, textLiteral);
      testLikeCase(left, textLiteral, false, true);
      testEqualCase(left, textLiteral);
      testNormalCases(left, textLiteral);

      // let left or right = null
      testEqualNullCase(left, null);
      testEqualNullCase(null,right);
      testNormalCases(null, null);
   }

   public void testMakeComparable2Date() throws Exception
   {
      java.util.Date left = new java.util.Date(1000);
      java.util.Date right= new java.util.Date(2000);

      testTheseCases(left, right);
      testLikeCase(left, right, false, false);
      testEqualCase(left, left);
      testNormalCases(left, right);

      // let left = date, right = date set, numeric set, text set
      java.text.SimpleDateFormat format = new java.text.SimpleDateFormat();
      PSDateLiteral day1 = new PSDateLiteral(left, format);
      PSDateLiteral day2 = new PSDateLiteral(right,format);

      PSLiteralSet literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSDateLiteral.class);
      literalSet.add(day1);
      literalSet.add(day2);

      testLiteralSet(left, literalSet);

      java.text.DecimalFormat numFormat = new java.text.DecimalFormat();
      java.math.BigDecimal numOne  = new java.math.BigDecimal(1000);
      PSNumericLiteral num1 = new PSNumericLiteral(numOne, numFormat);

      literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSNumericLiteral.class);
      literalSet.add(num1);
      literalSet.add(num1);

      testLiteralSet(left, literalSet);

      String left1 = "1000";
      //left1 = "This will not work";
      //left1 = "1999.08.12 AD at 12:33:44";
      PSTextLiteral text1 = new PSTextLiteral(left1);

      PSLiteralSet textLiteralSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSTextLiteral.class);
      textLiteralSet.add(text1);
      textLiteralSet.add(text1);

      //testLiteralSet(left, textLiteralSet);

      // let left = date, right = PSDateLiteral
      testTheseCases(left, day1);
      testLikeCase(left, day1, false, true);
      testEqualCase(left, day1);
      testNormalCases(left, day2);

      // let left = date, right = numeric
      java.math.BigDecimal rightNum = new java.math.BigDecimal(1000);
      testTheseCases(left, rightNum);
      testLikeCase(left, rightNum, false, true);
      testEqualCase(left, rightNum);
      testNormalCases(left, rightNum);

      // let left = date, right = PSNumericLiteral
      PSNumericLiteral numLiteral = new PSNumericLiteral(rightNum, numFormat);
      testTheseCases(left, numLiteral);
      testLikeCase(left, numLiteral, false, true);
      testEqualCase(left, numLiteral);
      testNormalCases(left, numLiteral);

      // let left = date, right = String, cannot compare normally
      // date format example: 1996.07.10 AD at 15:08:56
      String dateString = "1996.07.10 AD at 15:08:56";
      testNormalCases(left, dateString);
      testTheseCases(left, dateString);
      testLikeCase(left, dateString, false, false);

      // let left = date, right = PSTextLiteral
      PSTextLiteral textLiteral = new PSTextLiteral(dateString);
      testTheseCases(left, textLiteral);
      testLikeCase(left, textLiteral, false, false);
      testNormalCases(left, textLiteral);

      // test either side in null case
      testEqualNullCase(left, null);
      testEqualNullCase(null,right);
   }

   public void testMakeComparable2String() throws Exception
   {
      String left = "This is a test";
      String right= "This is only a test";

      testTheseCases(left, right);
      //testLikeCase(left, right);
      testEqualCase(left, left);

      // let left = String, right = text set
      left = "1000";
      right= "2000";
      PSTextLiteral text1 = new PSTextLiteral(left);
      PSTextLiteral text2 = new PSTextLiteral(right);

      PSLiteralSet literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSTextLiteral.class);
      literalSet.add(text1);
      literalSet.add(text2);

      testTextLiteralSet(left, literalSet);

      // let left = String, right = numeric
      java.math.BigDecimal rightNum = new java.math.BigDecimal(1000);
      testTheseCases(left, rightNum);
      testLikeCase(left, rightNum, false, true);
      testEqualCase(left, rightNum);
      testNormalCases(left, rightNum);

      // let left = String, right = PSNumericLiteral
      java.text.DecimalFormat format = new java.text.DecimalFormat();
      PSNumericLiteral numLiteral = new PSNumericLiteral(rightNum, format);
      testTheseCases(left, numLiteral);
      testEqualCase(left, numLiteral);
      testNormalCases(left, numLiteral);

      // let left = String, right = PSDateLiteral,
      java.util.Date oneDay = new java.util.Date(1000);
      String pattern = "yyyy.MM.dd";
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(pattern);
      PSDateLiteral dateLiteral = new PSDateLiteral(oneDay, dateFormat);

      String myString = "1999.08.12 AD at 14:02:44";  // in pattern
      PSTextLiteral myText = new PSTextLiteral(myString);

      testNormalCasesInverse(myString, dateLiteral);

      // let left = String, right = date
      testTheseCases(myString, oneDay);
      testLikeCase(myString, oneDay, false, false);
      testNormalCasesInverse(myString, oneDay);

      // let left = String, right = PSTextLiteral
      PSTextLiteral textLiteral = new PSTextLiteral("1000");
      testTheseCases(left, textLiteral);
      testEqualCase(left, textLiteral);
      testNormalCases(left, textLiteral);
   }

   public void testMakeComparable2PSDateLiteral() throws Exception
   {
      String pattern = "yyyy.MM.dd G";
      java.text.SimpleDateFormat format = new java.text.SimpleDateFormat(pattern);
      java.util.Date now = new java.util.Date(1000);
      java.util.Date then= new java.util.Date(2000);

      PSDateLiteral left = new PSDateLiteral(now, format);
      PSDateLiteral right= new PSDateLiteral(then,format);

      testTheseCases(left, right);
      testLikeCase(left, right, false, false);
      testEqualCase(left, left);
      testNormalCases(left, right);

      // let left = PSDateLiteral, right = date set, number set, text set
      PSLiteralSet literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSDateLiteral.class);
      literalSet.add(left);
      literalSet.add(right);

      testLiteralSet(left, literalSet);

      java.math.BigDecimal num = new java.math.BigDecimal(1000);
      java.text.DecimalFormat numFormat = new java.text.DecimalFormat();
      PSNumericLiteral rightNum = new PSNumericLiteral(num, numFormat);
      literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSNumericLiteral.class);
      literalSet.add(rightNum);
      literalSet.add(rightNum);

      testLiteralSet(left, literalSet);

      PSTextLiteral text1 = new PSTextLiteral("2000.01.01 BC");
      PSTextLiteral text2 = new PSTextLiteral("2000.01.01 AD");
      literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSTextLiteral.class);
      literalSet.add(text1);
      literalSet.add(text2);

      //testTextLiteralSet(left, literalSet);

      // let left = PSDateLiteral, right = numeric
      testTheseCases(left, num);
      testLikeCase(left, num, false, true);
      testEqualCase(left, num);
      testNormalCases(left, num);

      // let left = PSDateLiteral, right = PSNumericLiteral
      testTheseCases(left, rightNum);
      testLikeCase(left, rightNum, false, true);
      testEqualCase(left, rightNum);
      testNormalCases(left, rightNum);

      // let left = PSDateLiteral, right = date
      testTheseCases(left, now);
      testLikeCase(left, now, false, true);
      testEqualCase(left, now);
      testNormalCases(left, now);

      // let left = PSDateLiteral, right = String or PSTextLiteral
      java.util.Date oneDay = new java.util.Date(1000);
      // pattern = "yyyy.MM.dd G 'at' HH:mm:ss";
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(pattern);
      PSDateLiteral dateLiteral = new PSDateLiteral(oneDay, dateFormat);

      String myString = "1999.08.12 AD at 14:02:44";  // in pattern
      PSTextLiteral myText = new PSTextLiteral(myString);

      testNormalCases(dateLiteral, myString);
      testNormalCases(dateLiteral, myText);
   }

   public void testMakeComparable2PSNumericLiteral() throws Exception
   {
      java.text.DecimalFormat format = new java.text.DecimalFormat();
      java.math.BigDecimal numOne  = new java.math.BigDecimal(10);
      java.math.BigDecimal numTwo = new java.math.BigDecimal(10);

      PSNumericLiteral left = new PSNumericLiteral(numOne, format);
      PSNumericLiteral right= new PSNumericLiteral(numTwo, format);

      testTheseCases(left, right);
      testLikeCase(left, right, false, true);
      testEqualCase(left, left);
      testNormalCases(left, right);

      // let left = PSNumericLiteral, right = numeric set
      PSLiteralSet literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSNumericLiteral.class);
      literalSet.add(left);
      literalSet.add(right);

      testLiteralSet(left, literalSet);

      // let left = PSNumericLiteral, right = numeric
      testTheseCases(left, numOne);
      testLikeCase(left, numOne, false, true);
      testEqualCase(left, numOne);
      testNormalCases(left, numOne);

      // let left = PSNumericLiteral, right = date
      java.util.Date now = new java.util.Date(10);
      testTheseCases(left, now);
      testLikeCase(left, now, false, true);
      testEqualCase(left, now);
      testNormalCases(left, now);

      // let left = PSNumericLiteral, right = PSDateLiteral
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat();
      PSDateLiteral day = new PSDateLiteral(now, dateFormat);
      testTheseCases(left, day);
      testLikeCase(left, day, false, true);
      testEqualCase(left, day);
      testNormalCases(left, day);

      // let left = PSNumericLiteral, right = String
      String numString = "10";
      testTheseCases(left, numString);
      testLikeCase(left, numString, false, true);
      testEqualCase(left, numString);
      testNormalCases(left, numString);

      // let left = PSNumericLiteral, right = PSTextliteral
      PSTextLiteral text = new PSTextLiteral(numString);
      testTheseCases(left, text);
      testLikeCase(left, text, false, true);
      testEqualCase(left, text);
      testNormalCases(left, text);

      testNullNullCase(null, null);
   }

   public void testMakeComparable2PSTextLiteral() throws Exception
   {
      PSTextLiteral left = new PSTextLiteral("This is a test");
      PSTextLiteral right= new PSTextLiteral("This is only a test");

      testTheseCases(left, right);
      testEqualCase(left, left);
      testNormalCases(left, left);

      // let left = PSTextLiteral, right = text set
      PSLiteralSet literalSet = new PSLiteralSet(
         com.percussion.design.objectstore.PSTextLiteral.class);
      literalSet.add(left);
      literalSet.add(right);

      testTextLiteralSet(left, literalSet);

      // let left = PSTextLiteral, right = String
      String rightString = "This is a test";
      testTheseCases(left, rightString);
      testEqualCase(left, rightString);

      // let left = PSTextLiteral, right = numeric
      PSTextLiteral text = new PSTextLiteral("10");
      java.math.BigDecimal num = new java.math.BigDecimal(10);
      testTheseCases(text, num);
      testEqualCase(text, num);
      testNormalCases(text, num);

      // let left = PSTextLiteral, right = PSNumericLiteral
      java.text.DecimalFormat format = new java.text.DecimalFormat();
      PSNumericLiteral numLiteral = new PSNumericLiteral(num, format);
      testTheseCases(text, numLiteral);
      testEqualCase(text, numLiteral);
      testNormalCases(text, numLiteral);

      // let left = PSTextLiteral, right = PSDateLiteral
      java.util.Date oneDay = new java.util.Date(1000);
      String pattern = "yyyy.MM.dd G 'at' HH:mm:ss";
      java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat(pattern);
      PSDateLiteral dateLiteral = new PSDateLiteral(oneDay, dateFormat);

      String myString = "1999.08.12 AD at 14:02:44";  // in pattern
      PSTextLiteral myText = new PSTextLiteral(myString);

      testNormalCasesInverse(myText, dateLiteral);

      // let left = PSTextLiteral, right = date
      testNormalCasesInverse(myText, oneDay);
   }

   public void testMakeComparable2Lists() throws Exception
   {
      ArrayList left = new ArrayList();
      ArrayList right = new ArrayList();

      //both empty
      testIsNull(left, right);
      testEqualCase(right, right);
      testLikeCase(left, right, false, true);

      left.add("abc");
      left.add("abcd");
      left.add("abcde");

      //same list
      testIsNotNull(left, left);
      testEqualCase(left, left);
      testLikeCase(left, left, false, true);

      //one empty
      testIsNotNull(left, right);
      testNotEqualCase(left, right);
      testNotLikeCase(left, right);

      //same size different values
      right.add("abc");
      right.add("abcd");
      right.add("abcdx");
      testIsNotNull(left, right);
      testNotEqualCase(left, right);
      testLikeCase(left, right, false, true);

      //different size
      right.add("abcde");
      right.add("abcdef");
      testNotEqualCase(left, right);
      testLikeCase(left, right, false, true);

      //between case
      left.clear();
      right.clear();

      left.add(new Integer(1));
      right.add(new Integer(0));
      right.add(new Integer(2));
      testBetweenCase(left, right);
      testLikeCase(left, right, false, false);

      right.add(new Integer(1));
      testLikeCase(left, right, false, true);

      //greater than
      left.clear();
      right.clear();

      left.add(new Integer(2));
      right.add(new Integer(0));
      testGreaterThan(left, right);

      //less than
      left.clear();
      right.clear();

      left.add(new Integer(1));
      right.add(new Integer(2));
      testLessThan(left, right);
      testLikeCase(left, right, false, false);
   }

   private void testTheseCases(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_IN;
      boolean didThrow = false;
      try{
         PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      intOp = PSConditionalEvaluator.OPCODE_BETWEEN;
      didThrow = false;
      try{
         PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      intOp = PSConditionalEvaluator.OPCODE_ISNULL;
      didThrow = false;
      try{
         PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(!didThrow);
   }

   private void testIsNull(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_ISNULL;
      boolean didThrow = false;
      boolean result = false;

      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);
   }

   private void testIsNotNull(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_ISNOTNULL;
      boolean didThrow = false;
      boolean result = false;

      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);
   }

   private void testLikeCase(Object left, Object right, boolean expectThrow,
      boolean expectResult)
   {
      int intOp = PSConditionalEvaluator.OPCODE_LIKE;
      boolean didThrow = false;
      boolean result = false;

      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }

      assertTrue(didThrow == expectThrow);
      assertTrue(result == expectResult);
   }

   private void testNotLikeCase(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_NOTLIKE;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result==true);
   }

   private void testBetweenCase(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_BETWEEN;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result==true);
   }

   private void testLiteralSet(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_IN;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);  // expect true

      intOp = PSConditionalEvaluator.OPCODE_NOTIN;
      didThrow = false;
      result = true;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == false);  // expect false

      intOp = PSConditionalEvaluator.OPCODE_BETWEEN;
      didThrow = false;
      result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);  // expect true

      intOp = PSConditionalEvaluator.OPCODE_NOTBETWEEN;
      didThrow = false;
      result = true;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == false);  // expect false

      intOp = PSConditionalEvaluator.OPCODE_ISNULL;
      didThrow = false;
      try{
         PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
   }

   private void testTextLiteralSet(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_IN;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);  // expect true

      intOp = PSConditionalEvaluator.OPCODE_NOTIN;
      didThrow = false;
      result = true;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == false);  // expect false

      intOp = PSConditionalEvaluator.OPCODE_BETWEEN;
      didThrow = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   private void testEqualCase(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_EQUALS;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);   // expect true
   }

   private void testLessThan(Object left, Object right)
   {
      int intOp[] = {
         PSConditionalEvaluator.OPCODE_LESSTHAN,
         PSConditionalEvaluator.OPCODE_LESSTHANOREQUALS};

      for (int i = 0; i < intOp.length; i++)
      {
         boolean didThrow = false;
         boolean result = false;

         try{
            result = PSConditionalEvaluator.makeComparable2(left, right, intOp[i]);
         } catch(com.percussion.error.PSEvaluationException e){
            didThrow = true;
         }
         assertTrue(didThrow == false);
         assertTrue(result == true);   // expect true
      }
   }

   private void testGreaterThan(Object left, Object right)
   {
      int intOp[] = {
         PSConditionalEvaluator.OPCODE_GREATERTHAN,
         PSConditionalEvaluator.OPCODE_GREATERTHANOREQUALS};

      for (int i = 0; i < intOp.length; i++)
      {
         boolean didThrow = false;
         boolean result = false;

         try{
            result = PSConditionalEvaluator.makeComparable2(left, right, intOp[i]);
         } catch(com.percussion.error.PSEvaluationException e){
            didThrow = true;
         }
         assertTrue(didThrow == false);
         assertTrue(result == true);   // expect true
      }
   }

   private void testNotEqualCase(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_NOTEQUALS;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow == false);
      assertTrue(result == true);   // expect true
   }

   private void testEqualNullCase(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_EQUALS;
      boolean didThrow = false;
      try{
         PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   private void testNullNullCase(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_EQUALS;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(!didThrow);
      assertTrue(result == true); // expect true;
   }

   private void testNormalCases(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_LESSTHANOREQUALS;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(!didThrow);
      assertTrue(result == true); // expect true
   }

   private void testNormalCasesInverse(Object left, Object right)
   {
      int intOp = PSConditionalEvaluator.OPCODE_GREATERTHANOREQUALS;
      boolean didThrow = false;
      boolean result = false;
      try{
         result = PSConditionalEvaluator.makeComparable2(left, right, intOp);
      } catch(com.percussion.error.PSEvaluationException e){
         didThrow = true;
      }
      assertTrue(!didThrow);
      assertTrue(result == true); // expect true
   }
   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2Lists"));
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2Number"));
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2Date"));
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2String"));
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2PSDateLiteral"));
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2PSNumericLiteral"));
      suite.addTest(new PSConditionalEvaluatorTest("testMakeComparable2PSTextLiteral"));
      return suite;
   }
}
