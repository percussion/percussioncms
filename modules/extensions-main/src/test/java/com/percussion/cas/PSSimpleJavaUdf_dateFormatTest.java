/*
 *     Percussion CMS
 *     Copyright (C) Percussion Software, Inc.  1999-2020
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *      Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.percussion.cas;

import com.percussion.design.objectstore.PSTextLiteral;
import com.percussion.extensions.general.PSSimpleJavaUdfBaseTest;
import com.percussion.extensions.general.PSSimpleJavaUdf_dateFormat;
import com.percussion.extensions.general.PSSimpleJavaUdf_dateFormatEx;
import com.percussion.utils.testing.UnitTest;
import junit.framework.AssertionFailedError;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Tests both versions: {@link PSSimpleJavaUdf_dateFormat} and
 * {@link PSSimpleJavaUdf_dateFormatEx}.
 * 
 * @author DougRand
 */
@Category(UnitTest.class)
public class PSSimpleJavaUdf_dateFormatTest extends PSSimpleJavaUdfBaseTest
{
   @Test
   public void testDateFormat() throws Exception
   {
      PSSimpleJavaUdf_dateFormat dateFormat = new PSSimpleJavaUdf_dateFormat();
      PSTextLiteral truevalue = new PSTextLiteral("true");
      PSTextLiteral truevalue2 = new PSTextLiteral("True");
      PSTextLiteral falsevalue = new PSTextLiteral("false");
      PSTextLiteral falsevalue2 = new PSTextLiteral("False");
      
      // Arguments are format, date, [ returnNullForEmpty]
      String date = (String) callUDF(dateFormat, null, null);
      
      if (date == null || date.trim().length() == 0)
      {
         throw new AssertionFailedError("Date should be non-null");
      }
      
      String date2 = (String) callUDF(dateFormat, null, "MM/dd/yyyy");
      if (date2 == null || date2.trim().length() == 0)
      {
         throw new AssertionFailedError("Date should be non-null");
      }      
      if (date2.charAt(2) != '/' || 
         date2.charAt(5) != '/')
      {
         throw new AssertionFailedError("Date must follow output format");
      }         
      
      String date3 = (String) callUDF(dateFormat, null, null, null, truevalue);
      if (date3 != null)
      {
         throw new AssertionFailedError("Date must be null for input date null and returnNull true");
      }
      
      String date4 = (String) callUDF(dateFormat, null, null, null, truevalue2);
      if (date4 != null)
      {
         throw new AssertionFailedError("Date must be null for input date null and returnNull true");
      } 
      
      String date5 = (String) callUDF(dateFormat, null, null, null, falsevalue);
      if (date5 == null)
      {
         throw new AssertionFailedError("Date must be non-null for input date null and returnNull false");
      }  
      
      String date6 = (String) callUDF(dateFormat, null, null, null, falsevalue2);
      if (date6 == null)
      {
         throw new AssertionFailedError("Date must be non-null for input date null and returnNull false");
      }                  
   }

   @Test
   public void testDateFormatEx() throws Exception
   {
      PSSimpleJavaUdf_dateFormatEx dateFormatEx = new PSSimpleJavaUdf_dateFormatEx();
      PSTextLiteral truevalue = new PSTextLiteral("true");
      PSTextLiteral truevalue2 = new PSTextLiteral("True");
      PSTextLiteral falsevalue = new PSTextLiteral("false");
      PSTextLiteral falsevalue2 = new PSTextLiteral("False");
            
      // Arguments are format, date, [ returnNullForEmpty]
      String date = (String) callUDF(dateFormatEx, null, null);
      
      if (date == null || date.trim().length() == 0)
      {
         throw new AssertionFailedError("Date should be non-null");
      }
      
      String date2 = (String) callUDF(dateFormatEx, null, "MM/dd/yyyy");
      if (date2 == null || date2.trim().length() == 0)
      {
         throw new AssertionFailedError("Date should be non-null");
      }      
      if (date2.charAt(2) != '/' || 
         date2.charAt(5) != '/')
      {
         throw new AssertionFailedError("Date must follow output format");
      }         
      
      String date3 = (String) callUDF(dateFormatEx, null, null, null, null, truevalue);
      if (date3 != null)
      {
         throw new AssertionFailedError("Date must be null for input date null and returnNull true");
      }
      
      String date4 = (String) callUDF(dateFormatEx, null, null, null, null, truevalue2);
      if (date4 != null)
      {
         throw new AssertionFailedError("Date must be null for input date null and returnNull true");
      } 
      
      String date5 = (String) callUDF(dateFormatEx, null, null, null, null, falsevalue);
      if (date5 == null)
      {
         throw new AssertionFailedError("Date must be non-null for input date null and returnNull false");
      }  
      
      String date6 = (String) callUDF(dateFormatEx, null, null, null, null, falsevalue2);
      if (date6 == null)
      {
         throw new AssertionFailedError("Date must be non-null for input date null and returnNull false");
      }                  
   }   

}
