/******************************************************************************
 *
 * [ PSFieldValidationTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2007 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.validate;

import java.util.Date;

import junit.framework.TestCase;

/**
 * Check validations
 * 
 * @author dougrand
 */
public class PSFieldValidationTest extends TestCase
{
   Object[] packageArgs(Object... objects)
   {
      return objects;
   }
   
   public void testRequired() throws Exception
   {
      PSValidateRequired vr = new PSValidateRequired();
      
      assertFalse((Boolean) vr.processUdf(packageArgs((String) null), null));
      assertTrue((Boolean) vr.processUdf(packageArgs(1), null));
   }

   public void testJexl() throws Exception
   {
      PSValidateJexlExpression je = new PSValidateJexlExpression();

      assertTrue((Boolean) je.processUdf(packageArgs(0, "$value == 0"), null));
      assertFalse((Boolean) je.processUdf(packageArgs(0, "$value != 0"), null));
   }

   public void testNumber() throws Exception
   {
      PSValidateNumber numberV = new PSValidateNumber();

      // First the happy (success tests)

      assertTrue((Boolean) numberV.processUdf(packageArgs(1, 1, true, 1, true),
            null));
      assertTrue((Boolean) numberV.processUdf(packageArgs("1", "1", "true",
            "1", "true"), null));

      // Missing optional args
      assertTrue((Boolean) numberV.processUdf(packageArgs(1, 1, true, null,
            true), null));
      assertTrue((Boolean) numberV.processUdf(packageArgs(1), null));
      assertFalse((Boolean) numberV.processUdf(packageArgs(1, null, true, 1), null));

      // Fail validation
      assertFalse((Boolean) numberV.processUdf(
            packageArgs(2, 1, true, 1, false), null));
      assertFalse((Boolean) numberV.processUdf(
            packageArgs(0, 1, true, 1, false), null));
      assertFalse((Boolean) numberV.processUdf(packageArgs(0, 1, true, null,
            false), null));
      assertFalse((Boolean) numberV.processUdf(packageArgs(0, 1, true, null,
            false), null));
      assertFalse((Boolean) numberV.processUdf(packageArgs(1, 1, false, null,
            false), null));      
      assertFalse((Boolean) numberV.processUdf(packageArgs(1, 0, false, 1,
            false), null));
      assertFalse((Boolean) numberV.processUdf(packageArgs(null, null, true,
            null, true), null));
   }

   public void testString() throws Exception
   {
      PSValidateStringLength stringV = new PSValidateStringLength();
      PSValidateStringPattern stringP = new PSValidateStringPattern();

      // First the happy (success tests)
      String phone = "\\d\\d\\d-\\d\\d\\d-\\d\\d\\d\\d";

      assertTrue((Boolean) stringV.processUdf(packageArgs("a", 1, 3), null));
      assertTrue((Boolean) stringV.processUdf(packageArgs("abc", 1, 3), null));
      assertTrue((Boolean) stringP.processUdf(
            packageArgs("111-222-3333", phone), null));

      // Missing optional args
      assertTrue((Boolean) stringV.processUdf(packageArgs("a", 1, null), null));
      assertTrue((Boolean) stringV.processUdf(packageArgs("a", null, 2), null));
      assertTrue((Boolean) stringV.processUdf(packageArgs("a", null, null),
            null));

      assertFalse((Boolean) stringV.processUdf(packageArgs(null, 1, 3, true),
            null));
      assertFalse((Boolean) stringV.processUdf(packageArgs("abcd", 1, 3), null));
      assertFalse((Boolean) stringV.processUdf(packageArgs("abcd", 5, 8), null));
   }

   public void testDate() throws Exception
   {
      PSValidateDate dateV = new PSValidateDate();

      // First the happy (success tests)
      Date max = new Date(100);
      Date val = new Date(99);
      Date min = new Date(98);
      assertTrue((Boolean) dateV.processUdf(packageArgs(val, min, true, max, true),
            null));
      assertTrue((Boolean) dateV.processUdf(packageArgs(min, min, true, max, true),
            null));
      assertTrue((Boolean) dateV.processUdf(packageArgs(max, min, true, max, true),
            null));

      // Missing optional args
      assertTrue((Boolean) dateV.processUdf(packageArgs(val, null, true, max, true),
            null));
      assertTrue((Boolean) dateV.processUdf(packageArgs(val, min, true, null, true),
            null));
      assertTrue((Boolean) dateV.processUdf(
            packageArgs(val, null, true, null, false), null));

      // Failures
      assertFalse((Boolean) dateV.processUdf(
            packageArgs(min, min, false, max, false), null));
      assertFalse((Boolean) dateV.processUdf(
            packageArgs(max, min, false, max, false), null));      

      // Conversion
      String sval = "2005/01/02";
      String smin = "2005/01/01";
      String smax = "2005/01/03";
      assertTrue((Boolean) dateV.processUdf(
            packageArgs(sval, smin, true, smax, false), null));
      assertTrue((Boolean) dateV.processUdf(
            packageArgs(smin, smin, true, smax, false), null));
      assertTrue((Boolean) dateV.processUdf(
            packageArgs(smax, smin, true, smax, true), null));
   }

}
