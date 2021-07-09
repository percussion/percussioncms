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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.assembler.impl;

import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.pagemanagement.assembler.IPSRenderLinkContextFactory;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.filter.PSFilterException;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import org.springframework.beans.factory.annotation.Autowired;

import static org.apache.commons.lang.StringUtils.isNotBlank;

@PSSiteManageBean("renderLinkContextFactory")
public class PSRenderLinkContextFactory implements IPSRenderLinkContextFactory
{
    private IPSIdMapper idMapper;
    private IPSSiteDataService siteDataService;

    @Autowired
    public PSRenderLinkContextFactory(IPSIdMapper idMapper, IPSSiteDataService siteDataService)
    {
        super();
        this.idMapper = idMapper;
        this.siteDataService = siteDataService;
    }

    public PSAssemblyRenderLinkContext create(PSContentListItem listItem, IPSLinkableItem item) throws IPSDataService.DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        PSAssemblyRenderLinkContext linkContext = new PSAssemblyRenderLinkContext();
        
        linkContext.setFolderPath(item.getFolderPath());
        linkContext.setLegacyFileContext(listItem.getContext());
        linkContext.setLegacyLinkContext(listItem.getContext());
        linkContext.setSite(getSite(listItem.getSiteId(), item));
        linkContext.setFilter("public");
        linkContext.setDeliveryContext(true); // content list item only need to get the publish location, never linked url/location
        
        return linkContext;        
    }

    public PSAssemblyRenderLinkContext create(IPSAssemblyItem assemblyItem, IPSLinkableItem item) throws IPSDataService.DataServiceLoadException, DataServiceNotFoundException, PSFilterException, PSValidationException {
        PSAssemblyRenderLinkContext linkContext = new PSAssemblyRenderLinkContext();
        
        linkContext.setFolderPath(item.getFolderPath());
        linkContext.setLegacyFileContext(assemblyItem.getDeliveryContext());
        linkContext.setLegacyLinkContext(assemblyItem.getContext());
        linkContext.setSite(getSite(assemblyItem.getSiteId(), item));
        linkContext.setFilter(assemblyItem.getFilter().getName());

        return linkContext;
    }

    public PSAssemblyRenderLinkContext createPreview(PSPage page) throws DataServiceNotFoundException, PSValidationException {
        PSAssemblyRenderLinkContext linkContext = new PSAssemblyRenderLinkContext();
        
        linkContext.setFolderPath(page.getFolderPath());
        linkContext.setLegacyFileContext(0);
        linkContext.setLegacyLinkContext(0);
        linkContext.setSite(siteDataService.findByPath(page.getFolderPath()));
        linkContext.setFilter("preview");
        
        return linkContext;
    }

    public PSAssemblyRenderLinkContext createAssetPreview(String folderPath, PSAssetSummary asset) {
        PSAssemblyRenderLinkContext linkContext = new PSAssemblyRenderLinkContext();
        
        linkContext.setFolderPath(folderPath);
        linkContext.setLegacyFileContext(0);
        linkContext.setLegacyLinkContext(0);
        linkContext.setSite(PSNullSiteSummary.getInstance());
        linkContext.setFilter("preview"); 
        
        return linkContext;
        
    }
    
    /**
     * Gets the site from the specified site ID if not <code>null</code>; 
     * otherwise gets the site from the specified linkable item.
     *  
     * @param siteId the site ID, may be <code>null</code>.
     * @param item the linkable item, assumed not <code>null</code>.
     * 
     * @return the site, which may be {@link PSNullSiteSummary} if the site
     * is unknown from both parameters, never <code>null</code>.
     */
    private PSSiteSummary getSite(IPSGuid siteId, IPSLinkableItem item) throws IPSDataService.DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        PSSiteSummary rvalue = null;
        if (siteId != null && siteId.getUUID() != 0) {
            String legacySiteId = idMapper.getString(siteId);
            if (isNotBlank(legacySiteId))
                rvalue = siteDataService.findByLegacySiteId(legacySiteId, false);
        }
        else if (item.getFolderPath() != null) {
            rvalue = siteDataService.findByPath(item.getFolderPath());
        }
        if (rvalue == null)
            rvalue = PSNullSiteSummary.getInstance();
        return rvalue;
    }
    
}
