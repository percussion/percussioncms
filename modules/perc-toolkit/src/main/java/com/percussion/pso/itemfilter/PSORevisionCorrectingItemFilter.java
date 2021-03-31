/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
package com.percussion.pso.itemfilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.pso.utils.SimplifyParameters;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.filter.impl.PSBaseFilter;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.legacy.IPSCmsContentSummaries;
import com.percussion.services.legacy.PSCmsContentSummariesLocator;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSState;

import com.percussion.utils.guid.IPSGuid;

/**
 * Corrects revisions of items in Staging states where the current revision 
 * is desired. 
 * 
 * This filter determines if an item is in one of the named states set within
 * its parameters. If the item is in one of the states, the <code>Current</code> revision will
 * be used in place of the <code>Public</code> revision. 
 *
 * @author DavidBenua
 *
 */
public class PSORevisionCorrectingItemFilter extends PSBaseFilter
      implements
         IPSItemFilterRule
{
   /**
    * Logger for this class
    */
   private static final Log log = LogFactory.getLog(PSORevisionCorrectingItemFilter.class);

   /* System services */ 
   private static IPSWorkflowService work = null; 
   private static IPSGuidManager gmgr = null;
   private static IPSCmsContentSummaries summ = null; 
   /**
    * Default constructor
    */
   public PSORevisionCorrectingItemFilter()
   {
      super();
   }
  
   /**
    * @see com.percussion.services.filter.IPSItemFilterRule#filter(java.util.List, java.util.Map)
    */
   public List<IPSFilterItem> filter(List<IPSFilterItem> items,
         Map<String, String> params) throws PSFilterException
   {
      log.debug("Filtering items for revision" );
      List<String> wfStates = SimplifyParameters.
          getValueAsList(params.get(WORKFLOW_STATES));
      List<IPSFilterItem> results = new ArrayList<IPSFilterItem>(items.size()); 
      
      for(IPSFilterItem item : items)
      {
         IPSGuid itemguid = item.getItemId(); 
         log.debug("Original Guid is " + itemguid); 
         IPSGuid revguid = correctGuid(itemguid,wfStates);
         log.debug("Corrected Guid is " + revguid); 
         if(!itemguid.equals(revguid)) 
         {
            log.debug("replacing GUID " + itemguid + " with GUID " + revguid);
            IPSFilterItem revItem = item.clone(revguid);
            results.add(revItem);  
         }
         else
         {
            results.add(item);
         }
      }
      return results; 
   }
   
   /**
    * Corrects the revision of an individual GUID. 
    * @param in the GUID to check. 
    * @param wfStates a list of Workflow State names
    * @return a GUID with the corrected revision.
    */
   private static IPSGuid correctGuid(IPSGuid in, List<String> wfStates)
   {
      initServices();
      PSComponentSummary sum = summ.loadComponentSummary(gmgr.makeLocator(in).getId());
      IPSGuid workflowid = gmgr.makeGuid(sum.getWorkflowAppId(), PSTypeEnum.WORKFLOW);
      IPSGuid stateid = gmgr.makeGuid(sum.getContentStateId(), PSTypeEnum.WORKFLOW_STATE);      
      PSState state = work.loadWorkflowState(stateid, workflowid);
      
      if(wfStates.contains(state.getName()))
      {
         return gmgr.makeGuid(sum.getCurrentLocator());
      }
      PSLocator loc = new PSLocator(sum.getContentId(), sum.getPublicOrCurrentRevision());
      return gmgr.makeGuid(loc); 
  }
   
   /**
    * Initializes system services.  
    * Used to prevent references to these services during extension 
    * registration.
    */
   private static void initServices()
   {
      if(gmgr == null)
      {
         gmgr = PSGuidManagerLocator.getGuidMgr();
         summ = PSCmsContentSummariesLocator.getObjectManager();
         work = PSWorkflowServiceLocator.getWorkflowService(); 
      }
   }
   
   /**
    * The Workflow States Parameter name
    */
   public static final String WORKFLOW_STATES = "workflow_states";
   /**
    * @param gmgr The gmgr to set.
    * Used only in unit tests.
    */
   public static void setGmgr(IPSGuidManager gmgr)
   {
      PSORevisionCorrectingItemFilter.gmgr = gmgr;
   }

   /**
    * @param summ The summ to set.
    */
   public static void setSumm(IPSCmsContentSummaries summ)
   {
      PSORevisionCorrectingItemFilter.summ = summ;
   }

   /**
    * @param work The work to set.
    */
   public static void setWork(IPSWorkflowService work)
   {
      PSORevisionCorrectingItemFilter.work = work;
   }

  
}
