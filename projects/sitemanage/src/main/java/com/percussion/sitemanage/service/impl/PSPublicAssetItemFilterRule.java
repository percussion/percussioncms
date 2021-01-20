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

import com.percussion.extension.IPSExtensionDef;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension;
import com.percussion.itemmanagement.service.impl.PSAbstractWorkflowExtension.WorkflowItem.AssetType;
import com.percussion.pubserver.IPSPubServerService;
import com.percussion.recycle.service.IPSRecycleService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.contentchange.IPSContentChangeService;
import com.percussion.services.contentchange.data.PSContentChangeType;
import com.percussion.services.filter.IPSFilterItem;
import com.percussion.services.filter.IPSItemFilterRule;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSEdition;
import com.percussion.services.publisher.IPSPublisherService;
import com.percussion.services.pubserver.data.PSPubServer;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.system.data.PSContentStatusHistory;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.service.IPSSitePublishService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.system.IPSSystemWs;
import com.percussion.webservices.system.PSSystemWsLocator;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This filter is used for publish and unpublish items.
 * The default behavior is filtering items publishing.
 * It will remove assets that are not in the correct workflow states
 * (public workflow states) and will correct the revisions of non-local assets.
 * The revisions of local assets will not be altered as these always come from
 * slot finders which return the correct revision that we want.
 * <p>
 * However, if the parameter contains <code>sys_publish=unpublish</code>,
 * then it removes items that have public revision (or is publishable).
 * 
 * @author adamgent
 *
 */
public class PSPublicAssetItemFilterRule extends PSAbstractWorkflowExtension implements IPSItemFilterRule
{
	// Constant for Live step
	private static final String LIVE_STATE = "Live";

	@Autowired
	private IPSRecycleService recyclerService;
    
    @Override
    public List<IPSFilterItem> filter(List<IPSFilterItem> items, Map<String, String> params)
    {
        boolean isPublish = !"unpublish".equals(params.get(IPSHtmlParameters.SYS_PUBLISH));
        PSPubServer pubServer = findPubServer(params.get(IPSHtmlParameters.SYS_EDITIONID));
        String ignoreAssets = pubServer==null?"false":pubServer.getPropertyValue(PUBLISH_IGNORE_UNMODIFIED_ASSETS_PROPERTY);
        boolean ignoreUnModAssets = StringUtils.equals(ignoreAssets, "true");
        Long serverId = pubServer==null?null:pubServer.getServerId();
        
        WorkflowItemWorker worker = getWorker(params);
        List<IPSFilterItem> rvalue = new ArrayList<>();
        List<Integer> changedIds;
        Set<Integer> changedIdsSet = null;
        if (ignoreUnModAssets) {
            if (contentChangeService != null) {
                if(pubServer!= null) {
                    changedIds = contentChangeService.getChangedContent(pubServer.getSiteId(), PSContentChangeType.PENDING_LIVE);
                    changedIdsSet = new HashSet<>(changedIds);
                }
            }
        }
        for(IPSFilterItem item : items) {
            try {
                WorkflowItem wfItem = worker.getWorkflowItem(item.getItemId());
                IPSFilterItem r = process(worker, item, wfItem, isPublish, ignoreUnModAssets, serverId, changedIdsSet);
                if (r != null)
                    rvalue.add(r);
            }catch(Exception e){
                log.warn("Filter removing item: " + item.getItemId() + " because of the following error:" +  e.getMessage());
                log.debug("Filtered item stack trace: ", e);
            }
        }
        return rvalue;
    }
    
    /**
     * 
     * @param worker never <code>null</code>.
     * @param original never <code>null</code>.
     * @param wfItem never <code>null</code>.
     * @param isPublish
     * @param  ignoreUnModAssets
     * @param serverId
     * @param changedIdsSet
     * @return <code>null</code> indicates that the inputted item should be removed from the original list of items.
     */
    protected IPSFilterItem process(WorkflowItemWorker worker, IPSFilterItem original, WorkflowItem wfItem, boolean isPublish, boolean ignoreUnModAssets, Long serverId, Set<Integer> changedIdsSet)
    {
        IPSFilterItem rvalue;

        if(wfItem == null){
        	return null;
        }
        
        if (wfItem.assetType != AssetType.LOCAL && wfItem.assetType != AssetType.PAGE)
        {
            if (!isPublishableAsset(original, wfItem, ignoreUnModAssets, serverId, changedIdsSet))
            {
                return null;
            }
        }

        /*
         * See if its publishable.
         */
        if (wfItem.assetType == AssetType.LOCAL) {
            /*
             * For local content the revision should not need to be modified 
             * as the slot content finder will give us the right one.
             */
            if (log.isDebugEnabled())
                log.debug("Found local asset: " + wfItem);
            rvalue = original;
        }
        else if ((!isPublish) && (!wfItem.publishable))
        {
            rvalue = original;
        }
        else if (isPublish && wfItem.publishable && isScheduled(wfItem) && wfItem.publicRevision > 0) {
        	/*
        	 * Item is scheduled but was previously published so return the original value.
        	 */
        	int oldRevision = wfItem.publicRevision;
        	
        	IPSSystemWs systemws = PSSystemWsLocator.getSystemWebservice();
        	Map<IPSGuid, List<PSContentStatusHistory>> auditTrails = systemws.loadAuditTrails(Collections.singletonList(original.getItemId()));
        	List<PSContentStatusHistory> historyList = auditTrails.get(original.getItemId());
        	Collections.reverse(historyList);
        	for (PSContentStatusHistory historyItem : historyList)
        	{
        		if (historyItem.getStateName().equals(LIVE_STATE))
        		{	
        			oldRevision = historyItem.getRevision();
        			log.info("Publishing previous Item Revision: "+oldRevision+" Name:"+wfItem.itemSummary.getName());
        			break;
        		}
        	}

            if (log.isDebugEnabled())
                log.debug("Keeping original page or shared asset "+oldRevision+" due to scheduling: " + wfItem);
            
            IPSGuid newGuid = worker.makeGuidFromRevision(original.getItemId(), oldRevision);
            if (original.getItemId().equals(newGuid))
                rvalue = original;
            else
                rvalue = original.clone(newGuid);
        }
        else if (isPublish && wfItem.publishable && !isScheduled(wfItem)) {
            /*
             * This is the same logic as the public item filter.
             */
            if (log.isDebugEnabled())
                log.debug("Keeping page or shared asset: " + wfItem);


            IPSGuid newGuid = worker.makeGuidFromRevision(original.getItemId(), wfItem.publicRevision);
            if (original.getItemId().equals(newGuid))
                rvalue = original;
            else
                rvalue = original.clone(newGuid);

        }
        else {
            /*
             * The item should be removed
             */
            if (log.isDebugEnabled())
                log.debug("Removing item: " + wfItem);
            rvalue = null;
        }
        return rvalue;
    }

    protected PSPubServer findPubServer(String editionId)
    {
        if(StringUtils.isBlank(editionId))
        {
            return null;
        }
        PSPubServer pubServer = null;
        try
        {
            IPSEdition edition = pubService.loadEdition(guidMgr.makeGuid(editionId, PSTypeEnum.EDITION));
            IPSGuid serverGuid = edition.getPubServerId();
            pubServer = pubServerService.findPubServer(serverGuid.longValue());
        }
        catch(Exception e)
        {
            log.info("Error occurred while finding the status of ignore assets property, setting the value as false", e);
        }
        return pubServer;
    }
    
    /**
     * Determine if the item is scheduled to be published.  This means that it has a start date that is equivalent to the
     * next aging date, and that both dates are in the future.
     * 
     * @param wfItem The item to check, not <code>null</code>.
     * 
     * @return <code>true</code> if it's scheduled, <code>false</code> if not.
     */
    private boolean isScheduled(WorkflowItem wfItem)
    {
        Calendar nowCal = Calendar.getInstance();
        
        Date startDate = wfItem.itemSummary.getContentStartDate();
        if (startDate != null)
        {
            Calendar startCal = Calendar.getInstance();
            startCal.setTime(startDate);
            return nowCal.before(startCal);
        }
        
        return false;
    }

    private boolean isPublishableAsset(IPSFilterItem assetFilterItem, WorkflowItem wfItem, boolean ignoreUnModAssets, Long serverId, Set<Integer> changedIdsSet)
    {
        boolean result = true;
        try {
            //If Asset is in Recycle Folder, don't publish it.
            if(recyclerService.isInRecycler(assetFilterItem.getItemId())){
                return false;
            }
            String rootLevelFolderAllowedSites = folderHelper.getRootLevelFolderAllowedSitesPropertyValue(assetFilterItem.getItemId().toString());
            if (null != rootLevelFolderAllowedSites && null != assetFilterItem.getSiteId()) {
                if (!rootLevelFolderAllowedSites.contains(String.valueOf(assetFilterItem.getSiteId().longValue()))) {
                    result = false;
                }else{
                    result = true; //Not needed code wise but setting for clarity.
                }
            } else if (ignoreUnModAssets && wfItem.publicRevision != null && wfItem.publicRevision > 0 && serverId != null) {
                result = changedIdsSet != null && changedIdsSet.contains(wfItem.itemSummary.getContentId());
            } else {
                result = true;
            }
        }catch(NullPointerException npe){
            result = true;
        }
        return result;
    }
    
    @Override
    public int getPriority()
    {
        /*
         * We should be the only filter in the chain
         * so this number should not matter.
         */
        return 10;
    }
    
    @Override
    public void init(IPSExtensionDef def, File codeRoot)
    {
        super.init(def, codeRoot);
        //This is for wiring the services
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    private IPSFolderHelper folderHelper;
    private IPSPublisherService pubService;
    private IPSPubServerService pubServerService;
    private IPSGuidManager guidMgr;
    private IPSSitePublishService sitePublishService;
    private IPSSiteManager siteMgr;
    private IPSContentChangeService contentChangeService;

    public IPSContentChangeService getContentChangeService() {
        return contentChangeService;
    }

    public void setContentChangeService(IPSContentChangeService contentChangeService) {
        this.contentChangeService = contentChangeService;
    }

    public IPSSiteManager getSiteMgr() {
        return siteMgr;
    }

    public void setSiteMgr(IPSSiteManager siteMgr) {
        this.siteMgr = siteMgr;
    }

    public IPSSitePublishService getSitePublishService() {
        return sitePublishService;
    }

    public void setSitePublishService(IPSSitePublishService sitePublishService) {
        this.sitePublishService = sitePublishService;
    }


    public IPSGuidManager getGuidMgr() 
    {
        return guidMgr;
    }

    public void setGuidMgr(IPSGuidManager guidMgr) 
    {
        this.guidMgr = guidMgr;
    }
    
    public IPSPubServerService getPubServerService() 
    {
        return pubServerService;
    }
    
    public void setPubServerService(IPSPubServerService pubServerService) 
    {
        this.pubServerService = pubServerService;
    }
    
    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }
    
    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }
    
    public IPSPublisherService getPubService() 
    {
        return pubService;
    }
    
    public void setPubService(IPSPublisherService pubService) 
    {
        this.pubService = pubService;
    }

    public static final String PUBLISH_IGNORE_UNMODIFIED_ASSETS_PROPERTY = "ignoreUnModifiedAssets";
}

