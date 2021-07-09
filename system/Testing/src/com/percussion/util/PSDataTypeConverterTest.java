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
package com.percussion.util;

import java.text.ParseException;

import junit.framework.TestCase;

/**
 * Unit test class for {@link PSDataTypeConverter}. 
 * 
 * @author paulhoward
 */
public class PSDataTypeConverterTest extends TestCase
{
   /**
    * Test transforming date from 1 string format to another.
    * 
    * @throws Exception
    */
   public void testTransformDateString()
      throws Exception
   {
      assertNull(PSDataTypeConverter.transformDateString(null, null, null,
            false));
      String testDate = "11.1995-30T23";
      String inputFormat = "MM.yyyy-dd'T'HH";
      String result = PSDataTypeConverter.transformDateString(testDate,
            inputFormat, null, false);
      assertTrue(result.equals("1995-11-30 23:00:00.000"));

      testDate = "11.1995-30";
      try
      {
         result = PSDataTypeConverter.transformDateString(testDate,
               inputFormat, null, true);
         fail("Should have thrown ex.");
      }
      catch (ParseException success)
      {
      }

      inputFormat = "MM.yyyy-dd";
      result = PSDataTypeConverter.transformDateString(testDate, inputFormat,
            null, false);
      assertTrue(result.equals("1995-11-30"));

      String outputFormat = "dd-yyyy.MM";
      result = PSDataTypeConverter.transformDateString(testDate, inputFormat,
            outputFormat, false);
      assertTrue(result.equals("30-1995.11"));

      testDate = "2000/1/24 23:21";
      outputFormat = "HH:mm yyyy-MM-dd";
      result = PSDataTypeConverter.transformDateString(testDate, null,
            outputFormat, false);
      assertTrue(result.equals("23:21 2000-01-24"));
      
      testDate = "19980101T131415123";
      outputFormat = "yyyy/MM/dd HH:mm:ss.SSS";
      result = PSDataTypeConverter.transformDateString(testDate, null,
            outputFormat, false);
      assertTrue(result.equals("1998/01/01 13:14:15.123"));
   }
}
