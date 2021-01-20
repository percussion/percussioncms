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
package com.percussion.ui.service.impl;

import static com.percussion.webservices.PSWebserviceUtils.getStateById;
import static com.percussion.webservices.PSWebserviceUtils.getWorkflow;

import com.percussion.design.objectstore.PSLocator;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSSearchPathItemService;
import com.percussion.pathmanagement.service.impl.PSSitePathItemService;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.IPSItemEntry;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.ui.data.PSDisplayPropertiesCriteria;
import com.percussion.ui.service.IPSListViewHelper;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.types.PSPair;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


/**
 * A "CMS objects" implementation of the {@link IPSListViewHelper} interface.
 * 
 * @author federicoromanelli
 * 
 */
@PSSiteManageBean("cm1ListViewHelper")
@Lazy
public class PSCm1ListViewHelper extends PSBaseListViewHelper
{
    public static final String FOLDER_CONTENTTYPE = "Folder";
    public static final String SITE_CONTENTTYPE = "Site";
    public static final String PAGE_CONTENTTYPE = "Page";
    public static final String ASSET_CONTENTTYPE = "Asset";
    
    private IPSDataItemSummaryService dataItemSummaryService;
    
    private IPSCmsObjectMgr cmsObjectMgr;
    
    private IPSIdMapper idMapper;
    
    private IPSFolderHelper folderHelper;
    
    private static Map<String, String> contentIdMap = new HashMap<String, String>();
    
    /*
     * Initialization of content type map values
     */
    static
    {
        contentIdMap.put("SITE", SITE_CONTENTTYPE);
        contentIdMap.put("FOLDER", FOLDER_CONTENTTYPE);
        contentIdMap.put("PAGE", PAGE_CONTENTTYPE);
        contentIdMap.put("LANDING_PAGE", PAGE_CONTENTTYPE);
        contentIdMap.put("SECTION_FOLDER", FOLDER_CONTENTTYPE);
        contentIdMap.put("EXTERNAL_SECTION_FOLDER", FOLDER_CONTENTTYPE);
        contentIdMap.put("SYSTEM", FOLDER_CONTENTTYPE);
    }
    @Autowired
    public PSCm1ListViewHelper(IPSDataItemSummaryService dataItemSummaryService, IPSIdMapper idMapper, IPSFolderHelper folderHelper)
    {
        this.cmsObjectMgr = PSCmsObjectMgrLocator.getObjectManager();
        this.dataItemSummaryService = dataItemSummaryService;
        this.idMapper = idMapper;
        this.folderHelper = folderHelper;
    }
    
    /**
     * Returns the content type given a specific category (retrieved previously from a loaded PSPathItem object)
     * 
     * @param name the category retrieved from the PSPathItem object
     * @return a string with the corresponding content type: Page, Asset, Folder, Site
     */   
    private static String getContentType(String name)
    {
        String type = contentIdMap.get(name);
        if (type == null || type.equals(""))
            return ASSET_CONTENTTYPE;
        return type;
    }

    /**
     * @param criteria
     * @return
     */
    private Integer getContentId(PSPathItem pathItem)
    {
        try
        {
            String path = pathItem.getPath();
            
            if (path.startsWith(PSSitePathItemService.SITE_ROOT_SUB))
            {
                path = "/" + path;
            }
            else if (path.startsWith(PSSearchPathItemService.SEARCH_ROOT_SUB))
            {
                return null;
            }
            else
            {
                path = PSAssetPathItemService.ASSET_ROOT_SUB + path;
            }
            
            String id = dataItemSummaryService.pathToId("/" + path);
            
            return ((PSLegacyGuid) idMapper.getGuid(id)).getContentId();
        }
        catch (Exception e)
        {
            log.error("Error in getting the content id for path item: " + pathItem.getPath());
            return null;
        }
    }

    private String getCategory(PSPathItem pathItem)
    {
        String category = "SITE";
        if (pathItem.getCategory() != null)
        {
            category = pathItem.getCategory().name();
        }
        
        return category;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.ui.service.impl.PSBaseListViewHelper#getDisplayProperties(com.percussion.pathmanagement.data.PSPathItem)
     */
    @Override
    protected Map<String, String> getDisplayProperties(PSPathItem pathItem)
    {
        Map<String, String> displayProperties = new HashMap<String, String>();
        
        IPSItemEntry itemEntry = null;
        Object relatedObject = getRelatedObject(pathItem);
        
        // If the relatedObject was not specified, find it
        if (relatedObject != null)
            itemEntry = (IPSItemEntry) relatedObject;
        else
        {
            itemEntry = cmsObjectMgr.findItemEntry(getContentId(pathItem));
        }
        
        if (StringUtils.isNotBlank(itemEntry.getName()))
            displayProperties.put(TITLE_NAME, itemEntry.getName());
        
        if (StringUtils.isNotBlank(itemEntry.getStateName()))
            displayProperties.put(STATE_NAME, itemEntry.getStateName());

        // get workflow name and fixup last modified info if item is workflowed
        String stateName = itemEntry.getStateName();
        String lastModifier = itemEntry.getLastModifier();
        Date lastModifiedDate = itemEntry.getLastModifiedDate();
        if (StringUtils.isNotBlank(stateName))
        {
            PSWorkflow wf = getWorkflow(itemEntry.getWorkflowAppId());
            displayProperties.put(WORKFLOW_NAME, wf.getName());
            
            // if we have last modified info, then fix it up to last real user mod
            if (lastModifiedDate != null && StringUtils.isNotBlank(lastModifier))
            {
                PSState state = getStateById(wf, itemEntry.getContentStateId());
                PSPair<String, String> modInfo = folderHelper.fixupLastModified(idMapper.getGuid(new PSLocator(itemEntry.getContentId())), lastModifier, lastModifiedDate, state.isPublishable());
                
                displayProperties.put(CONTENT_LAST_MODIFIER_NAME, modInfo.getFirst());
                displayProperties.put(CONTENT_LAST_MODIFIED_DATE_NAME, modInfo.getSecond());                
            }
        }

        if (StringUtils.isNotBlank(getCategory(pathItem)))
            displayProperties.put(CONTENTTYPE_NAME, getContentType(getCategory(pathItem)));
        
        if (StringUtils.isNotBlank(itemEntry.getCreatedBy()))
            displayProperties.put(CONTENT_CREATEDBY_NAME, itemEntry.getCreatedBy());

        
        if (itemEntry.getPostDate() != null)
            displayProperties.put(POSTDATE_NAME, PSDateUtils.getDateToString(itemEntry.getPostDate()));
        
        if (itemEntry.getCreatedDate() != null)
            displayProperties.put(CONTENT_CREATEDDATE_NAME, PSDateUtils.getDateToString(itemEntry.getCreatedDate()));
        
        return displayProperties;
    }
    


    @Override
    public void fillDisplayProperties(PSDisplayPropertiesCriteria criteria)
    {
        super.fillDisplayProperties(criteria);
    }


    /* (non-Javadoc)
     * @see com.percussion.ui.service.impl.PSBaseListViewHelper#expectedRelatedObjectType()
     */
    @Override
    protected Class<?> expectedRelatedObjectType()
    {
        return IPSItemEntry.class;
    }

    /* (non-Javadoc)
     * @see com.percussion.ui.service.impl.PSBaseListViewHelper#areEmptyRelatedObjectsSupported()
     */
    @Override
    protected boolean areEmptyRelatedObjectsSupported()
    {
        return true;
    }

}
