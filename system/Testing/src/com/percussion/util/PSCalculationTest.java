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

package com.percussion.util;

import com.percussion.design.objectstore.PSNumericLiteral;
import com.percussion.design.objectstore.PSTextLiteral;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.lang3.time.FastDateFormat;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * The PSCalculationTest is the unit test for PSCalculation class which
 * handles basic mathematical operation.
 *
 * @author     Jian Huang
 * @version    2.0
 * @since      1.0
 */
public class PSCalculationTest extends TestCase
{
   public PSCalculationTest(String name)
   {
      super(name);
   }

   public void testAdd() throws Exception
   {
      PSCalculation calculate = new PSCalculation();

      Object o1, o2, result;
      double resultValue = 0;
      double value1 = 100;
      double value2 = -50;

      o1 = new Double(value1);
      o2 = new BigDecimal(value2);

      boolean didThrow;

      // test (null + null), (normal + null), (null + normal), (normal + normal),
      // (abnormal + normal), and (normal + abnormal)
      didThrow = false;
      try{
         result = calculate.add(null, null);
         assertTrue(result == null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.add(o1, null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.add(null, o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.add(o1, o2);
         resultValue = ((Number)result).doubleValue();
         assertTrue(resultValue == (value1 + value2));
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.add("abnormal", o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.add(o1, "abnormal");
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   public void testSubtract() throws Exception
   {
      PSCalculation calculate = new PSCalculation();

      Object o1, o2, result;
      double resultValue = 0;
      double value1 = 100;
      double value2 = -50;

      java.text.DecimalFormat format = new java.text.DecimalFormat();
      Double numOne  = new Double(value1);
      o1 = new PSNumericLiteral(numOne, format);
      o2 = new PSTextLiteral("-50");

      boolean didThrow;

      // test (null - null), (normal - null), (null - normal), (normal - normal),
      // (abnormal - normal), and (normal - abnormal)
      didThrow = false;
      try{
         result = calculate.subtract(null, null);
         assertTrue(result == null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.subtract(o1, null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.subtract(null, o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.subtract(o1, o2);
         resultValue = ((Number)result).doubleValue();
         assertTrue(resultValue == (value1 - value2));
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.subtract("abnormal", o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.subtract(o1, "abnormal");
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   public void testMultiply() throws Exception
   {
      PSCalculation calculate = new PSCalculation();

      Object o1, o2, result;
      double resultValue = 0;
      double value1 = 100;
      double value2 = -50;

      o1 = new Double(value1);
      o2 = new BigDecimal(value2);

      boolean didThrow;

      // test (null * null), (normal * null), (null * normal), (normal * normal),
      // (abnormal * normal), and (normal * abnormal)
      didThrow = false;
      try{
         result = calculate.multiply(null, null);
         assertTrue(result == null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.multiply(o1, null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.multiply(null, o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.multiply(o1, o2);
         resultValue = ((Number)result).doubleValue();
         assertTrue(resultValue == (value1 * value2));
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.multiply("abnormal", o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.multiply(o1, "abnormal");
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   public void testDivide() throws Exception
   {
      PSCalculation calculate = new PSCalculation();

      Object o1, o2, result;
      double resultValue = 0;
      double value1 = 100;
      double value2 = -50;

      o1 = new Double(value1);
      o2 = new BigDecimal(value2);

      boolean didThrow;

      // test (null / null), (normal / null), (null / normal), (normal / normal),
      // (abnormal / normal), (normal / abnormal), and (normal / 0)
      didThrow = false;
      try{
         result = calculate.divide(null, null);
         assertTrue(result == null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.divide(o1, null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.divide(null, o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.divide(o1, o2);
         resultValue = ((Number)result).doubleValue();
         assertTrue(resultValue == (value1 / value2));
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.divide("abnormal", o2);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.divide(o1, "abnormal");
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.divide(o1, new Integer(0));
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);
   }

   public void testNumberVerify() throws Exception
   {
      PSCalculation calculate = new PSCalculation();

      Object result;
      boolean didThrow;

      didThrow = false;
      try{
         result = calculate.numberVerify(null);
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.numberVerify("not a number");
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.numberVerify(new Date());
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(didThrow);

      didThrow = false;
      try{
         result = calculate.numberVerify(new Integer(0));
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);

      didThrow = false;
      try{
         result = calculate.numberVerify("100");
      } catch (IllegalArgumentException e){
         didThrow = true;
      }
      assertTrue(!didThrow);
   }

    public void testDateAdjust() throws Exception
   {
      PSCalculation calculate = new PSCalculation();

      Object result = calculate.dateAdjust(null, 1, 1, 1, 1, 1, 1);
      assertTrue(result == null);

      String strDate = "1999-12-01 00:00:00";
      int nYear    = 1;
      int nMonth   = 0;
      int nDay     = -1;
      int nHour    = 24;
      int nMin     = 3;
      int nSec     = 61;

      PSDate dateNew = null;
      Calendar dateOld = null;
      FastDateFormat df = FastDateFormat.getInstance(
         PSDataTypeConverter.getRecognizedDateFormat(strDate));
      if (df != null)
      {
         // this should not throw a parse exception
         Date day = df.parse(strDate);
         dateOld = Calendar.getInstance();
         dateOld.setTime(day);
         dateNew = PSCalculation.dateAdjust(dateOld, nYear, nMonth, nDay, nHour,
            nMin, nSec);
      }

      assertTrue("Could not parse date: " + strDate, dateNew != null);
      assertTrue("(" + dateNew.toString() + ") equals (" + strDate + ")", (
         dateNew.toString()).equals("2000-12-01 00:04:01"));
   }

   // collect all tests into a TestSuite and return it
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSCalculationTest("testAdd"));
      suite.addTest(new PSCalculationTest("testSubtract"));
      suite.addTest(new PSCalculationTest("testMultiply"));
      suite.addTest(new PSCalculationTest("testDivide"));
      suite.addTest(new PSCalculationTest("testNumberVerify"));
      suite.addTest(new PSCalculationTest("testDateAdjust"));
      return suite;
   }
}
