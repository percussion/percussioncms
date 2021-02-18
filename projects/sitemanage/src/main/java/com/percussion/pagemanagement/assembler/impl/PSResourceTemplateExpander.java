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
package com.percussion.pagemanagement.assembler.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.extension.IPSExtensionDef;
import com.percussion.pagemanagement.data.PSResourceLocation;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.services.assembly.IPSTemplateService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.publisher.IPSTemplateExpander;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.utils.guid.IPSGuid;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This template expander expands content items so that they are associated
 * with our resource template.
 * The resource template uses the resource assembler.
 * @author adamgent
 *
 */
public class PSResourceTemplateExpander extends PSAbstractTemplateExpanderAdapter<PSResourceTemplateExpander.TemplateCache> 
    implements IPSTemplateExpander
{

    private String resourceAssemblyTemplate = PSAssemblyConfig.PERC_RESOURCE_ASSEMBLY_TEMPLATE;
    private IPSTemplateService templateService;
    private PSItemDefManager itemDefManager;
    private IPSGuidManager guidManager;
    private List<String> excludedContentTypes = new ArrayList<>();
    private IPSResourceDefinitionService resourceDefinitionService;
    private PSAssemblyItemBridge assemblyItemBridge;
    
    @Override
    protected List<PSContentListItem> expandContentListItem(PSContentListItem contentListItem, Map<String, String> parameters)
    {
        notNull(contentListItem, "contentListItem");
        String contentTypeName = getContentTypeName(contentListItem);

        if(log.isTraceEnabled())
            log.trace("Trying to expand: " + contentTypeName + " item: " + contentListItem);
        if (isPageType(contentTypeName)) {
            log.debug("Expanding page");
            if (contentListItem.getFolderId() == null) {
                log.error("Not filing this page item for content list: " + contentListItem.getItemId() +
                        " as it has no affiliated folder.");
                return emptyList();
            }
            setLocation(contentListItem, parameters);
            return asList(contentListItem);
        }
        else if (isTemplateType(contentTypeName)) {
            log.debug("Expanding template");
            return emptyList();
        }
        else if ( isContentTypePublishable(contentTypeName)) {
            /*
             * The item must be a shared resource
             */
            if (contentListItem.getFolderId() == null) {
                log.error("Not filing this item for content list: " + contentListItem.getItemId() +
                        " as it has no affiliated folder.");
                return emptyList();
            }
            log.debug("Expanding shared asset: " + contentTypeName);
            setLocation(contentListItem, parameters);
            return asList(contentListItem);
        }
        if(log.isDebugEnabled()) {
            log.debug("Could not expand: " + contentTypeName);
        }
        return emptyList();
        
        
    }

    /**
     * Gets the resource ID from the given parameters.
     * @param parameters the parameters of the expander, assumed not <code>null</code>.
     * @return the resource ID. It may be <code>null</code> if the parameters does not contain resource ID.
     */
    private String getResourceId(Map<String, String> parameters)
    {
        String resourceId = parameters.get("resourceId");
        return isNotBlank(resourceId) ? resourceId : null;
    }
    
    private void setLocation(PSContentListItem contentListItem, Map<String, String> parameters)
    {
        try{
            PSResourceLocation loc = assemblyItemBridge.getResourceLocation(contentListItem, getResourceId(parameters));
            contentListItem.setLocation(loc.getFilePath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void init(IPSExtensionDef def, @SuppressWarnings("unused") File file)
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        String assemblyTemplateProperty = getClass().getCanonicalName() + ".resourceAssemblyTemplate";
        String excludedContentTypesProperty = getClass().getCanonicalName() + ".excludedContentTypes";
        String template = def.getInitParameter(assemblyTemplateProperty);
        if (isNotBlank(template)) {
            setResourceAssemblyTemplate(template);
        }
        String[] ctypes = StringUtils.split(excludedContentTypesProperty, ",");
        if (ctypes != null)
            setExcludedContentTypes(asList(ctypes));
    }
    
    protected boolean isPageType(String contentTypeName) {
        return IPSPageService.PAGE_CONTENT_TYPE.equals(contentTypeName);
    }
    
    protected boolean isTemplateType(String contentTypeName) {
        return com.percussion.pagemanagement.service.IPSTemplateService.TPL_CONTENT_TYPE.equals(contentTypeName);
    }
    protected boolean isContentTypePublishable(String contentTypeName) 
    {
        boolean exclude = ! getExcludedContentTypes().contains(contentTypeName);
        if (exclude == false) return false;
        List<PSAssetResource> assetResources = 
            resourceDefinitionService.findAssetResourcesForType(contentTypeName);
        return ! assetResources.isEmpty();
    }

    @Override
    protected IPSGuid getTemplateId(Map<String, String> parameters, TemplateCache cache) throws IPSResourceDefinitionService.PSResourceDefinitionInvalidIdException, PSValidationException, IPSDataService.DataServiceNotFoundException, IPSDataService.DataServiceLoadException {
        if (cache.templateId != null)
            return cache.templateId;
        
        String templateName = getResourceAssemblyTemplate();
        String resourceid = getResourceId(parameters);
        if (resourceid != null)
        {
            PSResourceDefinition resource = resourceDefinitionService.findResource(resourceid);
            if (resource instanceof PSAssetResource)
                templateName = ((PSAssetResource)resource).getLegacyTemplate();
        }
        notEmpty(templateName, "resourceAssemblyTemplate");
        try
        {
            cache.templateId = getTemplateService().findTemplateByName(templateName).getGUID();
        }
        catch (PSAssemblyException e)
        {
            throw new RuntimeException("Failed to find resourceAssemblyTemplate for assembly template: " + templateName, e);
        }

        return cache.templateId;
    }
    
    
    
    
    @Override
    protected TemplateCache createTemplateCache()
    {
        return new TemplateCache();
    }


    protected String getContentTypeName(PSContentListItem contentListItem) 
    {
        IPSGuid contentId = contentListItem.getItemId();
        PSLocator locator = getGuidManager().makeLocator(contentId);
        Long contentTypeId = getItemDefManager().getItemContentType(locator);
        String name = null;
        
        if(contentTypeId != null && contentTypeId != -1){
	        try
	        {
	        	name = getItemDefManager().contentTypeIdToName(contentTypeId);
	        }
	        catch (PSInvalidContentTypeException e)
	        {
	        	log.error("The content list item " + contentId.toStringUntyped() + " has something wrong with its content type",e);
	            throw new RuntimeException("The content list item " + contentId.toStringUntyped() + " has something wrong with its content type", e);
	        }
        }else{
        	log.error("The content list item " + contentId.toStringUntyped() + " has something wrong with its content type");
            throw new RuntimeException("The content list item " + contentId.toStringUntyped() + " has something wrong with its content type");
            
        }
        return name;
    }
    
    protected static class TemplateCache {
        public IPSGuid templateId;
    }
    
    public List<String> getExcludedContentTypes()
    {
        return excludedContentTypes;
    }

    public void setExcludedContentTypes(List<String> excludedContentTypes)
    {
        this.excludedContentTypes = excludedContentTypes;
    }

    public IPSGuidManager getGuidManager()
    {
        return guidManager;
    }

    public void setGuidManager(IPSGuidManager guidManager)
    {
        this.guidManager = guidManager;
    }

    public PSItemDefManager getItemDefManager()
    {
        return itemDefManager;
    }

    public void setItemDefManager(PSItemDefManager itemDefManager)
    {
        this.itemDefManager = itemDefManager;
    }

    public String getResourceAssemblyTemplate()
    {
        return resourceAssemblyTemplate;
    }

    public void setResourceAssemblyTemplate(String resourceAssemblyTemplate)
    {
        this.resourceAssemblyTemplate = resourceAssemblyTemplate;
    }

    public IPSTemplateService getTemplateService()
    {
        return templateService;
    }

    public void setTemplateService(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    
    
    public IPSResourceDefinitionService getResourceDefinitionService()
    {
        return resourceDefinitionService;
    }

    public void setResourceDefinitionService(IPSResourceDefinitionService resourceDefinitionService)
    {
        this.resourceDefinitionService = resourceDefinitionService;
    }

    public PSAssemblyItemBridge getAssemblyItemBridge()
    {
        return assemblyItemBridge;
    }

    public void setAssemblyItemBridge(PSAssemblyItemBridge assemblyItemBridge)
    {
        this.assemblyItemBridge = assemblyItemBridge;
    }



    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSResourceTemplateExpander.class);
    

}

