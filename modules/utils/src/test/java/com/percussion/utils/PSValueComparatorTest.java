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
package com.percussion.utils;

import com.percussion.utils.jsr170.PSValueComparator;
import com.percussion.utils.jsr170.PSValueFactory;
import junit.framework.TestCase;

import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

public class PSValueComparatorTest extends TestCase
{
   static PSValueComparator vc = new PSValueComparator();
   
   public void testCompareLongs() throws Exception
   {
      doTest(1L, 2L);
   }
   
   public void testCompareDoubles() throws Exception
   {
      doTest(1.1, 1.2);
   }   
   
   public void testCompareDates() throws Exception
   {
      Calendar c1, c2;
      
      c1 = new GregorianCalendar();
      c2 = new GregorianCalendar();
      
      c1.set(2005, 5, 1);
      c2.set(2005, 5, 2);
      
      doTest(c1, c2);
   }

   public void testCompareStrings() throws Exception
   {
      doTest("aaaa","aaab");
      
      vc.setLocale(new Locale("fr_FR"));
      doTest("aaaa","aaab");
      
      // The only distinct rule in spanish for Java's configuration
      // is for enya
      vc.setLocale(new Locale("es_ES"));
      doTest("n", "\u00F1");
      doTest("n", "\u00D1");
      doTest("N", "\u00F1");
      doTest("N", "\u00D1");
      doTest("\u00F1", "o");
      doTest("\u00D1", "o");
      doTest("\u00F1", "O");
      doTest("\u00D1", "O");
   }   
   
   
   public void testCompareBooleans() throws Exception
   {
      doTest(Boolean.FALSE, Boolean.TRUE);
   }   
   
   public void testCompareFirstNull() throws Exception
   {
      Value v2 = PSValueFactory.createValue((Object) "a");
      Value v1 = null;
      
      int r = vc.compare(v1, v2);
      assertTrue(r < 0);
   }
   
   public void testCompareSecondNull() throws Exception
   {
      Value v1 = PSValueFactory.createValue((Object) "a");
      Value v2 = null;
      
      int r = vc.compare(v1, v2);
      assertTrue(r > 0);
   }
   
   public void testCompareBothNull() throws Exception
   {
      int r = vc.compare(null, null);
      assertTrue(r == 0);
   }

   private void doTest(Object o1, Object o2) throws ValueFormatException
   {
      Value v1 = PSValueFactory.createValue(o1);
      Value v2 = PSValueFactory.createValue(o2);
      
      int r = vc.compare(v1, v2);
      assertTrue(r < 0);
      
      int r2 = vc.compare(v1, v1);
      assertTrue(r2 == 0);
      
      int r3 = vc.compare(v2, v1);
      assertTrue(r3 > 0);
   }
}
