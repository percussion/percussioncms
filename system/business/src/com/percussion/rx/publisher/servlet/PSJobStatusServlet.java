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
package com.percussion.rx.publisher.servlet;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSRxPublisherService;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.rx.publisher.PSRxPubServiceInternalLocator;
import com.percussion.rx.publisher.jsf.nodes.PSPublishingStatusHelper;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.PSPublisherServiceLocator;
import com.percussion.utils.guid.IPSGuid;

import java.io.IOException;
import java.io.Writer;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;

/**
 * Returns the status of one job or all jobs as JSON encoded data. The
 * invocation of this servlet can include the optional parameter "jobid". If
 * there's a job id included, then the returned data is an array of a single
 * status for that job. Otherwise all active jobs will be included.
 * <p>
 * Alternatively this servlet can cancel a running job. Available for calling
 * from Ajax.
 * <p>
 * This servlet is invoked from AJAX code that provides live updates to the
 * status of the job or jobs.
 * 
 * @author dougrand
 * 
 */
public class PSJobStatusServlet extends HttpServlet
{
   /**
    * The serialized class version id. 
    */
   private static final long serialVersionUID = -4550820178812015658L;

   @Override
   protected void doGet(HttpServletRequest request,
         HttpServletResponse response)
         throws ServletException, IOException
   {
      String requestid = request.getParameter("requestid");
      String edition = request.getParameter("edition");
      String stopjob = request.getParameter("stopjob");
      
      IPSRxPublisherServiceInternal psvc = PSRxPubServiceInternalLocator
            .getRxPublisherService();
      IPSPublisherService pubsvc = PSPublisherServiceLocator
            .getPublisherService();
      JSONArray arr = new JSONArray();
      DateFormat fmt = DateFormat.getDateTimeInstance();

      try
      {
         if (StringUtils.isNotBlank(stopjob))
         {
            psvc.cancelPublishingJob(Long.parseLong(stopjob));
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("cancelled", stopjob);
            arr.put(data);
         }
         else
         {
            for (Long job : getJobs(edition, requestid, psvc))
            {
               Map<String, Object> data;
               try
               {
                  data = getJobStatus(psvc, pubsvc, fmt, job);
               }
               catch (IllegalStateException e)
               {
                  data = new HashMap<String, Object>();
                  data.put("statename", IPSPublisherJobStatus.State.INACTIVE
                        .name());
               }
               data.put("jobid", job.toString());
               arr.put(data);
            }
         }
      }
      catch (Exception e)
      {
         Map<String, Object> data = new HashMap<String, Object>();
         data.put("exception", e.getLocalizedMessage());
         arr.put(data);
      }
      response.setContentType("text/plain");
      response.setHeader("Cache-Control", "no-cache");
      Writer w = response.getWriter();
      try
      {
         w.write(arr.toString());
      }
      finally
      {
         IOUtils.closeQuietly(w);
      }
   }

   /**
    * Get the jobs to query the status for.
    * 
    * @param edition the edition id, if specified then only get the job status
    *            for the single edition's job, otherwise get the status for all
    *            running jobs.
    * @param requestid the request id, if specified then only get the job status
    *            for the single job that the request is associated with.
    *            Can be <code>null</code>, blank or empty when the request id
    *            is not specified.
    * @param psvc the publisher business service, assumed never
    *            <code>null</code>.
    * @return the collection of jobs to query, never <code>null</code>.
    */
   private Collection<Long> getJobs(String edition,
         String requestid, IPSRxPublisherServiceInternal psvc)
   {
      Collection<Long> jobs = new ArrayList<Long>();
      if (edition != null)
      {
         IPSGuidManager gmgr = PSGuidManagerLocator.getGuidMgr();
         IPSGuid editionid = gmgr.makeGuid(Long.parseLong(edition),
               PSTypeEnum.EDITION);
         long jobid = psvc.getEditionJobId(editionid);
         if (jobid != 0)
            jobs.add(new Long(jobid));
      }
      else if (StringUtils.isNotBlank(requestid))
      {
         Long jobid = psvc.getDemandRequestJob(Long.parseLong(requestid));
         if (jobid != null)
         {
            jobs.add(jobid);
         }
      }
      else
      {
         jobs.addAll(psvc.getActiveJobIds());
      }
      return jobs;
   }

   /**
    * Get the status for the given job.
    * 
    * @param psvc the publisher business service, assumed never
    *            <code>null</code>.
    * @param pubsvc the publisher service, assumed never <code>null</code>
    * @param fmt the date format for the start and end date, assumed never
    *            <code>null</code>
    * @param job the job id
    * @return a map of data from the job's status, never <code>null</code>.
    */
   private Map<String, Object> getJobStatus(IPSRxPublisherService psvc,
         IPSPublisherService pubsvc, DateFormat fmt, Long job)
   {
      IPSPublisherJobStatus stat = psvc.getPublishingJobStatus(job);
      IPSEdition ed = pubsvc.loadEdition(stat.getEditionId());
      Map<String, Object> data = new HashMap<String, Object>();
      data.put("job_id", job);
      data.put("percent", PSPublishingStatusHelper.getJobCompletionPercent(stat));
      data.put("assembled", stat.countAssembledItems());
      data.put("queued", stat.countItemsQueuedForAssembly());
      data.put("failed", stat.countFailedItems());
      data.put("prepared", stat.countItemsPreparedForDelivery());
      data.put("delivered", stat.countItemsDelivered());
      data.put("state", stat.getState().getDisplayName());
      data.put("statename", stat.getState().name());
      data.put("start_time", fmt.format(stat.getStartTime()));
      data.put("elapsed_time", PSPublishingStatusHelper
            .convertMilliSecondToHhMmSs(stat.getElapsed()));
      data.put("edition_id", stat.getEditionId().longValue());
      data.put("edition_name", ed.getDisplayTitle());
      data.put("edition_behavior", ed.getEditionType().getDisplayTitle());
      return data;
   }
}
