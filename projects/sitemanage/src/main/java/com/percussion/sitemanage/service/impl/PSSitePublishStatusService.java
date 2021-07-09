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
package com.percussion.sitemanage.service.impl;

import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.IPSRxPublisherServiceInternal;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPubItemStatus;
import com.percussion.services.publisher.IPSPubStatus;
import com.percussion.services.publisher.IPSPubStatus.EndingState;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.publisher.IPSSiteItem;
import com.percussion.services.publisher.IPSSiteItem.Operation;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSitePublishItem;
import com.percussion.sitemanage.data.PSSitePublishItemList;
import com.percussion.sitemanage.data.PSSitePublishJob;
import com.percussion.sitemanage.data.PSSitePublishJobList;
import com.percussion.sitemanage.data.PSSitePublishLogDetailsRequest;
import com.percussion.sitemanage.data.PSSitePublishLogRequest;
import com.percussion.sitemanage.data.PSSitePublishPurgeRequest;
import com.percussion.sitemanage.service.IPSSitePublishStatusService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.string.PSStringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * @author DavidBenua
 * @author adamgent
 * 
 */
@Path("/pubstatus")
@Component("sitePublishStatusService")
@Lazy
public class PSSitePublishStatusService implements IPSSitePublishStatusService
{

    private static final Logger log = LogManager.getLogger(PSSitePublishStatusService.class);

    private IPSRxPublisherServiceInternal rxPubSvc;

    private IPSPublisherService pubSvc;

    private IPSSiteManager siteMgr;

    private IPSGuidManager guidMgr;

    private IPSPubServerDao pubServerDao;
    
    private String dateFormat = "MM/dd/yyyy";

    private String timeFormat = "hh:mm:ss a";

    private boolean dummyData = false;

    @Autowired
    public PSSitePublishStatusService(IPSRxPublisherServiceInternal rxPubSvc, IPSPublisherService pubSvc,
            IPSSiteManager siteMgr, IPSGuidManager guidMgr, IPSPubServerDao pubServerDao)
    {
        this.rxPubSvc = rxPubSvc;
        this.pubSvc = pubSvc;
        this.siteMgr = siteMgr;
        this.guidMgr = guidMgr;
        this.pubServerDao = pubServerDao;
    }

    /**
     * Gets the current jobs.
     * 
     * @see com.percussion.sitemanage.service.IPSSitePublishStatusService#getCurrentJobs()
     */
    @Override
    @GET
    @Path("/current")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSitePublishJob> getCurrentJobs() throws PSDataServiceException
    {
        try {
            log.debug("getting the current jobs");
            List<PSSitePublishJob> jobs = new ArrayList<>();

            if (dummyData) {
                final FastDateFormat format = FastDateFormat.getInstance(dateFormat);
                final FastDateFormat tformat = FastDateFormat.getInstance(timeFormat);
                final GregorianCalendar today = new GregorianCalendar();
                PSSitePublishJob job1 = new PSSitePublishJob() {
                    {
                        setSiteId("1");
                        setSiteName("Site1");
                        setStartDate(format.format(today.getTime()));
                        setStartTime(tformat.format(today.getTime()));
                        setJobId(1L);
                        setElapsedTime(483L);
                        setStatus(IPSPublisherJobStatus.State.WORKING.getDisplayName());
                        setCompletedItems(43L);
                        setTotalItems(107L);
                    }
                };
                jobs.add(job1);
                today.add(GregorianCalendar.HOUR, 1);
                PSSitePublishJob job2 = new PSSitePublishJob() {
                    {
                        setSiteId("2");
                        setSiteName("Site2");
                        setStartDate(format.format(today.getTime()));
                        setStartTime(tformat.format(today.getTime()));
                        setJobId(2L);
                        setElapsedTime(512L);
                        setStatus(IPSPublisherJobStatus.State.COMMITTING.getDisplayName());
                        setCompletedItems(17L);
                        setTotalItems(245L);
                    }
                };
                jobs.add(job2);
                today.add(GregorianCalendar.HOUR, 3);
                PSSitePublishJob job3 = new PSSitePublishJob() {
                    {
                        setSiteId("3");
                        setSiteName("Site3");
                        setStartDate(format.format(today.getTime()));
                        setStartTime(tformat.format(today.getTime()));
                        setJobId(3L);
                        setElapsedTime(687L);
                        setStatus(IPSPublisherJobStatus.State.QUEUEING.getDisplayName());
                        setCompletedItems(53L);
                        setTotalItems(784L);
                    }
                };
                jobs.add(job3);
            } else {
                // get all jobs
                jobs = buildCurrentJobs(null);
            }

            log.debug("Returning " + jobs.size() + " jobs");
            return new PSSitePublishJobList(jobs);
        } catch (PSNotFoundException e) {
           throw new WebApplicationException(e);
        }
    }
    
    /**
     * Gets the current jobs by site.
     * 
     * @param siteId the site id.
     * @see com.percussion.sitemanage.service.IPSSitePublishStatusService#getCurrentJobs()
     */
    @Override
    @GET
    @Path("/current/{siteId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSitePublishJob> getCurrentJobsBySite(@PathParam("siteId") String siteId) throws PSDataServiceException
    {
        try {
            log.debug("getting the current jobs");
            List<PSSitePublishJob> jobs = new ArrayList<>();

            if (dummyData) {
                final FastDateFormat format = FastDateFormat.getInstance(dateFormat);
                final FastDateFormat tformat = FastDateFormat.getInstance(timeFormat);
                final GregorianCalendar today = new GregorianCalendar();
                PSSitePublishJob job1 = new PSSitePublishJob() {
                    {
                        setSiteId("1");
                        setSiteName("Site1");
                        setStartDate(format.format(today.getTime()));
                        setStartTime(tformat.format(today.getTime()));
                        setJobId(1L);
                        setElapsedTime(483L);
                        setStatus(IPSPublisherJobStatus.State.WORKING.getDisplayName());
                        setCompletedItems(43L);
                        setTotalItems(107L);
                    }
                };
                jobs.add(job1);
                today.add(GregorianCalendar.HOUR, 1);
                PSSitePublishJob job2 = new PSSitePublishJob() {
                    {
                        setSiteId("2");
                        setSiteName("Site2");
                        setStartDate(format.format(today.getTime()));
                        setStartTime(tformat.format(today.getTime()));
                        setJobId(2L);
                        setElapsedTime(512L);
                        setStatus(IPSPublisherJobStatus.State.COMMITTING.getDisplayName());
                        setCompletedItems(17L);
                        setTotalItems(245L);
                    }
                };
                jobs.add(job2);
                today.add(GregorianCalendar.HOUR, 3);
                PSSitePublishJob job3 = new PSSitePublishJob() {
                    {
                        setSiteId("3");
                        setSiteName("Site3");
                        setStartDate(format.format(today.getTime()));
                        setStartTime(tformat.format(today.getTime()));
                        setJobId(3L);
                        setElapsedTime(687L);
                        setStatus(IPSPublisherJobStatus.State.QUEUEING.getDisplayName());
                        setCompletedItems(53L);
                        setTotalItems(784L);
                    }
                };
                jobs.add(job3);
            } else {
                // get jobs selected by siteId
                jobs = buildCurrentJobs(siteId);
            }

            log.debug("Returning " + jobs.size() + " jobs");
            return new PSSitePublishJobList(jobs);
        } catch (PSNotFoundException e) {
            throw new WebApplicationException(e);
        }
    }

    @Override
    @POST
    @Path("/logs")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSitePublishJob> getLogs(PSSitePublishLogRequest request) throws PSDataServiceException
    {
        try {
            log.debug("getting logs. Site=" + request.getSiteId() + " Days=" + request.getDays() + " maxcount=" + request.getMaxcount());

            List<PSSitePublishJob> jobs = new ArrayList<>();
            if (dummyData) {
                final FastDateFormat format = FastDateFormat.getInstance(dateFormat);
                final FastDateFormat tformat = FastDateFormat.getInstance(timeFormat);
                final GregorianCalendar today = new GregorianCalendar();
                for (int i = 0; i < request.getMaxcount(); i++) {

                    PSSitePublishJob job1 = new PSSitePublishJob() {
                        {
                            setStartDate(format.format(today.getTime()));
                            setStartTime(tformat.format(today.getTime()));
                            setElapsedTime(483L);
                            setStatus(IPSPublisherJobStatus.State.WORKING.getDisplayName());
                            setCompletedItems(43L);
                            setTotalItems(107L);
                            setFailedItems(1L);
                        }
                    };
                    job1.setSiteId(String.valueOf(i));
                    job1.setSiteName("Site" + String.valueOf(i));
                    job1.setJobId(301L + i);
                    jobs.add(job1);
                    today.add(GregorianCalendar.HOUR, 1);
                }
            } else {
                jobs = buildLogs(request.getSiteId(), request.getPubServerId(), request.getDays(), request.getMaxcount(), request.getSkipCount(), !request.isShowOnlyFailures());
            }

            log.debug("Returning " + jobs.size() + " jobs");
            return new PSSitePublishJobList(jobs);
        } catch (PSNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e);
        }
    }

    @Override
    @POST
    @Path("/purge")
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public void purgeLog(PSSitePublishPurgeRequest purgeReq) throws PSDataServiceException
    {
        try
        {
            for (long jobid : purgeReq.getJobids())
            {
                log.debug("purging log for job " + jobid);
                doPurge(jobid);
            }

        }
        catch (Exception ex)
        {
            String emsg = "Error during purge " + ex;
            log.error(emsg, ex);
            throw new RuntimeException(emsg, ex);
        }
    }

    @Override
    @POST
    @Path("/details")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSitePublishItem> getJobDetails(PSSitePublishLogDetailsRequest request) throws PSDataServiceException
    {
        long jobid = request.getJobid();

        log.debug("getting job details for job " + jobid);
        List<PSSitePublishItem> details = new ArrayList<>();

        if (dummyData)
        {
            PSSitePublishItem item = new PSSitePublishItem()
            {
                {
                    setContentid(1234L);
                    setElapsedTime(1723L);
                    setFileName("index.html");
                    setFileLocation("/home/section/index.html");
                }
            };
            details.add(item);
        }
        else
        {
            details = buildItemDetails(jobid);
        }
        return new PSSitePublishItemList(details);
    }

    protected List<PSSitePublishJob> buildCurrentJobs(String siteId) throws PSNotFoundException {
        List<PSSitePublishJob> jobs = new ArrayList<>();
        Collection<Long> activeJobs = null;
        if (siteId != null && !siteId.equals(""))
        {
            IPSGuid siteGUID = guidMgr.makeGuid(siteId, PSTypeEnum.SITE);
            activeJobs = rxPubSvc.getActiveJobIds(siteGUID);
        }
        else
        {
            activeJobs = rxPubSvc.getActiveJobIds();
        }
        for (long id : activeJobs)
        {
            IPSPublisherJobStatus status = rxPubSvc.getPublishingJobStatus(id);
            if (isJobActive(status))
            {
                jobs.add(buildJob(id, status));
            }
        }

        return jobs;
    }
    
    protected List<PSSitePublishJob> buildLogs(String siteId, String pubServerId, int days, int maxCount) throws PSNotFoundException {
    	return buildLogs(siteId, pubServerId, days, maxCount, 0, true); 
    }
            
    protected List<PSSitePublishJob> buildLogs(String siteId, String pubServerId, int days, int maxCount, int skipCount, boolean showAll) throws PSNotFoundException {
        List<PSSitePublishJob> jobs = new ArrayList<>();
        GregorianCalendar dateLimit = new GregorianCalendar();
        dateLimit.add(Calendar.DATE, -days);

        int counter = Math.max(skipCount, 0);

        //TODO:  Fix so the filtering is done in the back end service and add support for pagination.  The code as is is effectively getting all publishing logs from backend even when we are only trying to render 1 days worth.
        List<IPSPubStatus> pubStatus = null;
        if (isNotBlank(siteId))
        {
            IPSGuid siteGUID = guidMgr.makeGuid(siteId, PSTypeEnum.SITE);
            
            if (isNotBlank(pubServerId))
            {
                IPSGuid pubServerGUID = guidMgr.makeGuid(pubServerId, PSTypeEnum.PUBLISHING_SERVER);
                pubStatus = pubSvc.findPubStatusBySiteAndServerWithFilters(siteGUID, pubServerGUID, days, maxCount);
            }
            else {
                pubStatus = pubSvc.findPubStatusBySiteWithFilters(siteGUID, days, maxCount);
            }
        }
        else
        {
            pubStatus = pubSvc.findAllPubStatusWithFilters(days, maxCount);
        }
        for (IPSPubStatus status : pubStatus)
        {
            if(counter > 0) {
                if(showAll || isFailure(status.getEndingState())) {
                    counter--;
                    continue; 
                }
            }
            
            if((showAll || isFailure(status.getEndingState())))
            {
                jobs.add(buildJob(status));
            }
            if (jobs.size() >= maxCount)
                break;
        }
        return jobs;
    }
    
    protected boolean isFailure(EndingState estate)
    {
        if( estate == EndingState.ABORTED || estate == EndingState.CANCELED_BY_USER ||
            		estate == EndingState.COMPLETED_W_FAILURE || estate == EndingState.RESTARTNEEDED)

             return true;
    	return false; 
    }
    
    protected List<PSSitePublishItem> buildItemDetails(long jobid)
    {
    	return buildItemDetails(jobid, 0, false); 
    }
    
    protected List<PSSitePublishItem> buildItemDetails(long jobid, int skipCount, boolean showFailures)
    {
        List<PSSitePublishItem> details = new ArrayList<>();
        int counter = Math.max(skipCount, 0); 
        for (IPSPubItemStatus status : pubSvc.findPubItemStatusForJob(jobid))
        {
        	if(counter > 0)
        	{
        		if((!showFailures) || isDetailFailure(status.getStatus()))
        		{
        		   counter--;
        		   continue;
        		}
        	}
        	
        	if((!showFailures) || isDetailFailure(status.getStatus()))
        	{
               details.add(buildItem(status));
        	}
        }
        return details;
    }
    
    protected boolean isDetailFailure(IPSSiteItem.Status status)
    {
    	if(status == IPSSiteItem.Status.FAILURE ||
    	   status == IPSSiteItem.Status.CANCELLED) 
    	{
    		return true; 
    	}
    	return false; 
    }

    protected boolean isJobActive(IPSPublisherJobStatus pubJobStatus)
    {
        IPSPublisherJobStatus.State state = pubJobStatus.getState();
        if (state == IPSPublisherJobStatus.State.INACTIVE)
            return false;
        if (state == IPSPublisherJobStatus.State.COMPLETED)
            return false;
        if (state == IPSPublisherJobStatus.State.COMPLETED_W_FAILURE)
            return false;
        if (state == IPSPublisherJobStatus.State.ABORTED)
            return false;
        if (state == IPSPublisherJobStatus.State.PUBSERVERNEWDBCONFIG )
            return false;
        if (state == IPSPublisherJobStatus.State.CANCELLED)
            return false;
        if (log.isTraceEnabled())
            log.trace("job is active");
        return true;
    }

    protected static final String STATE_PENDING = "Pending";

    protected static final String STATE_RUNNING = "Running";

    protected static final String STATE_FAILED = "Failed";

    protected static final String STATE_COMPLETE = "Completed";
    
    protected static final String STATE_COMPLETE_W_FAILURES = "Completed with failures";

    protected static final String STATE_CANCELLED = "Cancelled";
    
    protected static final String STATE_PUBLISHED = "Published";
    
    protected static final String STATE_REMOVED = "Removed";

    protected String getStateDescription(IPSPublisherJobStatus.State state)
    {
        if (state == State.INITIAL || state == State.QUEUEING)
            return STATE_PENDING;
        if (state == State.CANCELLED)
            return STATE_CANCELLED;
        if (state == State.PRETASKS || state == State.POSTTASKS || state == State.WORKING || state == State.COMMITTING)
            return STATE_RUNNING;
        if (state == State.COMPLETED || state == State.INACTIVE)
            return STATE_COMPLETE;
        if (state == State.COMPLETED_W_FAILURE)
            return STATE_COMPLETE_W_FAILURES;
        if (state == State.ABORTED || state == State.PUBSERVERNEWDBCONFIG)
            return STATE_FAILED;

        return "";
    }

    protected String getStateDescription(IPSPubStatus.EndingState state)
    {
        if (state == EndingState.ABORTED || state == EndingState.RESTARTNEEDED)
            return STATE_FAILED;
        if (state == EndingState.CANCELED_BY_USER)
            return STATE_CANCELLED;
        if (state == EndingState.COMPLETED)
            return STATE_COMPLETE;
        if (state == EndingState.COMPLETED_W_FAILURE)
            return STATE_COMPLETE_W_FAILURES;
        return "";
    }

    protected String getStateDescription(IPSSiteItem.Status state)
    {
        if (state == IPSSiteItem.Status.FAILURE)
            return STATE_FAILED;
        if (state == IPSSiteItem.Status.SUCCESS)
            return STATE_COMPLETE;
        if (state == IPSSiteItem.Status.CANCELLED)
            return STATE_CANCELLED;
        return "";
    }

    protected String getSiteName(long editionId) throws PSNotFoundException {
        IPSGuid editionGUID = guidMgr.makeGuid(editionId, PSTypeEnum.EDITION);
        return getSiteName(editionGUID);
    }

    protected String getSiteName(IPSGuid editionId) throws PSNotFoundException {
        IPSEdition edition = pubSvc.loadEdition(editionId);
        return getSiteName(edition);
    }
    
    protected IPSGuid getPubServerId(IPSGuid editionId) throws PSNotFoundException {
        IPSEdition edition = pubSvc.loadEdition(editionId);
        return edition.getPubServerId();
    }

    protected String getSiteName(IPSEdition edition) throws PSNotFoundException {
        IPSSite site = siteMgr.loadSite(edition.getSiteId());
        return site.getName();
    }
    
    protected String getServerName(long serverId) throws PSNotFoundException {
        IPSGuid serverGUID = guidMgr.makeGuid(serverId, PSTypeEnum.PUBLISHING_SERVER);
        return getPubServerName(serverGUID);
    }

    private String getPubServerName(IPSGuid serverGUID) throws PSNotFoundException {
        IPSPubServer server = pubServerDao.loadPubServer(serverGUID);
        return server.getName();
    }

    protected PSSitePublishJob buildJob(long jobId, IPSPublisherJobStatus status) throws PSNotFoundException {
        FastDateFormat dformat = FastDateFormat.getInstance(dateFormat);
        FastDateFormat tformat = FastDateFormat.getInstance(timeFormat);
        PSSitePublishJob job = new PSSitePublishJob();
        job.setJobId(jobId);
        job.setCompletedItems(status.countItemsDelivered());
        job.setTotalItems(status.countTotalItems());
        job.setElapsedTime(status.getElapsed());
        job.setFailedItems(status.countFailedItems());
        job.setRemovedItems(0); // can't do this for an active item.
        job.setStartDate(dformat.format(status.getStartTime()));
        job.setStartTime(tformat.format(status.getStartTime()));
        job.setSiteName(getSiteName(status.getEditionId()));
        job.setStatus(getStateDescription(status.getState()));
        job.setIsStopping(job.getStatus().equalsIgnoreCase(IPSPublisherJobStatus.State.CANCELLED.toString()));
        job.setPubServerId(getPubServerId(status.getEditionId()).longValue());
        return job;
    }

    protected PSSitePublishJob buildJob(IPSPubStatus status) throws PSNotFoundException {
        FastDateFormat dformat = FastDateFormat.getInstance(dateFormat);
        FastDateFormat tformat = FastDateFormat.getInstance(timeFormat);
        PSSitePublishJob job = new PSSitePublishJob();
        job.setJobId(status.getStatusId());
        job.setCompletedItems(status.getDeliveredCount());
        job.setTotalItems(status.getDeliveredCount() + status.getRemovedCount() + status.getFailedCount());
        job.setRemovedItems(status.getRemovedCount());
        job.setFailedItems(status.getFailedCount());
        //NP below AG
        Date startDate = status.getStartDate();
        Date endDate = status.getEndDate();
        if (startDate != null) {
            job.setStartDate(dformat.format(startDate));
            job.setStartTime(tformat.format(startDate));
        }
        if (startDate != null && endDate != null) {
            job.setElapsedTime(endDate.getTime() - startDate.getTime());
        }
        
        job.setSiteName(getSiteName(status.getEditionId()));
        job.setStatus(getStateDescription(status.getEndingState()));
        job.setPubServerId(status.getPubServerId());
        
        String serverName = "";
        try
        {
            serverName = getServerName(status.getPubServerId());
        }
        catch (Exception e)
        {
            log.error("Error trying to get server " + status.getPubServerId());
        }
        job.setPubServerName(serverName);
        return job;
    }

    protected PSSitePublishItem buildItem(IPSPubItemStatus status)
    {
        PSSitePublishItem item = new PSSitePublishItem();
        item.setRevisionid(status.getRevisionId());
        item.setAssemblyUrl(status.getAssemblyUrl());
        item.setFolderid(PSStringUtils.NullToZero(status.getFolderId()));
        item.setTemplateid(PSStringUtils.NullToZero(status.getTemplateId()));
        item.setDeliveryType(status.getDeliveryType());
        item.setItemStatusId(status.getStatusId());
        item.setContentid(status.getContentId());
        Integer elapsed = status.getElapsed();
        item.setElapsedTime((elapsed != null) ? elapsed : 0);
        item.setFileLocation(status.getLocation());
        item.setFileName(status.getLocation());
        Operation pubOp = status.getOperation();
        item.setOperation(pubOp.toString());
        String pubStatus = getStateDescription(status.getStatus());
        if (pubStatus.equals(STATE_COMPLETE))
        {
            // set the status according to the operation
            pubStatus = (pubOp == Operation.PUBLISH) ? STATE_PUBLISHED : STATE_REMOVED;
        }
        else if (pubStatus.equals(STATE_FAILED))
        {
            // set the error message
            item.setErrorMessage(status.getMessage());
        }
        item.setStatus(pubStatus);
        return item;
    }

    protected void doPurge(long jobid)
    {
        pubSvc.purgeJobLog(jobid);
    }

    /**
     * @return the dateFormat
     */
    public String getDateFormat()
    {
        return dateFormat;
    }

    /**
     * @param dateFormat the dateFormat to set
     */
    public void setDateFormat(String dateFormat)
    {
        this.dateFormat = dateFormat;
    }

    /**
     * @return the dummyData
     */
    public boolean isDummyData()
    {
        return dummyData;
    }

    /**
     * @param dummyData the dummyData to set
     */
    public void setDummyData(boolean dummyData)
    {
        this.dummyData = dummyData;
    }

    @Override
    public boolean isSitePublished(IPSGuid siteGuid) throws PSDataServiceException
    {
        if(siteGuid == null){
            throw new IllegalArgumentException("siteGuid must not be null");
        }
        return pubSvc.isSitePublished(siteGuid);
    }
}
