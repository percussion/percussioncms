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
package com.percussion.assetmanagement.service.impl;

import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notNull;

import java.util.Collection;
import java.util.List;

import com.percussion.assetmanagement.service.impl.PSAssetNewFolderPathResolver.PSResolvedFolderPath.PSResolvedFolderPathType;
import com.percussion.pagemanagement.data.PSPageSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteTemplateService;

/**
 * Resolves what the folder path should be for an asset that is to be associated
 * to an owner.
 * <strong>It does not actually put the asset into the folder</strong>.
 * That is done elsewhere.
 * 
 * @author adamgent
 *
 */
public class PSAssetNewFolderPathResolver
{
    private IPSSiteTemplateService siteTemplateService;
    private IPSPageService pageService;
    
    public PSAssetNewFolderPathResolver(IPSPageService pageService, IPSSiteTemplateService siteTemplateService)
    {
        super();
        this.pageService = pageService;
        this.siteTemplateService = siteTemplateService;
    }

    /**
     * Resolves the folder path for where an asset should live in a site folder path
     * given an owner.
     * 
     * @param owner Could be a page or a template, never <code>null</code>.
     * @param asset The asset to be associated with the owner.
     * @return the resolved path.
     */
    public PSResolvedFolderPath resolveFolderPath(IPSItemSummary owner, IPSItemSummary asset) throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        List<String> assetPaths = asset.getFolderPaths();
        
        if (IPSTemplateService.TPL_CONTENT_TYPE.equals(owner.getType())) {
            /*
             * If the owner is a template lets see if its in the same site as the template.
             */
            String sitePath = getSiteFolderPath(owner.getId());
            List<String> matchingPaths = matchingPaths(sitePath, assetPaths);
            
            if (matchingPaths.isEmpty()) {
                /*
                 * If its not in the same site we will request to add it to the base.
                 */
                return new PSResolvedFolderPath(sitePath, false, PSResolvedFolderPathType.TEMPLATE);
            }
            
            return new PSResolvedFolderPath(matchingPaths.get(0), true, PSResolvedFolderPathType.TEMPLATE);
            
        }
        else if (IPSPageService.PAGE_CONTENT_TYPE.equals(owner.getType())) {
            /*
             * If the owner is a page lets see if its in the same site first.
             */
            
            PSPageSummary page = pageService.find(owner.getId());
            String templateId = page.getTemplateId();
            if (templateId != null) {
                String sitePath = getSiteFolderPath(templateId);
                List<String> matchingPaths = matchingPaths(sitePath, assetPaths);
                if (matchingPaths.isEmpty()) {
                    /*
                     * Not in the same site we will use the pages folder path.
                     */
                    return new PSResolvedFolderPath(page.getFolderPath(), false, PSResolvedFolderPathType.PAGE);
                }
                /*
                 * Pick one of the existing paths and set it so we don't add it to the folder.
                 */
                return new PSResolvedFolderPath(matchingPaths.get(0), true, PSResolvedFolderPathType.PAGE);
            }
            /*
             * The page does not have a template ?
             */
            throw new RuntimeException("Cannot add an asset to a folder with a page that does not have a template. Page: " + page );
        }
        else {
            throw new IllegalStateException("Cannot add item to owner of type: " + owner.getType() 
                    + " should be a page or template.");
        }
    }
    
    private String getSiteFolderPath(String templateId) {
        List<PSSiteSummary> sites = siteTemplateService.findSitesByTemplate(templateId);
        isTrue(! sites.isEmpty(), "Template should have a site associated with it");
        String folderPath = sites.get(0).getFolderPath();
        return folderPath;
    }
    
    private List<String> matchingPaths(String sitePath, Collection<String> folderPaths) {
        return PSFolderPathUtils.matchingDescedentPaths(sitePath, folderPaths);
    }
    
    
    /**
     * Represents a resolved path.
     * None of the properties should be null.
     * {@link #isAlreadyInFolder()} <code>true</code> means its
     * already in the folder.
     * @author adamgent
     *
     */
    public static class PSResolvedFolderPath {
        
        private PSResolvedFolderPathType type;
        private String folderPath;
        private boolean alreadyInFolder;
        
        
        public boolean isAlreadyInFolder()
        {
            return alreadyInFolder;
        }
        public void setAlreadyInFolder(boolean alreadyInFolder)
        {
            this.alreadyInFolder = alreadyInFolder;
        }
        public PSResolvedFolderPathType getType()
        {
            return type;
        }
        public void setType(PSResolvedFolderPathType type)
        {
            this.type = type;
        }
        public String getFolderPath()
        {
            return folderPath;
        }
        public void setFolderPath(String folderPath)
        {
            this.folderPath = folderPath;
        }
        public PSResolvedFolderPath(String folderPath, boolean alreadyInFolder, PSResolvedFolderPathType type)
        {
            super();
            notNull(folderPath);
            notNull(alreadyInFolder);
            notNull(type);
            this.folderPath = folderPath;
            this.alreadyInFolder = alreadyInFolder;
            this.type = type;
        }

        public enum PSResolvedFolderPathType {
            PAGE,
            TEMPLATE
        }
    }
    

}

