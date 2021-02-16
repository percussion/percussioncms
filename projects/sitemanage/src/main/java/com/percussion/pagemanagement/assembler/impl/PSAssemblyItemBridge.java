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

import static com.percussion.pagemanagement.assembler.impl.PSAssemblyConfig.PERC_RESOURCE_BINDING_NAME;
import static com.percussion.pagemanagement.assembler.impl.PSAssemblyConfig.PERC_RESOURCE_ID_PARAM_NAME;
import static com.percussion.pagemanagement.assembler.impl.PSAssemblyConfig.PREVIEW_PAGE_BINDING_NAME;
import static com.percussion.pagemanagement.assembler.impl.PSAssemblyConfig.PREVIEW_TEMPLATE_BINDING_NAME;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import java.util.Collections;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.pagemanagement.assembler.IPSRenderLinkContextFactory;
import com.percussion.pagemanagement.assembler.PSRenderAsset;
import com.percussion.pagemanagement.data.PSEmptyPage;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;
import com.percussion.pagemanagement.data.PSResourceLocation;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderLinkService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.pagemanagement.service.IPSResourceLinkAndLocationService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.rx.publisher.IPSAssemblyResultExpander;
import com.percussion.services.assembly.IPSAssemblyItem;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.contentmgr.IPSNodeDefinition;
import com.percussion.services.publisher.data.PSContentListItem;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSJcrNodeMap;
import com.percussion.share.data.PSItemSummaryUtils;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.content.IPSContentDesignWs;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Manipulates and converts assembly items 
 * to resource objects.
 * <strong>This class is private and should not be used outside of legacy assembly.</strong>
 * <p>
 * This is very loosely based on 
 * the bridge pattern as we are separating
 * the legacy AssemblyItem from our new domain objects.
 * 
 * @see PSResourceInstance
 * @see PSRenderAsset
 * @see PSPage
 * 
 * @author adamgent
 *
 */
@PSSiteManageBean("assemblyItemHelper")
public class PSAssemblyItemBridge {

    
    /*
     * The number of collaborators here is ridiculous.
     * Need to look into making this better as this class
     * might be doing to much.
     */
    private IPSResourceDefinitionService resourceDefinitionService;
    private IPSRenderLinkContextFactory renderLinkContextFactory;
    private IPSResourceLinkAndLocationService resourceLinkandLocationService;
    private IPSRenderLinkService renderLinkService;
    private IPSFolderHelper folderHelper;
    private IPSAssetService assetService;
    private IPSIdMapper idMapper;
    private PSItemIdResolver itemIdResolver;
    private IPSPageService pageService;
    private IPSTemplateService templateService;
    private IPSSiteTemplateService siteTemplateService;
    private IPSSiteDataService siteDataService;
    private PSItemDefManager itemDefManager;
    private IPSContentDesignWs contentDesignWs;

    @Autowired
    protected PSAssemblyItemBridge(
            IPSResourceDefinitionService resourceDefinitionService,
            IPSRenderLinkContextFactory renderLinkContextFactory,
            IPSResourceLinkAndLocationService resourceLinkandLocationService, IPSRenderLinkService renderLinkService,
            IPSFolderHelper folderHelper, IPSAssetService assetService, IPSIdMapper idMapper,
            PSItemIdResolver itemIdResolver, IPSPageService pageService, IPSTemplateService templateService,
            IPSSiteTemplateService siteTemplateService, IPSSiteDataService siteDataService,
            PSItemDefManager itemDefManager, IPSContentDesignWs contentDesignWs)
    {
        super();
        this.resourceDefinitionService = resourceDefinitionService;
        this.renderLinkContextFactory = renderLinkContextFactory;
        this.resourceLinkandLocationService = resourceLinkandLocationService;
        this.renderLinkService = renderLinkService;
        this.folderHelper = folderHelper;
        this.assetService = assetService;
        this.idMapper = idMapper;
        this.itemIdResolver = itemIdResolver;
        this.pageService = pageService;
        this.templateService = templateService;
        this.siteTemplateService = siteTemplateService;
        this.siteDataService = siteDataService;
        this.itemDefManager = itemDefManager;
        this.contentDesignWs = contentDesignWs;
    }

    /**
     * Creates a resource instance from a content list item for generating locations
     * during content list generating
     * @param item never <code>null</code>.
     * @param resourceId the unique resource ID. It may be blank or <code>null</code> if the resource is determined by the content type of the item.
     * @return never <code>null</code>.
     */
    public PSResourceInstance createResourceInstance(PSContentListItem item, String resourceId) throws Exception {

        IPSLinkableItem linkableItem = createLinkableItem(item.getItemId());
        PSRenderLinkContext context = this.renderLinkContextFactory.create(item, linkableItem);
        if (isBlank(resourceId))
        {
            PSAssetResource resourceDef = resourceDefinitionService.findDefaultAssetResourceForType(linkableItem.getType());
            resourceId = resourceDef.getUniqueId();
        }
        
        return resourceLinkandLocationService.createResourceInstance(context, linkableItem, resourceId);
    }
    
    /**
     * Convenience method that gets the first resource location from an assembly item.
     * If the resource is paginated this would only return the first page.
     * @param listItem never <code>null</code>.
     * @param resourceId the resource ID. It may be blank if the resource is determined by the content type of the item.
     * @return never <code>null</code>.
     */
    public PSResourceLocation getResourceLocation(PSContentListItem listItem, String resourceId) throws Exception {
        PSResourceInstance resource;

        resource = createResourceInstance(listItem, resourceId);

        List<PSResourceLinkAndLocation> linkAndLocations = resourceLinkandLocationService.resolveLinkAndLocations(resource);
        if (linkAndLocations.isEmpty()) return null;
        return linkAndLocations.get(0).getResourceLocation();
    }    
    
    /**
     * Loads the content node for the specified item.
     * 
     * @param guid the item, assumed not <code>null</code>.
     * 
     * @return the node of the item, not <code>null</code>.
     * 
     * @throws Exception if an error occurs.
     */
    private Node getNode(IPSGuid guid) throws Exception 
    {
        List<IPSGuid> guids = Collections.singletonList(guid);
        return contentDesignWs.findNodesByIds(guids, true).get(0);
    }
    
    /**
     * Creates a linkable item from an asset guid.
     * @param guid The asset item guid, may not be <code>null</code>
     * 
     * @return The item, never <code>null</code>.
     * @see IPSLinkableItem
     */
    public IPSLinkableItem createLinkableItem(IPSGuid guid) throws Exception
    {
        Validate.notNull(guid);
        
        return PSPathUtils.getLinkableItem(idMapper.getString(guid));
    }
    
    /**
     * Creates a resource instance from an assembly item.
     * @param assemblyItem never <code>null</code>.
     * @return never <code>null</code>.
     */
    public PSResourceInstance createResourceInstance(IPSAssemblyItem assemblyItem) throws PSAssemblyException {
        
        PSResourceInstance rvalue = getResourceInstance(assemblyItem);
        if (rvalue != null)
            return rvalue;
        
        String resourceDefinitionId = getResourceDefinitionId(assemblyItem);
        notNull(resourceDefinitionId, "resourceDefinitionId");
        String id = idMapper.getString(assemblyItem.getId());
        IPSLinkableItem linkableItem = PSPathUtils.getLinkableItem(id);
        PSRenderLinkContext context = getRenderLinkContext(assemblyItem, linkableItem);

        try {
            return resourceLinkandLocationService.createResourceInstance(context, linkableItem, resourceDefinitionId);
        } catch (IPSDataService.DataServiceNotFoundException | IPSDataService.DataServiceLoadException | IPSAssetService.PSAssetServiceException e) {
            throw new PSAssemblyException(22,e,id);
        }
    }
    
    /**
     * Convenience method that gets the first resource location from an assembly item.
     * If the resource is paginated this would only return the first page.
     * @param assemblyItem never <code>null</code>.
     * @return never <code>null</code>.
     */
    public PSResourceLocation getResourceLocation(IPSAssemblyItem assemblyItem) throws PSAssemblyException, IPSAssetService.PSAssetServiceException {
        PSResourceInstance resource = createResourceInstance(assemblyItem);
        List<PSResourceLinkAndLocation> linkAndLocations = resourceLinkandLocationService.resolveLinkAndLocations(resource);
        if (linkAndLocations.isEmpty()) return null;
        return linkAndLocations.get(0).getResourceLocation();
    }

    /**
     * Gets the cached resource instance that is bound to the assembly item.
     * <p>
     * <em>Notice that the return value maybe null</em>
     * 
     * @param item never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public PSResourceInstance getResourceInstance(IPSAssemblyItem item) {
        notNull(item, "item");
        return (PSResourceInstance) item.getBindings().get(PERC_RESOURCE_BINDING_NAME);
    }
    
    /**
     * Tells the assembly item to use the resource assembly result expander
     * during publishing.
     *  
     * @param item
     * @see PSResourceAssemblyResultExpander
     */
    public void setAssemblyResultExpander(IPSAssemblyItem item) {
        item.setParameterValue(IPSAssemblyResultExpander.ASSEMBLY_RESULT_EXPANDER_PARAM, 
                PSResourceAssemblyResultExpander.ASSEMBLY_RESULT_EXPANDER_NAME);
    }
    
    public void setResourceInstance(IPSAssemblyItem item, PSResourceInstance ri) {
        notNull(ri, "ri");
        item.getBindings().put(PERC_RESOURCE_BINDING_NAME, ri);
    }
    
    
    /**
     * Creates a render asset from an assembly item.
     * @param assemblyItem
     * @return never <code>null</code>.
     * @see PSRenderAsset
     */
    public PSRenderAsset createRenderAsset(IPSAssemblyItem assemblyItem) throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        PSRenderAsset asset = new PSRenderAsset();
        String id = idMapper.getString(assemblyItem.getId());
        PSAssetSummary sum = assetService.find(id);
        PSItemSummaryUtils.copyProperties(sum, asset);
        asset.setNode(assemblyItem.getNode());
        asset.setFields(new PSJcrNodeMap(assemblyItem.getNode()));
        
        int folderId = assemblyItem.getFolderId();
        String path = null;
        /*
         * See if the assembly items folder is ok to use.
         */
        if (folderId > 0)
        {
            try
            {
                path = folderHelper.findPathFromLegacyFolderId(folderId);

            }
            catch (Exception e)
            {
                log.warn("Error generating folder path for folder id: " + folderId, e);
            }
        }
        /*
         * If the assembly items folder is not ok then we need to
         * resolve the path.
         */
        if (path == null || ! asset.getFolderPaths().contains(path)) {
            notNull(assemblyItem.getSiteId(), 
                    "The assemblyItem must have a site associated with it.");
            String siteId = idMapper.getString(assemblyItem.getSiteId());
            PSSiteSummary site = siteDataService.findByLegacySiteId(siteId, false);
            path = renderLinkService.resolveFolderPath(asset, site);
        }
        
        /*
         * TODO: Well if we can't find a folder path we will at least log it.
         * We should check if its a local asset. If it is then we should 
         * not care if it has a folder path or not.
         */
        if (path == null) {
            if (log.isDebugEnabled())
                log.debug("Could not find a proper folder path for item: " + asset);
        }
        
        asset.setFolderPath(path);
        
        asset.setOwnerId(assemblyItem.getOwnerId());
        
        return asset;
    }
    
    /**
     * Finds the resources definitions associated with the given assembly items
     * <em>content type</em>. For page content type, only return the primary resource.
     * @param assemblyItem never <code>null</code>.
     * @return never <code>null</code> maybe empty.
     */
    public List<PSAssetResource> getResourceDefinitions(IPSAssemblyItem assemblyItem) throws IPSResourceDefinitionService.PSResourceDefinitionNotFoundException {
        String contentType = getContentType(assemblyItem);
        if (IPSPageService.PAGE_CONTENT_TYPE.equals(contentType))
        {
            PSAssetResource pageResource = resourceDefinitionService.findDefaultAssetResourceForType(contentType);
            return Collections.singletonList(pageResource);
        }
        else
        {
            return resourceDefinitionService.findAssetResourcesForType(contentType);
        }
    }
    
    protected PSRenderLinkContext getRenderLinkContext(IPSAssemblyItem assemblyItem, IPSLinkableItem item) {
        return renderLinkContextFactory.create(assemblyItem, item);
    }
    
    
    /**
     * Gets the resource definition id bound to this assembly item.
     * @param assemblyItem never <code>null</code>.
     * @return maybe <code>null</code>.
     */
    public String getResourceDefinitionId(IPSAssemblyItem assemblyItem) {
        notNull(assemblyItem, "assemblyItem");
        String rid = assemblyItem.getParameterValue(PERC_RESOURCE_ID_PARAM_NAME, null);
        return rid;
    }
    
    /**
     * Sets the resource definition id to this assembly item.
     * @param assemblyItem never <code>null</code>.
     * @param resourceId maybe <code>null</code>.
     */
    public void setResourceDefinititionId(IPSAssemblyItem assemblyItem, String resourceId) {
        notNull(assemblyItem, "assemblyItem");
        assemblyItem.setParameterValue(PERC_RESOURCE_ID_PARAM_NAME, resourceId);
    }
    
    /**
     * Gets the content type from the given assembly item.
     * 
     * @param assemblyItem the item in question, assumed not <code>null</code>.
     * 
     * @return the content type of the item, never blank.
     */
    public String getContentType(IPSAssemblyItem assemblyItem)
    {
        return getContentType(assemblyItem.getNode());
    }
    
    /**
     * Gets the content type from the given jcr node.
     * 
     * @param node the item in question, assumed not <code>null</code>.
     * 
     * @return the content type of the item, never blank.
     */
    public String getContentType(Node node)
    {
        notNull(node, "node");
        IPSNode ourNode = (IPSNode)node;
        try
        {
            String contentType = ((IPSNodeDefinition) ourNode.getDefinition()).getInternalName();
            return contentType;
        }
        catch (RepositoryException e)
        {
            String msg = "Failed to get Content Type for content ID = " + ourNode.getGuid().toString();
            log.error(msg, e);
            throw new IllegalArgumentException(msg, e);
        }        
    }
    
    private static List<String> htmlControls = asList("sys_tinymce");
    
    /**
     * Determines if the field for a given content type is an html field.
     * @param contentType never <code>null</code> or empty.
     * @param fieldName never <code>null</code> or empty.
     * @return never <code>null</code>.
     */
    public boolean isHtmlField(String contentType, String fieldName) {
        fieldName = removeStart(fieldName, "rx:");
        PSContentEditor editorDef = getContentEditor(contentType, fieldName);
        return htmlControls.contains(editorDef.getFieldControl(fieldName).getName());
    }
    
    /**
     * Determines if the field exists for a given content type.
     * @param contentType never <code>null</code> or empty.
     * @param fieldName never <code>null</code> or empty.
     * @return never <code>null</code>.
     */
    public boolean hasField(String contentType, String fieldName) {
        fieldName = removeStart(fieldName, "rx:");
        PSContentEditor editorDef = getContentEditor(contentType, fieldName);
        return editorDef.getFieldUiSet(fieldName) != null;
    }

    private PSContentEditor getContentEditor(String contentType, String fieldName)
    {
        notEmpty(contentType);
        notEmpty(fieldName);
        Long typeId;
        try
        {
            typeId = itemDefManager.contentTypeNameToId(contentType);
        }
        catch (PSInvalidContentTypeException e)
        {
            throw new RuntimeException("Invalid content type:", e);
        }
        PSContentEditor editorDef = itemDefManager.getContentEditorDef(typeId);
        return editorDef;
    }
    
    
    protected static void setPage(IPSAssemblyItem item, PSPage page) {
        item.getBindings().put(PREVIEW_PAGE_BINDING_NAME, page);
    }
    
    protected static void setTemplate(IPSAssemblyItem item, PSTemplate template) {
        item.getBindings().put(PREVIEW_TEMPLATE_BINDING_NAME, template);
    }
    
    protected static PSPage getPage(IPSAssemblyItem item) {
        return (PSPage) item.getBindings().get(PREVIEW_PAGE_BINDING_NAME);
    }
    
    protected static PSTemplate getTemplate(IPSAssemblyItem item) {
        return (PSTemplate) item.getBindings().get(PREVIEW_TEMPLATE_BINDING_NAME);
    }
    

    /**
     * Sets the delivery location on a assembly item.
     * @param workitem never <code>null</code>.
     * @param location maybe <code>null</code>.
     */
    public static void setDeliveryLocation(IPSAssemblyItem workitem, String location)
    {
       
       String oldContext = workitem.getParameterValue(
             IPSHtmlParameters.SYS_CONTEXT, null);
       
       workitem.setParameterValue(IPSHtmlParameters.SYS_CONTEXT, 
             String.valueOf(workitem.getDeliveryContext()));

       workitem.setDeliveryPath(location);
       
       workitem.setParameterValue(IPSHtmlParameters.SYS_CONTEXT, 
             oldContext);
    }
    
    
    
    /**
     * Gets the page and template from an assembly item.
     * <p>
     * If the assembly item is a template then a {@link PSEmptyPage} will be used for the page.
     * The cached results of this method will be stored in the assembly item so there is no
     * performance penatly for call this method multiple times on an assembly item.
     *  
     * @param item never <code>null</code>.
     * @return never <code>null</code>.
     * @throws RepositoryException
     */
    public TemplateAndPage getTemplateAndPage(IPSAssemblyItem item) throws RepositoryException {
        String contentType = ((IPSNodeDefinition) ((IPSNode) item.getNode()).getDefinition()).getInternalName();
        log.debug("Getting template and page for assembly item: " + item.getId() + " of type: " + contentType);
        TemplateAndPage tp = new TemplateAndPage();
        tp.page = getPage(item);
        tp.template = getTemplate(item);
        
        if (tp.page != null && log.isDebugEnabled())
            log.debug("Using preview page set in bindings");
        else if( log.isDebugEnabled() )
            log.debug("Page was not set in bindings.");
        
        if (tp.template != null && log.isDebugEnabled())
            log.debug("Using preview template set in bindings");
        else if( log.isDebugEnabled() )
            log.debug("Template was not set in bindings.");
        
        String idTemplateOrPage = itemIdResolver.getId(item);
        
        if (IPSTemplateService.TPL_CONTENT_TYPE.equals(contentType)) {
            if (tp.template == null) {
                tp.template = templateService.load(idTemplateOrPage);
            }
            if (tp.page == null) {
                /*
                 * Create a blank page for template preview.
                 */
                tp.page = createEmptyPage(item, tp.template);
                tp.page.setTemplateId(idTemplateOrPage);
                tp.itemType = TemplateAndPage.ItemType.TEMPLATE;
            }
        }
        else {
            if (tp.page == null) {
                try {
                    tp.page = pageService.load(idTemplateOrPage);
                } catch (IPSDataService.DataServiceLoadException | IPSDataService.DataServiceNotFoundException e) {
                    throw new RepositoryException(e.getMessage(),e);
                }
            }
            if (tp.template == null) {
                String templateId = tp.page.getTemplateId();
                notNull(templateId);
                tp.template = templateService.load(templateId);
            }
        }
        
        if (tp.page != null )
            itemIdResolver.updateItemId(tp.page);
        if (tp.template != null)
            itemIdResolver.updateItemId(tp.template);
        
        notNull(tp.page, "page");
        notNull(tp.template, "template");
        setPage(item, tp.page);
        setTemplate(item, tp.template);
        return tp;
    }
    
    /**
     * Creates an empty page based on the assembly item.
     * @param assemblyItem never <code>null</code>.
     * @param template never <code>null</code>.
     * @return never <code>null</code>.
     */
    protected PSEmptyPage createEmptyPage(IPSAssemblyItem assemblyItem, PSTemplateSummary template) {
        log.debug("Creating blank page");
        notNull(template, "template");
        List<PSSiteSummary> sites =  siteTemplateService.findSitesByTemplate(template.getId());
        if (sites.isEmpty()) {
            //TODO Improve this error handling.
            throw new RuntimeException("Could not find site associated to template: " + template);
        }
        
        if (sites.size() > 1) {
            log.warn("Template is associated with multiple sites. Template: " + template);
        }
        
        PSEmptyPage page =  new PSEmptyPage();
        page.setFolderPath(sites.get(0).getFolderPath());
        page.setType(IPSPageService.PAGE_CONTENT_TYPE);
        return page;
    }
    
    /**
     * 
     * Container that holds the template and page corresponding to
     * a rendering request (assembly item).
     * 
     * @author adamgent
     *
     */
    public static class TemplateAndPage {
        private PSTemplate template;
        private PSPage page;
        private ItemType itemType = ItemType.PAGE;

        /**
         * @return never <code>null</code>.
         */
        public PSTemplate getTemplate()
        {
            return template;
        }

        /**
         * @return never <code>null</code>.
         */
        public PSPage getPage()
        {
            return page;
        }
        
        /**
         * What type the underlying object that is being rendered.
         * <p>
         * If its a {@link ItemType#TEMPLATE} then {@link #getPage()} will
         * be a {@link PSEmptyPage}.
         * @return never <code>null</code>.
         */
        public ItemType getItemType()
        {
            return itemType;
        }

        public static enum ItemType {
            PAGE,TEMPLATE;
        }
        
        
    }
    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Log log = LogFactory.getLog(PSAssemblyItemBridge.class);
    
    
}

