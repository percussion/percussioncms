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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.pagemanagement.assembler.impl;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.error.PSException;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.pagemanagement.data.PSRenderLink;
import com.percussion.pagemanagement.service.IPSRenderLinkService;
import com.percussion.pagemanagement.service.impl.PSLinkableAsset;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * A legacy location scheme generator that uses
 * resource defintions. This is mainly used for Inline
 * links since the inline link generator calls the location scheme
 * generator directly. Non-inline links usually call 
 * {@link IPSRenderLinkService} directly through jexl methods.
 * <p>
 * Right now this generator is used only for links (urls) and 
 * locations (file paths).
 * 
 * @author adamgent
 *
 */
public class PSResourceAssemblyLocation extends PSAbstractAssemblyLocationAdapter
{

    private static final String PREVIEW_ITEM_FILTER = "preview";
    private static final String PUBLIC_ITEM_FILTER = "perc_public";
    private IPSRenderLinkService renderLinkService;
    private IPSIdMapper idMapper;
    private IPSFolderHelper folderHelper;
    private IPSAssetService assetService;
    private IPSSiteDataService siteDataService;
    
    
    @Override
    public void init(IPSExtensionDef extensionDef, File file)
    {
        super.init(extensionDef, file);
        PSSpringWebApplicationContextUtils.injectDependencies(this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    protected String createLocation(PSAssemblyLocationRequest locationRequest) throws PSDataServiceException, PSException {
        PSBeanValidationUtils.validate(locationRequest).throwIfInvalid();
        String resourceId = getResourceDefinitionId(locationRequest);
        /*
         * TODO: Better error handling for inline links with missing resource
         * definitions. Right now an illegal argument exception is thrown.
         */
        notEmpty(resourceId, "resourceId");
        ItemAndContext i = getItemAndContext(locationRequest);
        PSRenderLink link = renderLinkService.renderLink(i.linkContext, i.asset, resourceId);
        notNull(link);
        return link.getUrl();
    }
    
    /**
     * @param locationRequest never <code>null</code>.
     * @return <strong>SHOULD</strong> never be <code>null</code>.
     */
    protected String getResourceDefinitionId(PSAssemblyLocationRequest locationRequest) throws PSDataServiceException, PSException {
        String resourceId = locationRequest.getParameters().get(PSAssemblyConfig.PERC_RESOURCE_ID_PARAM_NAME);
        if (resourceId != null) {
            log.debug("Found resource in parameters");
            return resourceId;
        }
        String contentType = getContentTypeName(locationRequest);
        String templateName = getTemplateName(locationRequest);
        return renderLinkService.resolveResourceDefinition(resourceId, templateName, contentType).getUniqueId();
    }
    
    protected String getTemplateName(PSAssemblyLocationRequest locationRequest) {
       return getTemplate(locationRequest).getName();
    }
    
    
    /**
     * Creates the context and item for creating links.
     * @param locationRequest Assumed not <code>null</code>.
     * @return never <code>null</code>.
     */
    private ItemAndContext getItemAndContext(PSAssemblyLocationRequest locationRequest) throws IPSAssetService.PSAssetServiceException, IPSDataService.DataServiceLoadException, PSValidationException {
        PSAssemblyRenderLinkContext context = new PSAssemblyRenderLinkContext();
        
        /*
         * TODO the link context creation code should be moved to the context factory.
         */
        
        /*
         * Resolve the filter since its not always in the request.
         */
        String authType = getAuthtype(locationRequest);
        String filter = locationRequest.getItemFilter();
        isTrue(isNotBlank(authType) || isNotBlank(filter), 
                "The filter and authtype cannot both be null or empty");
        if (isBlank(filter) &&  authType.equals("0") ) {
            filter = PREVIEW_ITEM_FILTER;
        }
        else if (isBlank(filter)) {
            filter = PUBLIC_ITEM_FILTER;
        }
        context.setFilter(filter);
        
        /*
         * Some resolving of legacy publishing context.
         * We need a proper link and file context.
         */
        Number linkContext = locationRequest.getAssemblyContext();
        Number fileContext = locationRequest.getDeliveryContext();
        linkContext = linkContext == null ? locationRequest.getContext(): linkContext;
        fileContext = fileContext == null ? locationRequest.getContext(): fileContext;
        context.setLegacyLinkContext(linkContext);
        context.setLegacyFileContext(fileContext);
        context.setDeliveryContext(fileContext.intValue() == locationRequest.getContext().intValue());
        
        /*
         * Load the item we want to link to.
         */
        String contentId = idMapper.getString(locationRequest.getItemId());
        PSAsset asset = assetService.load(contentId,true);
        IPSItemSummary itemSummary = asset;
        
        /*
         * We need to resolve what site we belong to.
         */
        IPSGuid siteGuid = locationRequest.getSiteId();
        notNull(siteGuid, "siteGuid");
        PSSiteSummary siteSummary = siteDataService.findByLegacySiteId(idMapper.getString(siteGuid), false);
        
        
        /*
         * Find out which folder path we should use.
         */
        String providedPath = getFolderPath(locationRequest);
        String folderPath = renderLinkService.resolveFolderPath(itemSummary, siteSummary, 
                PSFolderPathUtils.toFolderPath(providedPath));
        
        context.setSite(siteSummary);
        context.setFolderPath(folderPath);
        
        
        ItemAndContext i = new ItemAndContext();
        i.asset = new PSLinkableAsset(asset, folderPath);
        i.linkContext = context;
        return i;
        
    }

    private String getFolderPath(PSAssemblyLocationRequest locationRequest)
    {
        String providedPath = null;
        
        if (locationRequest.getFolderId() != null) {
            try
            {
                Number legacyFolderId = idMapper.getLocator(locationRequest.getFolderId()).getId();
                providedPath = folderHelper.findPathFromLegacyFolderId(legacyFolderId);
            }
            catch (Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        return providedPath;
    }
    
    /**
     * A holder object to hold both the link context and 
     * item.
     * @author adamgent
     *
     */
    protected static class ItemAndContext {
        protected PSAssemblyRenderLinkContext linkContext;
        protected PSLinkableAsset asset;
    }
    
    public IPSRenderLinkService getRenderLinkService()
    {
        return renderLinkService;
    }

    public void setRenderLinkService(IPSRenderLinkService renderLinkService)
    {
        this.renderLinkService = renderLinkService;
    }
    
    

    public IPSIdMapper getIdMapper()
    {
        return idMapper;
    }

    public void setIdMapper(IPSIdMapper idMapper)
    {
        this.idMapper = idMapper;
    }

    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }

    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }

    public IPSAssetService getAssetService()
    {
        return assetService;
    }

    public void setAssetService(IPSAssetService assetService)
    {
        this.assetService = assetService;
    }

    public IPSSiteDataService getSiteDataService()
    {
        return siteDataService;
    }

    public void setSiteDataService(IPSSiteDataService siteDataService)
    {
        this.siteDataService = siteDataService;
    }    
    
}

