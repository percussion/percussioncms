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
package com.percussion.analytics.data;

import com.percussion.analytics.data.IPSAnalyticsQueryResult.DataType;
import com.percussion.analytics.data.impl.PSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsQueryResultException;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author erikserating
 *
 */
@SuppressWarnings({"deprecation"})
public class PSAnalyticsQueryResultTest
{
   
   @Before
   public void setUp() throws Exception
   {
      result = new PSAnalyticsQueryResult();
      result.put(KEY_STRING, VALUE_STRING);
      result.put(KEY_INT, VALUE_INT);
      result.put(KEY_LONG, VALUE_LONG);
      result.put(KEY_FLOAT, VALUE_FLOAT);
      result.put(KEY_DATE, VALUE_DATE);
      
   }
   
   @SuppressWarnings("unchecked")
   @Test
   public void testPutMethods() throws Exception
   {
      PSAnalyticsQueryResult r =  new PSAnalyticsQueryResult();
      Map<String, Object> vals = new HashMap<String, Object>();
      vals.put(KEY_STRING, VALUE_STRING);
      vals.put(KEY_INT, VALUE_INT);
      vals.put(KEY_LONG, VALUE_LONG);
      vals.put(KEY_FLOAT, VALUE_FLOAT);
      vals.put(KEY_DATE, VALUE_DATE);
      r.putAll(vals);
      assertEquals(r.keySet().size(), 5);
      
      try
      {
         r.put("INVALID_CLASS", new HashMap<>());
         fail("Class is an invalid type and put operation should throw an exception.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Class type is not supported.");
      }
      
      try
      {
    	  r.put(KEY_INT, null);
      }
      catch (IllegalArgumentException e)
      {
    	  assertEquals(e.getMessage(),"Value cannot be null.");
      }
      
      try
      {
    	  r.put(null, new HashMap());
      }
      catch (IllegalArgumentException e) 
      {
    	  assertEquals(e.getMessage(),"key cannot be null or empty.");
      }
      
      try
      {
    	  r.putAll(null);
      }
      catch (IllegalArgumentException e)
      {
    	  assertEquals(e.getMessage(),"values cannot be null or empty.");
      }
   }
   @Test
   public void testHasValue() throws Exception
   {
      assertTrue(result.hasValue(KEY_INT));
      assertFalse(result.hasValue("DUMMY_KEY"));
      
      try
      {
    	  boolean hBool = result.hasValue(null);
      }
      catch (IllegalArgumentException e)
      {
    	  assertEquals(e.getMessage(),"key cannot be null or empty.");
      }
   }
   
   @Test
   public void testGetDataType() throws Exception
   {
	   DataType testDataType;
	   try 
	   {
		   testDataType = result.getDataType(null);
	   }
	   catch (IllegalArgumentException e)
	   {
		   assertEquals(e.getMessage(),"key cannot be null or empty.");
	   }
   }

   @Test
   public void testGetDate() throws Exception
   {
	  Date d1 = null; 
	   
	  try 
	  {
		  d1 = result.getDate(null);
	  }
	  catch (IllegalArgumentException e) {
		  assertEquals(e.getMessage(),"key cannot be null or empty.");
	  }
	  
	  Date testDate = result.getDate("null");
	  assertEquals(null,testDate);
	  
      d1 = result.getDate(KEY_DATE);
      assertEquals(d1, VALUE_DATE);      
      try
      {
         result.getDate(KEY_INT);
         fail("Should not been able to return date for an integer type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Date");
      }
      try
      {
         result.getDate(KEY_LONG);
         fail("Should not been able to return date for a long type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Date");
      }
      try
      {
         result.getDate(KEY_FLOAT);
         fail("Should not been able to return date for a float type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Date");
      }
      try
      {
         result.getDate(KEY_STRING);
         fail("Should not been able to return date for a string type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Date");
      }
   }

   @Test
   public void testGetInt() throws Exception
   { 
	  try 
	  {
		  int testNull = result.getInt(null);		  
	  }
	  catch (IllegalArgumentException e)
	  {
		  assertEquals(e.getMessage(),"key cannot be null or empty.");
	  }
	   
	  int iNull = result.getInt("null");
	  assertEquals(iNull,-1);
	   
      int i1 = result.getInt(KEY_INT);
      assertEquals(i1, VALUE_INT);
      
      int i2 = result.getInt(KEY_FLOAT);
      assertEquals(i2, 23);
      
      int i3 = result.getInt(KEY_LONG);
      assertEquals(i3, 35);
      
      try
      {
         result.getInt(KEY_STRING);
         fail("Should not been able to return integer for a string type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Integer");
      }
      try
      {
         result.getInt(KEY_DATE);
         fail("Should not been able to return integer for a date type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Integer");
      }
      
      
   }

   @Test
   public void testGetString() throws Exception
   {
	  String sNull = result.getString("null");
	  assertEquals(sNull,null);
	   
      String s1 = result.getString(KEY_STRING);
      assertEquals(s1, VALUE_STRING);

      String s3 = result.getString(KEY_INT);
      assertEquals(s3, "14");
      
      String s4 = result.getString(KEY_FLOAT);
      assertEquals(s4, "23.45");
      
      String s5 = result.getString(KEY_LONG);
      assertEquals(s5, "35");
      
      try
      {
    	  String strNull = result.getString(null);
      }
      catch (IllegalArgumentException e)
      {
    	  assertEquals(e.getMessage(),"key cannot be null or empty.");
      }
   }

   @Test
   public void testGetFloat() throws Exception
   {
	  try
	  {
		  result.getFloat(null);
	  }
	  catch (IllegalArgumentException e)
	  {
		  assertEquals(e.getMessage(),"key cannot be null or empty.");
	  }
	   
      float f1 = result.getFloat(KEY_FLOAT);
      assertEquals(VALUE_FLOAT,f1,0);
      
      float f2 = result.getFloat(KEY_INT);
      assertEquals(14F,f2, 0);
      
      float f3 = result.getFloat(KEY_LONG);
      assertEquals( 35F, f3, 0);
      
      float f4 = result.getFloat("null");
      assertEquals(null,result.getDate("null"));
      
      try
      {
         result.getFloat(KEY_STRING);
         fail("Should not been able to return float for a string type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Float");
      }
      try
      {
         result.getFloat(KEY_DATE);
         fail("Should not been able to return float for a date type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Float");
      }
   }

   @Test
   public void testGetLong() throws Exception
   {
	  long fNull = result.getLong("null");
	  assertEquals(fNull, -1);
	   
      long f1 = result.getLong(KEY_LONG);
      assertEquals(f1, VALUE_LONG);
      
      long f2 = result.getLong(KEY_INT);
      assertEquals(f2, 14L);
      
      long f3 = result.getLong(KEY_FLOAT);
      assertEquals(f3, 23L);
      
      long f4 = result.getLong(KEY_DATE);
      assertEquals(f4, VALUE_DATE.getTime());
      
      try
      {
    	  long testNull = result.getLong(null);
      }
      catch (IllegalArgumentException e) 
      {
    	  assertEquals(e.getMessage(),"key cannot be null or empty.");
      }
      
      try
      {
         result.getLong(KEY_STRING);
         fail("Should not been able to return long for a string type field.");
      }
      catch (PSAnalyticsQueryResultException e)
      {
         assertEquals(e.getMessage(), "Type cannot be converted to a Long");
      }
   }

   @Test
   public void testConstructor() throws Exception
   {
	   Map<String,Object> testMap = new HashMap<String,Object>();
	   testMap.put(KEY_STRING,VALUE_STRING);
	   testMap.put(KEY_INT,VALUE_INT);
	   testMap.put(KEY_LONG,VALUE_LONG);
	   testMap.put(KEY_FLOAT,VALUE_FLOAT);
	   testMap.put(KEY_DATE,VALUE_DATE);
	   final PSAnalyticsQueryResult resultConstructorTest = new PSAnalyticsQueryResult(testMap);
	   assertEquals(resultConstructorTest.keySet().size(),5);
   }
   
   private PSAnalyticsQueryResult result;
   
   private static final String KEY_STRING = "stringKey";
   private static final String KEY_INT = "intKey";
   private static final String KEY_LONG = "longKey";
   private static final String KEY_FLOAT = "floatKey";
   private static final String KEY_DATE = "dateKey";
   
   private static final String VALUE_STRING = "FooBar";
   private static final int VALUE_INT = 14;
   private static final long VALUE_LONG = 35L;
   private static final float VALUE_FLOAT = 23.45F;
   private static final Date VALUE_DATE = new Date(89, 4, 10);
   
}
