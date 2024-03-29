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
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.pso.validation.PSODateRangeFieldValidator;
import com.percussion.server.IPSRequestContext;

public class PSODateRangeFieldValidatorTest
{
   private static final Logger log = LogManager.getLogger(PSODateRangeFieldValidatorTest.class);
   
   Mockery context; 
   IPSRequestContext request;
   IPSExtensionDef extDef; 
   
   PSODateRangeFieldValidator cut; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      request = context.mock(IPSRequestContext.class);
      cut = new PSODateRangeFieldValidator(); 
      
      extDef = context.mock(IPSExtensionDef.class); 
      cut.init(extDef, new File("foo"));
      final String[] pnames = {PSODateRangeFieldValidator.CURRENT_FIELD, PSODateRangeFieldValidator.SOURCE_FIELD,
            PSODateRangeFieldValidator.MIN_DAYS, PSODateRangeFieldValidator.MAX_DAYS};
      context.checking(new Expectations(){{
         one(extDef).getRuntimeParameterNames();
         will(returnIterator(pnames)); 
         allowing(request).getParameter(PSODateRangeFieldValidator.CURRENT_FIELD);
         will(returnValue(null));
         allowing(request).getParameter(PSODateRangeFieldValidator.SOURCE_FIELD);
         will(returnValue(null));
         allowing(request).getParameter(PSODateRangeFieldValidator.MIN_DAYS);
         will(returnValue(null));
         allowing(request).getParameter(PSODateRangeFieldValidator.MAX_DAYS);
         will(returnValue(null));
      }});
   }
   @Test
   public final void testProcessUdf()
   {
      log.debug("Testing current date in interval... expecting result=true" ); 
      final Date sourceDate = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
      final String sourceStr = format.format(sourceDate);
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(sourceDate); 
      cal.add(Calendar.DAY_OF_MONTH, 30); 
      final Date testDate = cal.getTime(); 
      final String testStr = format.format(testDate); 
      
      final String[] params = {testStr, "field1", "10", "60" };
      
      try
      {
         context.checking(new Expectations(){{
            one(request).getParameter("field1");
            will(returnValue(sourceStr));
         }});
         
         Boolean result = (Boolean) cut.processUdf(params, request);
         
         assertNotNull(result); 
         log.debug("Result is " + result);
         assertTrue(result.booleanValue());
         
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception Caught"); 
      } 
      
   }
   
   @Test
   public final void testProcessUdfAfter()
   {
      log.debug("testing current date after interval... expect result=false" ); 
      final Date sourceDate = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
      final String sourceStr = format.format(sourceDate);
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(sourceDate); 
      cal.add(Calendar.DAY_OF_MONTH, 90); 
      final Date testDate = cal.getTime(); 
      final String testStr = format.format(testDate); 
      
      final String[] params = {testStr, "field1", "0", "60" };
      
      try
      {
         context.checking(new Expectations(){{
            one(request).getParameter("field1");
            will(returnValue(sourceStr));
         }});
         
         Boolean result = (Boolean) cut.processUdf(params, request);
         
         assertNotNull(result); 
         log.debug("Result is " + result);
         assertFalse(result.booleanValue());
         
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception Caught"); 
      } 
   }
   
   @Test
   public final void testProcessUdfBefore()
   {
      log.debug("testing current date before interval... expect result=false" ); 
      final Date sourceDate = new Date();
      SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); 
      final String sourceStr = format.format(sourceDate);
      
      Calendar cal = Calendar.getInstance();
      cal.setTime(sourceDate); 
      cal.add(Calendar.DAY_OF_MONTH, 30); 
      final Date testDate = cal.getTime(); 
      final String testStr = format.format(testDate); 
      
      final String[] params = {testStr, "field1", "60", "90" };
      
      try
      {
         context.checking(new Expectations(){{
            one(request).getParameter("field1");
            will(returnValue(sourceStr));
         }});
         
         Boolean result = (Boolean) cut.processUdf(params, request);
         
         assertNotNull(result); 
         log.debug("Result is " + result);
         assertFalse(result.booleanValue());
         
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception Caught"); 
      } 
   }
}
