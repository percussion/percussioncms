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
   private JobStatus createJobStatus(Long jobId, IPSEdition edition,
         IPSPublisherJobStatus status)
   {
      JobStatus jobStatus = new JobStatus();

      jobStatus.setEditionNameWithId(PSDesignNode.getNameWithId(edition
            .getName(), ((PSEdition) edition).getId()));
      jobStatus.setEditionBehavior(edition.getEditionType()
            .getDisplayTitle());
      jobStatus.setStatus(status.getState().getDisplayName());
      jobStatus.setStatusEntry(new StatusEntry(jobId));
      jobStatus.setTerminated(isTerminated(status.getState()));
      jobStatus.setProgress(PSPublishingStatusHelper.getJobCompletionPercent(status));
      
      EndingState endState = PSPublishingStatusHelper.getEndingState(status
            .getState());
      String[] imgSrc = PSPublishingStatusHelper.getStatusImage(endState,
            true, true);
      jobStatus.setStatusImage(imgSrc[0]);

      return jobStatus;
   }

   public static class JobStatus{

      public JobStatus(){

      }
      private String editionNameWithId;

      public String getEditionNameWithId() {
         return editionNameWithId;
      }

      public String getEditionBehavior() {
         return editionBehavior;
      }

      public String getStatus() {
         return status;
      }

      public Integer getProgress() {
         return progress;
      }

      public StatusEntry getStatusEntry() {
         return statusEntry;
      }

      public Boolean getTerminated() {
         return isTerminated;
      }

      public Boolean getIsTerminal(){
         return this.getTerminated();
      }

      public Boolean getIsTerminated(){
         return isTerminated;
      }

      public String getStatusImage() {
         return statusImage;
      }

      private String editionBehavior;

      public void setEditionNameWithId(String editionNameWithId) {
         this.editionNameWithId = editionNameWithId;
      }

      public void setEditionBehavior(String editionBehavior) {
         this.editionBehavior = editionBehavior;
      }

      public void setStatus(String status) {
         this.status = status;
      }

      public void setProgress(Integer progress) {
         this.progress = progress;
      }

      public void setStatusEntry(StatusEntry statusEntry) {
         this.statusEntry = statusEntry;
      }

      public void setTerminated(Boolean terminated) {
         isTerminated = terminated;
      }

      public void setStatusImage(String statusImage) {
         this.statusImage = statusImage;
      }

      private String status;
      private Integer progress;
      private StatusEntry statusEntry;
      private Boolean isTerminated;
      private String statusImage;



   }

   /**
    * Gets the active jobs for the current node. The current node can be
    * the node that associate with this backing bean, the "Sites" node or
    * a node that associate with a specific site.
    * 
    * @return entries of all active jobs, never <code>null</code>, maybe empty.
    */
   public List<JobStatus> getActiveJobStatus()
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
      
      List<JobStatus> reval = new ArrayList<>();
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

         JobStatus entry = createJobStatus(jobId, ed, stat);
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
