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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.analytics.data;

import com.percussion.analytics.data.IPSAnalyticsQueryResult.DataType;
import com.percussion.analytics.data.impl.PSAnalyticsQueryResult;
import com.percussion.analytics.error.PSAnalyticsQueryResultException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * @author erikserating
 *
 */
@SuppressWarnings({"deprecation"})
public class PSAnalyticsQueryResultTest extends TestCase
{
   
   @Override
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
         r.put("INVALID_CLASS", new HashMap());
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
   
   public void testGetString() throws Exception
   {
	  String sNull = result.getString("null");
	  assertEquals(sNull,null);
	   
      String s1 = result.getString(KEY_STRING);
      assertEquals(s1, VALUE_STRING);
      
      String s2 = result.getString(KEY_DATE);
      assertEquals(s2, "Wednesday, May 10, 1989 12:00:00 AM EDT");
      
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
      assertEquals(f1, VALUE_FLOAT);
      
      float f2 = result.getFloat(KEY_INT);
      assertEquals(f2, 14F);
      
      float f3 = result.getFloat(KEY_LONG);
      assertEquals(f3, 35F);
      
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
