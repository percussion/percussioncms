/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
/*
 * test.percussion.pso.utils PSOItemSummaryFinderTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.pso.utils.PSOItemSummaryFinder;
import com.percussion.services.legacy.IPSCmsContentSummaries;

public class PSOItemSummaryFinderTest
{
   private static Log log = LogFactory.getLog(PSOItemSummaryFinderTest.class);
   
   private Mockery context = new Mockery() {{
      setImposteriser(ClassImposteriser.INSTANCE);
    }};

   PSComponentSummary summ; 
   IPSCmsContentSummaries sumsvc;
   PSOItemSummaryFinder cut; 
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery() {{
         setImposteriser(ClassImposteriser.INSTANCE);
      }}; 
      summ = context.mock(PSComponentSummary.class); 
      sumsvc = context.mock(IPSCmsContentSummaries.class);
      PSOItemSummaryFinder.setSumsvc(sumsvc); 
   }
   
   @Test
   public final void testGetSummaryInt()
   {
      log.debug("Starting summary test"); 
      
      context.checking(new Expectations(){{
         one(sumsvc).loadComponentSummary(1);
         will(returnValue(summ)); 
         one(summ).getCheckoutUserName();
         will(returnValue("fred")); 
      }});
      
      
      try
      {
         PSComponentSummary s1 = PSOItemSummaryFinder.getSummary(1);
         
         assertNotNull(s1); 
         assertEquals("fred", s1.getCheckoutUserName()); 
         context.assertIsSatisfied();
         log.debug("Finished summary test"); 
         
      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
      
   }
   
   @Test
   public final void testGetCheckoutStatus()
   {
      log.debug("Starting checkout status test"); 
         
      context.checking(new Expectations(){{
         atLeast(1).of(sumsvc).loadComponentSummary(1);
         will(returnValue(summ)); 
         atLeast(1).of(summ).getCheckoutUserName();
         will(returnValue("fred")); 
      }});
      
      try
      {
         int status = PSOItemSummaryFinder.getCheckoutStatus("1", "fred");
         assertEquals(PSOItemSummaryFinder.CHECKOUT_BY_ME, status);
         
         status = PSOItemSummaryFinder.getCheckoutStatus("1", "bob");
         assertEquals(PSOItemSummaryFinder.CHECKOUT_BY_OTHER, status); 
         
         context.assertIsSatisfied();
         
         
        
         log.debug("finished checkout status test");
      } catch (Exception ex)
      {
        log.error("Unexpected Exception " + ex,ex);
        fail("Exception");
      }
      
   }
   
   @Test
   public final void testCheckoutStatusNone()
   {
      log.debug("Starting checkout status none "); 
      context.checking(new Expectations(){{
         atLeast(1).of(sumsvc).loadComponentSummary(1);
         will(returnValue(summ)); 
         atLeast(1).of(summ).getCheckoutUserName();
         will(returnValue(null)); 
      }});
      
      try
      {
         int status = PSOItemSummaryFinder.getCheckoutStatus("1", "bob");
         assertEquals(PSOItemSummaryFinder.CHECKOUT_NONE, status);
         context.assertIsSatisfied();

      } catch (Exception ex)
      {
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception");
      }
      
      
   }
}
