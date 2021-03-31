/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.validation;

import static org.junit.Assert.*;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   Log log = LogFactory.getLog(PSODateRangeFieldValidatorTest.class);
   
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
