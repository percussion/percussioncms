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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.service.impl;

import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.cms.IPSConstants;
import com.percussion.foldermanagement.service.IPSFolderService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSTemplateSummaryList;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageTemplateService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.pagemanagement.service.impl.PSPageToTemplateException;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.IPSAsyncJobService;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.IPSFolderPath;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.data.PSSiteSummaryList;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.service.AssignTemplate;
import com.percussion.sitemanage.service.AssignTemplateList;
import com.percussion.sitemanage.service.IPSSiteImportService;
import com.percussion.sitemanage.service.IPSSiteSectionMetaDataService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.sitemanage.service.PSPageToTemplatePair;
import com.percussion.sitemanage.service.PSSiteTemplates;
import com.percussion.sitemanage.service.PSSiteTemplates.CreateTemplate;
import com.percussion.utils.guid.IPSGuid;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfBlank;
import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.SECTION_SYSTEM_FOLDER_NAME;
import static com.percussion.sitemanage.service.IPSSiteSectionMetaDataService.TEMPLATES;
import static java.text.MessageFormat.format;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * 
 * See interface.
 * @author adamgent
 *
 */
@Path("/sitetemplates")
@Component("siteTemplateService")
@Lazy
public class PSSiteTemplateService implements IPSSiteTemplateService
{
    private IPSSiteManager siteMgr;

    private IPSFolderHelper folderHelper;

    private IPSTemplateService templateService;

    private IPSSiteSectionMetaDataService siteSectionMetaDataService;

    private IPSiteDao siteDao;

    private IPSSiteImportService templateImportService;
    
    private IPSAsyncJobService asyncJobService;
    
    private IPSAssetService assetService;
    
    private IPSPageService pageService;
    
    private IPSPageTemplateService pageTemplateService;
    
    private IPSItemWorkflowService itemWorkflowService;
    
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;
    
    private IPSPageCatalogService pageCatalogService;
    
    public static final String REGION_CONTENT = "perc-content";
    

    
    private static final String IMPORT_TEMPLATE_JOB_BEAN = "templateImportJob"; 
    
    private static final String IMPORT_STATUS_MESSAGE_PREFIX = "Importing template:";

    @Autowired
    public PSSiteTemplateService(IPSiteDao siteDao, IPSSiteSectionMetaDataService siteSectionMetaDataService,
            @Qualifier("sys_templateService") IPSTemplateService templateService, IPSAsyncJobService asyncJobService,  IPSPageService pageService,
            IPSAssetService assetService, IPSItemWorkflowService itemWorkflowService, 
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService, IPSPageTemplateService pageTemplateService, IPSSiteManager siteMgr, IPSFolderHelper folderHelper)
    {
        super();
        this.siteDao = siteDao;
        this.siteSectionMetaDataService = siteSectionMetaDataService;
        this.templateService = templateService;
        this.asyncJobService = asyncJobService;
        this.pageService = pageService;
        this.assetService = assetService;
        this.itemWorkflowService = itemWorkflowService;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.pageTemplateService = pageTemplateService;
        this.siteMgr = siteMgr;
        this.folderHelper = folderHelper;
    }

    @GET
    @Path("/nosites")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findTemplatesWithNoSite()
    {
        try {
            List<PSTemplateSummary> sums = templateService.findAllUserTemplates();
            List<PSTemplateSummary> rvalue = new ArrayList<>();
            for (PSTemplateSummary sum : sums) {
                List<IPSFolderPath> paths = findFolderPaths(sum.getId());
                if (paths == null || paths.isEmpty()) {
                    rvalue.add(sum);
                }
            }

            return new PSTemplateSummaryList(rvalue);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }

    }


    private List<IPSFolderPath> findFolderPaths(String templateId) throws PSDataServiceException {
        rejectIfBlank("findSitesByTemplate", "templateId", templateId);
        PSTemplateSummary sum =  templateService.find(templateId);
        return siteSectionMetaDataService.findSections(TEMPLATES, sum.getId());
    }
    
    @GET
    @Path("/sites/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSSiteSummary> findSitesByTemplate(@PathParam("id") String templateId)
    {
        try {
            List<IPSFolderPath> folderPaths = findFolderPaths(templateId);

            List<String> paths = new ArrayList<>();
            for (IPSFolderPath fp : folderPaths) {
                paths.add(fp.getFolderPath());
            }

            List<PSSiteSummary> rvalue = new ArrayList<>();
            List<PSSiteSummary> sites = siteDao.findAllSummaries();
            for (PSSiteSummary site : sites) {
                if (paths.contains(site.getFolderPath()))
                    rvalue.add(site);
            }

            return new PSSiteSummaryList(rvalue);
        } catch (PSDataServiceException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }

    /**
     * This is used by <code>findTemplatesBySite</code> method.
     */
    private class FolderPath implements IPSFolderPath
    {
        private String folderPath;
        
        public String getFolderPath()
        {
            return folderPath;
        }
        
        public void setFolderPath(String folderPath)
        {
            this.folderPath = folderPath;
        }
    }
    
    @GET
    @Path("/templates/{id}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findTemplatesBySite(@PathParam("id") String siteId)
    {
        try {
            rejectIfBlank("findTemplatesBySite", "siteId", siteId);

            return new PSTemplateSummaryList(findTemplates(siteId, null, null));
        } catch (PSValidationException | IPSTemplateService.PSTemplateException | IPSDataService.DataServiceNotFoundException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @GET
    @Path("/templates/{id}/{widgetId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> findTemplatesBySite(@PathParam("id") String siteId,
          @PathParam("widgetId") String widgetId)
    {
        try {
            rejectIfBlank("findTemplatesBySite", "siteId", siteId);
            rejectIfBlank("findTemplatesBySite", "widgetId", widgetId);

            return new PSTemplateSummaryList(findTemplates(siteId, widgetId, null));
        } catch (PSValidationException | IPSTemplateService.PSTemplateException | IPSDataService.DataServiceNotFoundException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    /*
     * (non-Javadoc)
     * @see com.percussion.sitemanage.service.IPSSiteTemplateService#findTypedTemplatesBySite
     *  (java.lang.String, com.percussion.pagemanagement.data.PSTemplate.PSTemplateTypeEnum)
     */
    public List<PSTemplateSummary> findTypedTemplatesBySite(String siteId, PSTemplateTypeEnum type) throws PSValidationException, IPSTemplateService.PSTemplateException, IPSDataService.DataServiceNotFoundException {
        rejectIfBlank("findTemplatesBySite", "siteId", siteId);
                
        return  new PSTemplateSummaryList(findTemplates(siteId, null, type));
    }
    
    /**
     * Finds templates for the specified site which have at least one instance of the specified widget.
     * 
     * @param siteId The id of a site. This <strong>DOES NOT</strong> have to be a valid id of an existing site.
     * @param widgetId this is the widget definition id.  May be <code>null</code> to return all templates for the site.
     * @param type - the type of template. May be <code>null</code>
     * 
     * @return list of template summaries.  Never <code>null</code>, may be empty.
     */
    private List<PSTemplateSummary> findTemplates(String siteId, String widgetId, PSTemplateTypeEnum type) throws IPSTemplateService.PSTemplateException, IPSDataService.DataServiceNotFoundException {
        PSSiteSummary site=null;
        try
        {
            site = siteDao.find(siteId);
        }
        catch (Exception e)
        {
            log.error("Failed to load site: {} Error: {}" , siteId, e.getMessage());
        }
        if (site==null)
        {
            log.debug("Failed to load site: {}" , siteId);
            return new ArrayList<>();
        }

        FolderPath folderPath = new FolderPath();
        folderPath.setFolderPath(site.getFolderPath());
        List<IPSItemSummary> items =  siteSectionMetaDataService.findItems(folderPath, TEMPLATES);
        List<PSTemplateSummary> templates = itemsToTemplates(items, site.getName(), type);
        
        if (widgetId != null)
        {
            templates.removeIf(tempSum -> !((PSTemplate) tempSum).hasWidget(widgetId));
        }

        return  new PSTemplateSummaryList(templates);
    }
    
    private List<PSTemplateSummary> itemsToTemplates(List<IPSItemSummary> items, String siteName, PSTemplateTypeEnum type) throws IPSTemplateService.PSTemplateException {
        List<String> templateIds = new ArrayList<>();
        for (IPSItemSummary i : items) { templateIds.add(i.getId()); }
        List<PSTemplateSummary> templateSummaries = templateService.loadUserTemplateSummaries(templateIds, siteName);
        List<PSTemplateSummary> results = new ArrayList<>();
        
        for (PSTemplateSummary template : templateSummaries)
        {
            if (type == null || type.equals(PSTemplateTypeEnum.NORMAL))
            {
                if (template.getType() == null || PSTemplateTypeEnum.NORMAL.equals(PSTemplateTypeEnum.getEnum(template.getType())))
                    results.add(template);
            }
            else if (type.equals(PSTemplateTypeEnum.getEnum(template.getType())))
            {
                results.add(template);
            }            
        }
        return  new PSTemplateSummaryList(results);
    }

    @POST
    @Path("/")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public List<PSTemplateSummary> save(PSSiteTemplates siteTemplates)
    {
        try {
            validate(siteTemplates);
            for(CreateTemplate createTemplate : siteTemplates.getCreateTemplates()){
                String createTemplateName = createTemplate.getName();
                if (StringUtils.containsAny(createTemplateName, IPSConstants.INVALID_ITEM_NAME_CHARACTERS)){

                    for (int i = 0; i < IPSConstants.INVALID_ITEM_NAME_CHARACTERS.length(); i++){
                        // Replace any invalid characters present. output eg. createTemplateName = Box-Copy-2-
                        createTemplateName = StringUtils.replace(createTemplateName, String.valueOf(IPSConstants.INVALID_ITEM_NAME_CHARACTERS.charAt(i)), "-");
                    }

                    if(createTemplateName.substring(createTemplateName.length()-1).equalsIgnoreCase("-")){
                        //eg. createTemplateName = Box-Copy //the base name for copied template
                        createTemplateName = createTemplateName.substring(0, createTemplateName.length()-3);
                    }

                    IPSSite site = siteMgr.findSite(createTemplate.getSiteIds().get(0));
                    String templateFolderPathForSite = folderHelper.concatPath(site.getFolderRoot(), SECTION_SYSTEM_FOLDER_NAME, TEMPLATES);
                    //find unique name for copied template.
                    createTemplateName = folderHelper.getUniqueNameInFolder(templateFolderPathForSite, createTemplateName, "", 2, false);

                    createTemplate.setName(createTemplateName);
                }
            }
            List<CreateTemplate> createTemplates = siteTemplates.getCreateTemplates();
            List<AssignTemplate> assignTemplates = siteTemplates.getAssignTemplates();
            List<AssignTemplate> created = createTemplates(createTemplates);

            List<AssignTemplate> total = new ArrayList<>(assignTemplates);
            total.addAll(created);
            return new PSTemplateSummaryList(assignTemplates(total));
        } catch (PSDataServiceException | IPSPathService.PSPathNotFoundServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    
    @Override
    @POST
    @Path("/createFromUrl")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplateSummary createTemplateFromUrl(@Context HttpServletRequest request, PSSiteTemplates siteTemplates)
    {
        try {
            // Validation of required parameters
            notNull(siteTemplates);
            notNull(siteTemplates.getImportTemplate());
            notEmpty(siteTemplates.getImportTemplate().getSiteIds());
            notEmpty(siteTemplates.getImportTemplate().getUrl());

            // Get the user agent from request
            String userAgent = request.getHeader("User-Agent");

            // Get site name from parameter
            String siteName = siteTemplates.getImportTemplate().getSiteIds().get(0);
            PSSite site = findSiteById(siteName);
            if (site == null) {
                throw new WebApplicationException("There was an unexpected error retrieving the selected site." + siteName);
            }

            // Set url to use to import the template
            site.setBaseUrl(siteTemplates.getImportTemplate().getUrl());

            // Import the template from URL
            PSSiteImportCtx importContext = templateImportService.importSiteFromUrl(site, userAgent);

            // Load created template to return it.
            PSTemplateSummary newTemplate = findTemplateById(importContext.getTemplateId());
            if (newTemplate == null) {
                throw new WebApplicationException("There was an unexpected error creating the new template.");
            }

            return newTemplate;
        } catch (PSSiteImportException | PSDataServiceException e) {
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @Override
    @POST
    @Path ("/createFromPage")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplateSummary createTemplateFromPage(PSPageToTemplatePair pageToTemplatePair)
    {
        PSTemplateSummary templateSummary = null;
        try
        {
            templateSummary = buildTemplateFromPage(pageToTemplatePair.getPageId(), pageToTemplatePair.getSiteId());
        }
        catch (Exception e)
        {
            throw new WebApplicationException("There was an unexpected error creating the new template from the page.");
        }
      
        return templateSummary;
    }
    
    
    private PSTemplateSummary buildTemplateFromPage(String pageId, String siteId) throws Exception
    {
        PSTemplateSummary templateSummary = null;
        try
        {
            //Get the page
            PSPage page = getPageForPageId(pageId);
            
           //Load the Template
            templateSummary = templateService.createTemplate(this.generateNewTemplateName(PSPageManagementUtils.TEMPLATE_NAME, siteId), page.getTemplateId(), siteId);
            PSTemplate template = templateService.load(templateSummary.getId());
            
            //Transfer the metadata
            template.setAdditionalHeadContent(page.getAdditionalHeadContent());
            page.setAdditionalHeadContent("");
            template.setAfterBodyStartContent(page.getAfterBodyStartContent());
            page.setAfterBodyStartContent("");
            template.setBeforeBodyCloseContent(page.getBeforeBodyCloseContent());
            page.setBeforeBodyCloseContent("");
            template.setDescription(page.getDescription());
            page.setDescription("");
            template.setDocType(page.getDocType());
            template.setHtmlHeader(page.getAdditionalHeadContent());
            page.setAdditionalHeadContent("");
            
            template.setType(PSTemplateTypeEnum.NORMAL.getLabel());
            //Save changes to the page and the template
            template = templateService.save(template);
            page = pageService.save(page);
            
            
            
            //Change the page Template
            itemWorkflowService.checkOut(page.getId());
            pageTemplateService.changeTemplate(page.getId(), template.getId());
            itemWorkflowService.checkIn(page.getId());
            IPSPageImportQueue importQueue = (IPSPageImportQueue) getWebApplicationContext().getBean("pageImportQueue");
            importQueue.removeImportPage(siteId, page.getId());
        }
        catch (Exception e)
        {
            throw e;
        }

        return templateSummary;
    }

   

    private PSPage getPageForPageId(String pageId) throws PSPageToTemplateException, IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        PSPage page = null;
        if (pageId != null && !pageId.isEmpty())
            page = pageService.find(pageId);
        else
            throw new PSPageToTemplateException("Page ID can not be null");
        return page;
    }

    
    @Override
    @POST
    @Path("/createFromUrlAsync")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Long createTemplateFromUrlAsync(@Context HttpServletRequest request, PSSiteTemplates siteTemplates)
    {
        try {
            // Validation of required parameters
            notNull(siteTemplates);
            notNull(siteTemplates.getImportTemplate());
            notEmpty(siteTemplates.getImportTemplate().getSiteIds());
            notEmpty(siteTemplates.getImportTemplate().getUrl());

            // Get the user agent from request
            String userAgent = request.getHeader("User-Agent");

            // Get site name from parameter
            String siteName = siteTemplates.getImportTemplate().getSiteIds().get(0);
            PSSite site = findSiteById(siteName);
            if (site == null) {
                throw new WebApplicationException("There was an unexpected error retrieving the selected site." + siteName);
            }

            PSSiteImportCtx importContext = new PSSiteImportCtx();
            importContext.setSite(site);
            // Set url to use to import the template
            importContext.setSiteUrl(siteTemplates.getImportTemplate().getUrl());
            importContext.setStatusMessagePrefix(IMPORT_STATUS_MESSAGE_PREFIX);
            importContext.setUserAgent(userAgent);

            // Execute the Job to import the template from URL
            return asyncJobService.startJob(IMPORT_TEMPLATE_JOB_BEAN, importContext);

        } catch (IPSFolderService.PSWorkflowNotFoundException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(),e);
            throw new WebApplicationException(e.getMessage());
        }
    }
    
    @Override
    @GET
    @Path("/getImportedTemplate/{jobId}")
    @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public PSTemplateSummary getImportedTemplate(@PathParam("jobId") Long jobId) throws PSDataServiceException {
        notNull(jobId);

        PSAsyncJobStatus jobStatus = asyncJobService.getJobStatus(jobId);
        if (jobStatus == null)
            return null;

        if (jobStatus.getStatus().equals(IPSAsyncJob.COMPLETE_STATUS))
        {
            Object jobResult = asyncJobService.getJobResult(jobId);
            if (jobResult != null)
            {
                PSSiteImportCtx importContext = (PSSiteImportCtx) jobResult;
                return findTemplateById(importContext.getTemplateId());

            }
        }
        return null;
    }
    
    /**
     * Finds a template by its id using templateService. If any runtime
     * exception occurs, or if the template is not found, it will return null.
     * 
     * @param templateId The id of the template to retrieve.
     * @return The template summary. Can be null if not found or an exception
     *         occurred.
     */
    private PSTemplateSummary findTemplateById(String templateId) throws PSDataServiceException {
        return templateService.find(templateId);
    }

    /**
     * Finds a site by its name, which is also its id. using siteDao. If any
     * runtime exception occurs, or if the site is not found, it will return
     * null.
     * 
     * @param siteName The id(name) of the site to retrieve.
     * @return PSSite object, with site information. Can be null if not found or
     *         an exception occurred.
     */
    private PSSite findSiteById(String siteName)
    {
        try
        {
            return siteDao.find(siteName);
        }
        catch (RuntimeException | PSDataServiceException e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    @SuppressWarnings("deprecation")
    protected List<AssignTemplate> createTemplates(List<CreateTemplate> createTemplates) throws PSDataServiceException {
        List<AssignTemplate> rvalue = new ArrayList<>();
        for(CreateTemplate t : createTemplates) {
            PSTemplateSummary sum = createSiteTemplate(t);
            AssignTemplate assignTemplate = new AssignTemplate();
            assignTemplate.setSiteIds(t.getSiteIds());
            assignTemplate.setTemplateId(sum.getId());
            rvalue.add(assignTemplate);
        }
        return new AssignTemplateList(rvalue);
    }
    
    /**
     * Creates a template for a site.
     * 
     * @param ct must contain one site, assumed not <code>null</code>.
     * 
     * @return the created template, never <code>null</code>.
     */
    private PSTemplateSummary createSiteTemplate(CreateTemplate ct) throws PSDataServiceException {
        if (ct.getSiteIds().size() != 1)
        {
            throw new IllegalArgumentException("Can only create template for a specific site, no more or less.");
        }
        
        String siteId = ct.getSiteIds().get(0); // ID is name for now
        return templateService.createTemplate(ct.getName(), ct.getSourceTemplateId(), siteId);

    }
    
    protected List<PSTemplateSummary> assignTemplates(List<AssignTemplate> assignTemplates) throws PSDataServiceException {
        List<PSTemplateSummary> templates = new ArrayList<>();
        for(AssignTemplate t : assignTemplates) {
            PSTemplateSummary ts = assignTemplate(t);
            templates.add(ts);
        }
        return new PSTemplateSummaryList(templates);
    }
    
    @SuppressWarnings("unchecked")
    protected PSTemplateSummary assignTemplate(AssignTemplate assignTemplate) throws PSDataServiceException {
        String templateId = assignTemplate.getTemplateId();
        PSTemplateSummary template = templateService.find(templateId);
        List<String> siteIds = assignTemplate.getSiteIds();
        
        List<PSSiteSummary> oldSites = findSitesByTemplate(templateId);
        List<PSSiteSummary> newSites = new ArrayList<>();

        for (String siteId : siteIds) {
            PSSite site = siteDao.find(siteId);
            if (site!=null)
                newSites.add(siteDao.find(siteId));
        }
        Collection<PSSiteSummary> remove = CollectionUtils.subtract(oldSites, newSites);
        Collection<PSSiteSummary> add = CollectionUtils.subtract(newSites, oldSites);
        
        if (log.isDebugEnabled()) {
            log.debug(format("Removing sites:{1} from template:{0}", templateId, remove));
        }
        for(PSSiteSummary site : remove) {
            removeTemplate(site, templateId);
        }
        if (log.isDebugEnabled()) {
            log.debug(format("Add template:{0} to sites:{1}", templateId, add));
        }
        for(PSSiteSummary site : add) {
            addTemplate(site, templateId);
        }
        
        String oldName = template.getName();
        String newName = assignTemplate.getName();
        if (log.isDebugEnabled()) {
            log.debug(format("Old template name: {0}, new template name: {1}", oldName,newName));
        }
        if (isNotBlank(newName) && ! oldName.equals(newName)) 
        {
            log.debug("Changing templates name");
            PSTemplate fullTemplate = templateService.load(template.getId());
            fullTemplate.setName(newName);
            
            if (siteIds.size() > 1)
            {
                throw new IllegalArgumentException("Cannot save template for more than one site!");
            }
            if (siteIds.size() == 1)
                templateService.save(fullTemplate, siteIds.get(0));
            else
                templateService.save(fullTemplate);
        }
        PSTemplateSummary templateSummary = templateService.find(templateId);
        if (!newSites.isEmpty())
        {
            templateSummary.setImageThumbPath(templateService.getTemplateThumbPath(templateSummary, newSites.get(0).getName()));
        }
        return templateSummary;
        
    }
    
    public void removeTemplate(PSSiteSummary site, String templateId) {
        if(log.isDebugEnabled())
            log.debug(format("Removing template: {1} from site: {0}", site, templateId));
        siteSectionMetaDataService.removeItem(site, TEMPLATES, templateId);
    }
    
    public void addTemplate(PSSiteSummary site, String templateId) {
        if(log.isDebugEnabled())
            log.debug(format("Adding template: {1} to site: {0}", site, templateId));
        siteSectionMetaDataService.addItem(site, TEMPLATES, templateId);
    }
    
    public Map<String, String> copyTemplates(String site1Id, String site2Id) throws PSDataServiceException, PSSiteImportException {
        rejectIfBlank("copyTemplates", "site1Id", site1Id);
        rejectIfBlank("copyTemplates", "site2Id", site2Id);
        
        Map<String, String> tempMap = new HashMap<>();

        PSSiteSummary site2 = siteDao.findSummary(site2Id);
        if (site2 != null)
        {
            Set<String> site2TempSet = new HashSet<>();
            for (PSTemplateSummary site2Template : findTemplatesBySite(site2Id))
            {
                site2TempSet.add(site2Template.getName());
            }

            for (PSTemplateSummary site1Template : findTemplatesBySite(site1Id))
            {
                String s1TempName = site1Template.getName();
                if (!site2TempSet.contains(s1TempName))
                {
                    PSTemplateSummary site2Template = templateService.createTemplate(s1TempName, site1Template.getId(),
                            site2Id);
                    tempMap.put(site1Template.getId(), site2Template.getId());
                }
            }

            // copy the unassigned template if exists
            PSSiteSummary site1 = siteDao.findSummary(site1Id);
            String templateId = getPageCatalogService().getCatalogTemplateIdBySite(site1.getName());
            if (!StringUtils.isBlank(templateId))
            {
                PSTemplateSummary unassignedTemplate = findTemplateById(templateId);
                String s1TempName = unassignedTemplate.getName();
                if (!site2TempSet.contains(s1TempName))
                {
                    PSTemplateSummary site2Template = templateService.createTemplate(s1TempName, templateId, site2Id);
                    tempMap.put(templateId, site2Template.getId());
                }
            }
        }
        
        return tempMap;
    }

    public PSValidationErrors validate(PSSiteTemplates siteTemplates) throws PSBeanValidationException {
        return PSBeanValidationUtils.getValidationErrorsOrFailIfInvalid(siteTemplates);
    }
    
    public IPSSiteImportService getTemplateImportService()
    {
        return templateImportService;
    }

    @Autowired
    public void setTemplateImportService(@Qualifier("templateImportService") IPSSiteImportService templateImportService)
    {
        this.templateImportService = templateImportService;
    }
    
    /**
     * @return the pageCatalogService
     */
    public IPSPageCatalogService getPageCatalogService()
    {
        if (pageCatalogService == null)
        {
            pageCatalogService = (IPSPageCatalogService) getWebApplicationContext().getBean("pageCatalogService");
        }

        return pageCatalogService;
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSiteTemplateService.class);

    /**
     * Generates a name for a new template, using site-wide naming conventions.
     * It avoids collision with existing templates appending a numerical value
     * to the end if necessary (Example: New-Template-2)
     * 
     * @param templateName Initial name. If it doesn't exist, it will be
     *            returned unchanged. Otherwise a number will be appended to it,
     *            to avoid name collision.
     * @param siteId The id of the site to which this template will belong. If
     *            the site already has a template with the same name, a
     *            numerical value will be appended to the end of the generated
     *            name to avoid collisions.
     * @return String The automatically generated name for the template. Never
     *         null or empty.
     * 
     */
    public String generateNewTemplateName(String templateName, String siteId)
    {
 
        List<PSTemplateSummary> siteTemplates = this.findTemplatesBySite(siteId);
        List<String> existingTemplateNames = new ArrayList<>();
        for (PSTemplateSummary templateSummary : siteTemplates)
        {
            existingTemplateNames.add(templateSummary.getName());
        }

        int count = 0;
        String generatedTemplateName = templateName;
        while (existingTemplateNames.contains(generatedTemplateName))
        {
            count++;
            generatedTemplateName = PSPageManagementUtils.getNameForCount(templateName, count);
        }

        return generatedTemplateName;
    }
   
    
}
