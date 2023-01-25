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
