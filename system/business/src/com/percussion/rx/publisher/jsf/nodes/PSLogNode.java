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
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.jsf.beans.PSRuntimeNavigation;
import com.percussion.rx.publisher.jsf.data.PSStatusLogEntry;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.guid.IPSGuid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.trinidad.model.RowKeySet;
import org.apache.myfaces.trinidad.model.RowKeySetImpl;

/**
 * Common functionality for the site and edition publishing logs
 * 
 * @author dougrand
 */
public abstract class PSLogNode extends PSNodeBase
{
   /**
    * Logger
    */
   private static Log ms_log = LogFactory.getLog(PSLogNode.class);
   
   /**
    * Defaults to <code>null</code>. See {@link #getPageRows()} for detail.
    */
   private Integer m_pageRows = null;
   
   /**
    * The current entries, used in the actions to determine what the selected
    * log is.
    */
   protected List<PSStatusLogEntry> m_currentEntries = null;
   
   /**
    * The archive link, set in the archive method. Used by the user to navigate
    * and view the archive file on the server from a browser.
    */
   protected String m_archiveLink = null;
   
   /**
    * Needed ctor
    * @param title
    * @param outcome
    */
   public PSLogNode(String title, String outcome) {
      super(title, outcome);
   }
   
   /**
    * Get the status logs
    * @return the status logs, never <code>null</code>.
    */
   public abstract List<IPSPubStatus> getStatusLogs();
   
   /**
    * Determines if the site column need to be rendered or not.
    * @return <code>true</code> if the site column need to be rendered.
    */
   public abstract boolean isShowSiteColumn();
   
   /**
    * @return a list of records from prior complete runs of this edition. The
    * maps contain the following data from each run: <ul>
    * <li>statusid
    * <li>start
    * <li>end
    * <li>elapsed
    * <li>delivered
    * <li>removed
    * <li>failures
    * <li>statusentry - a {@link PSStatusLogEntry} object
    * </ul>
    * To accomplish this the method will dispatch to {@link #getStatusLogs()}
    * to get the raw data. The current entries will also be setup to allow
    * later actions to determine what status row was selected. 
    */
   public List<Map<String,Object>> getProcessedStatusLogs()
   {
      m_selectedRowKeys.clear();
    
      List<IPSPubStatus> stati = getStatusLogs();
      setPageRows(stati);
      m_currentEntries = new ArrayList<>();

      List<Map<String,Object>> rval = PSPublishingStatusHelper
         .processStatus(stati, useAnimatedIcon(), (PSRuntimeNavigation) getModel().getNavigator());
      for(Map<String,Object> ent : rval)
      {
         m_currentEntries.add((PSStatusLogEntry) ent.get("statusentry"));
      }    
      return rval;
   }
   
   /**
    * Determines whether to use the animated icon or not. Use animated icon
    * if the page automatically refreshes itself; otherwise use static icon.
    * 
    * @return <code>true</code> if use animated icon.
    */
   protected boolean useAnimatedIcon()
   {
      return false;
   }
   
   /**
    * The place holder for the selected row keys. It is updated by 
    * JSF/Trinidad table.
    * 
    * @see #getSelectedRowKeys()
    */
   private RowKeySet m_selectedRowKeys = new RowKeySetImpl();
   
   /**
    * Gets the "backing bean" for selected row keys. This object is managed by
    * JSF/Trinidad table.
    * 
    * @return the selected row keys object, never <code>null</code>.
    */
   public RowKeySet getSelectedRowKeys()
   {
      return m_selectedRowKeys;
   }
   
   /**
    * Gets the number of rows per page for the table that displays
    * the publishing summary status for the current node. 
    * @return the number of rows per page.
    */
   public int getPageRows()
   {
      setPageRows(null);
      return m_pageRows.intValue();
   }

   /**
    * Set the rows per page if needed.
    * @param stati the list of rows will be displayed on the table.
    */
   private void setPageRows(List<IPSPubStatus> stati)
   {
      if (m_pageRows != null)
         return;
      
      if (stati == null)
         stati = getStatusLogs();
      
      m_pageRows = PSNodeBase.getPageRows(stati.size());
   }
   
   /**
    * Archive and purge the selected log and setup any information to allow the
    * user to view the last archived log.
    * 
    * @return the warning or error action page if nothing is selected or
    * failed to archive or purge; otherwise return <code>null</code>.
    */
   public String archiveSelected()
   {
      return purgeArchiveSelected(true);
   }

   /**
    * Purge the selected logs.
    * 
    * @return the warning or error action page if nothing is selected or
    * failed to purge; otherwise return <code>null</code>.
    */
   public String purgeSelected()
   {
      return purgeArchiveSelected(false);
   }
   
   /**
    * Purges and/or archives the selected log entries.
    * 
    * @return the warning or error action page if nothing is selected or
    * failed to purge/archive; otherwise return <code>null</code>.
    */
   private String purgeArchiveSelected(boolean isArchive)
   {
      PSStatusLogEntry selectedLogEntry = null;
      for (int i=0; i<m_currentEntries.size(); i++)
      {
         if (m_selectedRowKeys.contains(i))
         {
            selectedLogEntry = m_currentEntries.get(i);
            
            // only remove/archive terminated log entries
            if (selectedLogEntry.isTerminated())
            {
               if (isArchive)
               {
                  m_errorMessage = archiveJobLog(selectedLogEntry.getJobid());
                  if (m_errorMessage != null)
                     return "pub-runtime-error-message";
               }

               m_errorMessage = purgeJobLog(selectedLogEntry.getJobid());
               if (m_errorMessage != null)
                  return "pub-runtime-error-message";
            }
         }
      }
      if (selectedLogEntry == null)
         return "pub-runtime-no-selection-warning";
      
      // clear the selections afterwards 
      m_selectedRowKeys.clear();
      return null;
   }

   
   /**
    * Error message for the last archive or purge action, may be 
    * <code>null</code> or empty.
    */
   private String m_errorMessage = null;
   
   /**
    * Gets the error message for the last archive or purge action.
    * This method is used by JSF to retrieve the error message.
    * 
    * @return the error message, may be <code>null</code> or empty.
    */
   public String getErrorMessage()
   {
      return m_errorMessage;
   }
   
   /**
    * Archive the log data from the given job
    * @param jobid the job id, assumed not <code>null</code>.
    * @return <code>null</code> if successfully archived the pub log; otherwise 
    * return error message if failed to archive the given pub log. 
    */
   private String archiveJobLog(Long jobId)
   {
      IPSRxPublisherServiceInternal psvc = PSRxPubServiceInternalLocator
            .getRxPublisherService();
      try
      {
         FacesContext ctx = FacesContext.getCurrentInstance();
         HttpServletRequest req = (HttpServletRequest) ctx
               .getExternalContext().getRequest();
         m_archiveLink = psvc.archivePubLog(jobId, req);
         return null;
      }
      catch (Exception e)
      {
         String msg = "Failed to archive job (id=" + jobId
               + ") to location: '" + psvc.getArchiveLocation() + "'.";
         ms_log.error(msg, e);
         return msg + " The underlying error is: " + e.getLocalizedMessage();
      }
   }

   /**
    * Purge the specified job log from the repository.
    * @param jobId the to be purged job ID, assumed not <code>null</code>.
    * @return <code>null</code> if successfully purged the pub log; otherwise 
    * return error message if failed to purge the given pub log. 
    */
   private String purgeJobLog(Long jobId)
   {
      try
      {
         IPSRxPublisherServiceInternal rxsvc = PSRxPubServiceInternalLocator
               .getRxPublisherService();
         Collection<Long> activeJobIds = rxsvc.getActiveJobIds();

         IPSPublisherService psvc = PSPublisherServiceLocator
               .getPublisherService();

         psvc.purgeJobLog(jobId);

         // remove the Job status from memory cache
         if (activeJobIds.contains(jobId))
            rxsvc.removePublishingJobStatus(Collections.singletonList(jobId));
         
         return null;
      }
      catch (Exception e)
      {
         String msg = "Failed to purge Job ID: " + jobId + ".";
         ms_log.error(msg, e);
         return msg + " The underlying error is: " + e.getLocalizedMessage();
      }
   }
   
   /**
    * Gets the archive XML file location.
    * @return the archive file location, may be empty if there is no URL  
    *    points to an archived file.
    */
   public String getArchiveLocation()
   {
      if (StringUtils.isBlank(getArchiveLink()))
         return "";
      
      String dir = PSRxPubServiceInternalLocator.getRxPublisherService()
            .getArchiveLocation();
      String normLink = m_archiveLink.replace('\\', '/');
      int index = normLink.lastIndexOf('/');
      String loc = dir + normLink.substring(index);
      return loc;
   }

   /**
    * Determines if the archived publishing log can be viewed remotely from
    * a browser. The archived log can only be viewed remotely if it is from
    * the default archive location.
    * 
    * @return <code>true</code> if it can be viewed from a browser.
    */
   public boolean getRemoteViewableArchive()
   {
      String link = getArchiveLink();
      if (StringUtils.isBlank(link))
         return false;
      
      return link.startsWith("http");
   }
   
   /**
    * @return the archiveLink
    */
   public String getArchiveLink()
   {
      return (m_archiveLink == null) ? "" : m_archiveLink;
   }

   /**
    * Deletes all entries in site item table for the given site.
    * @param siteId the site ID, never <code>null</code>.
    */
   public void deleteSiteItems(IPSGuid siteId)
   {
      if (siteId == null)
         throw new IllegalArgumentException("siteId may not be null.");
      
      IPSPublisherService psvc = PSPublisherServiceLocator
            .getPublisherService();
      psvc.deleteSiteItems(siteId);
   }
}
