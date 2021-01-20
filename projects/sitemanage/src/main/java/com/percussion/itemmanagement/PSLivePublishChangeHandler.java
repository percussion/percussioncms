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
package com.percussion.itemmanagement;

import com.google.common.primitives.Longs;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.PSEditorChangeEvent;
import com.percussion.cms.PSRelationshipChangeEvent;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.design.objectstore.PSRelationshipSet;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.itemmanagement.service.IPSWorkflowHelper.PSItemTypeEnum;
import com.percussion.pagemanagement.service.IPSPageTemplateService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentchange.IPSContentChangeHandler;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.data.PSContentChangeEvent;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.impl.PSSitePublishDaoHelper;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author JaySeletz
 *
 */
@Component("livePublishChangeHandler")
public class PSLivePublishChangeHandler implements IPSContentChangeHandler
{
    private static final Log log = LogFactory.getLog(PSLivePublishChangeHandler.class);
    
    private IPSContentChangeService changeSvc;
    private IPSWorkflowHelper wfHelper;
    private IPSIdMapper idMapper;
    private IPSFolderHelper folderHelper;
    private IPSSiteDataService siteDataService;
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    private IPSPageTemplateService pageTemplateService;
    private IPSResourceDefinitionService resourceDefinitionService;
    
    @Autowired
    public PSLivePublishChangeHandler(IPSContentChangeService changeSvc, IPSWorkflowHelper wfHelper, IPSIdMapper idMapper, 
            IPSFolderHelper folderHelper, IPSSiteDataService siteDataService, IPSWidgetAssetRelationshipService widgetAssetRelationshipService,
            IPSPageTemplateService pageTemplateService, IPSResourceDefinitionService resourceDefinitionService)
    {
        changeSvc.addContentChangeHander(this);
        this.changeSvc = changeSvc;
        this.wfHelper = wfHelper;
        this.idMapper = idMapper;
        this.folderHelper = folderHelper;
        this.siteDataService = siteDataService;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.pageTemplateService = pageTemplateService;
        this.resourceDefinitionService = resourceDefinitionService;
    }
    
    @Override
    public void handleEvent(PSEditorChangeEvent e)
    {
        switch (e.getActionType())
        {
           case PSEditorChangeEvent.ACTION_TRANSITION:
               handleTransition(e);
               break;
           case PSEditorChangeEvent.ACTION_DELETE:
               handleDelete(e);
               break;
           case PSEditorChangeEvent.ACTION_UPDATE:
               handleUpdate(e);
        }
    }

    
    @Override
    public void handleEvent(PSRelationshipChangeEvent e)
    {
        // handle delete for a shared asset, check if owner is a Live page
        PSRelationshipSet rels = e.getRelationships();
        Iterator<PSRelationship> iter = rels.iterator();
        String id="<not set>";
        while(iter.hasNext())
        {
            try {
                PSRelationship rel = iter.next();
                PSLocator owner = rel.getOwner();
                id = idMapper.getString(owner);
                if (isPublishedPage(id)) {
                    doAdd(id, getPageSiteId(id));
                } else if (isStagingItem(id)) {
                    doAdd(id, PSContentChangeType.PENDING_STAGED, getPageSiteId(id));
                } else if (wfHelper.isTemplate(id)) {
                    handleTemplateChange(id);
                }
            }catch(Exception ex){
                log.debug("Skipping content change event for item id:" + id + " most likely item was deleted. Root exception follows: ", ex);
            }
        }        
    }

    /**
     * @param e
     */
    private void handleTransition(PSEditorChangeEvent e)
    {
        IPSGuid guid = idMapper.getGuid(new PSLocator(e.getContentId()));
        String id = guid.toString();
        long contentTypeId = e.getContentTypeId();
        PSItemTypeEnum itemType = wfHelper.getItemTypeFromCType(contentTypeId);
        if (!(itemType.equals(PSItemTypeEnum.PAGE) || itemType.equals(PSItemTypeEnum.ASSET)))
            return;
        
        if (wfHelper.isArchived(id))
        {
            handleRemove(id, itemType, contentTypeId);
        }
        else
        {
            if (isPending(id))
            {
                // if not scheduled, add.  If scheduled, remove queued entry if any
                if (wfHelper.getComponentSummary(id).getContentStartDate() == null)
                    handleAdd(id, itemType, PSContentChangeType.PENDING_LIVE, contentTypeId);
                else
                    doRemove(id, PSContentChangeType.PENDING_LIVE);
            }
            //Add or remove from staging
            if(isStagingItem(id))
            {
                 handleAdd(id, itemType, PSContentChangeType.PENDING_STAGED, contentTypeId);
            }
            else
            {
                doRemove(id, PSContentChangeType.PENDING_STAGED);
            }
        }
    }
    

    private void handleDelete(PSEditorChangeEvent e)
    {
        IPSGuid guid = idMapper.getGuid(new PSLocator(e.getContentId()));
        String id = guid.toString();
        long contentTypeId = e.getContentTypeId();
        PSItemTypeEnum itemType = wfHelper.getItemTypeFromCType(contentTypeId);
        if (!(itemType.equals(PSItemTypeEnum.PAGE) || itemType.equals(PSItemTypeEnum.ASSET)))
            return;
        
        if (itemType.equals(PSItemTypeEnum.PAGE))
            doRemove(id);
        else
        {
            if (isResource(contentTypeId))
                doRemove(id);
        }
    }

    /**
     * @param e
     */
    private void handleUpdate(PSEditorChangeEvent e)
    {
        IPSGuid guid = idMapper.getGuid(new PSLocator(e.getContentId()));
        String id = guid.toString();
        if (wfHelper.isLocalAsset(id))
        {
            Set<String> owners = widgetAssetRelationshipService.getRelationshipOwners(id);
            for (String owner : owners)
            {
                if (wfHelper.isTemplate(owner))
                {
                    handleTemplateChange(owner);
                }
                else if(wfHelper.isPage(owner) && isStagingItem(owner))
                {
                    doAdd(owner, PSContentChangeType.PENDING_STAGED, getPageSiteId(owner));
                }
            }
        }
        else if(wfHelper.isPage(id) && isStagingItem(id))
        {
            doAdd(id, PSContentChangeType.PENDING_STAGED, getPageSiteId(id));
        }
    }


    private void handleAdd(String id, PSItemTypeEnum itemType, PSContentChangeType changeType, long contentTypeId)
    {
        if (itemType.equals(PSItemTypeEnum.PAGE))
        {
            // page approved
            doAdd(id, changeType, getPageSiteId(id));
        }
        else
        {
            if (isResource(contentTypeId))
            {
               doAdd(id, changeType, getAssetSiteIds(id));
            }
            
            handleAssetChangeForPages(id);
        }
    }


    private void handleRemove(String id, PSItemTypeEnum itemType, long contentTypeId)
    {
        if (itemType.equals(PSItemTypeEnum.PAGE))
        {
            doRemove(id);
        }
        else
        {
            if (isResource(contentTypeId))
                doRemove(id);
            
            handleAssetChangeForPages(id);
        }
    }
    

    /**
     * @param assetId
     */
    private void handleAssetChangeForPages(String assetId)
    {
        // shared asset is approved (all live pages, all templates using it-their pages)
        Set<String> owners = widgetAssetRelationshipService.getRelationshipOwners(assetId);
        Set<String> pageOwners = new HashSet<String>();
        Set<String> pageOwnersStaging = new HashSet<String>();
        Set<String> templateOwners = new HashSet<String>();
        for (String owner : owners)
        {

            if (isPublishedPage(owner))
            {
                pageOwners.add(owner);
            }
            else if(isStagingItem(owner))
            {
            	pageOwnersStaging.add(owner);
            }
            else if (wfHelper.isTemplate(owner))
            {
                templateOwners.add(owner);
            }
        }
        
        // process page owners first, then templates as a page may be added and subsequently removed by a template change
        for (String owner : pageOwners)
        {
        	doAdd(owner, getPageSiteId(owner));
        }
        for(String owner : pageOwnersStaging)
        {
            doAdd(owner, PSContentChangeType.PENDING_STAGED, getPageSiteId(owner));
        }
        for (String owner : templateOwners)
        {
            handleTemplateChange(owner);
        }
    }
    
    private void handleTemplateChange(String templateId)
    {
        // Find all live pages using the template, need to get non-revision specific id
        templateId = idMapper.getString(new PSLocator(idMapper.getLocator(templateId).getId()));
        List<Integer> pageIds = pageTemplateService.findPageIdsByTemplate(templateId);
        for (Integer id : pageIds)
        {
            String pageId = idMapper.getString(new PSLocator(id));
            if (isPublishedPage(pageId))
            {
                doAdd(pageId, getPageSiteId(pageId));
            }
            else if(wfHelper.isItemInStagingState(id))
            {
                doAdd(pageId, PSContentChangeType.PENDING_STAGED, getPageSiteId(pageId));
            }
        }
    }

    private void doAdd(String id, long... siteIds)
    {
    	doAdd(id, PSContentChangeType.PENDING_LIVE, siteIds);
    	doAdd(id, PSContentChangeType.PENDING_STAGED, siteIds);
    }
    private void doAdd(String id, PSContentChangeType changeType, long... siteIds)
    {
        for (long siteId : siteIds)
        {
            if (siteId == -1L)
            {
                return;
            }
            
            PSContentChangeEvent changeEvent = new PSContentChangeEvent();
            changeEvent.setChangeType(changeType);
            changeEvent.setContentId(idMapper.getContentId(id));
            changeEvent.setSiteId(siteId);

            changeSvc.contentChanged(changeEvent);
        }
        
    }
    
    private void doRemove(String id)
    {
        doRemove(id, PSContentChangeType.PENDING_LIVE);
        doRemove(id, PSContentChangeType.PENDING_STAGED);
    }

    private void doRemove(String id, PSContentChangeType changeType)
    {
        long siteId = getPageSiteId(id);

        changeSvc.deleteChangeEvents(siteId, idMapper.getContentId(id), changeType);

    }

    private boolean isPublishedPage(String id)
    {
        return wfHelper.isPage(id) && wfHelper.isLive(id);
    }

    private boolean isStagingItem(String id)
    {
    	return (wfHelper.isPage(id) || wfHelper.isAsset(id)) && isStagingItem(idMapper.getContentId(id));
    }
    
    private boolean isStagingItem(int contentId)
    {
    	return wfHelper.isItemInStagingState(contentId);
    }
    
    private boolean isPending(String id)
    {
        return wfHelper.isPending(id);
    }
    
    /**
     * Get the site id for a page
     * 
     * @param id an item id
     * 
     * @return the site id, or -1 if the item is not found in a site folder (or is an asset)
     */
    private long getPageSiteId(String id)
    {
        List<IPSSite> sites = folderHelper.getItemSites(id);
        if (sites.isEmpty())
        {
            return -1L;
        }
        return sites.get(0).getSiteId().longValue();
    }

    private long[] getAssetSiteIds(String assetId)
    {
        List<Long> siteList = new ArrayList<Long>();
        String allowedSites = folderHelper.getRootLevelFolderAllowedSitesPropertyValue(assetId);
        if (!StringUtils.isBlank(allowedSites))
        {
            String[] allowedIds = allowedSites.split(",");
            
            for (String id : allowedIds)
            {
                try
                {
                    IPSGuid guid = PSGuidUtils.makeGuid(id, PSTypeEnum.SITE);
                    try{
                        PSSiteSummary sum = siteDataService.findByLegacySiteId(guid.toString(),false);
                        log.debug("Found site with id "+guid.toString());
                    }
                    catch(DataServiceLoadException de){
                        //Ignore if the site doesn't exist, we don't update folder properties on site delete.
                        log.debug("Folder property has a site with ID " + id + " but it doesn't exist in the system and ignored.");
                        continue;
                    }
                    siteList.add(Long.valueOf(id));
                }
                catch (NumberFormatException e)
                {
                    // ignore it
                }                
            }
        }
        else
        {
            List<PSSiteSummary> sums = siteDataService.findAll();
            for (PSSiteSummary sum : sums)
            {
                siteList.add(sum.getSiteId());
            }
        }
        
        // remove any sites that do xml or db pub for incremental
        List<Long> results = new ArrayList<Long>();
        for (Long siteId : siteList)
        {
            if (canPublishAssetIncremental(siteId))
            {
                results.add(siteId);
            }
        }
        
        return Longs.toArray(results);
    }
    
    private boolean canPublishAssetIncremental(Long siteId)
    {
        PSPubServer pubServer = PSSitePublishDaoHelper.getDefaultPubServer(PSGuidUtils.makeGuid(siteId, PSTypeEnum.SITE));
        if (pubServer.isXmlFormat() || pubServer.isDatabaseType())
            return false;
        
        return true;
    }

    private boolean isResource(long contentTypeId)
    {
        boolean isResource = false;
        try
        {
            isResource = !resourceDefinitionService.findAssetResourcesForType(PSItemDefManager.getInstance().contentTypeIdToName(contentTypeId)).isEmpty();
        }
        catch (PSInvalidContentTypeException e)
        {
            log.error("Failed to determine if change involves a resource: " + e.getLocalizedMessage());        
        }
        
        return isResource;
    }
}
