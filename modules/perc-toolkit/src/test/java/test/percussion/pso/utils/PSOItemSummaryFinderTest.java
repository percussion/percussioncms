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
/*
 * test.percussion.pso.utils PSOItemSummaryFinderTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.utils;

import static org.junit.Assert.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   private static final Logger log = LogManager.getLogger(PSOItemSummaryFinderTest.class);
   
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
