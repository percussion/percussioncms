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

import java.util.Hashtable;
import java.util.Locale;
import java.util.MissingResourceException;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *   Unit tests for the PSHashTableFromBundle class. This class is private
 *   and is not meant to be shipped with the product.
 */
public class PSHashTableFromBundleTest extends TestCase
{
   public PSHashTableFromBundleTest(String name)
   {
      super(name);
   }

   /**
    *   Test with a valid resource bundle file using the default
    *   locale. Make sure the returned hashtable is equal to the
    *   known valid hashtable that is contained in that .properties
    *   file.
    */
   public void testConstructorWithValidBundleAndDefaultLocale()
   {
      PSHashTableFromBundle table = new PSHashTableFromBundle(
         m_validBundleName, Locale.getDefault());

      assertTrue(null != table);
      assertEquals(table, m_validTable);
      assertTrue(!table.equals(m_invalidTable));
   }

   /**
    *   Test with a valid resource bundle file using no locale.
    *   Make sure the returned hashtable is equal to the
    *   known valid hashtable that is contained in that .properties
    *   file.
    */
   public void testConstructorWithValidBundleAndNoLocale()
   {
      PSHashTableFromBundle table =
         new PSHashTableFromBundle(m_validBundleName);

      assertTrue(null != table);
      assertEquals(table, m_validTable);
      assertTrue(!table.equals(m_invalidTable));
   }

   /**
    *   Test with an existing but invalid bundle (one that has
    *   keys which cannot be represented as integers). Make sure
    *   it returns null.
    */
   public void testConstructorWithInvalidBundleAndDefaultLocale()
   {
      boolean didThrow = false;
      try
      {
         PSHashTableFromBundle table = 
            new PSHashTableFromBundle(m_invalidBundleName,
               Locale.getDefault());
      }
      catch (NumberFormatException nfe)
      {
         didThrow = true;
      }

      assertTrue(didThrow);
   }

   /**
    *   Test with an existing but invalid bundle (one that has
    *   keys which cannot be represented as integers). Make sure
    *   it returns null.
    */
   public void testConstructorWithInvalidBundleAndNoLocale()
   {
      boolean didThrow = false;
      try
      {
         PSHashTableFromBundle table =
            new PSHashTableFromBundle(m_invalidBundleName);
      }
      catch (NumberFormatException nfe)
      {
         didThrow = true;
      }

      assertTrue(didThrow);
   }

   /**
    *   Test with a non existent bundle and make sure it throws
    *   MissingResourceException
    */
   public void testConstructorWithNonExistentBundleAndDefaultLocale()
   {
      boolean didThrow = false;
      try
      {
         PSHashTableFromBundle table = new PSHashTableFromBundle(
            m_nonExistentBundleName, Locale.getDefault());
      }
      catch (MissingResourceException e)
      {
         didThrow = true;
      }

      assertTrue(didThrow);
   }

   /**
    *   Test with a non existent bundle and make sure it throws
    *   MissingResourceException
    */
   public void testConstructorWithNonExistentBundleAndNoLocale()
   {
      boolean didThrow = false;
      try
      {
         PSHashTableFromBundle table = new PSHashTableFromBundle(
            m_nonExistentBundleName);
      }
      catch (MissingResourceException e)
      {
         didThrow = true;
      }

      assertTrue(didThrow);
   }

   /**
    *   Set up variables for unit tests
    */
   public void setUp()
   {
      m_validTable = new Hashtable(6);
      m_validTable.put(new Integer(1), "The first number");
      m_validTable.put(new Integer(2), "A perfect number");
      m_validTable.put(new Integer(100), "The number of senators");
      m_validTable.put(new Integer(255), "Two to the eigth power minus one");
      m_validTable.put(new Integer(1024), "Two to the tenth power");
      m_validTable.put(new Integer(666), "The number of the beast");

      m_invalidTable = new Hashtable(1);
      m_invalidTable.put("Yo" , "NOT The number of senators");
   }


   /** 
    *   collect all tests into a TestSuite and return it
    */
   public static Test suite()
   {
      TestSuite suite = new TestSuite();
      suite.addTest(new PSHashTableFromBundleTest(
         "testConstructorWithValidBundleAndDefaultLocale"));
      suite.addTest(new PSHashTableFromBundleTest(
         "testConstructorWithValidBundleAndNoLocale"));
      suite.addTest(new PSHashTableFromBundleTest(
         "testConstructorWithInvalidBundleAndDefaultLocale"));
      suite.addTest(new PSHashTableFromBundleTest(
         "testConstructorWithInvalidBundleAndNoLocale"));
      suite.addTest(new PSHashTableFromBundleTest(
         "testConstructorWithNonExistentBundleAndDefaultLocale"));
      suite.addTest(new PSHashTableFromBundleTest(
         "testConstructorWithNonExistentBundleAndNoLocale"));
      return suite;
   }

   private String m_validBundleName = "com.percussion.testing.PSHashTestBundleValid";
   private String m_invalidBundleName = "com.percussion.testing.PSHashTestBundleInvalid";
   private String m_nonExistentBundleName = "com.percussion.foo.PSHashTestBundle";

   private Hashtable m_validTable = null;
   private Hashtable m_invalidTable = null;
}
