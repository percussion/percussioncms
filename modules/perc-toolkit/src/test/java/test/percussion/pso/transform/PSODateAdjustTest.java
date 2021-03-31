/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package test.percussion.pso.transform;

import static org.junit.Assert.*;

import java.io.File;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.data.PSConversionException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.pso.transform.PSODateAdjust;
import com.percussion.server.IPSRequestContext;

public class PSODateAdjustTest
{
   Log log = LogFactory.getLog(PSODateAdjustTest.class); 
   
   Mockery context;
   PSODateAdjust cut; 
   IPSRequestContext request; 
   IPSExtensionDef def; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery();
      request = context.mock(IPSRequestContext.class);
      cut = new PSODateAdjust(); 
      def = context.mock(IPSExtensionDef.class);
      cut.init(def, new File("foo")); 
      
      final String[] rnames = new String[0]; 
      final String[] pnames = {"sourceFieldName", "years", "months", "days", "hours", "minutes", "seconds"};
      
      context.checking(new Expectations(){{
         one(def).getRuntimeParameterNames();
         will(returnIterator(pnames));
         one(request).getParameter("sourceFieldName");
         will(returnValue(null));
         one(request).getParameter("years");
         will(returnValue(null));
         one(request).getParameter("months");
         will(returnValue(null));
         one(request).getParameter("days");
         will(returnValue(null));
         one(request).getParameter("hours");
         will(returnValue(null));
         one(request).getParameter("minutes");
         will(returnValue(null));
         one(request).getParameter("seconds");
         will(returnValue(null));
      }});
   }
   
   @Test
   public final void testProcessUdf()
   {
      final Object[] params = new Object[]{"field1", "0", "0", "0", "0", "0", "0"}; 
      final Date dateNow = new Date(); 
      final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
      final String dateString = formatter.format(dateNow); 
      
      context.checking(new Expectations(){{
         one(request).getParameter("field1"); 
         will(returnValue(dateString)); 
       
      }});
      
      try
      {
         Timestamp result = (Timestamp) cut.processUdf(params, request);
         assertNotNull(result); 
         long rl = result.getTime(); 
         long dl = dateNow.getTime();
         long diff = dl - rl; 
         log.debug("Date diff is " + diff); 
         assertTrue(Math.abs(diff) < 1000); 
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
          
   }
   
   @Test
   public final void testProcessUdfNullDate()
   {
      final Object[] params = new Object[]{"field1", "0", "0", "0", "0", "0", "0"}; 
      final Date dateNow = new Date(); 
      
      context.checking(new Expectations(){{
         one(request).getParameter("field1"); 
         will(returnValue(null)); 
       
      }});
      
      try
      {
         Timestamp result = (Timestamp) cut.processUdf(params, request);
         assertNotNull(result); 
         long rl = result.getTime(); 
         long dl = dateNow.getTime();
         long diff = dl - rl; 
         log.debug("Date diff is " + diff); 
         assertTrue(Math.abs(diff) < 1000); 
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
      
      
   }
   
   @Test
   public final void testProcessUdfBlankDate()
   {
      final Object[] params = new Object[]{"", "0", "0", "0", "0", "0", "0"}; 
      final Date dateNow = new Date(); 
      try
      {
         Timestamp result = (Timestamp) cut.processUdf(params, request);
         assertNotNull(result); 
         long rl = result.getTime(); 
         long dl = dateNow.getTime();
         long diff = dl - rl; 
         log.debug("Date diff is " + diff); 
         assertTrue(Math.abs(diff) < 1000); 
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
      
   }
   
   @Test
   public final void testProcessUdf1Year()
   {
      final Object[] params = new Object[]{"", "1", "0", "0", "0", "0", "0"}; 
      final Date dateNow = new Date(); 
      try
      {
         Timestamp result = (Timestamp) cut.processUdf(params, request);
         assertNotNull(result); 
         long rl = result.getTime(); 
         long dl = dateNow.getTime();
         long diff = dl - rl; 
         log.debug("Date diff is " + diff); 
         assertTrue(Math.abs(diff) > 10000); 
         context.assertIsSatisfied(); 
         
      } catch (PSConversionException ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception caught"); 
      } 
      
   }
}
