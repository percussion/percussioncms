/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
package com.percussion.utils.jsr170;

import com.percussion.utils.testing.UnitTest;
import org.apache.commons.lang3.time.FastDateFormat;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import javax.jcr.PropertyType;
import javax.jcr.Value;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(UnitTest.class)
public class PSValuesTest
{
   static final FastDateFormat ms_date =
           FastDateFormat.getInstance("yyyy-MM-dd HH:mm:ss");

   public PSValuesTest() {

   }
   @Test
   public void testBoolean() throws Exception
   {
      PSValueFactory fact = new PSValueFactory();

      Value bool = fact.createValue(true);
      assertEquals(PropertyType.BOOLEAN, bool.getType());
      assertEquals("true", bool.getString());
      assertEquals(true, bool.getBoolean());
      try
      {
         bool.getDate();
         assertTrue("Should have thrown exception", false);
      }
      catch (Exception e)
      {
         // Ignore, correct
      }
      try
      {
         bool.getLong();
         assertTrue("Should have thrown exception", false);
      }
      catch (Exception e)
      {
         // Ignore, correct
      }
      try
      {
         bool.getDouble();
         assertTrue("Should have thrown exception", false);
      }
      catch (Exception e)
      {
         // Ignore, correct
      }
      assertNotNull(bool.getStream());
   }

   @Test
   public void testDate() throws Exception
   {
      PSValueFactory fact = new PSValueFactory();
      Calendar cal = new GregorianCalendar();
      cal.setTime(new Date());
      Value date = fact.createValue(cal);
      String dateStr = PSValueConverter.convertToString(cal);
      assertEquals(PropertyType.DATE, date.getType());
      assertEquals(dateStr, date.getString());
      assertEquals(cal, date.getDate());
      assertEquals(cal.getTimeInMillis(), date.getLong());
      assertEquals(cal.getTimeInMillis(), (long) date.getDouble());
      try
      {
         date.getBoolean();
         assertTrue("Should have thrown exception", false);
      }
      catch (Exception e)
      {
         // Ignore, correct
      }
      assertNotNull(date.getStream());
   }

   @Test
   public void testDouble() throws Exception
   {
      PSValueFactory fact = new PSValueFactory();
      double pi = Math.PI;
      Value d = fact.createValue(pi);
      assertEquals(PropertyType.DOUBLE, d.getType());
      assertEquals(pi, d.getDouble(),0);
      assertEquals(3, d.getLong());
      assertEquals(Double.toString(pi), d.getString());
      assertNotNull(d.getStream());
      assertEquals(3, d.getDate().getTimeInMillis());
      try
      {
         d.getBoolean();
         assertTrue("Should have thrown exception", false);
      }
      catch (Exception e)
      {
         // Ignore, correct
      }
   }

   @Test
   public void testBinary() throws Exception
   {
      PSValueFactory fact = new PSValueFactory();
      byte arr[] = new byte[3];
      arr[0] = '1';
      arr[1] = '2';
      arr[2] = '3';
      InputStream stream = new ByteArrayInputStream(arr);
      Value bin = fact.createValue(stream);
      assertEquals("123", bin.getString());
      assertEquals(123, bin.getLong());
      assertEquals(123.0, bin.getDouble(),0);
      assertEquals(false, bin.getBoolean());
      try
      {
         bin.getStream();
         fail();
      }
      catch(IllegalStateException e)
      {
         // OK
      }
      bin = fact.createValue(stream);
      assertNotNull(bin.getStream());
      
      long time = System.currentTimeMillis() / 1000; // Round to the second
      time = time * 1000;
      Calendar cal = PSValueConverter.convertToCalendar(time);
      String date = PSValueConverter.convertToString(cal);
      arr = date.getBytes();
      stream = new ByteArrayInputStream(arr);
      bin = fact.createValue(stream);
      assertEquals(ms_date.parse(date), bin.getDate().getTime());
   }

   @Test
   public void testLong() throws Exception
   {
      PSValueFactory fact = new PSValueFactory();
      long foo = 150201;
      Value d = fact.createValue(foo);
      assertEquals(PropertyType.LONG, d.getType());
      assertEquals((double) foo, d.getDouble(),0);
      assertEquals(foo, d.getLong());
      assertEquals(Long.toString(foo), d.getString());
      assertNotNull(d.getStream());
      assertEquals(foo, d.getDate().getTimeInMillis());
      try
      {
         d.getBoolean();
         assertTrue("Should have thrown exception", false);
      }
      catch (Exception e)
      {
         // Ignore, correct
      }
   }

   @Test
   public void testString() throws Exception
   {
      PSValueFactory fact = new PSValueFactory();
      Value d = fact.createValue("12345");
      assertEquals(PropertyType.STRING, d.getType());
      assertEquals("12345", d.getString());
      assertEquals(12345, d.getLong());
      assertEquals(12345.0, d.getDouble(),0);
      assertNotNull(d.getStream());
      assertEquals(false, d.getBoolean());
      
      long time = System.currentTimeMillis() / 1000; // Round to the second
      time = time * 1000;
      Calendar cal = PSValueConverter.convertToCalendar(time);
      String date = PSValueConverter.convertToString(cal);
      d = fact.createValue(date);
      assertEquals(ms_date.parse(date), d.getDate().getTime());
   }

   @Test
   public void testRuntimeCheck() throws Exception
   {
      Value d = PSValueFactory.createValue((Object) 1.2);
      assertEquals(PropertyType.DOUBLE, d.getType());
      d = PSValueFactory.createValue((Object) 1);
      assertEquals(PropertyType.LONG, d.getType());
      d = PSValueFactory.createValue((Object) 134L);
      assertEquals(PropertyType.LONG, d.getType());
      d = PSValueFactory.createValue((Object) new Date());
      assertEquals(PropertyType.DATE, d.getType());
      d = PSValueFactory.createValue((Object) new GregorianCalendar());
      assertEquals(PropertyType.DATE, d.getType());
      d = PSValueFactory.createValue((Object) "how now");
      assertEquals(PropertyType.STRING, d.getType());
      d = PSValueFactory.createValue((Object) new byte[] { 1, 3, 4 });
      assertEquals(PropertyType.BINARY, d.getType());
      d = PSValueFactory.createValue((Object) false);
      assertEquals(PropertyType.BOOLEAN, d.getType()); 
      
      try
      {
         d = PSValueFactory.createValue(Boolean.class);
         assertTrue("Should have throw exception ", false);
      }
      catch (Exception e)
      {
         // OK, Expected
      }
   }
   

}
