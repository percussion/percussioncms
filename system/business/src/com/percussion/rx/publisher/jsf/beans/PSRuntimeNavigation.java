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
package com.percussion.rx.publisher.jsf.beans;

import com.percussion.rx.jsf.PSCategoryNodeBase;
import com.percussion.rx.jsf.PSNavigation;
import com.percussion.rx.jsf.PSNodeBase;
import com.percussion.rx.jsf.PSTreeModel;
import com.percussion.rx.publisher.jsf.data.PSPubItemEntry;
import com.percussion.rx.publisher.jsf.nodes.PSPublishingStatusHelper;
import com.percussion.rx.publisher.jsf.nodes.PSRuntimeEditionNode;
import com.percussion.rx.publisher.jsf.nodes.PSRuntimeLogsNode;
import com.percussion.rx.publisher.jsf.nodes.PSRuntimeSiteNode;
import com.percussion.rx.publisher.jsf.nodes.PSRuntimeStatusNode;
import com.percussion.rx.publisher.jsf.nodes.PSSiteContainerNode;
import com.percussion.rx.ui.jsf.beans.PSHelpTopicMapping;
import com.percussion.server.PSRequest;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSEditionTaskDef;
import com.percussion.services.publisher.IPSEditionTaskLog;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.request.PSRequestInfo;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.myfaces.trinidad.event.RangeChangeEvent;

/**
 * The runtime execution tree for publishing.
 * 
 * @author dougrand
 */
public class PSRuntimeNavigation extends PSNavigation
{
   /**
    * The pub log, created on first access.
    */
   private JobPubLog m_jobPubLog = null;
   
   /**
    * The job id, set by nodes.
    */
   private long m_jobid;

   /**
    * The item from the pub log, set by the entry.
    */
   private PSPubItemEntry m_detailItem;

   /**
    * The parent node for all sites. Initialized by constructor, never 
    * <code>null</code> after that.
    */
   private PSSiteContainerNode m_sites;
   
   /**
    * The container class for an Edition and its Site name.
    */
   public static class EditionSiteName
   {
      /**
       * Constructs an object with the given names.
       * @param siteName site name, assumed not <code>null</code> or empty.
       * @param editionName edition name, assumed not <code>null</code> or empty.
       */
      private EditionSiteName(String siteName, String editionName)
      {
         mi_siteName = siteName;
         mi_editionName = editionName;
      }
      
      /**
       * Get the name of the Edition.
       * @return Edition name, never <code>null</code>.
       */
      public String getEditionName()
      {
         return mi_editionName;
      }
      
      /**
       * Gets the name of the site.
       * @return the site name, never <code>null</code> or empty.
       */
      public String getSiteName()
      {
         return mi_siteName;
      }
      
      String mi_editionName;
      String mi_siteName;
   }
   
   /**
    * It manages the log entries for a published Edition (or a published job).
    */
   public class JobPubLog
   {
      /**
       * The runtime navigation object, initialized by constructor, never
       * <code>null</code> after that.
       */
      PSRuntimeNavigation mi_nav;
      
      /**
       * The pub log, created on first access, set by {@link #getLog()},
       * never <code>null</code> after that.
       */
      private PSPubLogBean mi_pubLog = null;
      

      /**
       * Create an instance.
       * @param parent the navigation instance, never <code>null</code>.
       */
      public JobPubLog(PSRuntimeNavigation parent)
      {
         if (parent == null)
            throw new IllegalArgumentException("parent may not be null.");
         
         mi_nav = parent;
      }
      
      /**
       * Get the number of rows per page for the paginated table.
       * @return the rows per page.
       */
      public int getPageRows()
      {
         return getLog().getPageRows();
      }
      
      /**
       * Sets the new value on the component, then skips to the render phase.
       * @param ev Supplied by framework, assumed never <code>null</code>.
       */
      public void rowRangeChanged(RangeChangeEvent ev)
      {
         getLog().setCurrRange(ev.getNewStart(), ev.getNewEnd());
      }
      
      /**
       * Get the pub log
       * @return the pub log, whose contents are affected by the current setting
       * of the job id, never <code>null</code>.
       */
      public PSPubLogBean getLog()
      {
         if (mi_pubLog == null)
         {
            mi_pubLog = new PSPubLogBean(mi_nav);
            mi_pubLog.setJobId(getJobId());
         }
         return mi_pubLog;
      }
      
      /**
       * Get task status for the given job.
       * @return a list of task status data, never <code>null</code>.
       */
      public List<Map<String, Object>> getTasks()
      {
         List<Map<String, Object>> rval = new ArrayList<Map<String,Object>>();
         IPSPublisherService pubsvc = PSPublisherServiceLocator.
            getPublisherService();
         List<IPSEditionTaskLog> entries =
            pubsvc.findEditionTaskLogEntriesByJobId(getJobId());
         for(IPSEditionTaskLog entry : entries)
         {
            Map<String,Object> rec = new HashMap<String, Object>();
            rec.put("statusid", entry.getJobId());
            double elapsed = entry.getElapsed() / 1000.0;
            rec.put("elapsed", elapsed + "s");
            rec.put("referenceid", entry.getReferenceId());
            
            IPSEditionTaskDef task = pubsvc
                  .findEditionTaskById(entry.getTaskId());
            if (task != null)
               rec.put("taskname", task.getExtensionName());
            else
               rec.put("taskname", entry.getTaskId());
            
            rec.put("message", entry.getMessage());
            rec.put("status", entry.getStatus() ? "Success" : "Failure");
            rval.add(rec);
         }
         return rval;
      }
   }
   
   /**
    * Get the instance of Job Publishing Log
    * @return the log instance.
    */
   public JobPubLog getJobPubLog()
   {
      if (m_jobPubLog == null)
         m_jobPubLog = new JobPubLog(this);
      return m_jobPubLog;
   }
   
   /**
    * Creates an instance of the runtime navigation node.
    */
   public PSRuntimeNavigation()
   {
      PSCategoryNodeBase root = new PSCategoryNodeBase("root", null);
      m_tree = new PSTreeModel(root, this);
      
      m_sites = new PSSiteContainerNode("Sites", false);
      PSNodeBase status = new PSRuntimeStatusNode("Publishing Status", this);
      PSNodeBase logs = new PSRuntimeLogsNode();
      root.addNode(m_sites);
      root.addNode(status);
      root.addNode(logs);

      setStartingNode(status);
      Object saved = m_tree.getRowKey();
      for(int i = 0; i < m_tree.getRowCount(); i++)
      {
         m_tree.setRowIndex(i);
         getDisclosedRows().add(m_tree.getRowKey());   
      }
      m_tree.setRowKey(saved);
   }

   @Override
   protected void focusOnStartingNode()
   {
      super.focusOnStartingNode();
      m_sites.resetChildren();
   }
   
   /**
    * Action fired when we're finished looking at the logs. This action decides
    * where to go based on the current node.
    * @return the appropriate action
    */
   public String pubLogDone()
   {
      PSNodeBase current = getCurrentNode();
      if (current == null)
      {
         // go to the starting node if current node is not defined
         // this happens when goto publishing log page directly
         focusOnStartingNode();
         current = getCurrentNode();
         return (current != null) ? current.getOutcome() : null;
      }
      else if (current instanceof PSRuntimeEditionNode)
      {
         return "edition";
      }
      else if (current instanceof PSRuntimeStatusNode)
      {
         return "status";
      }
      else
      {
         return "return";
      }
   }
   
   /**
    * Set the job id for use in the status views.
    * @param jobid the job id
    */
   public void setJobId(long jobid)
   {
      m_jobid = jobid;
      m_jobPubLog = null; // clean/flush previous log entries if there is any.
   }
   
   /**
    * @return the current edition data based on the job id, never 
    * <code>null</code> but may be empty.
    */
   public Map<String,Object> getJobEditionData()
   {
      DateFormat fmt = DateFormat.getDateTimeInstance();
      IPSPublisherService pubsvc = PSPublisherServiceLocator.
         getPublisherService();
      Map<String,Object> rval = new HashMap<String, Object>();
      IPSGuid edid = pubsvc.findEditionIdForJob(getJobId());
      if (edid != null)
      {
         IPSEdition edition = pubsvc.loadEdition(edid);
         rval.put("name", edition.getDisplayTitle());
      }
      IPSPubStatus stat = pubsvc.findPubStatusForJob(getJobId());
      if (stat != null)
      {
         rval.put("start", fmt.format(stat.getStartDate()));
         String elapsed = PSPublishingStatusHelper.getElapseTime(
               stat.getStartDate(), stat.getEndDate());
         rval.put("elapsed", elapsed);
      }
      return rval;
   }
   
   /**
    * Get the job ID. It returned the job ID specified in the request parameter
    * if exist; otherwise it is the job ID saved in this object. 
    * 
    * @return the job id described above, never <code>null</code>.
    */
   public Long getJobId()
   {
      long jobId = getJobIdFromRequest();
      if (jobId != -1L)
         return jobId;

      return m_jobid;
   }

   /**
    * Get the publishing job ID from request parameter if specified.
    * @return the publishing job ID. It is <code>-1L</code> if the job
    *    ID is not specified in the request parameter.
    */
   @SuppressWarnings("cast")
   private long getJobIdFromRequest()
   {
      PSRequest req = (PSRequest) PSRequestInfo
            .getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
      HttpServletRequest request = (HttpServletRequest) req
            .getServletRequest();
      String jobIdString = request
            .getParameter(IPSHtmlParameters.PUBLISH_JOB_ID);
      
      if (!StringUtils.isBlank(jobIdString))
      {         
         try
         {
            long jobId = Long.parseLong(jobIdString);
            return jobId > 0L ? jobId : -1L;
         }
         catch (Exception e)
         {
         }
      }
      return -1L;
   }
   
   /**
    * Set the item to view in the single item page
    * @param pubItemEntry the new item, never <code>null</code>.
    */
   public void setDetailItem(PSPubItemEntry pubItemEntry)
   {
      if (pubItemEntry == null)
      {
         throw new IllegalArgumentException("pubItemEntry may not be null");
      }
      m_detailItem = pubItemEntry;
   }
  
   /**
    * @return get the detail item, never <code>null</code> but will throw
    * an exception if there's no item set.
    */
   public PSPubItemEntry getDetailItem()
   {
      if (m_detailItem == null)
      {
         throw new IllegalStateException("No detail item set yet");
      }
      return m_detailItem;
   }   

   /**
    * Gets the mapping for all Edition ID and its site/edition name.
    * @return the mapping, maps Edition ID to names, never <code>null</code>,
    *    but may be empty if there is no Edition defined.
    */
   public Map<Long, EditionSiteName> getEditionIdNameMap()
   {
      Map<Long, EditionSiteName> map = new HashMap<Long, EditionSiteName>();
      for (PSNodeBase node : m_sites.getChildren())
      {
         PSRuntimeSiteNode snode = (PSRuntimeSiteNode) node;
         PSCategoryNodeBase editions = (PSCategoryNodeBase) snode
               .getChildren().get(0);
         if (editions.getChildren() == null)
            continue;
         
         for (PSNodeBase enode : editions.getChildren())
         {
            PSRuntimeEditionNode renode = (PSRuntimeEditionNode) enode;
            EditionSiteName names = new EditionSiteName(snode.getName(),
                  renode.getEdition().getName());
            map.put(renode.getEditionId(), names);
         }
      }
      
      return map;
   }
   
   /**
    * Get the help file name for a Job Publishing Log page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getJobPubLogHelpFile()
   {
      return PSHelpTopicMapping.getFileName("JobPubLog");
   }

   /**
    * Get the help file name for an Item Publishing Log page.
    * 
    * @return  the help file name, never <code>null</code> or empty.
    */
   public String getItemPubLogHelpFile()
   {
      return PSHelpTopicMapping.getFileName("ItemPubLog");      
   }
   
}
