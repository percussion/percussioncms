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
package com.percussion.sitemanage.service.impl;

import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.itemmanagement.data.PSItemDates;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.licensemanagement.service.impl.PSLicenseService;
import com.percussion.monitor.process.PSPublishingProcessMonitor;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.pubserver.data.PSPublishServerInfo;
import com.percussion.rest.Guid;
import com.percussion.rx.publisher.IPSPublisherJobStatus;
import com.percussion.rx.publisher.IPSPublisherJobStatus.State;
import com.percussion.rx.publisher.data.PSDemandWork;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.pubserver.IPSPubServerDao;
import com.percussion.services.pubserver.PSPubServicePubJobStatusHandler;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.data.PSPagedItemList;
import com.percussion.share.data.PSPagedObjectList;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSitePublishDao;
import com.percussion.sitemanage.data.PSPublishingAction;
import com.percussion.sitemanage.data.PSSitePublishResponse;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.sitemanage.service.IPSSitePublishService;
import com.percussion.sitemanage.service.IPSSitePublishServiceHelper;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.types.PSPair;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * Implements the {@link IPSSitePublishService} interface
 */
@Component("sitePublishService")
@Lazy
public class PSSitePublishService implements IPSSitePublishService
{
    /**
     * Compares the item entries using name, asc
     * 
     * @author JaySeletz
     * 
     */
    private final class CompareItemEntry implements Comparator<IPSItemEntry>
    {
        @Override
        public int compare(IPSItemEntry o1, IPSItemEntry o2)
        {
            Object prop1 = o1.getName();
            Object prop2 = o2.getName();

            int compareResult = new CompareToBuilder().append(prop1, prop2).toComparison();

            return compareResult;
        }
    }

    /**
     * Constructs a PSPublishingService object.
     * 
     * @param pubWs The webservice used for publishing item operations.
     * @param idMapper The service used for GUID to string translation.
     * @param contentWs The webservice used for content item operations.
     * @param widgetAssetRelationshipService The service used for relationship
     *            operations.
     * @param itemWorkflowService The service used for item workflow operations.
     * @param itemSummaryService The service used for item summary operations.
     * @param siteDao Used for site operations.
     */
    @Autowired
    public PSSitePublishService(IPSPublishingWs pubWs, IPSIdMapper idMapper, IPSContentWs contentWs,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService,
            IPSItemWorkflowService itemWorkflowService, IPSDataItemSummaryService itemSummaryService,
            IPSiteDao siteDao, IPSPubServerService pubServerService, PSLicenseService licenseService,
            IPSItemService itemService, IPSCmsObjectMgr cmsObjectMgr, IPSContentChangeService contentChangeService,
            @Qualifier("cm1ListViewHelper") IPSListViewHelper listViewHelper, IPSWorkflowHelper workflowHelper, IPSPageService pageService,
            PSSitePublishDao sitePublishDao, IPSSitePublishServiceHelper sitePublishServiceHelper)
    {
        this.pubWs = pubWs;
        this.idMapper = idMapper;
        this.contentWs = contentWs;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.itemWorkflowService = itemWorkflowService;
        this.itemSummaryService = itemSummaryService;
        this.siteDao = siteDao;
        this.pubServerService = pubServerService;
        this.licenseService = licenseService;
        this.itemService = itemService;
        this.cmsObjectMgr = cmsObjectMgr;
        this.contentChangeService = contentChangeService;
        this.listViewHelper = listViewHelper;
        this.pageService = pageService;
        this.workflowHelper = workflowHelper;
        this.sitePublishDao = sitePublishDao;
        this.sitePublishServiceHelper = sitePublishServiceHelper;
    }

    public PSSitePublishResponse publish(String siteName, PubType type, String id, boolean isResource, String server)
            throws PSDataServiceException, IPSPubServerService.PSPubServerServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSItemService.PSItemServiceException {
        type = type == null ? PubType.FULL : type;
        String edtn = publishTypeMap.get(type);
        if (edtn == null)
            throw new UnsupportedOperationException("The request type of publishing: " + edtn + " is not supported. ");
        long jobId = 0;
        String warningMessage = "";

        // Check if publication is allowed to proceed. Otherwise, return a
        // ResponsePublishNotAllowed and don't publish.
        if (!isPublishAllowed())
        {
            return createResponseWontPublish(siteName, State.FORBIDDEN);
        }

        if (!isPublishValid(siteName, server, type))
        {
            return createResponseWontPublish(siteName, State.INVALID);
        }
        boolean isStagingOnDemand = type.equals(PubType.STAGE_NOW) || type.equals(PubType.REMOVE_FROM_STAGING_NOW);
        boolean isProductionOnDemand = type.equals(PubType.PUBLISH_NOW) || type.equals(PubType.TAKEDOWN_NOW);
        //If there is no staging server return with an error
        if(isStagingOnDemand && !isStagingServerAvailable(id))
        {
            return createResponseWontPublish(siteName, State.NOSTAGING_SERVERS);
        }
        // check connection to all pubservers if publish_now on an asset else
        // check connection to the only pubserver
        StringBuilder connectionWarning = new StringBuilder();
        if (!checkConnectionForAllPublishTypes(connectionWarning, siteName, type, id, isResource, server))
        {
            return createResponseWontPublish(siteName, State.BADCONFIG);
        }

        if (isStagingOnDemand || isProductionOnDemand)
        {
            PSPair<Long, String> pair = publishTakedownNow(id, type, isResource, edtn);
            jobId = pair.getFirst();
            warningMessage = pair.getSecond();
        }
        else
        {
            PSPublishingJobStatusCallbackWrapper callBackWrapper = new PSPublishingJobStatusCallbackWrapper();
            callBackWrapper.addCallBack(PSPublishingProcessMonitor.getPublishingJobStatusCallback());
            if (type.equals(PubType.FULL))
                callBackWrapper.addCallBack(new PSPubServicePubJobStatusHandler());
            jobId = pubWs
                    .startPublishingJob(findEditionBySiteServer(siteName, server, type).getGUID(), callBackWrapper);
            PSPublishingProcessMonitor.startPublishingJob(jobId);
        }

        if (!connectionWarning.toString().isEmpty())
        {
            return createResponseBadConfigMultipleSites(jobId, StringUtils.substringBeforeLast(connectionWarning.toString(),","),
                    State.BADCONFIGMULTIPLESITES);
        }

        return createResponseByNotBlocking(siteName, jobId, warningMessage);
    }
    
	@Override
	public PSSitePublishResponse publishIncremental(String siteName, String id,
			boolean isResource, String server) throws PSDataServiceException, IPSPubServerService.PSPubServerServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSItemService.PSItemServiceException {

		PSPublishServerInfo pubServerInfo = findPubServerInfo(siteName, server, true);
        boolean isStaging = PSPubServer.STAGING.equalsIgnoreCase( (pubServerInfo.getServerType()));
		PubType type = isStaging ? PubType.STAGING_INCREMENTAL : PubType.INCREMENTAL;
		return publish(siteName, type, null, false, server);
	}

    @Override
    public PSSitePublishResponse publishIncrementalWithApproval(String siteName, String id, boolean isResource,
                                                                String server,String itemsToApprove) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSPubServerService.PSPubServerServiceException, IPSItemService.PSItemServiceException {

        PSPublishServerInfo pubServerInfo = findPubServerInfo(siteName, server, true);
        String relatedProp = pubServerInfo.findProperty(IPSPubServerDao.PUBLISH_RELATED_PROPERTY);
        boolean isStaging = PSPubServer.STAGING.equalsIgnoreCase( (pubServerInfo.getServerType()));
        if(StringUtils.equalsIgnoreCase(relatedProp, Boolean.TRUE.toString()) && !isStaging){
        	approveRelatedItems(siteName, server,itemsToApprove);
        }
        PubType type = isStaging ? PubType.STAGING_INCREMENTAL : PubType.INCREMENTAL;
        return publish(siteName, type, null, false, server);
    }
	
	private void approveRelatedItems(String siteName, String server,String itemsToApprove) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        JSONArray arr = new JSONArray(itemsToApprove);
        List<Integer> listForApproval = new ArrayList<Integer>();
        for(int i = 0; i < arr.length(); i++){
            String id = (String)arr.get(i);
            Guid guid = new Guid(id);
            if(guid != null)
                listForApproval.add(Integer.valueOf(guid.getUuid()));
        }
        for (Integer contentId : listForApproval) {
            itemWorkflowService.performApproveTransition(idMapper.getGuid(new PSLocator(contentId)).toString(), false, null);
        }
    }
	
    private boolean checkConnectionForAllPublishTypes(StringBuilder connectionWarning, String siteName, PubType type,
            String id, boolean isResource, String server) throws IPSPubServerService.PSPubServerServiceException, PSSitePublishException {
        boolean connection = true;
        IPSSite site = null;
        PSPublishServerInfo pubServerInfo = null;
        if (type.equals(PubType.PUBLISH_NOW) || type.equals(PubType.TAKEDOWN_NOW) || 
        		type.equals(PubType.STAGE_NOW) || type.equals(PubType.REMOVE_FROM_STAGING_NOW))
        {
            if (!isResource)
            {
                List<IPSSite> sites = pubWs.getItemSites(idMapper.getGuid(id));
                if(sites != null && sites.size() >0) {
                    String siteId = sites.get(0).getSiteId().toString();
                    PSPubServer pubServer = type.equals(PubType.STAGE_NOW) || type.equals(PubType.REMOVE_FROM_STAGING_NOW) ?
                            pubServerService.getStagingPubServer(sites.get(0).getGUID()) :
                            pubServerService.getDefaultPubServer(sites.get(0).getGUID());
                    pubServerInfo = pubServerService.getPubServer(siteId, Long.toString(pubServer.getServerId()));
                    site = this.pubWs.findSite(sites.get(0).getName());
                    connection = this.pubServerService.checkPubServerConfig(pubServerInfo, site);
                }
            }
            else
            {
                boolean con = false;
                List<IPSSite> sites = new ArrayList<>();
                List<String> notAllowedServers = new ArrayList<>();
                List<PSPublishServerInfo> pubServerInfos = new ArrayList<>();

                // Calculate the List notAllowedServers and siteNames.
                getPubServerInfos(notAllowedServers, sites, pubServerInfos, type);

                // If any connection passes we continue with publishing
                // if a connection fails then we add a warning to
                // connectionWarning
                for (int i = 0; i < pubServerInfos.size(); i++)
                {
                    if (this.pubServerService.checkPubServerConfig(pubServerInfos.get(i), sites.get(i)))
                    {
                        con = true;
                    }
                    else
                    {
                        connectionWarning.append(" " + sites.get(i).getName() + ",\n");
                    }
                }
                connection = con;
            }
        }
        else
        {
            site = this.pubWs.findSite(siteName);
            pubServerInfo = findPubServerInfo(siteName, server);
            connection = this.pubServerService.checkPubServerConfig(pubServerInfo, site);
        }

        return connection;
    }

    private boolean isPublishValid(String siteName, String server, PubType type) throws IPSPubServerService.PSPubServerServiceException, PSSitePublishException {
        boolean isValid = true;
        if (type.equals(PubType.INCREMENTAL))
        {
            PSPublishServerInfo pubServerInfo = findPubServerInfo(siteName, server);
            boolean canPublish = pubServerInfo.getCanIncrementalPublish();
            isValid = canPublish && !pubServerInfo.getIsFullPublishRequired();
        }

        return isValid;
    }

    private PSPair<Long, String> publishTakedownNow(String id, PubType type, boolean isResource, String edtn) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException, IPSItemService.PSItemServiceException {
        if (isBlank(id))
        {
            throw new PSSitePublishException("Invalid request for "
                    + "demand publishing : item was not supplied with request");
        }

        long jobId = 0;
        String warningMessage = "";

        if(type.equals(PubType.PUBLISH_NOW) || type.equals(PubType.TAKEDOWN_NOW))
        {
	        clearScheduledDate(id, type);
	        transitionIfNeeded(id, type);
        }
        else if(type.equals(PubType.STAGE_NOW) || type.equals(PubType.REMOVE_FROM_STAGING_NOW))
        {
        	itemWorkflowService.checkIn(id);
        }

        // build the demand work item with requested item
        PSDemandWork work = new PSDemandWork();
        addWorkItem(work, id);

        // add resources to demand work item
        Set<String> resources = widgetAssetRelationshipService.getResourceAssets(id);
        for (String resource : resources)
        {
            addWorkItem(work, resource);
        }

        List<String> siteNames = new ArrayList<>();

        if (!isResource)
        {
            List<IPSSite> sites = pubWs.getItemSites(idMapper.getGuid(id));
            if (!sites.isEmpty())
            {
                // this is a page, publish to parent site
                siteNames.add(sites.get(0).getName());
            }
        }
        else
        {
            List<String> notAllowedServers = new ArrayList<>();

            // Calculate the List notAllowedServers and siteNames.
            getNotAllowedOnDemandServers(notAllowedServers, siteNames, type);

            // Generate a warning message for the user if exist at least one not
            // allowed server to publish.
            warningMessage = getNotAllowedWarningMessage(notAllowedServers);
        }

        // invoke publish for each site
        for (String sName : siteNames)
        {
            IPSGuid editionId = getOndemandEdition(sName, edtn, type);

            pubWs.queueDemandWork(editionId.getUUID(), work);
                        
            // do not wait for status.  We do not need to know the job id for publish now
            // be careful if adding multiple sites there may be more than one jobId
        }
        return new PSPair<>(jobId, warningMessage);
    }

    private void clearScheduledDate(String id, PubType type) throws IPSItemService.PSItemServiceException {
        PSItemDates dates = itemService.getItemDates(id);

        int cid = idMapper.getGuid(id).getUUID();
        List<Integer> cids = Arrays.asList(cid);

        if (type == PubType.PUBLISH_NOW)
        {
            if (isBlank(dates.getStartDate()))
                return;

            cmsObjectMgr.clearStartDate(cids);
        }
        else
        {
            if (isBlank(dates.getEndDate()))
                return;

            cmsObjectMgr.clearExpiryDate(cids);
        }
    }

    private void transitionIfNeeded(String id, PubType type) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        String trigger = (type == PubType.PUBLISH_NOW)
                ? IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE
                : IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE;

        // before the item is published/taken down, it must be in the
        // pending/archive state
        if (itemWorkflowService.isTriggerAvailable(id, trigger))
        {
            if (trigger.equalsIgnoreCase(IPSItemWorkflowService.TRANSITION_TRIGGER_APPROVE))
                itemWorkflowService.performApproveTransition(id, false, null);
            else if (trigger.equalsIgnoreCase(IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE))
                itemWorkflowService.transition(id, IPSItemWorkflowService.TRANSITION_TRIGGER_ARCHIVE);
        }
    }

    /**
     * Update both list received with allowed and not allowed sites to publish
     * 
     * @param notAllowedServers sites that are not allowed for publishing
     *            resources (sites with database or XML default server)
     * @param siteNames Sites allowed to publish.
     */
    private void getNotAllowedOnDemandServers(List<String> notAllowedServers, List<String> siteNames, PubType type)
    {
        for (PSSiteSummary site : siteDao.findAllSummaries())
        {
            PSGuid siteGuid = new PSGuid(PSTypeEnum.SITE, site.getSiteId());
            PSPubServer pubServer = type.equals(PubType.STAGE_NOW) || type.equals(PubType.REMOVE_FROM_STAGING_NOW) ? 
            		pubServerService.getStagingPubServer(siteGuid) : pubServerService.getDefaultPubServer(siteGuid);
            // Don't publish on database or XML servers.
            if(pubServer == null)
            {
                notAllowedServers.add(site.getName());
            }
            else if (pubServer.isDatabaseType() || pubServer.isXmlFormat())
            {
                notAllowedServers.add(site.getName());
            }
            else
            {
                siteNames.add(site.getName());
            }
        }
    }

    
    /**
     * adds info to the list of siteNames, List of non-publish-Now Servers, and
     * list of pubserverinfos
     * 
     * @param notAllowedServers
     * @param sites
     * @param pubInfos
     */
    private void getPubServerInfos(List<String> notAllowedServers, List<IPSSite> sites,
            List<PSPublishServerInfo> pubInfos, PubType type) throws IPSPubServerService.PSPubServerServiceException {
        for (PSSiteSummary site : siteDao.findAllSummaries())
        {
            PSGuid siteGuid = new PSGuid(PSTypeEnum.SITE, site.getSiteId());
            PSPubServer pubServer = type.equals(PubType.STAGE_NOW) || type.equals(PubType.REMOVE_FROM_STAGING_NOW)?
            		pubServerService.getStagingPubServer(siteGuid):
            		pubServerService.getDefaultPubServer(siteGuid);
            if(pubServer == null)
            {
            	continue;
            }
            // Don't publish on database or XML servers.
            if (pubServer.isDatabaseType() || pubServer.isXmlFormat())
            {
                notAllowedServers.add(site.getName());
            }
            else
            {
                pubInfos.add(pubServerService.getPubServer(site.getSiteId().toString(),
                        Long.toString(pubServer.getServerId())));
                sites.add(this.pubWs.findSite(site.getName()));
            }
        }
    }

    /**
     * Generate a warning message indicating what server are not allowed to
     * publish a resource.
     * 
     * @param notAllowedServers sites that are not allowed for publishing
     *            resources (sites with database or XML default server)
     * @return
     */
    private String getNotAllowedWarningMessage(List<String> notAllowedServers)
    {
        String warningMessage = "";
        if (notAllowedServers.size() > 0)
        {
            String sites = "";
            int msSize = notAllowedServers.size();
            for (int i = 0; i < msSize; i++)
            {
                String sName = notAllowedServers.get(i);
                String sep = "";
                if (msSize > 1)
                {
                    if (i == (msSize - 1))
                    {
                        sep = " and ";
                    }
                    else if (sites != "")
                    {
                        sep = ", ";
                    }
                }
                sites += sep + "\"" + sName + "\"";
            }
            warningMessage = "The default publishing settings do not allow publishing or removing this item for "
                    + sites + ((msSize > 1) ? " sites." : " site.");
        }
        return warningMessage;
    }

    /**
     * Get the specified on demand edition. The edition will be created if it
     * does not exist.
     * 
     * @param siteName the name of the site, assumed not blank.
     * @param suffix the suffix of the edition name, assumed not blank.
     * @param type the type of the on demand edition, assumed not
     *            <code>null</code>.
     * 
     * @return the existing or created edition, never <code>null</code>.
     */
    private IPSGuid getOndemandEdition(String siteName, String suffix, PubType type) throws PSSitePublishException {
        IPSGuid editionId;

        editionId = findEditionBySite(siteName, suffix).getGUID();
        return editionId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.sitemanage.service.IPSSitePublishService#getPublishingActions
     * (java.lang.String)
     */
    public List<PSPublishingAction> getPublishingActions(String id) throws PSDataServiceException
    {
        List<PSPublishingAction> pubActions = new ArrayList<>();
        IPSItemSummary sum = itemSummaryService.find(id);

        if ((sum.isPage() || sum.isResource()))
        {
            PSPublishingAction tdActionPublishProps = new PSPublishingAction(
                    PSPublishingAction.PUBLISHING_ACTION_PUBLISH, true);

            if (itemWorkflowService.isTriggerAvailable(id, IPSItemWorkflowService.TRANSITION_TRIGGER_PUBLISH))
            {
                tdActionPublishProps.setEnabled(true);
            }

            pubActions.add(tdActionPublishProps);

            pubActions.add(new PSPublishingAction(PSPublishingAction.PUBLISHING_ACTION_SCHEDULE, true));

            PSPublishingAction tdActionProps = new PSPublishingAction(PSPublishingAction.PUBLISHING_ACTION_TAKEDOWN,
                    false);

            if (itemWorkflowService.isTriggerAvailable(id, IPSItemWorkflowService.TRANSITION_TRIGGER_REMOVE))
            {
                tdActionProps.setEnabled(true);
            }
            
            pubActions.add(tdActionProps);
            
            //Add staging actions make sure staging server exists before we add staging actions
            if(isStagingServerAvailable(sum))
            {
                PSPublishingAction stagingActionProps = new PSPublishingAction(PSPublishingAction.PUBLISHING_ACTION_STAGE,
                        false);
    
                if (itemWorkflowService.isStagingOptionAvailable(id))
                {
                    stagingActionProps.setEnabled(true);
                }
                
                pubActions.add(stagingActionProps);
    
    
                PSPublishingAction rfStagingActionProps = new PSPublishingAction(PSPublishingAction.PUBLISHING_ACTION_REMOVE_FROM_STAGING,
                        false);
    
                if (itemWorkflowService.isRemoveFromStagingOptionAvailable(id))
                {
                    rfStagingActionProps.setEnabled(true);
                }
                
                pubActions.add(rfStagingActionProps);
            }
            
        }
        return pubActions;
    }

    /**
     * Convenient method that loads item summary and {@link #isStagingServerAvailable(IPSItemSummary)}
     * to determine staging server is available or not.
     * @param id item id assumed not <code>null</code>
     * @return <code>true</code> if at least one staging server is available otherwise <code>false</code>
     */
    private boolean isStagingServerAvailable(String id) throws PSDataServiceException {
        IPSItemSummary sum = itemSummaryService.find(id);
        return isStagingServerAvailable(sum);
    }
    
    /**
     * Checks whether staging server is available for the item, if the sum corresponds to a resource
     * then checks whether a staging server available for all the sites in the system, if it
     * corresponds to a page then checks the page's site only (found by item sites of 0).
     * If the supplied summary doesn't correspond to a page or resource then returns false.
     * @param sum assumed not <code>null</code>
     * @return <code>true</code> if at least one staging server is available otherwise <code>false</code>
     */
    private boolean isStagingServerAvailable(IPSItemSummary sum)
    {
        if (!(sum.isPage() || sum.isResource()))
        {
            return false;
        }        
        boolean result = false;
        if(sum.isPage())
        {
        	List<IPSSite> sites = pubWs.getItemSites(idMapper.getGuid(sum.getId()));
            if(pubServerService.getStagingPubServer(sites.get(0).getGUID()) != null)
            {
                result = true;
            }
        }
        else if(sum.isResource())
        {
        	List<PSSiteSummary> sites = siteDao.findAllSummaries();
            for (PSSiteSummary siteSum : sites) 
            {
                PSGuid siteGuid = new PSGuid(PSTypeEnum.SITE, siteSum.getSiteId());
                if(pubServerService.getStagingPubServer(siteGuid) != null)
                {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }
    /**
     * Finds the specified edition for the specified site.
     * 
     * @param siteName the name of the site, assumed not blank.
     * @param suffix the suffix of the edition name, assumed not blank.
     * 
     * @return the specified edition, never <code>null</code>.
     */
    private IPSEdition findEditionBySite(String siteName, String suffix) throws PSSitePublishException {
        IPSSite site = pubWs.findSite(siteName);
        List<IPSEdition> editions = pubWs.findAllEditionsBySite(site.getGUID());
        boolean staging = suffix.contains("STAGING");
        for (IPSEdition edition : editions)
        {
            //  Hack to fix issue that looking for suffix without staging can also find staging editions
            if (edition.getName().endsWith("_"+suffix)
                    && (staging || !edition.getName().endsWith("_STAGING_" + suffix)))
            {
                return edition;
            }
        }

        throw new PSSitePublishException("Cannot find edition with type \"" + suffix + "\" in site \"" + siteName
                + "\".");
    }

    /**
     * Finds the FULL edition for the specified site and server
     * 
     * @param siteName the name of the site, assumed not blank.
     * @param serverName name of server
     * @param pubType the suffix of the edition name, assumed not blank.
     * 
     * @return the specified edition, never <code>null</code>.
     */
    private IPSEdition findEditionBySiteServer(String siteName, String serverName, PubType pubType) throws PSSitePublishException, IPSPubServerService.PSPubServerServiceException {
        long serverId = findPubServerInfo(siteName, serverName).getServerId();

        IPSEdition edition = sitePublishDao.findEdition(PSGuidUtils.makeGuid(serverId, PSTypeEnum.PUBLISHING_SERVER),
                pubType);

        if (edition == null)
        {
            throw new PSSitePublishException("Cannot find edition for server \"" + serverName + "\" in site \""
                    + siteName + "\".");
        }

        return edition;
    }

    private PSPublishServerInfo findPubServerInfo(String siteName, String serverName) throws IPSPubServerService.PSPubServerServiceException, PSSitePublishException {
        return findPubServerInfo(siteName, serverName, false);
    }

    private PSPublishServerInfo findPubServerInfo(String siteName, String serverName, boolean loadProperties) throws IPSPubServerService.PSPubServerServiceException, PSSitePublishException {
        IPSSite site = pubWs.findSite(siteName);
        List<PSPublishServerInfo> servers = pubServerService.getPubServerList(site.getSiteId().toString());

        PSPublishServerInfo pubServerInfo = null;
        for (PSPublishServerInfo server : servers)
        {
            if (server.getServerName().equalsIgnoreCase(serverName))
            {
                pubServerInfo = server;
                break;
            }
        }
        if (pubServerInfo == null)
        {
            log.error("Failed to find a server with name \"" + serverName + "\" in site \"" + siteName + "\".");
            throw new PSSitePublishException("Cannot find server with name \"" + serverName + "\" in site \""
                    + siteName + "\".");
        }
        if (loadProperties) {
        	pubServerInfo = pubServerService.getPubServer(site.getSiteId().toString(), pubServerInfo.getServerId().toString());
        }
        return pubServerInfo;
    }

    private PSSitePublishResponse createResponseByNotBlocking(String siteName, long jobId, String warningMessage)
    {
        sleep();
        IPSPublisherJobStatus jobStatus = null;
        if (jobId != 0)
        {
            jobStatus = pubWs.getPublishingJobStatus(jobId);
        }
        PSSitePublishResponse response = createResponse(siteName, jobStatus, jobId);
        response.setWarningMessage(warningMessage);

        return response;
    }

    private PSSitePublishResponse createResponse(String siteName, IPSPublisherJobStatus jobStatus, long jobId)
    {
        PSSitePublishResponse response = new PSSitePublishResponse();
        if (jobStatus != null)
        {
            response.setDelivered(String.valueOf(jobStatus.countItemsDelivered()));
            response.setFailures(String.valueOf(jobStatus.countFailedItems()));
            response.setStatus(jobStatus.getState().getDisplayName());
        }
        response.setSiteName(siteName);
        response.setJobid(jobId);
        return response;
    }

    @SuppressWarnings("unused")
    private PSSitePublishResponse createResponseByBlocking(String siteName, long jobId)
    {
        IPSPublisherJobStatus jobStatus;
        State jobState;
        do
        {
            jobStatus = pubWs.getPublishingJobStatus(jobId);
            jobState = jobStatus.getState();
            sleep();
        }
        while (jobState != State.COMPLETED && jobState != State.COMPLETED_W_FAILURE);

        PSSitePublishResponse response = createResponse(siteName, jobStatus, jobId);

        return response;
    }

    private PSSitePublishResponse createResponseBadConfigMultipleSites(long jobId, String connectionWarning, State state)
    {
        PSSitePublishResponse response = new PSSitePublishResponse();
        response.setJobid(jobId);
        response.setStatus(state.toString());
        response.setWarningMessage(connectionWarning);
        if (jobId != 0)
        {
            IPSPublisherJobStatus jobStatus = pubWs.getPublishingJobStatus(jobId);
            response.setDelivered(String.valueOf(jobStatus.countItemsDelivered()));
            response.setFailures(String.valueOf(jobStatus.countFailedItems()));
        }
        return response;
    }

    /**
     * Creates a response with the supplied status when publishing wont proceed.
     * Since publication is stopped, there are 0 delivered and 0 failures.
     */
    private PSSitePublishResponse createResponseWontPublish(String siteName, State state)
    {
        PSSitePublishResponse response = new PSSitePublishResponse();
        response.setDelivered("0");
        response.setFailures("0");
        response.setStatus(state.toString());
        response.setSiteName(siteName);
        response.setWarningMessage(state.getDisplayName());
        return response;
    }

    private void sleep()
    {
        try
        {
            Thread.sleep(300);
        }
        catch (InterruptedException e)
        {
        }
    }

    private void addWorkItem(PSDemandWork work, String id)
    {
        IPSGuid contentg = idMapper.getGuid(idMapper.getLocator(id));
        List<PSItemSummary> folders = contentWs.findFolderParents(contentg, false);
        work.addItem(folders.get(0).getGUID(), contentg);
    }

    /**
     * Checks if the publishing job is allowed to run. Currently only checks if
     * license status is Active. If the license is Inactive or suspended,
     * publication will not be allowed.
     * 
     * @return true if Publish Job is allowed to run, false otherwise.
     */
    private boolean isPublishAllowed()
    {
       return true;
    }

    public PSPagedItemList getQueuedIncrementalContent(String siteName, String serverName, int startIndex, int pageSize)
            throws PSSitePublishException
    {
        Validate.notEmpty(siteName);
        Validate.notEmpty(serverName);

        try
        {
            List<Integer> changedContent = getChangedContentIds(siteName, serverName);
            return getPagedItems(startIndex, pageSize, changedContent);
        }
        catch (Exception e)
        {
            throw new PSSitePublishException(e.getLocalizedMessage(), e);
        }
    }

    private PSPagedItemList getPagedItems(int startIndex, int pageSize, List<Integer> changedContent) throws PSDataServiceException {
        List<IPSItemEntry> allItemEntries = cmsObjectMgr.findItemEntries(changedContent, new CompareItemEntry());

        int realStartIndex = startIndex < 1 ? 1 : startIndex;
        Integer maxItems = pageSize < 1 ? null : pageSize;
        PSPagedObjectList<IPSItemEntry> pageGroup = PSPagedObjectList.getPage(allItemEntries, realStartIndex,
                maxItems);

        // Convert items to PSPathItem objects
        List<IPSItemEntry> pagedItemEntries = pageGroup.getChildrenInPage();
        Integer resultingStartIndex = pageGroup.getStartIndex();

        // Get page of Path Items
        List<PSPathItem> itemsInPage = new ArrayList<PSPathItem>();
        for (IPSItemEntry pageEntry : pagedItemEntries)
        {
            // Get path for each page entry.
            IPSGuid myGuid = PSGuidUtils
                    .makeGuid(Long.valueOf(pageEntry.getContentId()), PSTypeEnum.LEGACY_CONTENT);
            String id = myGuid.toString();
            String[] pagePaths = contentWs.findFolderPaths(myGuid);
            String path = "";
            if (pagePaths != null && pagePaths.length > 0)
            {
                path = PSPathUtils.getFinderPath(pagePaths[0] + "/" + pageEntry.getName());
            }

            // Create path item and set name and path to it.
            IPSItemSummary sum = itemSummaryService.find(myGuid.toString());
            PSPathItem pathItem = new PSPathItem();
            PSItemSummaryUtils.copyProperties(sum, pathItem);
            
			try {
                if (workflowHelper.isPage(id)) {
                    PSPage page = pageService.find(id);
                    pathItem.setName(page.getLinkTitle());
                } else {
                    pathItem.setName(sum.getName());
                }
            }catch (PSNotFoundException e) {
                    log.error("Error setting path for item in incremental queue with id: " + id, e);
                    continue;
			}

            pathItem.setPath(path);
            pathItem.setRelatedObject(pageEntry);
            pathItem.setId(myGuid.toString());

            itemsInPage.add(pathItem);
        }

        PSDisplayPropertiesCriteria criteria = new PSDisplayPropertiesCriteria(itemsInPage, null);
        criteria.setDisplayFormatRequired(false);
        listViewHelper.fillDisplayProperties(criteria);

        // Create Paged Item List
        PSPagedItemList pagesByTemplatePagedList = new PSPagedItemList(itemsInPage, allItemEntries.size(),
                resultingStartIndex);
        return pagesByTemplatePagedList;
    }

    private List<Integer> getChangedContentIds(String siteName, String serverName) throws IPSPubServerService.PSPubServerServiceException, PSSitePublishException {
        IPSSite site = pubWs.findSite(siteName);
        
        PSContentChangeType changeType = PSContentChangeType.PENDING_LIVE;
        PSPublishServerInfo pubServerInfo = findPubServerInfo(siteName, serverName);
        if(PSPubServer.STAGING.equalsIgnoreCase( pubServerInfo.getServerType()))
        {
            changeType = PSContentChangeType.PENDING_STAGED;
        }
        List<Integer> changedContent = contentChangeService.getChangedContent(site.getSiteId(),
                changeType);
        return changedContent;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.sitemanage.service.IPSSitePublishService#getQueuedIncrementalRelatedContent(java.lang.String, java.lang.String, int, int)
     */
    public PSPagedItemList getQueuedIncrementalRelatedContent(String siteName, String serverName, int startIndex, int pageSize)
            throws PSSitePublishException
    {
        Validate.notEmpty(siteName);
        Validate.notEmpty(serverName);

        try
        {
            List<Integer> changedContent = getChangedContentIds(siteName, serverName);
            Set<Integer> contentIds = new HashSet<Integer>(changedContent);
            Collection<Integer> relatedContent = sitePublishServiceHelper.findRelatedItemIds(contentIds);
            return getPagedItems(startIndex, pageSize, new ArrayList<Integer>(relatedContent));
        }
        catch (Exception e)
        {
            throw new PSSitePublishException(e.getLocalizedMessage(), e);
        }
    }
    
    /**
     * The publishing ws. Initialized in ctor, never <code>null</code> after
     * that.
     */
    private IPSPublishingWs pubWs;

    /**
     * The id mapper. Initialized by constructor, never <code>null</code> after
     * that.
     */
    private IPSIdMapper idMapper;

    /**
     * Used for asset relationship operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;

    /**
     * Used for content item operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    private IPSContentWs contentWs;

    /**
     * Used for item workflow operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    private IPSItemWorkflowService itemWorkflowService;

    /**
     * Used for item find operations. Initialized in ctor, never
     * <code>null</code> after that.
     */
    private IPSDataItemSummaryService itemSummaryService;

    /**
     * Used for site operations. Initialized in ctor, never <code>null</code>
     * after that.
     */
    private IPSiteDao siteDao;

    /**
     * Used for getting the servers info.
     */
    private IPSPubServerService pubServerService;

    private PSSitePublishDao sitePublishDao;

    /**
     * Used for checking license status before publishing.
     */
    private PSLicenseService licenseService;

    private IPSItemService itemService;

    private IPSCmsObjectMgr cmsObjectMgr;

    private IPSContentChangeService contentChangeService;

    private IPSListViewHelper listViewHelper;

    private IPSPageService pageService;

    private IPSWorkflowHelper workflowHelper;

    private IPSSitePublishServiceHelper sitePublishServiceHelper;
    /**
     * Maps publishing type to associated edition.
     */
    private static Map<PubType, String> publishTypeMap = new HashMap<>();

    static
    {
        publishTypeMap.put(PubType.FULL, PSSitePublishDao.FULL);
        publishTypeMap.put(PubType.INCREMENTAL, PSSitePublishDaoHelper.INCREMENTAL);
        publishTypeMap.put(PubType.STAGING_INCREMENTAL, PSSitePublishDaoHelper.STAGING_INCREMENTAL);
        publishTypeMap.put(PubType.PUBLISH_NOW, PSSitePublishDaoHelper.PUBLISH_NOW);
        publishTypeMap.put(PubType.TAKEDOWN_NOW, PSSitePublishDaoHelper.UNPUBLISH_NOW);
        publishTypeMap.put(PubType.STAGE_NOW, PSSitePublishDaoHelper.STAGING_PUBLISH_NOW);
        publishTypeMap.put(PubType.REMOVE_FROM_STAGING_NOW, PSSitePublishDaoHelper.STAGING_UNPUBLISH_NOW);
    }

    public static final Logger log = LogManager.getLogger(PSSitePublishService.class);

}
