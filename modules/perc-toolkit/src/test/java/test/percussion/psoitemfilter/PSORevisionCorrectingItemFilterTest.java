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
 * test.percussion.psoitemfilter PSORevisionCorrectingItemFilterTest.java
 *  
 * @author DavidBenua
 *
 */
package test.percussion.psoitemfilter;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;


import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.itemfilter.PSORevisionCorrectingItemFilter;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSState;
import com.percussion.utils.guid.IPSGuid;

public class PSORevisionCorrectingItemFilterTest
{
   
   private static final Logger log = LogManager.getLogger(PSORevisionCorrectingItemFilterTest.class);
   
   
   Mockery context; 
   PSORevisionCorrectingItemFilter cut; 
   
   IPSGuidManager gmgr;
   IPSWorkflowService work;
   IPSCmsContentSummaries summ; 
   
   @Before
   public void setUp() throws Exception
   {
      context = new Mockery(){{setImposteriser(ClassImposteriser.INSTANCE);}};
      
      cut = new PSORevisionCorrectingItemFilter();

      gmgr = context.mock(IPSGuidManager.class,"gmgr");
      PSORevisionCorrectingItemFilter.setGmgr(gmgr);
      
      work = context.mock(IPSWorkflowService.class,"work");
      PSORevisionCorrectingItemFilter.setWork(work); 
      
      summ = context.mock(IPSCmsContentSummaries.class,"summ");
      PSORevisionCorrectingItemFilter.setSumm(summ);
   }
   @Test
   public final void testFilterListOfIPSFilterItemMapOfStringString()
   {
      String wfStates = "fee,fi,fo,fum"; 
      Map<String, String> params = new HashMap<String, String>(); 
      params.put(PSORevisionCorrectingItemFilter.WORKFLOW_STATES, wfStates); 
      
      final IPSFilterItem item = context.mock(IPSFilterItem.class,"item");
      final IPSFilterItem item2 = context.mock(IPSFilterItem.class,"item2");
      final IPSGuid originalGuid = new PSLegacyGuid(3,2); 
      final IPSGuid correctedGuid = new PSLegacyGuid(3,1); 
      final PSLocator badLocator = new PSLocator(3,1);
      final PSLocator goodLocator = new PSLocator(3,2);
      
      final IPSGuid workflowAppGuid = new PSLegacyGuid(4,1);
      final IPSGuid workflowStateGuid = new PSLegacyGuid(5,1); 
      
      
      final PSComponentSummary summary = context.mock(PSComponentSummary.class,"summary");
      
      final PSState state = context.mock(PSState.class,"state");
      
      try
      {
         context.checking(new Expectations(){{
            one(summ).loadComponentSummary(3);
            will(returnValue(summary));
            one(summary).getWorkflowAppId(); will(returnValue(4));
            one(summary).getContentStateId(); will(returnValue(5)); 
            one(gmgr).makeGuid(4, PSTypeEnum.WORKFLOW); will(returnValue(workflowAppGuid));  
            one(gmgr).makeGuid(5,PSTypeEnum.WORKFLOW_STATE); will(returnValue(workflowStateGuid));
            one(item).getItemId(); will(returnValue(originalGuid));
            one(gmgr).makeLocator(originalGuid); will(returnValue(badLocator));
            one(item).clone(with(any(IPSGuid.class))); will(returnValue(item2)); 
            one(work).loadWorkflowState(workflowStateGuid, workflowAppGuid); 
            will(returnValue(state)); 
            one(state).getName(); will(returnValue("fee")); 
            one(summary).getCurrentLocator(); will(returnValue(badLocator)); 
            one(gmgr).makeGuid(badLocator); will(returnValue(correctedGuid)); 
            one(item2).getItemId(); will(returnValue(correctedGuid)); 
         }});
         
         List<IPSFilterItem> res = cut.filter(Collections.<IPSFilterItem>singletonList(item), params);
         assertNotNull(res);
         assertEquals(1,res.size());
         assertEquals(correctedGuid, res.get(0).getItemId()); 
         
         context.assertIsSatisfied(); 
         
      } catch (PSFilterException ex)
      {        
         log.error("Unexpected Exception " + ex,ex);
         fail("Exception"); 
      }
      
   }
}
