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
package com.percussion.utils.beans;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

/**
 * Unit test for the property wrapper class. Uses a static class to create
 * objects to test with.
 * 
 * @author dougrand
 * 
 */
public class PSPropertyWrapperTest extends TestCase
{
   final static int COUNT = 50000;

   PSPropertyWrapper testWrapper = null;

   TestPropClass testObj = null;

   /*
    * (non-Javadoc)
    * 
    * @see junit.framework.TestCase#setUp()
    */
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();

      // Create instance and wrapper
      testObj = new TestPropClass("otter", 123, 3.14159);
      testWrapper = new PSPropertyWrapper(testObj);
   }

   /**
    * Required ctor for JUnit
    * 
    * @param arg0 name argument
    */
   public PSPropertyWrapperTest(String arg0) {
      super(arg0);
   }

   /**
    * Test class provides a number of data members to access for the tests. The
    * three data members are all basic data, but provide a decent cross section
    * of typical property access
    */
   static class TestPropClass
   {
      String mi_aaa;

      long mi_bbb;

      double mi_ccc;

      /**
       * Ctor for test class
       * @param a
       * @param b
       * @param c
       */
      public TestPropClass(String a, long b, double c) {
         mi_aaa = a;
         mi_bbb = b;
         mi_ccc = c;
      }

      /**
       * @return Returns the aaa.
       */
      public String getAaa()
      {
         return mi_aaa;
      }

      /**
       * @return Returns the bbb.
       */
      public long getBbb()
      {
         return mi_bbb;
      }

      /**
       * @return Returns the ccc.
       */
      public double getCcc()
      {
         return mi_ccc;
      }

      /**
       * @param aaa The aaa to set.
       */
      public void setAaa(String aaa)
      {
         mi_aaa = aaa;
      }

      /**
       * @param bbb The bbb to set.
       */
      public void setBbb(long bbb)
      {
         mi_bbb = bbb;
      }

      /**
       * @param ccc The ccc to set.
       */
      public void setCcc(double ccc)
      {
         mi_ccc = ccc;
      }

      /**
       * Get property value by name
       * 
       * @param prop the prop to return, must match a property
       * @return the value as an object
       */
      public Object getByName(String prop)
      {
         if (StringUtils.isBlank(prop))
         {
            throw new IllegalArgumentException("prop may not be null or empty");
         }
         if (prop.equals("aaa"))
         {
            return mi_aaa;
         }
         if (prop.equals("bbb"))
         {
            return new Long(mi_bbb);
         }
         if (prop.equals("ccc"))
         {
            return new Double(mi_ccc);
         }
         throw new IllegalArgumentException("Unknown property " + prop);
      }
   }

   /**
    * As the name implies, tests access to the string property via the accessor.
    * Each test compares calling the accessor directly with calling the accessor
    * via reflection
    * 
    * @throws Exception
    */
   public void testStringAccess() throws Exception
   {
      Object rval = testWrapper.getPropertyValue("aaa");
      assertEquals(rval.getClass(), String.class);
      assertEquals(rval, testObj.getAaa());
   }

   /**
    * As the name implies, tests access to the long property via the accessor.
    * Each test compares calling the accessor directly with calling the accessor
    * via reflection
    * 
    * @throws Exception
    */
   public void testLongAccess() throws Exception
   {
      Object rval = testWrapper.getPropertyValue("bbb");
      assertEquals(rval.getClass(), Long.class);
      assertEquals(rval, new Long(testObj.getBbb()));
   }

   /**
    * As the name implies, tests access to the double property via the accessor.
    * Each test compares calling the accessor directly with calling the accessor
    * via reflection
    * 
    * @throws Exception
    */
   public void testDoubleAccess() throws Exception
   {
      Object rval = testWrapper.getPropertyValue("ccc");
      assertEquals(rval.getClass(), Double.class);
      assertEquals(rval, new Double(testObj.getCcc()));
   }

   /**
    * It's useful to know the relative performance metrics are comparing the
    * property wrapper versus direct access
    * 
    * @throws Exception
    */
   public void testAccessTime() throws Exception
   {
      testWrapper.getPropertyValue("aaa");
      testWrapper.getPropertyValue("bbb");
      testWrapper.getPropertyValue("ccc");

      long start = System.nanoTime();
      for (int i = 0; i < COUNT; i++)
      {
         testObj = new TestPropClass("foo", i, 1.23);
         testWrapper = new PSPropertyWrapper(testObj);
         testWrapper.getPropertyValue("aaa");
         testWrapper.getPropertyValue("bbb");
         testWrapper.getPropertyValue("ccc");
      }
      long end = System.nanoTime();
      long per = (end - start) / (3 * COUNT);
      System.out.println("Per prop time " + per + "ns");
   }

   /**
    * Calculate the type to construct a wrapper
    * 
    * @throws Exception
    */
   public void testConsOverheadTime() throws Exception
   {
      long start = System.nanoTime();
      for (int i = 0; i < COUNT; i++)
      {
         testObj = new TestPropClass("foo", i, 1.23);
         testWrapper = new PSPropertyWrapper(testObj);
      }
      long end = System.nanoTime();
      long per = (end - start) / COUNT;
      System.out.println("Per cons time " + per + "ns");
   }

   /**
    * By name access type calculation
    * 
    * @throws Exception
    */
   public void testPropTime() throws Exception
   {
      long start = System.nanoTime();
      for (int i = 0; i < COUNT; i++)
      {
         testObj.getByName("aaa");
         testObj.getByName("bbb");
         testObj.getByName("ccc");
      }
      long end = System.nanoTime();
      long per = (end - start) / (3 * COUNT);
      System.out.println("Per byname time " + per + "ns");
   }

   /**
    * As the name implies, tests writing to the string property. Each test
    * compares calling the setter directly with calling the setter via
    * reflection
    * 
    * @throws Exception
    */
   public void testStringSetter() throws Exception
   {
      testWrapper.setProperty("aaa", "newvalue");
      assertEquals("newvalue", testObj.getAaa());
   }

   /**
    * As the name implies, tests writing to the long property. Each test
    * compares calling the setter directly with calling the setter via
    * reflection
    * 
    * @throws Exception
    */
   public void testLongSetter() throws Exception
   {
      testWrapper.setProperty("bbb", 159);
      assertEquals(159, testObj.getBbb());
   }

   /**
    * As the name implies, tests writing to the double property. Each test
    * compares calling the setter directly with calling the setter via
    * reflection
    * 
    * @throws Exception
    */
   public void testDoubleSetter() throws Exception
   {
      testWrapper.setProperty("ccc", 2.71828);
      assertEquals(2.71828, testObj.getCcc());
   }

   /**
    * Check that lazy loading feature is operational
    * 
    * @throws Exception
    */
   public void testLazyLoad() throws Exception
   {
      IPSPropertyLoader loader = new IPSPropertyLoader()
      {

         public Object getLazy()
         {
            return new TestPropClass("foobar", 98989, 2.5);
         }
      };

      PSPropertyWrapper llwrap = new PSPropertyWrapper(loader);
      String value = (String) llwrap.getPropertyValue("aaa");
      assertEquals("foobar", value);
   }
}
