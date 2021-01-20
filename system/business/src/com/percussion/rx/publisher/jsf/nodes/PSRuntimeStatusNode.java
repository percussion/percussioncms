/*
 *     Percussion CMS
 *     Copyright (C) 1999-2020 Percussion Software, Inc.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     Mailing Address:
 *
 *      Percussion Software, Inc.
 *      PO Box 767
 *      Burlington, MA 01803, USA
 *      +01-781-438-9900
 *      support@percussion.com
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.rx.publisher.jsf.nodes;

import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.rx.publisher.jsf.beans.PSRuntimeNavigation;
import com.percussion.rx.publisher.jsf.data.PSStatusLogEntry;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.publisher.IPSPubStatus.EndingState;
import com.percussion.services.publisher.data.PSEdition;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the runtime status node. It displays the status of all active
 * jobs. The display (in JSF) uses AJAX to query status of all active jobs.
 */
public class PSRuntimeStatusNode extends PSNodeBase
{
   /**
    * The outcome of this node.
    */
   public static final String STATUS_VIEW = "pub-runtime-status-view";
   
   /**
    * The runtime navigation node, It is also the parent of this node.
    * Never <code>null</code> after the constructor. 
    */
   private PSRuntimeNavigation m_runtimeNav;
   
   /**
    * Constructs an instance of the node.
    * @param title the title of the node, may not be <code>null</code> or empty.
    * @param runtimeNav the runtime navigation node, which is also the parent
    * node. Never <code>null</code>.
    */
   public PSRuntimeStatusNode(String title, PSRuntimeNavigation runtimeNav) {
      super(title, STATUS_VIEW);
      
      assert runtimeNav != null;
      m_runtimeNav = runtimeNav;
   }

   /**
    * This class is used to populate the table in status.jsp page. It is also
    * provide actions/methods to view log of the job or stop the job.
    */
   public class StatusEntry
   {
      private Long m_jobId;
      PSStatusLogEntry m_logEntry;
      
      public StatusEntry(Long id)
      {
         m_jobId = id;
         m_logEntry = new PSStatusLogEntry(id, m_runtimeNav);
      }
      
      /**
       * Action to view the log of the publishing job.
       * 
       * @return the outcome, never <code>null</code> or empty.
       */
      public String viewLog()
      {
         return m_logEntry.perform();
      }

      /**
       * Action to stop/cancel the current job.
       */
      public void stopJob()
      {
         IPSRxPublisherService rxpub = PSRxPublisherServiceLocator
               .getRxPublisherService();      

         rxpub.cancelPublishingJob(m_jobId);
      }      
   }
   
   /**
    * Determines whether a job is terminated from its state.
    *  
    * @param state the state of the job, assumed not <code>null</code>.  
    * 
    * @return <code>true</code> if it is terminated; otherwise return 
    *    <code>false</code>.
    */
   private Boolean isTerminated(IPSPublisherJobStatus.State state)
   {
      return state.isTerminal();
   }
   
   /**
    * Creates an entry for a given job.
    * 
    * @param jobId the job id, assumed not <code>null</code>.
    * @param edition the edition of the job, assumed not <code>null</code>
    *    or empty.
    * @param status the status of the job, assumed not <code>null</code>.
    * 
    * @return the created entry, never <code>null</code>.
    */
   @SuppressWarnings("unchecked")
   private Map<String, Object> createJobStatus(Long jobId, IPSEdition edition,
         IPSPublisherJobStatus status)
   {
      Map props = new HashMap<String, Object>();
      
      props.put("editionNameWithId", PSDesignNode.getNameWithId(edition
            .getName(), ((PSEdition) edition).getId()));
      props.put("editionBehavior", edition.getEditionType()
            .getDisplayTitle());
      props.put("status", status.getState().getDisplayName());
      props.put("statusEntry", new StatusEntry(jobId));
      props.put("isTerminal", isTerminated(status.getState()));
      props.put("progress", ""
            + PSPublishingStatusHelper.getJobCompletionPercent(status));
      
      EndingState endState = PSPublishingStatusHelper.getEndingState(status
            .getState());
      String[] imgSrc = PSPublishingStatusHelper.getStatusImage(endState,
            true, true);
      props.put("statusImage", imgSrc[0]);

      return props;      
   }
   
   /**
    * Gets the active jobs for the current node. The current node can be
    * the node that associate with this backing bean, the "Sites" node or
    * a node that associate with a specific site.
    * 
    * @return entries of all active jobs, never <code>null</code>, maybe empty.
    */
   public List<Map<String, Object>> getActiveJobStatus()
   {
      IPSRxPublisherServiceInternal rxpub = PSRxPubServiceInternalLocator
            .getRxPublisherService();
      IPSPublisherService pubsvc = PSPublisherServiceLocator
            .getPublisherService();
      
      IPSGuid siteId = null;
      if (m_runtimeNav.getCurrentNode() instanceof PSRuntimeSiteNode)
      {
         siteId = ((PSRuntimeSiteNode)m_runtimeNav.getCurrentNode()).getSiteID();
      }
      
      List<Map<String, Object>> reval = new ArrayList<Map<String, Object>>();
      for (Long jobId : rxpub.getActiveJobIds())
      {
         IPSPublisherJobStatus stat = rxpub.getPublishingJobStatus(jobId);
         IPSEdition ed = null;
         try
         {
            ed = pubsvc.loadEdition(stat.getEditionId());
            // if for a specific site, then skip editions for other sites
            if (siteId != null && (!ed.getSiteId().equals(siteId)))
               continue;
         }
         catch (PSNotFoundException e)
         {
            continue; // don't show if cannot find the Edition
         }

         Map<String, Object> entry = createJobStatus(jobId, ed, stat);
         reval.add(entry);
      }
      
      return reval;
   }
   
   @Override
   public String getHelpTopic()
   {
      return "ActiveJobStatus";
   }

}
