/******************************************************************************
 *
 * [ PSTranslationTest.java ]
 *
 * COPYRIGHT (c) 1999 - 2006 by Percussion Software, Inc., Woburn, MA USA.
 * All rights reserved. This material contains unpublished, copyrighted
 * work including confidential and proprietary information of Percussion.
 *
 *****************************************************************************/
package com.percussion.extensions.translations;

import com.percussion.data.PSConversionException;

import junit.framework.TestCase;

public class PSTranslationTest extends TestCase
{
   public void testDateTranslations() throws Exception
   {
      PSFormatDate fd = new PSFormatDate();
      PSNormalizeDate nd = new PSNormalizeDate();

      doTest("2004 03 05", "yyyy MM dd", fd, nd);
      doTest("03 2004 05 09:41.12,555", "MM yyyy dd hh:mm.ss,SSS", fd, nd);

   }

   public void testMapTranslations() throws Exception
   {
      PSMapInputValue mip = new PSMapInputValue();
      PSMapOutputValue mop = new PSMapOutputValue();
      String map = "dog=cat&black=white&up=down";

      doTest("dog", "cat", map, mip, mop);
      doTest("black", "white", map, mip, mop);
      doTest("up", "down", map, mip, mop);
   }

   public void testJexlTranslation() throws Exception
   {
      PSJexlInputTranslation jit = new PSJexlInputTranslation();

      Object result = jit.processUdf(packageArgs("32", "$value / 4"), null);
      assertEquals(8.0, result);
   }

   public void testTrim() throws Exception
   {
      PSTrimStringValue tsv = new PSTrimStringValue();

      assertEquals("x", tsv.processUdf(packageArgs("   x   ", null), null));
      assertEquals("x", tsv.processUdf(packageArgs("   x   ", "both"), null));
      assertEquals("x   ", tsv.processUdf(packageArgs("   x   ", "start"), null));
      assertEquals("   x", tsv.processUdf(packageArgs("   x", "end"), null));
   }

   private void doTest(String input, String output, String map,
         PSMapInputValue mip, PSMapOutputValue mop)
         throws PSConversionException
   {
      String rval = (String) mip.processUdf(packageArgs(input, map), null);
      assertNotNull(rval);
      assertEquals(output, rval);

      rval = (String) mop.processUdf(packageArgs(rval, map), null);
      assertNotNull(rval);
      assertEquals(input, rval);
   }

   private void doTest(String input, String fmt, PSFormatDate fd,
         PSNormalizeDate nd) throws PSConversionException
   {
      String parsed = (String) nd.processUdf(packageArgs(input, fmt), null);
      assertNotNull(parsed);

      String output = (String) fd.processUdf(packageArgs(parsed, fmt), null);
      assertEquals(input, output);
   }

   Object[] packageArgs(Object... objects)
   {
      return objects;
   }
}
