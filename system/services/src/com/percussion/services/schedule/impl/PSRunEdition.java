/*
 *     Percussion CMS
 *     Copyright (C) 1999-2021 Percussion Software, Inc.
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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.services.schedule.impl;

import com.percussion.extension.IPSExtensionDef;
import com.percussion.extension.PSExtensionException;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSPublishingJobStatusCallback;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.server.PSServer;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.services.schedule.IPSTask;
import com.percussion.services.schedule.IPSTaskResult;
import com.percussion.services.schedule.PSSchedulingException;
import com.percussion.services.schedule.PSSchedulingException.Error;
import com.percussion.services.schedule.data.PSTaskResult;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.sitemgr.PSSiteManagerException;
import com.percussion.services.sitemgr.PSSiteManagerLocator;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * This is used to publishing a specified Edition. The name of the Edition
 * is expected to by specified by <code>editionName</code> parameter of the 
 * Extension.
 *
 * @author Yu-Bing Chen
 */
public class PSRunEdition implements IPSTask, IPSPublishingJobStatusCallback
{
   /**
    * Publishes the specified Edition. The returned task result will contain the
    * following variables, which can be used in notification template: <TABLE
    * BORDER="1">
    * <TR>
    * <TH>Variable Name</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>$sys.editionName</TD>
    * <TD>The name of the published Edition.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.siteName</TD>
    * <TD>The name of the published Site.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.editionLogUrl</TD>
    * <TD>The URL can be used to view the Edition Log.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.failureCount</TD>
    * <TD>The number of failures in this publishing run.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.successCount</TD>
    * <TD>The number of successfully published pages.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.executionDatetime</TD>
    * <TD>The starting date and time of the publishing run.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.executionElapsedTime</TD>
    * <TD>The duration of the publishing run.</TD>
    * </TR>
    * <TR>
    * <TD>$editionName</TD>
    * <TD>The edition name parameter.</TD>
    * </TR>
    * </TABLE>
    * <p>
    * The following context variables will be added by the framework: <TABLE
    * BORDER="1">
    * <TR>
    * <TH>Variable Name</TH>
    * <TH>Description</TH>
    * </TR>
    * <TR>
    * <TD>$sys.taskName</TD>
    * <TD>The name of the scheduled task.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.completed</TD>
    * <TD>It is true if the Edition publishing was completed; otherwise false.</TD>
    * </TR>
    * <TR>
    * <TD>$sys.problemDesc</TD>
    * <TD>The problem description in case of execution failure.</TD>
    * </TR>
    * <TR>
    * <TD>$tools.*</TD>
    * <TD>The problem description in case of execution failure.</TD>
    * </TR>
    * </TABLE>
    * 
    * @param parameters It must contains a <code>editionName</code> parameter
    * as the name of the published Edition, never <code>null</code> or empty.
    * 
    * @return the {@link IPSTaskResult#getNotificationVariables()} contains all
    * input parameters, never <code>null</code>.
    */
   public IPSTaskResult perform(Map<String,String> parameters)
   {
      IPSEdition edition = null;
      long startTime = System.currentTimeMillis();
      try
      {
         edition = getEdition(parameters);
         runEdition(edition);
         return getTaskResult(edition);
      }
      catch (Throwable e)
      {
         return getFaulureTaskResult(edition, e, startTime);
      }
   }

   /**
    * Get the result of the failed publishing job.
    * 
    * @param edition the published Edition, assumed not <code>null</code>.
    * @param e the exception caught for this failure, assumed not 
    *    <code>null</code>.
    * @param startTime the starting time of the execution.
    * 
    * @return the result of the failed job, never <code>null</code>.
    * 
    * @throws PSSiteManagerException if failed to find the published site.
    */
   private IPSTaskResult getFaulureTaskResult(IPSEdition edition, Throwable e,
         long startTime)
   {
      Throwable cause = e.getCause() == null ? e : e.getCause();
      String errMsg = edition == null ? cause.getLocalizedMessage() 
         : getErrorMessage(edition, cause, null);
      
      Map<String, Object> vars = new HashMap<>();
      vars.put("$sys.editionName", edition == null 
         ? "Unknown" : edition.getName());
      vars.put("$sys.siteName", edition == null 
         ? "Unknown" : getSiteName(edition.getSiteId()));
      vars.put("$sys.failureCount", -1);
      vars.put("$sys.successCount", -1);
      vars.put("$sys.executionElapsedTime", System.currentTimeMillis()
            - startTime);
      vars.put("$sys.executionDatetime", new Date(startTime));
      vars.put("$sys.editionLogUrl", "");

      return new PSTaskResult(false, errMsg, vars);
   }   

   /**
    * Get the result of the publishing job.
    * @param edition the published Edition, assumed not <code>null</code>.
    * @return the result of the publishing job, never <code>null</code>.
    * @throws PSSiteManagerException if failed to find the published site.
    */
   private IPSTaskResult getTaskResult(IPSEdition edition)
   {
      if (m_status == null)
         throw new IllegalStateException("m_status must not be null.");
      
      Map<String, Object> vars = new HashMap<>();
      vars.put("$sys.editionName", edition.getName());
      vars.put("$sys.siteName", getSiteName(edition.getSiteId()));
      vars.put("$sys.failureCount", m_status.countFailedItems());
      vars.put("$sys.successCount", m_status.countItemsDelivered());
      vars.put("$sys.executionElapsedTime", m_status.getElapsed());
      vars.put("$sys.executionDatetime", m_status.getStartTime());
      vars.put("$sys.editionLogUrl", getPublishingLogURL());

      if (m_status.getState().ordinal() == State.COMPLETED.ordinal())
      {
         return new PSTaskResult(true, null, vars);
      }
      else if (m_status.getState().ordinal() == State.COMPLETED_W_FAILURE.ordinal())
      {
         return new PSTaskResult(false, m_status.getState().getDisplayName(), vars);
      }
      else
      {
         String cause = getErrorMessage(edition, null, m_status);
         return new PSTaskResult(false, cause, vars);
      }
   }
   
   /**
    * @return the URL for viewing the publishing log.
    */
   private String getPublishingLogURL()
   {
      String protocol = "http";
      if (PSServer.getListenerPort() == PSServer.getSslListenerPort())
         protocol = "https";
      String url = protocol + "://" + PSServer.getFullyQualifiedHostName() + ":"
            + PSServer.getListenerPort() + PSServer.getRequestRoot()
            + "/ui/pubruntime/JobPubLog.faces?"
            + IPSHtmlParameters.PUBLISH_JOB_ID + "=" + m_jobId;
      return url;
   }
   
   /**
    * Get the name of the published site.
    * @param siteId the ID of the site.
    * @return the site name, never <code>null</code>, but may be empty if 
    *    failed to load the site.
    */
   private String getSiteName(IPSGuid siteId)
   {
      try
      {
         IPSSiteManager sitemgr = PSSiteManagerLocator.getSiteManager();
         IPSSite site = sitemgr.loadUnmodifiableSite(siteId);
         return site.getName();
      }
      catch (Exception e)
      {
         ms_log.error("Failed to load site: id = " + siteId, e);
         return "";
      }
   }
   
   /**
    * Get the error message when caught an exception
    *  
    * @param edition the publishing Edition, may be <code>null</code> if unknown
    * @param e the caught exception, may be <code>null</code>.
    * @param status the status of the publishing job, it may be <code>null</code>.
    * 
    * @return the error message, never <code>null</code>.
    */
   private String getErrorMessage(IPSEdition edition, Throwable e,
         IPSPublisherJobStatus status)
   {
      PSSchedulingException se; 
      String errorMsg = (e != null) ? e.getLocalizedMessage() : "";
      if (e != null && StringUtils.isBlank(errorMsg))
         errorMsg = e.toString();
      
      long id = (edition != null) ? edition.getGUID().longValue() : 0L;
      if (edition == null)
      {
         se = new PSSchedulingException(Error.FAILED_RUN_EDITION.ordinal(), e);
      }
      else if (e != null)
      {
         se = new PSSchedulingException(Error.FAILED_RUN_SPECIFIED_EDITION
               .ordinal(), e, id, edition.getName(), errorMsg);
      }
      else if (status != null
            && status.getState().ordinal() == State.CANCELLED.ordinal())
      {
         se = new PSSchedulingException(Error.EDITION_CANCELED_BY_USER
               .ordinal(), e, id, edition.getName());
      }
      else
      {
         se = new PSSchedulingException(Error.EDITION_ABORTED.ordinal(), e,
               id, edition.getName());         
      }
      return se.getLocalizedMessage();
   }
   
   /*
    * //see base class method for details
    */
   @SuppressWarnings("unused")
   public void init(@SuppressWarnings("unused") IPSExtensionDef def, 
         @SuppressWarnings("unused") File codeRoot) throws PSExtensionException
   {
      // No initialization
   }

   /**
    * Get the Edition from the parameters of the Extension.
    * 
    * @param params the parameters of the Extension, assumed not
    *    <code>null</code>.
    *    
    * @return the Edition, never <code>null</code>.
    */
   private IPSEdition getEdition(Map<String, String> params)
   {
      String editionName = params.get("editionName");
      if (StringUtils.isBlank(editionName))
      {
         throw new IllegalArgumentException(
               "editionName parameter is not defined.");
      }
      IPSPublisherService srv = PSPublisherServiceLocator.getPublisherService();
      IPSEdition edition = srv.findEditionByName(editionName);
      if (edition == null)
      {
         throw new IllegalArgumentException("Cannot find Edition with name: "
               + editionName);
      }
      
      return edition;
   }

   /**
    * Run the supplied Edition.
    * @param edition the published Edition, assumed not <code>null</code>.
    */
   private void runEdition(IPSEdition edition) throws InterruptedException
   {
      IPSRxPublisherService srv = PSRxPublisherServiceLocator
            .getRxPublisherService();
      m_jobId = srv.startPublishingJob(edition.getGUID(), this);

      synchronized (this)
      {
         wait();
      }
   }
   
   /*
    * //see base class method for details
    */
   public void notifyStatus(IPSPublisherJobStatus status)
   {
      if (status == null)
         throw new IllegalArgumentException("status may not be null.");
      
      m_status = status;
      
      synchronized (this)
      {
         notifyAll();
      }
   }
   
   /**
    * The finished job status. Set by
    * {@link #notifyStatus(IPSPublisherJobStatus)}, never <code>null</code>
    * after that.
    */
   private IPSPublisherJobStatus m_status;
   
   private long m_jobId = -1L;
   
   /**
    * Logger.
    */
   private static final Logger ms_log = LogManager.getLogger(PSRunEdition.class);
}
