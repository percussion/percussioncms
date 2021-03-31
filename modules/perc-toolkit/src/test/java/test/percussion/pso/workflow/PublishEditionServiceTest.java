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
 * test.percussion.pso.workflow PublishEditionServiceTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.workflow;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

import com.percussion.pso.workflow.PublishEditionService;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.services.guidmgr.IPSGuidManager;


public class PublishEditionServiceTest
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory
         .getLog(PublishEditionServiceTest.class);

   Mockery context; 
   IPSGuidManager gmgr;
   IPSRxPublisherService rps; 
   
   TestablePublishEditionService svc = null; 

   @Before
   public void setUp() throws Exception
   {
    
      context = new Mockery(); 
      gmgr = context.mock(IPSGuidManager.class);
      rps = context.mock(IPSRxPublisherService.class);
      
      svc = new TestablePublishEditionService(); 
      svc.setGmgr(gmgr);
      svc.setRps(rps); 
      
   }
   
   @Test
   public final void testFindEdition()
   {
      /*
       * Map of workflows
       *    Map of transitions
       *       Map of communities
       *          Value is edition
       */
      final Map<String,Map<String,Map<String,String>>> workflows 
         = new HashMap<String,Map<String,Map<String,String>>>(){{
            put("5", new HashMap<String, Map<String,String>>(){{
                put("301", new HashMap<String,String>(){{
                   put("1001","314");
                   put("1002","315"); 
                }});   
            }});             
           }};
      
      svc.setWorkflows(workflows);
      
     
      assertEquals(314, svc.findEdition(5, 301, 1001));
      assertEquals(315, svc.findEdition(5, 301, 1002));
      
      try
      {
         log.info("Expect not to find workflow 6"); 
         svc.findEdition(6, 301, 1001);
         fail("expected exception, invalid workflow id"); 
      }
      catch (IllegalArgumentException iae) 
      {
         //iae.printStackTrace(); 
         //this is expected
      }
      
      try
      {
         log.info("Expect not to find transition 304");
         svc.findEdition(5, 304, 1001);
         fail("expected exception, invalid transition id");
      }
      catch (IllegalArgumentException iae)
      {
         //this is expected         
      }
   }
   
   private class TestablePublishEditionService extends PublishEditionService
   {

      @Override
      public void setGmgr(IPSGuidManager gmgr)
      {
         super.setGmgr(gmgr);
      }

      @Override
      public void setRps(IPSRxPublisherService rps)
      {
         super.setRps(rps);
      }
      
   }
}
