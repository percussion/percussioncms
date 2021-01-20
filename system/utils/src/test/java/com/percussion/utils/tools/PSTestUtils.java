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
package com.percussion.utils.tools;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;

/**
 * Class of unit test utility functions.
 */
public class PSTestUtils
{
  /**
    * Convenience method that calls 
    * {@link #testSetter(Object, String, String, Class, boolean) 
    * testSetter(obj, prop, val, String.class, shouldThrow)}
    */
   public static void testSetter(Object obj, String prop, String val, 
      boolean shouldThrow) throws Exception
   {
      testSetter(obj, prop, val, String.class, shouldThrow);
   }
  /**
    * Tests the specified get/set methods.  Invokes the set method of the 
    * supplied object with the supplied value.  If this suceeds and 
    * <code>shouldThrow</code> is <code>false</code>, it then tests equality on 
    * the supplied value with the value returned from the get method to see if 
    * they are equal.
    * 
    * @param obj The object on which to call the methods, may not be 
    * <code>null</code>.
    * @param prop The bean style property name, e.g. the portion of the set/get
    * method name following the word "get" or "set", case-sensitive (do not
    * camel case the property name).  May not be <code>null</code> or empty. 
    * @param val The value to set, may be <code>null</code>.
    * @param valClass The class of the value, supplied in case <code>val</code>
    * is <code>null</code>, may not be <code>null</code>.
    * @param shouldThrow <code>true</code> if setting the supplied value should 
    * cause an IllegalArgumentException to be thrown, <code>false</code> if not.
    * 
    * @throws Exception If the test fails.
    */
   public static void testSetter(Object obj, String prop, Object val, 
      Class valClass, boolean shouldThrow) throws Exception
   {
      if (obj == null)
         throw new IllegalArgumentException("obj may not be null");
      
      if (StringUtils.isBlank(prop))
         throw new IllegalArgumentException("prop may not be null or empty");
      
      if (valClass == null)
         throw new IllegalArgumentException("valClass may not be null");
      
      Class objClass = obj.getClass();
      Method setter = objClass.getMethod("set" + prop, new Class[] {valClass});
      Method getter = objClass.getMethod("get" + prop, new Class[] {});
      
      boolean didThrow = false;
      try
      {
         setter.invoke(obj, new Object[] {val});
      }
      catch (InvocationTargetException e)
      {
         if (e.getCause() instanceof IllegalArgumentException)
            didThrow = true;
         else
         {
            e.fillInStackTrace();
            throw e;
         }
      }
      

      TestCase.assertEquals(shouldThrow, didThrow);
      if (!didThrow)
         TestCase.assertEquals(val, getter.invoke(obj, new Object[] {}));

   }
   
   /**
    * Tests constructing an object from the provided ctor arguments. All args
    * can be <code>null</code> or empty.
    * 
    * @param objClass The class to instantiate, may not be <code>null</code>.
    * @param params Array of ctor param types, may not be <code>null</code>.
    * @param args Array of ctor args, may not be <code>null</code>.
    * @param shouldThrow <code>true</code> if the supplied args should cause
    * an exception to be thrown, <code>false</code> if not.
    * 
    * @return The constructed object.
    * 
    * @throws Exception If the test fails.
    */
   @SuppressWarnings(value={"unchecked"})
   public static Object testCtor(Class objClass, Class[] params, Object[] args, 
      boolean shouldThrow) throws Exception
   {
      if (objClass == null)
         throw new IllegalArgumentException("objClass may not be null");
      if (params == null)
         throw new IllegalArgumentException("params may not be null");
      if (args == null)
         throw new IllegalArgumentException("args may not be null");
      
      boolean result = !shouldThrow;
      Object test = null;
      try
      {
         Constructor ctor = objClass.getConstructor(params);
         test = ctor.newInstance(args);
      }
      catch (Exception e)
      {
         result = shouldThrow;
      }
      
      TestCase.assertTrue(result);
      
      return test;
   }
}

