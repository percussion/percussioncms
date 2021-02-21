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
package com.percussion.pagemanagement.service.impl;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.cms.PSSingleValueBuilder;
import com.percussion.error.PSException;
import com.percussion.pagemanagement.assembler.IPSRenderLinkContextFactory;
import com.percussion.pagemanagement.assembler.PSAbstractAssemblyContext.EditType;
import com.percussion.pagemanagement.assembler.impl.PSLegacyLinkGenerator;
import com.percussion.pagemanagement.assembler.impl.PSLegacyLinkGenerator.PSLegacyLink;
import com.percussion.pagemanagement.assembler.impl.PSResourceInstanceHelper;
import com.percussion.pagemanagement.data.IPSResourceDefinitionVisitor;
import com.percussion.pagemanagement.data.PSInlineLinkRequest;
import com.percussion.pagemanagement.data.PSInlineRenderLink;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRenderLink;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSRenderLinkContext.Mode;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFileResource.PSFileResourceType;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSFolderResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSResourceDefinition;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;
import com.percussion.pagemanagement.data.PSThemeResource;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSRenderLinkService;
import com.percussion.pagemanagement.service.IPSResourceDefinitionService;
import com.percussion.pagemanagement.service.IPSResourceLinkAndLocationService;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.services.linkmanagement.IPSManagedLinkDao;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSAbstractFilter;
import com.percussion.share.data.PSAbstractTransformer;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.IPSDataService.PSThemeNotFoundException;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSSpringValidationException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.impl.PSThemeService;
import com.percussion.validation.ValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.helper.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.percussion.pagemanagement.assembler.PSResourceLinkAndLocationUtils.concatPath;
import static com.percussion.pagemanagement.assembler.PSResourceLinkAndLocationUtils.escapePathForUrl;
import static com.percussion.share.web.service.PSRestServicePathConstants.ID_PATH_PARAM;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;


/**
 * 
 * See the interfaces for documentation.
 * @author adamgent
 *
 */
@Path("/renderlink")
@Component("renderLinkService")
@Lazy
public class PSRenderLinkService implements IPSRenderLinkService, IPSResourceLinkAndLocationService
{
    /**
     * Name of the system (shared) resources dependencies file.
     */
    private static final String systemResourcesFileName = "percSystem";

    private IPSPageService pageService;

    private IPSResourceDefinitionService resourceDefinitionService;

    private IPSRenderLinkContextFactory renderLinkContextFactory;
    
    private PSLegacyLinkGenerator legacyLinkGenerator;
    
    private IPSManagedLinkDao managedLinkDao;
    
    private PSResourceInstanceHelper resourceInstanceHelper;
    
    private PSThemeService themeService;
    
    private IPSIdMapper idMapper;
    
    private static final IPSFolderPath assetsRootFolderPath = PSFolderPathUtils.toFolderPath(PSAssetPathItemService.ASSET_ROOT);
    private static final IPSFolderPath sitesRootFolderPath = PSFolderPathUtils.toFolderPath("//Sites");
        
    private String themePreviewUrlBase = "/Rhythmyx/web_resources";
    
    private String filePreviewUrlBase = "/Rhythmyx";
    
    @Autowired
    public PSRenderLinkService(IPSIdMapper idMapper,PSLegacyLinkGenerator legacyLinkGenerator,
            IPSPageService pageService, IPSRenderLinkContextFactory renderLinkContextFactory,
            IPSResourceDefinitionService resourceDefinitionService, PSResourceInstanceHelper resourceInstanceHelper, 
            PSThemeService themeService, IPSManagedLinkDao managedLinkDao)
    {
        super();
        this.idMapper = idMapper;
        this.legacyLinkGenerator = legacyLinkGenerator;
        this.pageService = pageService;
        this.renderLinkContextFactory = renderLinkContextFactory;
        this.resourceDefinitionService = resourceDefinitionService;
        this.resourceInstanceHelper = resourceInstanceHelper;
        this.themeService = themeService;
        this.managedLinkDao = managedLinkDao;
    }

    @Override
    public List<PSResourceLinkAndLocation> resolveLinkAndLocations(PSResourceInstance resourceInstance) throws IPSAssetService.PSAssetServiceException {
        return resourceInstanceHelper.getLinkAndLocations(resourceInstance);
    }

    /**
     * {@inheritDoc}
     */
    public PSRenderLink renderLink(PSRenderLinkContext context, IPSLinkableItem item) throws PSDataServiceException {
        PSAssetResource r = resolveResourceDefinition(null, null, item.getType());
        return renderLinkHelper(context, r, item);
    }

    /**
     * {@inheritDoc}
     */
    public PSRenderLink renderLink(PSRenderLinkContext context, IPSLinkableItem item, String resourceDefinitionId) throws PSDataServiceException {
        notNull(resourceDefinitionId, "resourceDefinitionId");
        PSAssetResource r = resolveResourceDefinition(resourceDefinitionId, null, null);
        return renderLinkHelper(context, r, item);
    }
    
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public List<PSRenderLink> renderCssLinks(PSRenderLinkContext context, Set<String> widgetDefIds) throws DataServiceLoadException, DataServiceNotFoundException {
        return renderFileLinks(context, PSFileResourceType.css, widgetDefIds);
    }
    
    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public List<PSRenderLink> renderJavascriptLinks(PSRenderLinkContext context, Set<String> widgetDefIds) throws DataServiceLoadException, DataServiceNotFoundException {
        return renderFileLinks(context, PSFileResourceType.javascript, widgetDefIds);
    }


    /**
     * Returns a list of files links sorted by depdendencies.
     * 
     * @param context never <code>null</code>.
     * @param type the file type.
     * @return never <code>null</code>, maybe empty.
     */
    private List<PSRenderLink> renderFileLinks(final PSRenderLinkContext context, final PSFileResourceType type, final Set<String> widgetDefIds) throws DataServiceLoadException, DataServiceNotFoundException {
        List<PSResourceDefinition> resources = resourceDefinitionService.findAllResources();
        notNull(resources, "resources should not be null");
         
        /*
         * Filter out the resources that are not files and of the given type.
         */
        resources = new PSAbstractFilter<PSResourceDefinition>() {

            @Override
            public boolean shouldKeep(PSResourceDefinition resource)
            {
                return  (resource instanceof PSFileResource && 
                        ((PSFileResource)resource).getType() == type &&
                        (((PSFileResource)resource).getContext()==null ||
                        ((PSFileResource)resource).getContext().toString().equals(context.getMode().toString())) &&
                         (widgetDefIds.contains(resource.getGroupId()) || 
                          resource.getGroupId().equalsIgnoreCase(systemResourcesFileName)));
            }
        
        }.filter(resources);
        
        /*
         * Remove the duplicated resources (eg: perc_common_ui.js is included in several files)
         */
        resources = removeDuplicatedResources(resources);
        
        /*
         * Sort the resources by dependencies.
         */
        resources = PSResourceDefinitionUtils.sortByDependencies(resources);
        
        /*
         * Now turn them into links.
         */
        return  new PSAbstractTransformer<PSResourceDefinition, PSRenderLink>() {

            @Override
            protected PSRenderLink doTransform(PSResourceDefinition old) throws PSDataServiceException {
                return renderLink(context, old.getUniqueId());
            }
        
        }.collect(resources);
        
    }

    /**
     * Removes any duplicated resources based on the Id value (it's not a comparison between objects, but of its file names)
     * 
     * @author federicoromanelli
     * @param resources the list of resources that may contain duplicated values (comparison is based on id, not uniqueId).
     * @return the list of resources with the duplicated object deleted, or the original list if objects couldn't be removed,
     * never <code>null</code>.
     */
    private List<PSResourceDefinition> removeDuplicatedResources(List<PSResourceDefinition> resources)
    {
        List <PSResourceDefinition> res = resources;
        List <PSResourceDefinition> resourcesToDelete = new ArrayList<>();
        
        HashSet<String> uniqueResources = new HashSet<>();
        Iterator<PSResourceDefinition> iterator = res.iterator();
        
        while(iterator.hasNext())
        {
            PSResourceDefinition resourceDef = iterator.next();
            String id = resourceDef.getId();
            if (!uniqueResources.contains(id))
            {
                uniqueResources.add(id);
            }
            else
            {
                resourcesToDelete.add(resourceDef);
            }
        }
        if (res.removeAll(resourcesToDelete))
        {
            return res;
        }
        
        return resources;
    }
    
    @GET
    @Path("/preview/{id}/page")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSInlineRenderLink renderPreviewPageLink(@PathParam(ID_PATH_PARAM) String pageId)
    {
        try {
            return renderPreviewPageLink(pageId, "html");
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    public PSInlineRenderLink renderPreviewPageLink(String pageId, String renderType) throws PSDataServiceException {
        notNull(pageId, "pageId");
        PSPage page = null;
        try
        {
            page = pageService.find(pageId);
        }
        catch (DataServiceNotFoundException | DataServiceLoadException | PSValidationException e)
        {
            log.error("page target {} does not exist. Error: {}",pageId, e.getMessage());
            log.debug(e.getMessage(),e);
            try
            {
                managedLinkDao.cleanupOrphanedLinks();
            }
            catch (Exception ex)
            {
                log.error("Cannot cleanup orphaned links.  Error: {}", ex.getMessage());
                log.debug(ex.getMessage(),ex);
            }
            return null;
        }
        if (page == null)
            return null;
        
        page.setId(pageId);
        PSRenderLinkContext context = renderLinkContextFactory.createPreview(page);
        PSInlineRenderLink renLink = new PSInlineRenderLink();
        String resourceId = "percSystem.page";
        if ("xml".equalsIgnoreCase(renderType))
            resourceId = "percSystem.pageXml";
        else if ("database".equalsIgnoreCase(renderType))
            resourceId = "percSystem.pageDatabase";
            
        PSAssetResource r = resolveResourceDefinition(resourceId, null, null);
        renderLinkHelper(renLink, context, r, page);
        renLink.setStateClass(getStateClass(pageId));
        renLink.setTitle(page.getLinkTitle());
        legacyLinkGenerator.addLegacyDataToInlineLink(renLink, page);
        return renLink;
    }

    @GET
    @Path("/preview/{id}/default")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSInlineRenderLink renderPreviewLink(
            
            @PathParam(ID_PATH_PARAM) String targetId) {
        try {
            return renderPreviewLink(targetId, null, null);
        } catch (DataServiceLoadException | DataServiceNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @GET
    @Path("/preview/{id}/{resourceDef}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSInlineRenderLink renderPreviewLink(
            
            @PathParam(ID_PATH_PARAM) String targetId,
            @PathParam("resourceDef") String resourceDefinitionId) {
        try {
            return renderPreviewLink(targetId, resourceDefinitionId, null);
        }
        catch (DataServiceLoadException | DataServiceNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @GET
    @Path("/preview/{id}/{resourceDef}/{thumbResourceDef}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSInlineRenderLink renderPreviewLink(
            
            @PathParam(ID_PATH_PARAM) String targetId,
            @PathParam("resourceDef") String resourceDefinitionId, 
            @PathParam("thumbResourceDef") String thumbResourceDefinitionId) throws DataServiceLoadException, DataServiceNotFoundException {

        try {
        IPSItemSummary itemSummary = resourceInstanceHelper.findResourceAsset(targetId);
        if (itemSummary.isPage()) {
            return renderPreviewPageLink(targetId);
        }
        
        PSInlineLinkRequest lr = new PSInlineLinkRequest();
        lr.setTargetId(targetId);
        lr.setResourceDefinitionId(resourceDefinitionId);
        lr.setThumbResourceDefinitionId(thumbResourceDefinitionId);

            return renderPreviewResourceLink(lr);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @POST
    @Path("/preview")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSInlineRenderLink renderPreviewResourceLink(PSInlineLinkRequest request) throws DataServiceNotFoundException, DataServiceLoadException {

        try {
            PSBeanValidationUtils.validate(request).throwIfInvalid();
        } catch (PSSpringValidationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
        PSAsset asset;
        try {
             asset = resourceInstanceHelper.loadPartialAsset(request.getTargetId());
        } catch (IPSAssetService.PSAssetServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }

        if (asset == null)
        {
            log.error("link target {} does not exist",request.getTargetId());
            try
            {
                managedLinkDao.cleanupOrphanedLinks();
            }
            catch (Exception e)
            {
                log.error("Cannot cleanup orphaned links. Error: {}", e.getMessage());
                log.debug(e.getMessage(),e);
            }
            return null;
        }
       
       
        List<PSAssetResource> resources = resourceDefinitionService.findAssetResourcesForType(asset.getType());
        
        /**
         * here we are assuming at most two resources, binary
         * and thumb nail binary.  currently assuming the first
         * resource that is not a thumb nail is the main binary type
         */
        for(PSAssetResource res : resources) {
            if(res.getId().toLowerCase().contains("thumbbinary")) {
                request.setThumbResourceDefinitionId(res.getUniqueId());
            }
            else {
                request.setResourceDefinitionId(res.getUniqueId());
            }
        }
        
        String folderPath = resolveFolderPathForAsset(asset);
        
        PSRenderLinkContext context = renderLinkContextFactory.createAssetPreview(folderPath,asset);
        PSLinkableAsset linkAsset = new PSLinkableAsset(asset, folderPath);

        PSInlineRenderLink renLink = new PSInlineRenderLink();
        try {
        /*
         * Resolve the targets resource definition.
         */
        PSAssetResource rd = resolveResourceDefinition(
                request.getResourceDefinitionId(), 
                null,
                asset.getType());
        isTrue(rd != null, "Target does not have a resource definition: ", asset);


            renderLinkHelper(renLink, context, rd, linkAsset);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }

        /*
         * Resolve the targets thumbnail resource definition
         * if it has one.
         */
        if (isNotBlank(request.getThumbResourceDefinitionId())) {
            try {
            PSAssetResource thumbNailResource = 
                resolveResourceDefinition(request.getThumbResourceDefinitionId(), null, null);

               if (thumbNailResource != null) {
                   PSInlineRenderLink thumbLink = new PSInlineRenderLink();
                   renderLinkHelper(thumbLink, context, thumbNailResource, linkAsset);
                   renLink.setThumbUrl(thumbLink.getUrl());
                   renLink.setThumbResourceDefinition(thumbLink.getResourceDefinition());
               }
           } catch (PSDataServiceException e) {
               log.error(e.getMessage());
               log.debug(e.getMessage(),e);
               //It is just the thumbnail so don't completely fail out
           }
        }
        
        
        renLink.setStateClass(getStateClass(request.getTargetId()));
        renLink.setAltText((String) asset.getFields().get("alttext"));
        renLink.setTitle((String) asset.getFields().get("displaytitle"));
       
        /*
         * Fill in legacy data.
         */
        legacyLinkGenerator.addLegacyDataToInlineLink(renLink, request, asset);
        
        return renLink;
    }
    
    
    private String getStateClass(String id)
    {
    	String flag = PSSingleValueBuilder.getValidFlag(idMapper.getContentId(id));
        
    	String classValue="";
    	if (flag.equals("u"))
        {
    		classValue = PSSingleValueBuilder.PERC_BROKENLINK;
        } else if (!flag.equals("y") && !flag.equals("i"))
        {
        	classValue = PSSingleValueBuilder.PERC_NOTPUBLICLINK;
        }
        return classValue;
    }
    /**
     * {@inheritDoc}
     */
    public PSAssetResource resolveResourceDefinition(
            String resourceDefinitionId,  
            String legacyTemplate,
            String contentType) throws PSDataServiceException {
        if (resourceDefinitionId != null)
            return findAssetResourceDefinition(resourceDefinitionId);
        if (legacyTemplate != null)
            return findAssetResourceDefinitionForTemplate(legacyTemplate, contentType);
        if (contentType != null) 
            return resourceDefinitionService.findDefaultAssetResourceForType(contentType);
        throw new IllegalArgumentException("One of the arguments must not be null");
    }
    
    

    private String resolveFolderPathForAsset(PSAsset asset)
    {
        String folderPath = resolveFolderPath(asset);

        if (folderPath == null) {
            log.error("Asset does not have any valid folder paths: {}" , asset);
            notEmpty(folderPath);
        }
        else {
            log.debug("Resolved folder path to: {}", folderPath);
        }
        return folderPath;
    }
    
    
    /**
     * {@inheritDoc}
     * <p>
     * The assets or some other site folder path will be used if the other paths fail.
     */
    public String resolveFolderPath(IPSItemSummary item, IPSFolderPath ... paths) {
        ArrayList<IPSFolderPath> combined = new ArrayList<>();
        if (paths != null) {
            Collections.addAll(combined, paths);
        }
        
        if (item == null) {
            log.warn("Unable to resolveFolderPath() IPSItemSummary item  = null");
            return null;
        }
        else if (item.isPage()) {
            /*
             * If its a page will also accept other site folder paths.
             */
            combined.add(sitesRootFolderPath);
        }
        else {
            /*
             * If its not a page but an asset then we will accept asset folder paths.
             */
            combined.add(assetsRootFolderPath);
        }
        return PSFolderPathUtils.resolveFolderPath(item, combined.toArray(new IPSFolderPath[combined.size()]));
    }
    
    @Override
    public String resolveFolderPath(IPSItemSummary item)
    {
        return resolveFolderPath(item, (IPSFolderPath[]) null);
    }    
    
    private PSRenderLink renderLinkHelper(
            PSRenderLinkContext context,
            PSAssetResource resourceDefinition,
            IPSLinkableItem item) throws IPSAssetService.PSAssetServiceException, DataServiceNotFoundException, PSValidationException {
        PSRenderLink rl = new PSRenderLink();
        renderLinkHelper(rl, context, resourceDefinition, item);
        return rl;
    }
    
    /**
     * Process and fills a {@link PSRenderLink} object.
     * 
     * @param <T> a render link type to process.
     * @param renderLink never <code>null</code>.
     * @param context never <code>null</code>.
     * @param resourceDefinition not <code>null</code>.
     * @param item never <code>null</code>.
     */
    private <T extends PSRenderLink> void renderLinkHelper(
            T renderLink, 
            PSRenderLinkContext context, 
            PSAssetResource resourceDefinition,
            IPSLinkableItem item) throws IPSAssetService.PSAssetServiceException, DataServiceNotFoundException, PSValidationException {
        notNull(resourceDefinition, "resourceDefinition");
        if (log.isTraceEnabled())
            log.trace(format("Generating link for context:{0} resourceDefinitionId:{1} item:{2}", context, resourceDefinition, item));
        
        PSRenderLink rl = null;
        validateLinkContext(context);
        if (context.getMode() == PSRenderLinkContext.Mode.PUBLISH) {
            log.debug("Using resource instance to create location. Mode is publish");
            PSResourceInstance r = resourceInstanceHelper.createResourceInstance(context, item, resourceDefinition);
            List<PSResourceLinkAndLocation> links =  resourceInstanceHelper.getLinkAndLocations(r);
            rl = links.get(0).getRenderLink();
        }
        else {
            PSLegacyLink link = new PSLegacyLink();
            try {
                legacyLinkGenerator.fillLegacyLink(context, item, resourceDefinition, link);
                String url = legacyLinkGenerator.generate(link);
                rl = new PSRenderLink(url, resourceDefinition);
            }catch (ValidationException | PSException ex){
                log.warn("Error: {}", ex.getMessage());
                log.debug(ex.getMessage(),ex);
                rl = new PSRenderLink();
            }

        }
        
        //Making link pointing to section if canonical option is ON and set to "sections" 
        if (context.getSite().isCanonical() && context.getSite().getCanonicalDist().equals("sections")) {
    		String urlBase;
        	
        	//in case default document listed among params in url first take out params
        	if (rl.getUrl().indexOf("?") > 0) urlBase = rl.getUrl().substring(0, rl.getUrl().indexOf("?"));
        	else urlBase = rl.getUrl();
        	
        	//we need to remove the file name from url only if the it is a default document
        	if (urlBase.lastIndexOf(context.getSite().getDefaultDocument()) == rl.getUrl().lastIndexOf("/") + 1) {
            	String urlParams = "";
            	
            	if (rl.getUrl().indexOf("?") > 0) urlParams = rl.getUrl().substring(rl.getUrl().indexOf("?"));
        		urlBase = urlBase.substring(0, urlBase.lastIndexOf(context.getSite().getDefaultDocument()));
				rl.setUrl(urlBase + urlParams);
        	}
        }
        
        renderLink.setUrl(rl.getUrl());
        renderLink.setResourceType(rl.getResourceType());
        renderLink.setResourceDefinitionId(rl.getResourceDefinitionId());
        renderLink.setResourceDefinition(rl.getResourceDefinition());
    }
    
    /**
     * {@inheritDoc}
     */
    public PSResourceInstance createResourceInstance(PSRenderLinkContext context, IPSLinkableItem item, String resourceDefinitionId) throws PSDataServiceException {
        PSAssetResource rd = resolveResourceDefinition(resourceDefinitionId, null, item.getType());
        return resourceInstanceHelper.createResourceInstance(context, item, rd);
    }
    

    /**
     * {@inheritDoc}
     */
    public PSRenderLink renderLinkThemeRegionCSS(final PSRenderLinkContext context, String themeName, 
            boolean isEdit, EditType editType) throws PSThemeNotFoundException, PSValidationException, IPSResourceDefinitionService.PSResourceDefinitionInvalidIdException {
        boolean useCachedRegionCSS = isEdit && editType == EditType.TEMPLATE;

        log.debug("context: {}, useCached: {}, editType: {}", context.getMode(), useCachedRegionCSS,editType.name());
        
        PSThemeResource resource = (PSThemeResource) getResourceDefinition("theme." + themeName);
        if (resource == null)
            return new PSRenderLink("", resource);
        
        PSThemeSummary summary = resource.getThemeSummary();
        if (!useCachedRegionCSS &&
                StringUtil.isBlank(summary.getRegionCssFilePath()))
        {
                return new PSRenderLink("", resource);
        }
        String regionCssPath = getRegionCSSRelativePath(themeName, useCachedRegionCSS, summary);
        final String baseUrl = resourceInstanceHelper.getBaseUrlPath(context);
        String url = makeThemeURL(context, baseUrl, regionCssPath, useCachedRegionCSS);
        String renderUrl = escapePathForUrl(url);
        if (context.getMode() != PSRenderLinkContext.Mode.PUBLISH)
        {
            renderUrl = String.format("%s?time=%d", renderUrl, System.currentTimeMillis());
        }
        return new PSRenderLink(renderUrl, resource);
    }

    private String getRegionCSSRelativePath(String themeName, boolean useCached, PSThemeSummary summary) throws PSThemeNotFoundException {
        String regionCssPath;
        if (useCached)
        {
            regionCssPath = themeService.getCachedRegionCSSRelativeURL(themeName);
        }
        else
        {
            regionCssPath = summary.getRegionCssFilePath();
        }
        return regionCssPath;
    }
    
    private String makeThemeURL(final PSRenderLinkContext context, String baseUrl, String fileName, boolean useCached)
    {
        fileName = "/" + removeStart(fileName, "/");
        if (context.getMode() == Mode.PUBLISH) {
            return concatPath(baseUrl, themeService.getThemesRootRelativeUrl(), fileName);
        }
        
        if (useCached)
            return concatPath(filePreviewUrlBase, themeService.getThemesTempRootRelativeUrl(), fileName);
        else
            return concatPath(themePreviewUrlBase, "themes", fileName);
    }

    /**
     * {@inheritDoc}
     */
    public PSRenderLink renderLink(final PSRenderLinkContext context, String resourceDefinitionId) throws PSDataServiceException {
        PSResourceDefinition rd = getResourceDefinition(resourceDefinitionId);
        final String baseUrl = resourceInstanceHelper.getBaseUrlPath(context);
        final StringBuilder url = new StringBuilder();

        IPSResourceDefinitionVisitor v = new IPSResourceDefinitionVisitor()
        {
            public void visit(PSAssetResource resource) throws PSDataServiceException {
                throw new PSDataServiceException("Is not a file or folder");
            }
            public void visit(PSFileResource resource)
            {
                String file = resource.getFile();
                url.append(makeUrl(file));
            }
            public void visit(PSFolderResource resource)
            {
                String path = resource.getPath();
                url.append(makeUrl(path));
            }
            public void visit(PSThemeResource resource)
            {
                url.append(makeThemeUrl(resource.getThemeSummary().getCssFilePath()));
            }

            private String makeThemeUrl(String fileName)
            {
                return makeThemeURL(context, baseUrl, fileName, false);
            }
            
            private String makeUrl(String fileName)
            {
                fileName = "/" + removeStart(fileName, "/");
                if (context.getMode() == Mode.PUBLISH) {
                    fileName = concatPath(baseUrl, fileName);
                    return fileName;
                }
                return concatPath(filePreviewUrlBase, fileName);
            }
        };

        if(rd != null)
        {
            rd.accept(v);
        }
        final String renderUrl = escapePathForUrl(url.toString());
        return new PSRenderLink(renderUrl, rd);
    }

    private PSResourceDefinition getResourceDefinition(String resourceDefinitionId) throws IPSResourceDefinitionService.PSResourceDefinitionInvalidIdException, PSValidationException {
        PSResourceDefinition rd = null;
        try
        {
            rd = resourceDefinitionService.findResource(resourceDefinitionId);
        } 
        catch(PSThemeNotFoundException e)
        {
            // issue CM-276 - If the user deletes the percussion theme folder,
            // we should allow the rendering anyway (in that case, this type of
            // exception will be thrown
            log.warn("Theme folder may have been deleted. Error:{}",e.getMessage());
            log.debug(e.getMessage(),e);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
        }
        return rd;
    }
    
    private PSAssetResource findAssetResourceDefinition(String resourceDefinitionId) throws PSDataServiceException {
        PSResourceDefinition definition = resourceDefinitionService.findResource(resourceDefinitionId);
        if (definition instanceof PSAssetResource)
        {
            return (PSAssetResource) definition;

        }
        throw new DataServiceLoadException(String.format("resourceDefinitionId: %s is not an asset resource", resourceDefinitionId));
    }
    
    private PSAssetResource findAssetResourceDefinitionForTemplate(String legacyTemplate, final String contentType) {
        notEmpty(legacyTemplate, "template");

        List<PSAssetResource> definition = resourceDefinitionService.findAssetResourcesForLegacyTemplate(legacyTemplate);
        if (isNotBlank(contentType)) {
            definition = new PSAbstractFilter<PSAssetResource>()
            {
                @Override
                public boolean shouldKeep(PSAssetResource resource)
                {
                    return contentType.equals(resource.getContentType());
                }
                
            }.filter(definition);
        }
        notEmpty(definition);
        isTrue(definition.size() == 1 , "Should only have one legacy template for inline links");
        return definition.get(0);
    }


    public void validateLinkContext(PSRenderLinkContext context) throws PSBeanValidationException {
        PSBeanValidationUtils.getValidationErrorsOrFailIfInvalid(context);
    }
    


    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSRenderLinkService.class);

}
