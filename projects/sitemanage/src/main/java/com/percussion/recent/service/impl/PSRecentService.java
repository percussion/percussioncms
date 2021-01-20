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

package com.percussion.recent.service.impl;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSWorkflowAction;
import com.percussion.itemmanagement.service.impl.PSWorkflowHelper;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.recent.data.PSRecent.RecentType;
import com.percussion.recent.service.IPSRecentServiceBase;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.webservices.PSWebserviceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.PathParam;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation=Propagation.REQUIRED)
@Component("recentService")
@Lazy
public class PSRecentService implements IPSRecentService
{
 
    @Autowired
    private @Qualifier("recentServiceBase") IPSRecentServiceBase recentService;

    @Autowired
    private @Qualifier("pathService") PSPathService pathService;

    @Autowired
    private IPSIdMapper idMapper;
   
    @Autowired
    private IPSFolderHelper folderHelper;
    
    @Autowired
    private IPSAssetService assetService;

    @Autowired
    private IPSSiteTemplateService siteTemplateService;

    static Log ms_log = LogFactory.getLog(PSRecentService.class);

    /*
    @Autowired
    
    public PSRecentService(IPSRecentServiceBase recentService, IPSPathService pathService, IPSIdMapper idMapper,
            IPSFolderHelper folderHelper, IPSSiteTemplateService siteTemplateService)
    {
        this.recentService = recentService;
        this.pathService = pathService;
        this.idMapper = idMapper;
        this.folderHelper = folderHelper;
        this.siteTemplateService = siteTemplateService;
    }
    */

    @Override
    public List<PSItemProperties> findRecentItem(boolean ignoreArchivedItems)
    {
        String user = PSWebserviceUtils.getUserName();
        List<String> recentEntries = recentService.findRecent(user, null, RecentType.ITEM);
        List<PSItemProperties> items = new ArrayList<PSItemProperties>();
        List<String> toDelete = new ArrayList<String>();
        for (String entry : recentEntries)
        {
            PSItemProperties itemProps = null;
            try
            {
                itemProps = folderHelper.findItemPropertiesById(entry);
                if (itemProps != null )
                    //don't return archived items and items with no path on home page.
                    if(ignoreArchivedItems && (itemProps.getStatus().equals(PSWorkflowHelper.WF_STATE_ARCHIVE) || itemProps.getPath() == null)) {
                        continue;
                    }else{
                        items.add(itemProps);
                    }
                else
                    ms_log.debug("Removing recent item find returned null :" + entry);
            }
            catch (Exception e)
            {
                ms_log.debug("removing error entry from recent item list " + entry + " ", e);
            }
            if (itemProps == null)
                toDelete.add(entry);
        }
        if (!toDelete.isEmpty())
            recentService.deleteRecent(user, null, RecentType.ITEM, toDelete);
        return items;
    }

    @Override
    public List<PSTemplateSummary> findRecentTemplate(@PathParam("siteName")
    String siteName)
    {
        String user = PSWebserviceUtils.getUserName();
        List<String> recentEntries = recentService.findRecent(user, siteName, RecentType.TEMPLATE);
        List<PSTemplateSummary> templates = new ArrayList<PSTemplateSummary>();
        List<String> toDelete = new ArrayList<String>();

        Map<String,PSTemplateSummary> siteTemplateMap = new HashMap<String,PSTemplateSummary>();
        // if we do not find site we will remove all entries for site that no
        // longer exist

        List<PSTemplateSummary> siteTemplates = siteTemplateService.findTemplatesBySite(siteName);

        for (PSTemplateSummary siteTemplate : siteTemplates)
        {
            siteTemplateMap.put(siteTemplate.getId(),siteTemplate);
           
        }

        for (String entry : recentEntries)
        {
            PSTemplateSummary template = siteTemplateMap.get(entry);
               
            // Cleanup old or invalid entries
            if (template == null)
            {
                ms_log.debug("Removing recent template not a current site template :" + entry);
                toDelete.add(entry);
            }
            else 
            {
                templates.add(template);
            }
        }

        if (!toDelete.isEmpty())
            recentService.deleteRecent(user, siteName, RecentType.TEMPLATE, toDelete);
        return templates;
    }

    @Override
    public List<PSPathItem> findRecentSiteFolder(String siteName)
    {
        String user = PSWebserviceUtils.getUserName();
        List<String> recentEntries = recentService.findRecent(user, siteName, RecentType.SITE_FOLDER);
        List<PSPathItem> pathItems = new ArrayList<PSPathItem>();
        List<String> toDelete = new ArrayList<String>();

        for (String entry : recentEntries)
        {
            PSPathItem pathItem = null;
            try
            {
                pathItem = pathService.find(entry);
                if (pathItem != null)
                    pathItems.add(pathItem);
                else
                    ms_log.debug("Removing recent siteFolder entry find returned null :" + entry);
            }
            catch (Exception e)
            {
                ms_log.debug("removing error entry from recent siteFolder list " + entry + " ", e);
            }
            if (pathItem == null)
                toDelete.add(entry);
        }
        if (!toDelete.isEmpty())
            recentService.deleteRecent(user, siteName, RecentType.SITE_FOLDER, toDelete);
        return pathItems;
    }

    @Override
    public List<PSPathItem> findRecentAssetFolder()
    {
        String user = PSWebserviceUtils.getUserName();
        List<String> recentEntries = recentService.findRecent(user, null, RecentType.ASSET_FOLDER);
        List<PSPathItem> pathItems = new ArrayList<PSPathItem>();

        List<String> toDelete = new ArrayList<String>();

        for (String entry : recentEntries)
        {
            PSPathItem pathItem = null;
            try
            {
                pathItem = pathService.find(entry);
                if (pathItem != null)
                    pathItems.add(pathItem);
                else
                    ms_log.debug("Removing recent assetFolder entry find returned null :" + entry);

                // FB:NP_NULL_ON_SOME_PATH, UNUSED - NC 1-16-16 -  pathItem.getType();
            }
            catch (Exception e)
            {
                ms_log.debug("removing error entry from recent assetFolder list " + entry + " ", e);
            }
            if (pathItem == null)
                toDelete.add(entry);
        }
        if (!toDelete.isEmpty())
            recentService.deleteRecent(user, null, RecentType.ASSET_FOLDER, toDelete);
        return pathItems;
    }

    @Override
    public void addRecentItem(@FormParam("value")
    String value)
    {
        String user = PSWebserviceUtils.getUserName();
        if (PSTypeEnum.LEGACY_CONTENT.getOrdinal() != idMapper.getGuid(value).getType())
            throw new IllegalArgumentException("Value must be an item guid");
        // store guid as a revisionless guid.
        PSLocator locator = new PSLocator(idMapper.getContentId(value));
        locator.setRevision(-1);
        value = idMapper.getString(locator);
        recentService.addRecent(user, null, RecentType.ITEM, value);
    }

    @Override
    public void addRecentTemplate(@PathParam("siteName")
    String siteName, @FormParam("value")
    String value)
    {
        String user = PSWebserviceUtils.getUserName();
        // Templates are stored as items. We do not check if it is a real item
        // here as that
        // requires accessing the full template currently. We will check on the
        // way out.
        if (PSTypeEnum.LEGACY_CONTENT.getOrdinal() != idMapper.getGuid(value).getType())
            throw new IllegalArgumentException("Value must be a template guid");
        // Not actually checking template exists for performance, check and
        // filter done on find.
        recentService.addRecent(user, siteName, RecentType.TEMPLATE, value);
    }

    @Override
    public void addRecentSiteFolder(@FormParam("value")
    String value)
    {
        String user = PSWebserviceUtils.getUserName();
        if(StringUtils.isBlank(value) || !(StringUtils.startsWith(value, "//") || StringUtils.startsWith(value, "/")))
            throw new IllegalArgumentException("Not a Site Folder Path");
        String folderPath = StringUtils.startsWith(value, "//")?value.substring(1):value;
        String siteName = PSPathUtils.getSiteFromPath(folderPath);
        if(siteName == null)
            throw new IllegalArgumentException("Not a Site Folder Path");
        // Not checking database for folder to improve performance, check done
        // on way out.
        
        recentService.addRecent(user, siteName, RecentType.SITE_FOLDER, folderPath);
    }

    @Override
    public void addRecentAssetFolder(@FormParam("value")
    String value)
    {
        String user = PSWebserviceUtils.getUserName();
        int pos = value.indexOf("Assets");
        if (pos >= 0 && pos <= 2)
            value = "/" + value.substring(pos);
        else
            throw new IllegalArgumentException("Not a Asset Folder Path");

        // Not checking database for folder to improve performance, check done
        // on way out.

        recentService.addRecent(user, null, RecentType.ASSET_FOLDER, value);

    }
    
    @Override
    public void addRecentAssetType(String value)
    {
        String user = PSWebserviceUtils.getUserName();
        recentService.addRecent(user, null, RecentType.ASSET_TYPE, value);    
    }
    
    @Override
    public List<PSWidgetContentType> findRecentAssetType()
    {
        List<PSWidgetContentType> resultList = new ArrayList<PSWidgetContentType>();
        String user = PSWebserviceUtils.getUserName();
        List<String> recentEntries = recentService.findRecent(user, null, RecentType.ASSET_TYPE);
        List<String> toDelete = new ArrayList<String>();
        Map<String, PSWidgetContentType> widgetTypeMap = new HashMap<String, PSWidgetContentType>();
        List<PSWidgetContentType> widgetTypes = assetService.getAssetTypes("yes");
        for (PSWidgetContentType wt : widgetTypes)
        {
            widgetTypeMap.put(wt.getWidgetId(), wt);
        }
        
        for (String entry : recentEntries)
        {
            PSWidgetContentType wtype = widgetTypeMap.get(entry);
               
            // Cleanup old or invalid entries
            if (wtype == null)
            {
                ms_log.debug("Removing recent template not a current site template :" + entry);
                toDelete.add(entry);
            }
            else 
            {
                resultList.add(wtype);
            }
        }

        if (!toDelete.isEmpty())
            recentService.deleteRecent(user, null, RecentType.ASSET_TYPE, toDelete);
        return resultList;
    }

    @Override
    public void deleteUserRecent(@PathParam("user") String user)
    {
        recentService.deleteRecent(user, null, null);
    }

    @Override
    public void deleteSiteRecent(@PathParam("siteName") String siteName)
    {
        recentService.deleteRecent(null, siteName, null);
    }

    @Override
    public void updateSiteNameRecent(String oldSiteName, String newSiteName) {
        try {
            recentService.renameSiteRecent(oldSiteName, newSiteName);
        } catch (Exception e) {
            ms_log.error("Error updating PSX_RECENT table to rename site from: " + oldSiteName +
                    " to: " + newSiteName, e);
        }
    }

    @Override
	public void addRecentItemByUser(String userName, String value) {
		recentService.addRecent(userName, null, RecentType.ITEM, value); 
	}

	@Override
	public void addRecentTemplateByUser(String userName, String siteName, String value) {
		recentService.addRecent(userName, null, RecentType.TEMPLATE, value);
	}

	@Override
	public void addRecentSiteFolderByUser(String userName, String value) {
		recentService.addRecent(userName, null, RecentType.SITE_FOLDER, value);
	}

	@Override
	public void addRecentAssetFolderByUser(String userName, String value) {
		recentService.addRecent(userName, null, RecentType.ASSET_FOLDER, value); 
	}

	@Override
	public void addRecentAssetTypeByUser(String userName, String value) {
		recentService.addRecent(userName, null, RecentType.ASSET_TYPE, value);  
	}
}
