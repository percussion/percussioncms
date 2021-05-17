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


import static com.percussion.pagemanagement.assembler.PSResourceLinkAndLocationUtils.concatPath;
import static com.percussion.pagemanagement.assembler.PSResourceLinkAndLocationUtils.createDefaultLinkAndLocation;
import static com.percussion.pagemanagement.assembler.PSResourceLinkAndLocationUtils.validateAsPhysicalPath;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.removeStart;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.pagemanagement.assembler.PSResourceScriptEvaluatorContext;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSRenderLinkContext;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSAssetResource;
import com.percussion.pagemanagement.data.PSResourceDefinitionGroup.PSLinkAndLocationsScript;
import com.percussion.pagemanagement.data.PSResourceInstance;
import com.percussion.pagemanagement.data.PSResourceLinkAndLocation;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.impl.PSLinkableAsset;
import com.percussion.pagemanagement.service.impl.PSRenderLinkService;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.utils.jexl.PSServiceJexlEvaluatorBase;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.IPSLinkableContentItem;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.IPSLinkableItem;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.jexl.IPSScript;
import com.percussion.utils.jexl.PSJexlEvaluator;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Helper to process {@link PSResourceInstance}s.
 * Right now just for link and locations.
 * <p>
 * Its responsibilities are to creates resource instances and manage executing them 
 * (getting links and one day output).
 * <p>
 * The {@link PSRenderLinkService} delegates to this class even though publicly
 * it is the one responsible for resource instances.
 * @author adamgent
 *
 */
@PSSiteManageBean("resourceInstanceHelper")
public class PSResourceInstanceHelper
{

    private IPSSiteDataService siteDataService;
    private IPSPageService pageService;
    private IPSSiteTemplateService siteTemplateService;
    private IPSAssetService assetService;
    private IPSAssemblyService assemblyService;


    @Autowired
    private PSResourceInstanceHelper(IPSAssetService assetService, IPSSiteDataService siteDataService, IPSPageService pageService,
            IPSSiteTemplateService siteTemplateService, IPSAssemblyService assemblyService)
    {
        super();
        this.assetService = assetService;
        this.siteDataService = siteDataService;
        this.pageService = pageService;
        this.siteTemplateService = siteTemplateService;
        this.assemblyService = assemblyService;
    }

    /**
     * Executes a resource instance with the given script.
     * The script should return either a list of {@link PSResourceLinkAndLocation}
     * or a single {@link PSResourceLinkAndLocation}.
     * Mulptile {@link PSResourceLinkAndLocation}s will indicate pagination.
     * 
     * @param resourceInstance
     * @param script the source code of the script.
     * @return never <code>null</code>, maybe empty.
     */
    @SuppressWarnings("unchecked")
    private List<PSResourceLinkAndLocation> executeResourceLinkScript(PSResourceInstance resourceInstance, String script) throws IPSAssetService.PSAssetServiceException {
        notNull(resourceInstance, "resourceInstance");
        notEmpty(script, "script");
        if(log.isDebugEnabled()) {
            log.debug("Executing Script: " + script + " for resourceInstance: " + resourceInstance);
        }
        PSServiceJexlEvaluatorBase jexlEvaluator = new PSServiceJexlEvaluatorBase(true);
        try
        {
            PSResourceScriptEvaluatorContext perc = createContext(resourceInstance);
            jexlEvaluator.bind("$perc", perc);
            IPSScript jexlScript = PSJexlEvaluator.createScript(script);
            Object rvalue = jexlEvaluator.evaluate(jexlScript);
            if (rvalue instanceof List ) {
                List<PSResourceLinkAndLocation> links = (List) rvalue;
                Validate.allElementsOfType(links, PSResourceLinkAndLocation.class);
                return links;
            }
            else if (rvalue instanceof PSResourceLinkAndLocation) {
                List<PSResourceLinkAndLocation> links = new ArrayList<>();
                links.add((PSResourceLinkAndLocation) rvalue);
            }
            else {
                if (log.isDebugEnabled())
                    log.debug("Script did not return an object of type : " 
                            + PSResourceLinkAndLocation.class.getSimpleName());
            }
            return resourceInstance.getLinkAndLocations();
        }
        catch (Exception e)
        {
            throw new IPSAssetService.PSAssetServiceException("Error executing link script for resource instance: " + resourceInstance, e);
        }
    }
    
    /**
     * Create the resource script evaluator context.
     * Usually this object is bound to <code>$perc</code>
     * @param resourceInstance never <code>null</code>.
     * @return never <code>null</code>.
     */
    private PSResourceScriptEvaluatorContext createContext(PSResourceInstance resourceInstance) {
        PSResourceScriptEvaluatorContext perc = new PSResourceScriptEvaluatorContext();
        perc.setResourceInstance(resourceInstance);
        return perc;
    }
    
    
    /**
     * Gets the physical publish location folder path of the resource.
     * @param r never <code>null</code>.
     * @return never <code>null</code>.
     * @throws RuntimeException if the folder path for resource is invalid.
     */
    private String getPublishLocationFolderPath(PSResourceInstance r) throws DataServiceNotFoundException, PSValidationException {
        notNull(r, "r");
        String path = r.getItem().getFolderPath();
        path = path == null ? r.getLinkContext().getFolderPath() : path;
        PSSiteSummary site = resolveSite(r);
        String siteFolderPath = site.getFolderPath();
        /*
         * See if the path starts with the site folder path or the asset folder path.
         */
        if (startsWith(path, siteFolderPath)) {
            path = removeStart(path, siteFolderPath);
        }
        else if(startsWith(path, PSAssetPathItemService.ASSET_ROOT)) {
            path = removeStart(path, PSAssetPathItemService.ASSET_ROOT);
            path = concatPath(PSPathUtils.ASSETS_FINDER_ROOT, path);
        }
        else {
            throw new RuntimeException("The asset or link context associated with " +
            		"the resource instance does not have a proper folder path: " + r);
        }
        
        if (isBlank(path))
            path = "/";
        
        validateAsPhysicalPath(path);
        
        return path;
    }
    
    public String getBaseUrl(PSResourceInstance r) {
        PSSiteSummary site = r.getSite();
        if (r.isCrossSite()) {
            return site.getBaseUrl();
        }
        return getBaseUrlPath(r);
    }
    
    /**
     * Resolve the site from the resource instance.
     * @param r never <code>null</code>.
     * @return never <code>null</code>.
     */
    private PSSiteSummary resolveSite(PSResourceInstance r) throws DataServiceNotFoundException, PSValidationException {
        if (r.getSite() != null) return r.getSite();
        PSSiteSummary site = r.getLinkContext().getSite();
        //If item is a resource e.g asset then folderpath = //Folders/$System$/Assets/uploads
        //thus siteDataService.findByPath throws DataServiceNotFoundException which causes publishing of resources to fail
        // thus adding the check to make sure we only make this call for pages and site related stuff
        if(!r.getItem().isResource()) {
            site = siteDataService.findByPath(r.getItem().getFolderPath());
        }

        notNull(site, "Either the link context or the item needs to belong a site");
        return site;
    }
    
    public PSResourceInstance createResourceInstance(PSRenderLinkContext context, IPSLinkableItem item, PSAssetResource rd) throws IPSAssetService.PSAssetServiceException, DataServiceNotFoundException, PSValidationException {
        PSResourceInstance r = new PSResourceInstance();
        if (item instanceof IPSLinkableContentItem) {
            r.setItem((IPSLinkableContentItem) item);
        }
        else {
            r.setItem(getLinkableItem(item.getId(), item.getFolderPath()));
        }
        r.setLinkContext(context);
        r.setResourceDefinition(rd);
        PSSiteSummary site =  resolveSite(r);
        r.setSite(site);
        
        r.setLocationFolderPath(getPublishLocationFolderPath(r));
        
        return r;
    }
    
    private IPSLinkableContentItem getLinkableItem(String assetId, String folderPath) throws IPSAssetService.PSAssetServiceException {
        PSAsset asset = loadPartialAsset(assetId);
        return new PSLinkableAsset(asset, folderPath);
    }
    
    /**
     * Loads the specified asset, which includes summary properties only. 
     * It does not include fields relate to Clob or Blob columns.
     * The returned asset is typically used to create links, but should 
     * not be used to render the asset.
     * 
     * @param assetId the ID of the asset.
     * 
     * @return the asset that includes summary properties only.
     */
    public PSAsset loadPartialAsset(String assetId) throws IPSAssetService.PSAssetServiceException {
        return assetService.load(assetId, true);
    }
    
    public IPSItemSummary findResourceAsset(String assetId) throws PSDataServiceException {
        return assetService.find(assetId);
    }
    
    /**
     * Gets links from the resource instance by executing its link
     * and location script.
     * @param r never <code>null</code>.
     * @return never <code>null</code>, maybe empty.
     */
    public List<PSResourceLinkAndLocation> getLinkAndLocations(PSResourceInstance r) throws IPSAssetService.PSAssetServiceException {
        PSAssetResource rd = r.getResourceDefinition();
        PSLinkAndLocationsScript script = rd.getLinkAndLocationsScript();
        if (script != null && isNotBlank(script.getValue())) {
            List<PSResourceLinkAndLocation> links = executeResourceLinkScript(r, script.getValue());
            if (links.isEmpty()) {
                log.debug("Resource script returned an empty set of links");
            }
            return links;
        }
        else if( IPSPageService.PAGE_CONTENT_TYPE.equals(r.getItem().getType())) {
            log.debug("Generating link for page.");
            PSResourceLinkAndLocation link = createDefaultLinkAndLocation(r, assemblyService);
            return asList(link);
        }
        log.warn("Resource definition: " + r.getResourceDefinition() + " does not generate any links. ");
        return emptyList();
    }
    
    /**
     * Calculates the base url path.
     * 
     * @param context never <code>null</code>.
     * @return never <code>null</code> or empty.
     */
    public String getBaseUrlPath(PSRenderLinkContext context)
    {
        PSSiteSummary site = context.getSite();
        return getBaseUrlPath(site);
    }

    /**
     * @param r never <code>null</code>.
     * @return never <code>null</code>.
     * @see #getBaseUrlPath(PSSiteSummary)
     */
    private String getBaseUrlPath(PSResourceInstance r)
    {
        return getBaseUrlPath(r.getSite());
    }
    /**
     * If the site is relative (not cross site) we 
     * extract the path from the base url.
     * 
     * That is the host information is removed.
     * 
     * @param site never <code>null</code>.
     * @return never <code>null</code>.
     */
    private String getBaseUrlPath(PSSiteSummary site)
    {
        String bu = site.getBaseUrl();
        try
        {
            bu = new URL(bu).getPath();
        }
        catch (MalformedURLException e)
        {
            bu = "/";
        }
        return bu;
    }
    
    /**
     * Finds site ID for page ID
     * 
     * @param pageId never <code>null</code>.
     * @return site  never <code>null</code>.
     */
    public PSSiteSummary getSiteForPageId(String pageId) throws IPSDataService.DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        /*
         * TODO should return page summary not whole page.
         */
        PSPage page = pageService.find(pageId);
        return siteDataService.findByPath(page.getFolderPath());
    }
    
    /**
     * Finds site ID for template ID
     * 
     * @param templateId never <code>null</code>.
     * @return site never <code>null</code>.
     */
    public PSSiteSummary getSiteForTemplateId(String templateId)
    {
        List<PSSiteSummary> sites = siteTemplateService.findSitesByTemplate(templateId);
        notEmpty(sites, "Template should be associated with atleast one site");
        return sites.get(0);
    }

    
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSResourceInstanceHelper.class);
}

