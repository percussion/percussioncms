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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
   
   private static Log log = LogFactory.getLog(PSORevisionCorrectingItemFilterTest.class); 
   
   
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
