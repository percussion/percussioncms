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
 * test.percussion.pso.workflow PublishEditionServiceTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.pso.workflow;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
   private static final Logger log = LogManager.getLogger(PublishEditionServiceTest.class);

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
         //this is expected
         log.error(iae.getMessage());
         log.debug(iae.getMessage(), iae);
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
