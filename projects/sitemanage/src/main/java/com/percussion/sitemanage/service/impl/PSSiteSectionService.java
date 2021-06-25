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

package com.percussion.sitemanage.service.impl;

import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSContentEvent;
import com.percussion.cms.PSCmsException;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSFolder;
import com.percussion.comments.data.PSCommentsSummary;
import com.percussion.comments.service.IPSCommentsService;
import com.percussion.design.objectstore.PSNotFoundException;
import com.percussion.extension.IPSExtensionErrors;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.fastforward.managednav.IPSNavigationErrors;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.pagemanagement.assembler.IPSRenderAssemblyBridge;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.data.PSFolderPermission.Access;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.content.data.PSItemSummary;
import com.percussion.services.contentmgr.IPSContentMgr;
import com.percussion.services.contentmgr.IPSNode;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.sitemgr.IPSSiteManager;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.dao.PSJcrNodeFinder;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.IPSItemSummary.Category;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.PSSiteCopyUtils;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.data.PSCreateExternalLinkSection;
import com.percussion.sitemanage.data.PSCreateSectionFromFolderRequest;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSMoveSiteSection;
import com.percussion.sitemanage.data.PSReplaceLandingPage;
import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteBlogPosts;
import com.percussion.sitemanage.data.PSSiteBlogProperties;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTargetEnum;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.data.PSUpdateSectionLink;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.PSErrorResultsException;
import com.percussion.webservices.PSWebserviceUtils;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.webservices.publishing.IPSPublishingWs;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;
import javax.ws.rs.WebApplicationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.percussion.share.dao.PSFolderPermissionUtils.getFolderPermission;
import static com.percussion.share.dao.PSFolderPermissionUtils.setFolderPermission;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeTouchedFile;
import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * The actual implementation of the CRUD service for site sections.
 * 
 * @author YuBingChen
 */

@Component("siteSectionService")
@Lazy
public class PSSiteSectionService implements IPSSiteSectionService
{
    private static final String NAVON_FIELD_TYPE = "no_type";

    private static final String NAVON_FIELD_DISPLAYTITLE = "displaytitle";

    private static final String NAVON_FIELD_TARGET = "no_target";

    private static final String NAVON_FIELD_EXTERNALURL = "no_externalurl";
    
    private static final String NAVON_FIELD_REQUIRESLOGIN = "requiresLogin";
    
    private static final String NAVON_FIELD_ALLOWACCESSTO = "allowAccessTo";
    
    private static final String NAVON_FIELD_CSSCLASSNAMES = "cssClassNames";

    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSContentEvent psContentEvent;


    /**
     * The navigation service, initialized by constructor.
     */
    private IPSManagedNavService navSrv;

    /**
     * The content service, initialized by constructor.
     */
    private IPSContentWs contentSrv;

    /**
     * The page dao, initialized by constructor.
     */
    private IPSPageDao pageDao;
    
    /**
     * The page dao helper, initialized by constructor.
     */
    private IPSPageDaoHelper pageDaoHelper;

    /**
     * The content design service, initialized by constructor.
     */
    private IPSContentDesignWs contentDsSrv;

    /**
     * The site manager, initialized by constructor.
     */
    private IPSSiteManager siteMgr;

    private IPSFolderHelper folderHelper;

    private IPSWorkflowHelper workflowHelper;

    /**
     * The ID map service, initialized by constructor.
     */
    private IPSIdMapper idMapper;

    /**
     * The render assembly bridge service, initialized by constructor.
     */
    private IPSRenderAssemblyBridge asmBridge;
    
    /**
     * The publishing webservice, initialized by constructor.
     */
    private IPSPublishingWs publishingWs;
    
    private IPSTemplateService templateSrv;
    
    private IPSiteDao siteDao;
    
    private IPSSiteTemplateService siteTemplateSrv;
    
    private IPSCommentsService commentsService;
    
    private IPSItemWorkflowService itemWorkflowService;
    
    @SuppressWarnings("unused")
    private IPSContentMgr contentMgr;
    
    private PSJcrNodeFinder jcrNodeFinder;

    @Autowired
    public PSSiteSectionService(IPSPageDao pageDao, IPSManagedNavService navService, IPSContentWs contentSrv,
            IPSContentDesignWs contentDsSrv, IPSSiteManager siteMgr, IPSIdMapper idMapper,
            @Qualifier("renderAssemblyBridge") IPSRenderAssemblyBridge asmBridge, IPSFolderHelper folderHelper, IPSWorkflowHelper workflowHelper,
            IPSPublishingWs publishingWs, @Qualifier("sys_templateService") IPSTemplateService templateSrv, IPSiteDao siteDao,
                                @Qualifier("siteTemplateService") IPSSiteTemplateService siteTemplateSrv, IPSCommentsService commentsService, IPSPageDaoHelper pageDaoHelper,
            IPSItemWorkflowService itemWorkflowService, IPSContentMgr contentMgr)
    {
        notNull(navService);
        notNull(contentSrv);
        notNull(pageDao);
        notNull(pageDaoHelper);
        notNull(contentDsSrv);
        notNull(siteMgr);
        notNull(idMapper);
        notNull(asmBridge);
        notNull(folderHelper);
        notNull(publishingWs);
        notNull(templateSrv);
        notNull(siteDao);
        notNull(siteTemplateSrv);
        notNull(commentsService);
        notNull(itemWorkflowService);

        this.navSrv = navService;
        this.contentSrv = contentSrv;
        this.pageDao = pageDao;
        this.pageDaoHelper = pageDaoHelper;
        this.contentDsSrv = contentDsSrv;
        this.siteMgr = siteMgr;
        this.idMapper = idMapper;
        this.asmBridge = asmBridge;
        this.folderHelper = folderHelper;
        this.workflowHelper = workflowHelper;
        this.publishingWs   = publishingWs;
        this.templateSrv = templateSrv;
        this.siteDao = siteDao;
        this.contentMgr = contentMgr;
        this.siteTemplateSrv = siteTemplateSrv;
        this.commentsService = commentsService;
        this.itemWorkflowService = itemWorkflowService;
        jcrNodeFinder = new PSJcrNodeFinder(contentMgr,
                IPSPageService.PAGE_CONTENT_TYPE, "sys_title");
    }
    
    /*
     * see base interface method for details
     */
    public PSSiteSection create(PSCreateSiteSection req) throws PSDataServiceException {
        if (log.isDebugEnabled())
            log.debug("Received create site section: " + req.toString());

        createRequestValidator.validate(req).throwIfInvalid();

        String blogPostTempId = null;
        String blogIndexTempId = null;
        
        PSSectionTypeEnum type = req.getSectionType();
        if (type == PSSectionTypeEnum.blog)
        {
           // create the required templates
           String name = req.getPageTitle();
           PSSiteSummary site = siteDao.findByPath(req.getFolderPath());
           String siteId = site.getId();

           if (req.getCopyTemplates()){
               blogIndexTempId = createBlogTemplate(name, req.getTemplateId(), siteId);
               if (blogIndexTempId != null)
               {
                  req.setTemplateId(blogIndexTempId);
               }
               
               blogPostTempId = createBlogTemplate(name, req.getBlogPostTemplateId(), siteId);    
           }
           else{
               blogIndexTempId = req.getTemplateId();
               blogPostTempId = req.getBlogPostTemplateId();
           }
        }
        
        IPSGuid parentId = contentSrv.getIdByPath(req.getFolderPath());
        PSFolder folder = createFolder(req.getPageUrlIdentifier(), req.getFolderPath(), blogPostTempId);
        if(blogIndexTempId != null)
        {
            folder.setProperty(BLOG_INDEX_TEMPLATE_PROPS, blogIndexTempId);
        }
        String createdFolderPath = req.getFolderPath() + "/" + folder.getName();
        PSLegacyGuid guid = new PSLegacyGuid(folder.getLocator());
        int workflowId = pageDaoHelper.getWorkflowIdForPath(createdFolderPath);
        IPSGuid navonId = navSrv.addNavonToFolder(parentId, guid, folder.getName() + "-Navon", req.getPageLinkTitle(), workflowId);
        createLandingPage(req, createdFolderPath, navonId);

        PSSiteSection section = new PSSiteSection();
        section.setFolderPath(createdFolderPath);
        section.setTitle(req.getPageLinkTitle());
        section.setSectionType(req.getSectionType());
        navonId = resetRevision(navonId);
        section.setId(idMapper.getString(navonId));
        
        List<IPSSite> sites = publishingWs.getItemSites(idMapper.getGuid(section.getId()));
        if(sites.get(0).isSecure())
        {
            removeSiteTouchedFile(sites.get(0).getName());
        }
        psContentEvent=new PSContentEvent(section.getId(),section.getId().substring(section.getId().lastIndexOf("-")+1,section.getId().length()),section.getFolderPath(), PSContentEvent.ContentEventActions.create, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
        psAuditLogService.logContentEvent(psContentEvent);
        return section;

    }

    /*
     * see base interface method for details
     */
    public PSSiteSection createExternalLinkSection(PSCreateExternalLinkSection req) throws PSValidationException, IPSPathService.PSPathNotFoundServiceException, PSSiteSectionException {
        if (log.isDebugEnabled())
            log.debug("Received create site section: " + req.toString());

        createExternalLinkRequestValidator.validate(req).throwIfInvalid();
        IPSGuid parentId = contentSrv.getIdByPath(req.getFolderPath());
        String esFolderName = folderHelper.getUniqueFolderName(req.getFolderPath(), EXTERNAL_SECTION_NAME_PREFIX
                + req.getLinkTitle());
        PSFolder folder = createFolder(esFolderName, req.getFolderPath(), null);
        String createdFolderPath = req.getFolderPath() + "/" + folder.getName();
        PSLegacyGuid guid = new PSLegacyGuid(folder.getLocator());
        IPSGuid navonId = navSrv.addNavonToFolder(parentId, guid, folder.getName() + "-Navon", req.getLinkTitle(), 
                pageDaoHelper.getWorkflowIdForPath(createdFolderPath));

        updateExternalLinkSection(req.getLinkTitle(), idMapper.getString(navonId), req.getExternalUrl(),
                PSSectionTargetEnum._self, null);
        Set<String> idSet = new HashSet<>();
        idSet.add(idMapper.getString(navonId));
        workflowHelper.transitionToPending(idSet);
        PSSiteSection section = new PSSiteSection();
        section.setFolderPath(createdFolderPath);
        section.setTitle(req.getLinkTitle());
        section.setSectionType(req.getSectionType());
        navonId = resetRevision(navonId);
        section.setId(idMapper.getString(navonId));
        return section;
    }

    /*
     * see base interface method for details
     */
    public PSSiteSection createSectionLink(String targetSectionGuid,
            String parentSectionGuid) throws PSSiteSectionException {
        IPSGuid targetGuid = idMapper.getGuid(targetSectionGuid);
        IPSGuid parentGuid = idMapper.getGuid(parentSectionGuid);

        if(navSrv.isNavTree(targetGuid))
        {
            throw new PSSiteSectionException("Cannot create a section link that points to the home page.");
        }
        
        navSrv.addNavonToParentNavon(targetGuid, parentGuid, -1);
        PSSiteSection section = loadSiteSection(targetGuid, parentGuid, null, true, true, null);
        return section;
    }
    
    @Override
    public PSSiteSection createSectionFromFolder(PSCreateSectionFromFolderRequest req) throws PSDataServiceException {
        //validate the input
    	PSCreateSectionFromFolderValidator val = new PSCreateSectionFromFolderValidator();
        val.validate(req).throwIfInvalid();
        
        String parentPath = req.getParentFolderPath();
        String sourcePath = req.getSourceFolderPath();
        String pageName = req.getPageName();
        IPSGuid parentId = contentSrv.getIdByPath(parentPath);
        IPSGuid childId = contentSrv.getIdByPath(sourcePath);
        IPSGuid landingPageId = contentSrv.getIdByPath(sourcePath + "/" + req.getPageName());
        
        //Remove trailing slash if exists from sourcePath
        if(sourcePath.endsWith("/"))
        	sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        if(parentPath.endsWith("/"))
        	parentPath = parentPath.substring(0, parentPath.length() - 1);

        String folderName = sourcePath.substring(sourcePath.lastIndexOf("/") + 1);
        int workflowId = pageDaoHelper.getWorkflowIdForPath(sourcePath);
        
        //Move the folder if needed
        if(!sourcePath.substring(0,sourcePath.lastIndexOf("/")).equalsIgnoreCase(parentPath)){
        	folderHelper.moveItem(parentPath, sourcePath, true);
        	sourcePath = parentPath + "/" + folderName;
        }
        
        PSPage landingPage = pageDao.find(landingPageId.toString());

        //Add navon to folder
        IPSGuid navonId = navSrv.addNavonToFolder(parentId, childId, folderName + "-Navon", landingPage.getLinkTitle(), workflowId);
        
        
        String filename = getDefaultDocument(parentPath);


        
        //If landing page name doesn't start with index or index. then change its name to index,
        //if page with name "index" exists then rename it as per our rename rules.
        if(!StringUtils.equals(pageName, filename))
        {
        	IPSGuid indexPageId = contentSrv.getIdByPath(sourcePath + "/" + filename);
        	if(indexPageId != null){
        		PSPage indexPage = pageDao.find(indexPageId.toString());
        		PSItemStatus indStatus = contentSrv.prepareForEdit(idMapper.getGuid(indexPage.getId()));
        		renameLandingPage(indexPage);
                contentSrv.releaseFromEdit(indStatus, false);
                contentSrv.checkinItems(Collections.singletonList(idMapper.getGuid(indexPage.getId())), null);
        	}
        	PSItemStatus status = contentSrv.prepareForEdit(landingPageId);
        	landingPage.setName(filename);
        	pageDao.save(landingPage);
            contentSrv.releaseFromEdit(status, false);
            contentSrv.checkinItems(Collections.singletonList(landingPageId), null);
        }
        
        //Add the landing page to navon
        PSItemStatus status = contentSrv.prepareForEdit(navonId);
        navSrv.addLandingPageToNavnode(landingPageId, navonId, asmBridge.getDispatchTemplate());
        contentSrv.releaseFromEdit(status, false);
        contentSrv.checkinItems(Collections.singletonList(navonId), null);
        
        // approve the navon if landing page is pending or live but navon is not
        if (workflowHelper.isItemInApproveState(idMapper.getContentId(landingPageId)) && !workflowHelper.isItemInApproveState(idMapper.getContentId(navonId)))
        {
            try {
                itemWorkflowService.performApproveTransition(idMapper.getString(navonId), false, null);
            } catch (PSDataServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | com.percussion.services.error.PSNotFoundException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
            }
        }
        
        //Create a section object to return
        PSSiteSection section = new PSSiteSection();
        section.setFolderPath(sourcePath);
        section.setTitle(landingPage.getLinkTitle());
        section.setSectionType(PSSectionTypeEnum.section);
        navonId = resetRevision(navonId);
        section.setId(idMapper.getString(navonId));
        
        List<IPSSite> sites = publishingWs.getItemSites(idMapper.getGuid(section.getId()));
        if(sites.get(0).isSecure())
        {
            removeSiteTouchedFile(sites.get(0).getName());
        }

        return section;
    }



	private String getDefaultDocument(String parentPath) {
		PSSiteSummary site = siteDao.findByPath(parentPath);
        String filename = site.getDefaultDocument();
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isEmpty(filename))
        {
        	sb.append("index");
        } else if(filename.contains("."))
            sb.append(filename, 0, filename.lastIndexOf('.'));
        else
            sb.append(filename);

        String defaultFileExtension = site.getDefaultFileExtention();
        if (StringUtils.isNotEmpty(defaultFileExtension))
        {
            sb.append(".");
            sb.append(defaultFileExtension);
        }

		return sb.toString();
	}


    @Override
    public PSNoContent deleteSectionLink(String sectionGuid,
           String parentSectionGuid)
    {
        notNull(sectionGuid);
        notNull(parentSectionGuid);
        if (sectionGuid.indexOf("_") != -1)
        {
            sectionGuid = sectionGuid.split("_")[0];
        }
        navSrv.deleteNavonRelationship(idMapper.getGuid(sectionGuid), idMapper.getGuid(parentSectionGuid));
        return new PSNoContent("Section link is deleted.");
    }

    @Override
    public PSSiteSection updateExternalLink(String sectionGuid,
            PSCreateExternalLinkSection req) throws PSSiteSectionException, PSValidationException {
        notNull(sectionGuid);
        updateExternalLinkSection(req.getLinkTitle(), sectionGuid, req.getExternalUrl(), req.getTarget(), req.getCssClassNames());
        Set<String> idSet = new HashSet<>();
        idSet.add(sectionGuid);
        workflowHelper.transitionToPending(idSet);
        PSSiteSection section = loadSiteSection(idMapper.getGuid(sectionGuid), null, null, true, false, null);
        return section;
    }

    @Override
    public PSSiteSection updateSectionLink(PSUpdateSectionLink req) throws PSSiteSectionException {
        notNull(req);
        String oldSectionId = req.getOldSectionId();
        if (oldSectionId.indexOf("_") != -1)
        {
            oldSectionId = oldSectionId.split("_")[0];
        }

        if(navSrv.isNavTree(idMapper.getGuid(req.getNewSectionId())))
        {
            throw new PSSiteSectionException("Cannot update a section link that points to the home page.");
        }
        
        navSrv.replaceNavon(idMapper.getGuid(oldSectionId), idMapper.getGuid(req.getNewSectionId()), idMapper
                .getGuid(req.getParentSectionId()));
        PSSiteSection section = loadSiteSection(idMapper.getGuid(req.getNewSectionId()), idMapper.getGuid(req
                .getParentSectionId()), null, true, true, null);
        return section;
    }
    

    @Override
    public List<PSSiteBlogProperties> getBlogsForSite(String siteName) throws PSValidationException {
        notNull(siteName, "Site Name cannot be null.");
        notEmpty(siteName, "Site Name cannot be empty.");
        IPSSite site = siteMgr.findSite(siteName);
        if (site == null)
        {
            PSValidationErrorsBuilder builder = validateParameters("getBlogsForSingleSite");
            String msg = "cannot find site with name: \"" + siteName + "\"";
            builder.reject("siteName", msg).throwIfInvalid();
        }
        
        List<PSSiteBlogProperties> siteBlogsList = new ArrayList<>();
        try
        {
        	siteBlogsList = getBlogsForSingleSite(site.getName());
        }
        catch (PSNotFoundException | PSDataServiceException e)
        {
           log.error("Failed to load the root section for the site:{} Error:{}" , siteName,e.getMessage());
           log.debug(e.getMessage(),e);
        }
        return siteBlogsList;
    }
    
    @Override
    public List<PSSiteBlogProperties> getAllBlogs() throws PSDataServiceException {
        List<PSSiteBlogProperties> allBlogsList = new ArrayList<>();
        List<PSSite> sites = siteDao.findAll();
        for(PSSite site:sites)
        {
            List<PSSiteBlogProperties> siteBlogsList = new ArrayList<>();
            try
            {
                siteBlogsList = getBlogsForSingleSite(site.getName());
            }
            catch (PSNotFoundException e)
            {
                 log.error("Failed to load the root section for the site  :" + site.getName());
            }
            allBlogsList.addAll(siteBlogsList);
        }
        return allBlogsList;
    }
    
    /*
     * //see base interface method for details
     */
    @Override
    public List<String> findAllTemplatesUsedByBlogs(String siteName) throws PSDataServiceException, com.percussion.services.error.PSNotFoundException {
       List<String> blogTemplateId = new ArrayList<>();
       List<PSSite> sites = new ArrayList<>();
       if(StringUtils.isNotBlank(siteName))
       {
           //load the site
           IPSSite site = siteMgr.findSite(siteName);
           if(site == null)
           {
               throw new PSSiteSectionException("Cannot load the site with name " + siteName);
           }
           sites.add((PSSite)site);
       }
       else
       {
          sites = siteDao.findAll();
       }
       for(PSSite site:sites)
       {
          //get all blog template ids for each site and add it to the list.
           blogTemplateId.addAll(getBlogTemplateIdsForSite(site.getName()));
       }
       return blogTemplateId;
    }

    public PSSiteBlogPosts getBlogPosts(String id) throws PSValidationException, PSSiteSectionException {
        notEmpty(id);
        
        PSSiteSection blog = null;
        blog = load(id);

        PSServerFolderProcessor fProc = PSServerFolderProcessor.getInstance();
        
        String blogPath = blog.getFolderPath();
        String blogTitle = blog.getTitle();
        
        PSSiteBlogPosts blogPosts = new PSSiteBlogPosts();
        blogPosts.setBlogSectionPath(blogPath);
        blogPosts.setBlogTitle(blog.getTitle());
        
        List<PSItemProperties> posts = new ArrayList<>();
        if (blog.getSectionType() == PSSectionTypeEnum.section)
        {
            String blogPostTemplateId = getBlogPostTemplateId(blogPath);
            if (blogPostTemplateId != null)
            {
                blogPosts.setBlogPostTemplateId(blogPostTemplateId);
                
                List<IPSNode> postPages = getPostPages(blogPath, blogPostTemplateId);
                Map<String, PSCommentsSummary> commentsMap = null;
                for (IPSNode postPage : postPages)
                {
                    PSComponentSummary summary = getItemSummary(((PSLegacyGuid) postPage.getGuid()).getContentId());

                    final String tagsError = "Error getting tags from node with id: " + postPage.getGuid();
                    Collection<String> multiValues = new ArrayList<>();
                    Value[] values;
                    try {
                        values = postPage.getProperty("page_tags").getValues();
                        for (Value value : values) {
                            multiValues.add(value.getString());
                        }
                    } catch (ValueFormatException e1) {
                        log.error(tagsError, e1);
                    } catch (PathNotFoundException e1) {
                        log.error(tagsError, e1);
                    } catch (RepositoryException e1) {
                        log.error(tagsError, e1);
                    }
                    
                    PSItemProperties postProps = new PSItemProperties();
                    try {
                        postProps.setAuthor(postPage.getProperty("page_authorname").getString());
                        postProps.setSummary(postPage.getProperty("page_summary").getString());
                        postProps.setName(postPage.getProperty("resource_link_title").getString());
                        postProps.setId(idMapper.getString(postPage.getGuid()));
                        postProps.setTags(multiValues);
                    } catch (RepositoryException e) {
                        log.error("Error setting properties for blog post with ID: " + postProps.getId(), e);
                    }
                    
                    String folderPath = "";
                    try {
                        folderPath = fProc.getFolderPaths(summary.getCurrentLocator())[0];
                    } catch (PSCmsException e) {
                        log.error("Error retrieving folder path for id: " + postPage.getGuid());
                    }
                    
                    String pagePath = folderHelper.concatPath(folderPath, summary.getName());
                    postProps.setPath(PSPathUtils.getFinderPath(pagePath));

                    String postDate = PSDateUtils.getDateToString((summary.getContentPostDate() == null) ? null : summary.getContentPostDate()); 
                    
                    postProps.setPostDate(postDate);

                    if (commentsMap == null)
                    {
                        PSSiteSummary siteSum = siteDao.findByPath(folderPath);
                        commentsMap = getSiteCommentCounts(siteSum.getName());
                    }
                    
                    PSCommentsSummary commentsSum = commentsMap.get(postProps.getPath());
                    if (commentsSum != null)
                    {
                        postProps.setCommentsCount(commentsSum.getCommentCount());
                        postProps.setNewCommentsCount(commentsSum.getNewCount());
                    }
                    else
                    {
                        postProps.setCommentsCount(0);
                        postProps.setNewCommentsCount(0);
                    }
                    posts.add(postProps);
                }
            }
            else
            {
                log.warn("Attempted to get posts for section '" + blogTitle + "'.");
            }
        }
        else
        {
            log.warn("Attempted to get posts for non-blog '" + blogTitle + "'.");
        }
        
        Collections.sort(posts, new Comparator<PSItemProperties>() {
            public int compare(PSItemProperties o1, PSItemProperties o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        
        blogPosts.setPosts(posts);
        
        return blogPosts;
    }


    /**
     * Get summaries for all pages w/comments on the site with just the page path and counts.
     * 
     * @param siteName The name of the site
     * 
     * @return The map, key is the page finder path, value is the summary, never <code>null</code>, may be empty.
     */
    private Map<String, PSCommentsSummary> getSiteCommentCounts(String siteName)
    {
        Map<String, PSCommentsSummary> commentsMap = new HashMap<>();
        
        try
        {
            List<PSCommentsSummary> sums = commentsService.getCommentCountsForSite(siteName);
            for (PSCommentsSummary sum : sums)
            {
                commentsMap.put(sum.getPath(), sum);
            }
        }
        catch (WebApplicationException e)
        {
            log.warn("Failed to find comments information for site '" + siteName + "'.");
        }

        return commentsMap;
    }

    public String getBlogPostTemplateId(String path)
    {
        notEmpty(path);
        
        String id = null;
        
        try
        {
            List<PSFolder> folders = contentSrv.loadFolders(new String[]{path});
            if (!folders.isEmpty())
            {
                return folders.get(0).getPropertyValue(BLOG_POST_TEMPLATE_PROP);
            }
        }
        catch (PSErrorResultsException e)
        {
            log.error("Failed to load the folder for the path" +  path);
        }
        
        return id;
    }
    
    
    
    /**
     * Returns list of blogs for the provided site.
     * 
     * @param siteName Name of an existing site.
     * @return Never <code>null</code>.
     * @throws PSNotFoundException If the specified site does not exist.
     */
    private List<PSSiteBlogProperties> getBlogsForSingleSite(String siteName)
            throws PSNotFoundException, PSDataServiceException {
        List<PSFolder> allBlogs = contentSrv
                .getFoldersByProperty("blogPostTemplate");

        // Only return blogs for the specified site
        List<PSFolder> filteredBlogs = new ArrayList<>();
        for (PSFolder f : allBlogs) {
            List<IPSSite> sites = this.folderHelper.getItemSites(f.getGuid()
                    .toString());
            for (IPSSite s : sites) {
                if (s.getName().equals(siteName)) {
                    filteredBlogs.add(f);
                }
            }
        }

        List<PSSiteBlogProperties> blogList = new ArrayList<>();
        PSSiteBlogProperties blogProperties = null;

        for (PSFolder folder : filteredBlogs) {

            String blogPostTemplateId = getBlogPostTemplateId(folder
                    .getFolderPath());
            if (blogPostTemplateId != null) {
                blogProperties = new PSSiteBlogProperties();
                IPSGuid sectionId = navSrv.findNavigationIdFromFolder(folder
                        .getGuid());

                if (sectionId == null) {
                    continue;
                }
                IPSGuid landingPageId = navSrv
                        .getLandingPageFromNavnode(sectionId);
                if (landingPageId == null) {
                    log.warn("Cannot find landing page for blog section with template id: {}"
                            , blogPostTemplateId);
                    continue;
                }
                PSPage landingPage = pageDao.find(idMapper
                        .getString(landingPageId));
                String blogPath = landingPage.getFolderPath();
                String landingPagePath = folderHelper.concatPath(blogPath,
                        landingPage.getName());
                blogProperties.setPath(landingPagePath.replaceFirst("//", "/"));
                blogProperties.setBlogPostTemplateId(blogPostTemplateId);
                blogProperties.setTitle(landingPage.getLinkTitle());
                blogProperties.setId(idMapper.getString(sectionId));
                blogProperties.setPageId(landingPage.getId());
                blogProperties.setDescription(landingPage.getSummary());
                try {
                    PSItemProperties props = folderHelper
                            .findItemProperties(landingPagePath);
                    blogProperties.setLastPublishDate(props
                            .getLastPublishedDate());
                } catch (Exception e) {
                    log.error("Error getting properties for " + landingPagePath
                            + " [" + e.getLocalizedMessage() + "]", e);
                }
                blogList.add(blogProperties);
            }
        }
        return blogList;
    }
    
    /*
     * Used for site copy,  modify the blog templates to the new ids passed in with the template id map.
     */
    public void updateSectionBlogTemplates(String siteName, Map<String, String> tempMap) throws PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
        List<PSSiteSection> allSections = loadAllSectionsForSingleSite(siteName);

        for (PSSiteSection siteSection : allSections)
        {
            List<PSFolder> folders = null;
            try
            {
                folders = contentSrv.loadFolders(new String[]
                {siteSection.getFolderPath()});
            }
            catch (PSErrorResultsException e)
            {
                log.error("Failed to load the folder for the path" + siteSection.getFolderPath());
            }

            if (folders != null && !folders.isEmpty())
            {
                PSFolder folder = folders.get(0);
                String newIndexTemplate = null;
                String newPostTemplate = null;
                try
                {
                    newPostTemplate = tempMap.get(folder.getPropertyValue(BLOG_POST_TEMPLATE_PROP));
                    newIndexTemplate = tempMap.get(folder.getPropertyValue(BLOG_INDEX_TEMPLATE_PROPS));

                    if (newPostTemplate != null)
                    {
                        folder.setProperty(BLOG_POST_TEMPLATE_PROP, newPostTemplate);
                    }

                    if (newIndexTemplate != null)
                    {
                        folder.setProperty(BLOG_INDEX_TEMPLATE_PROPS, newIndexTemplate);
                    }

                    contentSrv.saveFolder(folder);
                }
                catch (Exception e)
                {
                    log.error("Cannot update blog templates on folder " + folder.getFolderPath() + " post template id="
                            + newPostTemplate + " indexTemplateid = " + newIndexTemplate);
                }

            }
        }
    }

    /**
     * Returns list of blog template ids for the provided sitename. These ids are blogposttemplate and 
     * blogindextemplate ids. And if site doesn't have any blogs then it returns empty list 
     * @param siteName assumed not blank.
     * @return list of blog template ids never null may be empty.
     */
    private List<String> getBlogTemplateIdsForSite(String siteName) throws PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
       List<String> blogTemplateIds = new ArrayList<>();
       List<PSSiteSection> allSections =loadAllSectionsForSingleSite(siteName);
       
       for(PSSiteSection siteSection : allSections)
       {
           try
           {
               List<PSFolder> folders = contentSrv.loadFolders(new String[]{siteSection.getFolderPath()});
               if (!folders.isEmpty())
               {
                    String blogPostTemplateId = folders.get(0).getPropertyValue(BLOG_POST_TEMPLATE_PROP);
                    String blogIndexTemplateId = folders.get(0).getPropertyValue(BLOG_INDEX_TEMPLATE_PROPS);
                    
                    if(StringUtils.isNotBlank(blogIndexTemplateId))
                          blogTemplateIds.add(blogIndexTemplateId);
                    
                    if(StringUtils.isNotBlank(blogPostTemplateId))
                          blogTemplateIds.add(blogPostTemplateId);
               }
           }
           catch (PSErrorResultsException e)
           {
               log.error("Failed to load the folder for the path" +  siteSection.getFolderPath());
           }
       }
       return blogTemplateIds; 
    }

    /**
     * The helper class used for replacing landing page of a section.
     */
    private class ReplaceLandingPageHelper
    {
        private IPSGuid navNodeId;

        private IPSGuid newLandingPageId;

        private IPSGuid oldLandingPageId;

        private String oldLandingPageName;

        private String oldLandingPageLinkTitle;

        private PSPage oldLandingPage;

        private String renamedLandingPageName;

        private PSItemStatus oldPageStatus;

        private PSItemStatus newPageStatus;

        private PSItemStatus navNodeStatus;

        /**
         * Creates the helper from a request.
         * 
         * @param request the request, assumed not <code>null</code>.
         */
        private ReplaceLandingPageHelper(PSReplaceLandingPage request) throws PSDataServiceException {
            navNodeId = idMapper.getGuid(request.getSectionId());
            newLandingPageId = idMapper.getGuid(request.getNewLandingPageId());
            oldLandingPageId = navSrv.getLandingPageFromNavnode(navNodeId);
            oldLandingPage = pageDao.find(idMapper.getString(oldLandingPageId));
            oldLandingPageName = oldLandingPage.getName();
            oldLandingPageLinkTitle = oldLandingPage.getLinkTitle();
        }

        /**
         * Prepare all items (landing pages and navigation item) before modify
         * them. Must call {@link #releaseFromEdit()} after finishing editing
         * the items. The workflow state and the revision of the items may be
         * changed after this call.
         */
        private void prepareForEdit() throws PSValidationException {
            oldPageStatus = prepareForEdit(oldLandingPageId, true);
            newPageStatus = prepareForEdit(newLandingPageId, true);
            navNodeStatus = prepareForEdit(navNodeId, false);

            // update the IDs, because the revision of the items may be changed
            // after a call to "prepareForEdit(...)"
            oldLandingPageId = getIdFromStatus(oldPageStatus);
            newLandingPageId = getIdFromStatus(newPageStatus);
            navNodeId = getIdFromStatus(navNodeStatus);
        }

        /**
         * Prepare for editing the specified item. This will throw validation
         * error if failed to prepare the item for edit.
         * 
         * @param id the ID of the item, assumed not <code>null</code>.
         * @param isPage <code>true</code> if the item is a page.
         * 
         * @return the prepared status of the item, never <code>null</code>.
         */
        private PSItemStatus prepareForEdit(IPSGuid id, boolean isPage) throws PSValidationException {
            try
            {
                return contentSrv.prepareForEdit(id);
            }
            catch (PSErrorException e)
            {
                log.debug("Failed to prepare for edit for item id=" + id.toString());

                String type = isPage ? "page" : "asset";
                PSComponentSummary summary = getItemSummary(((PSLegacyGuid) id).getContentId());
                String msg;
                if (!isEmpty(summary.getCheckoutUserName()) && !isItemCheckedOutToUser(summary))
                {
                    msg = "The " + type + " '" + summary.getName() + "' is being edited by user '"
                            + summary.getCheckoutUserName() + "', please open it to override.";
                }
                else
                {
                    msg = "You are not authorized to modify " + type + " \"" + summary.getName()
                            + "\", please open it to override.";
                }

                PSValidationErrorsBuilder builder = validateParameters("replaceLandingPage");
                builder.reject("no.authority.to.edit", msg).throwIfInvalid();
                return null;
            }
        }

        /**
         * Checks in all modified items (landing pages and navigation item), a
         * matching call to {@link #prepareForEdit()}.
         * 
         * Note, this does not change workflow state of the items, only checks
         * in items.
         * <p>
         * After {@link #prepareForEdit()} and {@link #releaseFromEdit()}, the
         * workflow state of the items may be changed. For example, if an item
         * was in PENDING or LIVE, then it be in QUICK-EDIT afterwards.
         */
        private void releaseFromEdit()
        {
            if (oldPageStatus != null)
                contentSrv.releaseFromEdit(oldPageStatus, true);
            if (newPageStatus != null)
                contentSrv.releaseFromEdit(newPageStatus, true);
            if (navNodeStatus != null)
                contentSrv.releaseFromEdit(navNodeStatus, true);
        }

        /**
         * Gets current GUID of the specified item.
         * 
         * @param status the status of the item, assumed not <code>null</code>.
         * @return the GUID of the item, never <code>null</code>.
         */
        private IPSGuid getIdFromStatus(PSItemStatus status)
        {
            IPSGuid guid = new PSLegacyGuid(status.getId(), -1);
            return contentDsSrv.getItemGuid(guid);
        }

        /**
         * Performs the actual landing page replacement operation.
         */
        private void doReplace() throws PSDataServiceException {
            // rename old landing page to old-name(#)
            renamedLandingPageName = renameLandingPage(oldLandingPage);

            // link navigation to the new page
            navSrv.addLandingPageToNavnode(newLandingPageId, navNodeId, asmBridge.getDispatchTemplate());

            // set name & link-text for the new landing page
            updateLandingPage(newLandingPageId, navNodeId, oldLandingPageName, oldLandingPageLinkTitle);
        }

        /**
         * Updates the name and link-text for the specified landing page.
         * 
         * @param pageId the ID of the landing page, assumed not
         *            <code>null</code> .
         * @param newName the new name of the landing page, assumed not blank.
         * @param newLinkTitle the new link text of the landing page, assumed
         *            not blank.
         */
        private void updateLandingPageFields(IPSGuid pageId, String newName, String newLinkTitle) throws PSDataServiceException {
            PSPage landingPage = pageDao.find(idMapper.getString(pageId));
            landingPage.setName(newName);
            landingPage.setLinkTitle(newLinkTitle);
            pageDao.save(landingPage);
        }

        /**
         * Moves the specified landing page into the same folder as the
         * navigation node if needed, and set name and link-text to the
         * specified landing page.
         * 
         * @param pageId the specified landing page, assumed not
         * <code>null</code> .
         * @param navigationNodeId the ID of the specified navigation node,
         * assumed not <code>null</code>.
         * @param newName the new name of the landing page, assumed not blank.
         * @param newLinkTitle the new link text of the landing page, assumed
         * not blank.
         */
        private void updateLandingPage(IPSGuid pageId, IPSGuid navigationNodeId, String newName, String newLinkTitle) throws PSDataServiceException {
            IPSGuid navParentId = folderHelper.getParentFolderId(navigationNodeId);
            IPSGuid pageParentId = folderHelper.getParentFolderId(pageId);
            if (navParentId.equals(pageParentId))
            {
                updateLandingPageFields(pageId, newName, newLinkTitle);
                return;
            }

            contentSrv.removeFolderChildren(pageParentId, Collections.singletonList(pageId), false);
            updateLandingPageFields(pageId, newName, newLinkTitle);
            contentSrv.addFolderChildren(navParentId, Collections.singletonList(pageId));
        }

        /**
         * Gets the name of the workflow state for the specified item.
         * 
         * @param id the ID of the item in question, assumed not
         *            <code>null</code> .
         * 
         * @return the workflow name, never blank.
         */
        private String getWorkflowState(IPSGuid id)
        {
            int contentId = ((PSLegacyGuid) id).getContentId();
            PSComponentSummary compSum = PSWebserviceUtils.getItemSummary(contentId);
            PSWorkflow wf = PSWebserviceUtils.getWorkflow(compSum.getWorkflowAppId());
            PSState state = PSWebserviceUtils.getStateById(wf, compSum.getContentStateId());
            return state.getName();
        }

        /**
         * Gets the response or result of the replacement operation.
         * 
         * @return the response, never <code>null</code>.
         */
        private PSReplaceLandingPage getReponse()
        {
            PSReplaceLandingPage response = new PSReplaceLandingPage();
            response.setSectionId(idMapper.getString(navNodeId));
            response.setNewLandingPageId(idMapper.getString(newLandingPageId));

            response.setNewLandingPageName(oldLandingPageName);
            response.setOldLandingPageName(renamedLandingPageName);
            response.setOldLandingPageToState(getWorkflowState(oldLandingPageId));
            response.setNewLandingPageToState(getWorkflowState(newLandingPageId));

            return response;
        }
    }

    /*
     * see base interface method for details
     */
    public PSReplaceLandingPage replaceLandingPage(PSReplaceLandingPage request) throws PSDataServiceException {
        ReplaceLandingPageHelper helper = new ReplaceLandingPageHelper(request);

        try
        {
            helper.prepareForEdit();
            helper.doReplace();
            return helper.getReponse();
        }
        finally
        {
            helper.releaseFromEdit();
        }
    }

    /*
     * see base interface method for details
     */
    public PSSiteSection move(PSMoveSiteSection req) throws PSValidationException, PSSiteSectionException {
        moveRequestValidator.validate(req).throwIfInvalid();
         
        if (req.getSourceId().indexOf("_") > -1)
        {
            moveSectionLink(req);
        }
        else
        {
            moveSection(req);
        }
        return load(req.getTargetId());
    }

    /**
     * Move a specified section from the source location to a new location.
     * @param req the request info, assumed not <code>null</code>.
     */
    private void moveSection(PSMoveSiteSection req) throws PSSiteSectionException, PSValidationException {
        validateMoveSection(req.getSourceId());
        
        IPSGuid sourceParentId = idMapper.getGuid(req.getSourceParentId());
        IPSGuid sourceId = idMapper.getGuid(req.getSourceId());
        IPSGuid targetId = idMapper.getGuid(req.getTargetId());
        try
        {
            // ensure Admin perms
            folderHelper.hasFolderPermission(folderHelper.getParentFolderId(sourceParentId).toString(), Access.ADMIN);
            folderHelper.hasFolderPermission(folderHelper.getParentFolderId(sourceId).toString(), Access.ADMIN);
            folderHelper.hasFolderPermission(folderHelper.getParentFolderId(targetId).toString(), Access.ADMIN);
            navSrv.moveNavon(sourceId, sourceParentId, targetId, req.getTargetIndex());
        }
        catch (Exception ex)
        {
            log.error("Failed to move the source navigation node {} to the target navigation node {} Error was: {}",req.getSourceId() , req.getTargetId() , ex.getMessage());
            throw new PSSiteSectionException(ex.getMessage());

        }
    }

    /**
     * Validates the section for move operation, make sure the section is only under one site
     * It is invalid if the section is under more than 1 site.
     * @param sourceId the ID of the section, assumed not <code>null</code>.
     */
    private void validateMoveSection(String sourceId) throws PSValidationException {
        List<IPSSite> sites = publishingWs.getItemSites(idMapper.getGuid(sourceId));
        if((sites != null) && (!sites.isEmpty()))
        { 
           PSSiteCopyUtils.throwCopySiteMessageIfNotAllowed(sites.get(0).getName(), "move",
                PSSiteCopyUtils.CAN_NOT_MOVE_SECTION);
        }
    }

    /**
     * Move a specified section link from the source location to a new location.
     * @param req the request info, assumed not <code>null</code>.
     */
    private void moveSectionLink(PSMoveSiteSection req) throws PSSiteSectionException, PSValidationException {
        String sectionId = req.getSourceId().split("_")[0]; 
        validateMoveSection(sectionId);

        String parentId = req.getSourceId().split("_")[1];
        IPSGuid sectionGuid = idMapper.getGuid(sectionId);
        IPSGuid targetGuid = idMapper.getGuid(req.getTargetId());
        IPSGuid parentGuid = idMapper.getGuid(parentId);
        
        
        if (!targetGuid.equals(parentGuid))
        {
            List<IPSGuid> targetChildNavonIds = navSrv.findChildNavonIds(contentDsSrv.getItemGuid(targetGuid));
            for (IPSGuid ipsGuid : targetChildNavonIds)
            {

                if (ipsGuid.longValue() == sectionGuid.longValue())
                    throw new PSSiteSectionException(
                           "Section and a link to it or duplicate section links at the same level are not allowed");
            }
        }
        deleteSectionLink(sectionId, parentId);
        navSrv.addNavonToParentNavon(sectionGuid, targetGuid, req.getTargetIndex());
    }

    public PSSiteSectionProperties getSectionProperties(String id) throws PSSiteSectionException {
        IPSGuid guid = contentDsSrv.getItemGuid(idMapper.getGuid(id));
        String title = navSrv.getNavTitle(guid);

        String[] paths = contentSrv.findFolderPaths(guid);
        if (paths == null || paths.length == 0)
            throw new PSSiteSectionException("Cannot find folder for nav-id = " + id.toString());

        IPSGuid folderId = contentSrv.getIdByPath(paths[0]);
        PSFolder folder = contentSrv.loadFolder(folderId, false);
        List<String> temp = new ArrayList<>();
        temp.add(NAVON_FIELD_TARGET);
        temp.add(NAVON_FIELD_REQUIRESLOGIN);
        temp.add(NAVON_FIELD_ALLOWACCESSTO);
        temp.add(NAVON_FIELD_CSSCLASSNAMES);
        
        Map<String, String> navProps = navSrv.getNavonProperties(idMapper.getGuid(id), temp);
        PSSectionTargetEnum stgtEnum = PSSectionTargetEnum._self;
        String target = navProps.get(NAVON_FIELD_TARGET);
        try
        {
            stgtEnum = PSSectionTargetEnum.valueOf(target);
        }
        catch (Exception e)
        {
            stgtEnum = PSSectionTargetEnum._self;
        }

        PSSiteSectionProperties props = new PSSiteSectionProperties();
        props.setId(id);
        props.setTitle(title);
        props.setFolderName(folder.getName());
        props.setTarget(stgtEnum);
        props.setFolderPermission(getFolderPermission(folder));
        props.setCssClassNames(navProps.get(NAVON_FIELD_CSSCLASSNAMES));

        addSectionSecurityProperties(load(guid.toString()), props);
        
        return props;
    }

    /**
     * 
     * @param psSiteSection the id of the section
     * @param props the {@link PSSiteSectionProperties} object to add the site
     *            properties. Assumed not <code>null</code>.
     */
    private void addSectionSecurityProperties(PSSiteSection psSiteSection, PSSiteSectionProperties props)
    {
        // Add site properties
        List<IPSSite> sites = folderHelper.getItemSites(psSiteSection.getId());
        IPSSite site = sites.get(0);
        props.setSecureSite(site.isSecure());
        
        if(!site.isSecure())
        {
            return;
        }

        // Calculate section properties
        List<IPSGuid> pathIds = contentSrv.findPathIds(psSiteSection.getFolderPath());
        for(int i = 0; i < pathIds.size(); i++)
        {
            IPSGuid folderGuid = pathIds.get(i);
            IPSGuid guid = navSrv.findNavigationIdFromFolder(folderGuid);
            if(guid == null)
            {
                continue;
            }
            Map<String, String> navProps = getSectionSecurityProperties(guid.toString());

            // if parent section is secure, copy the corresponding properties
            if(Boolean.valueOf(navProps.get(NAVON_FIELD_REQUIRESLOGIN)))
            {
                props.setRequiresLogin(Boolean.valueOf(navProps.get(NAVON_FIELD_REQUIRESLOGIN)));
                props.setAllowAccessTo(navProps.get(NAVON_FIELD_ALLOWACCESSTO));

                // set parent is secure only if it is not the section itself that is secure
                props.setSecureAncestor(i != pathIds.size() - 1);
                
                break;
            }
        }
        
    }

    /**
     * @param guid
     * @return
     */
    private Map<String, String> getSectionSecurityProperties(String guid)
    {
        List<String> temp = new ArrayList<>();
        temp.add(NAVON_FIELD_REQUIRESLOGIN);
        temp.add(NAVON_FIELD_ALLOWACCESSTO);
        return  navSrv.getNavonProperties(idMapper.getGuid(guid), temp);
    }

    /*
     * see base interface method for details
     */
    public PSSiteSection update(PSSiteSectionProperties req) throws PSDataServiceException {
        updateRequestValidator.validate(req).throwIfInvalid().getValidationErrors();
        List<IPSSite> sites = publishingWs.getItemSites(idMapper.getGuid(req.getId()));
        if((sites != null) && (sites.size() >= 1))
        { 
           PSSiteCopyUtils.throwCopySiteMessageIfNotAllowed(sites.get(0).getName(), "update", 
                PSSiteCopyUtils.CAN_NOT_EDIT_SECTION);
        }
        IPSGuid navonId = idMapper.getGuid(req.getId());
        setLinkTitleForLandingPage(navonId, req.getTitle());

        updateSectionFolder(req);
        PSSectionTargetEnum target = req.getTarget();
        String tgt = target == null || target == PSSectionTargetEnum._self ? "" : target.name();
       
        Map<String, String> map = new HashMap<>();
        map.put(NAVON_FIELD_DISPLAYTITLE, req.getTitle());
        map.put(NAVON_FIELD_TARGET, tgt);
        map.put(NAVON_FIELD_REQUIRESLOGIN, Boolean.toString(req.isRequiresLogin()));
        map.put(NAVON_FIELD_ALLOWACCESSTO, req.getAllowAccessTo());
        map.put(NAVON_FIELD_CSSCLASSNAMES, req.getCssClassNames());
        
        navSrv.setNavonProperties(navonId, map);

        if (!req.isSiteRootSection() && sites.get(0).isSecure())
        {
           removeSiteTouchedFile(sites.get(0).getName());
        }
        psContentEvent=new PSContentEvent(req.getId(),req.getId().substring(req.getId().lastIndexOf("-")+1,req.getId().length()),req.getFolderName(), PSContentEvent.ContentEventActions.update, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
        psAuditLogService.logContentEvent(psContentEvent);
        return load(req.getId());
    }


    /**
     * 
     * @param sitename
     */
    private void removeSiteTouchedFile(String sitename) throws DataServiceSaveException {
        try
        {
            removeTouchedFile(sitename);
        }
        catch (IOException e)
        {
            String errorMsg = "Failed removing the tch file for site '" + sitename
                    + "' after changing section properties";
            log.error(errorMsg, e);
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    @Override
    public void generateSecurityConfigurationFiles(IPSSite site) throws DataServiceSaveException, PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
        PSSectionNode tree = loadTree(site.getName());
        try
        {
            PSSiteConfigUtils.updateSecureSectionsConfiguration(site.getName(), site.getLoginPage(), tree);
        }
        catch(Exception e)
        {
            String errorMsg = "Failed updating the configuration properties for site '" + site.getName()
                    + "' after updating the site properties.";
            log.error(errorMsg, e);
            throw new DataServiceSaveException(errorMsg, e);
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.service.IPSSiteSectionService#clearSectionsSecurityInfo(java.lang.String)
     */
    @Override
    public void clearSectionsSecurityInfo(String sitename) throws PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
        PSSectionNode tree = loadTree(sitename);
        unsecureChildNode(tree);
    }

    /**
     * Clears the properties for security in the given node, and recursively
     * process all its child nodes.
     * 
     * @param childNode {@link PSSectionNode} node object. Assumed not
     *            <code>null</code>
     */
    private void unsecureChildNode(PSSectionNode childNode)
    {
        unsecureNode(childNode);
        
        // base case
        if(childNode.getChildNodes() == null)
        {
            return;
        }
        
        for(PSSectionNode child : childNode.getChildNodes())
        {
            unsecureChildNode(child);
        }
    }

    /**
     * Sets the security properties values to the default ones.
     * 
     * @param sectionTree {@link PSSectionNode} node object. Assumed not
     *            <code>null</code>.
     */
    private void unsecureNode(PSSectionNode sectionTree)
    {
        // check first if the update is needed
        if(!sectionTree.isRequiresLogin() && StringUtils.isBlank(sectionTree.getAllowAccessTo()))
        {
            return;
        }

        IPSGuid navonId = idMapper.getGuid(sectionTree.getId());
        
        Map<String, String> map = new HashMap<>();
        map.put(NAVON_FIELD_REQUIRESLOGIN, Boolean.toString(false));
        map.put(NAVON_FIELD_ALLOWACCESSTO, null);
        
        navSrv.setNavonProperties(navonId, map);
    }

    /**
     * Sets the link title for the landing page of the specified navigation
     * node.
     * 
     * @param navonId the ID of the navigation node in question, assumed not
     *            blank.
     * @param linkTitle the new link title of the landing page, assumed not
     *            blank.
     * 
     * @return the starting state of the landing page, which may be transitioned
     *         into different state after this call.
     */
    private long setLinkTitleForLandingPage(IPSGuid navonId, String linkTitle) throws PSDataServiceException {
        IPSGuid id = navSrv.getLandingPageFromNavnode(navonId);
        if(id == null){
            return 0;
        }

        PSItemStatus status;
        try
        {
            status = contentSrv.prepareForEdit(id);
        }
        catch (PSErrorException e)
        {

            if (PSWebserviceUtils.isRootCauseOfType(e, IPSExtensionErrors.CHECKOUT_NOT_ALLOWED))
            {
                throw new PSSiteSectionException("Cannot modify section properties because its landing page is " +
                    "checked out to someone else.");
            }
            
            throw e;
        }
                
        long fromStateId = status.getFromStateId();

        String pageId = idMapper.getString(id);
        PSPage page = pageDao.find(pageId);
        page.setLinkTitle(linkTitle);
        pageDao.save(page);

        contentSrv.releaseFromEdit(status, false);

        return fromStateId;
    }

    /**
     * Updates the folder of the specified section.
     * 
     * @param req the new section properties, assumed not <code>null</code>.
     */
    private void updateSectionFolder(PSSiteSectionProperties req) throws PSValidationException {
        IPSGuid folderId = folderHelper.getParentFolderId(idMapper.getGuid(req.getId()));

        PSFolderProperties folderProps = new PSFolderProperties();
        folderProps.setId(idMapper.getString(folderId));
        folderProps.setName(req.getFolderName());
        folderProps.setPermission(req.getFolderPermission());

        folderHelper.saveFolderProperties(folderProps);
    }

    /*
     * //see base interface method for details
     */
    public PSSiteSection loadRoot(String siteName) throws PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
        IPSSite site = siteMgr.loadSite(siteName);
        IPSGuid navTreeId = navSrv.findNavigationIdFromFolder(site.getFolderRoot());
        if (navTreeId == null)
        {
            PSNavException ne = new PSNavException(
                    IPSNavigationErrors.NAVIGATION_SERVICE_CANNOT_FIND_NAVTREE_FOR_SITE,
                    siteName
            );

            log.error(ne.getLocalizedMessage());
            log.warn("Removing invalid site definition:" + siteName);
            siteMgr.deleteSite(site);
        }

        PSSiteSection root = loadSiteSection(navTreeId, null, site.getFolderRoot(), true, false, null);
        return root;
    }

    /*
     * //see base interface method for details
     */
    public List<PSSiteSection> loadChildSections(PSSiteSection section)
    {
        List<PSSiteSection> result = new ArrayList<>();
        
        if (section.getSectionType() == PSSectionTypeEnum.section)
        {
            for (String cId : section.getChildIds())
            {
                String idStr = cId;
                if (cId.indexOf("_") != -1)
                {
                    idStr = cId.split("_")[0];
                }
                IPSGuid childId = idMapper.getGuid(idStr);
                PSSiteSection child = null;
                try{
                	child = loadSiteSection(childId, idMapper.getGuid(section.getId()), null, true, true, section.getDisplayTitlePath());
                }catch(Exception e){
                	log.error("Error loading section with id:" + idStr, e);
                }
                
                if(child!=null)
                	result.add(child);
            }
        }
        
        return result;
    }

    /**
     * Loads a specified site section.
     * 
     * @param id the ID of the site section, assumed not <code>null</code>.
     * @param folderPath the folder path of the section. It may be
     *            <code>null</code> if unknown.
     * @param resolvePath <code>true</code> if need to resolve or set the folder
     *            path for the returned section object.
     * @param resolveDisplayPath
     * @return the specified site section, never <code>null</code>.
     */
    private PSSiteSection loadSiteSection(IPSGuid id, IPSGuid parentGuid, String folderPath, boolean resolvePath,
            boolean resolveDisplayPath, String parentDisplayPath) throws PSSiteSectionException {
        String idStr = idMapper.getString(id);
        if (idStr.indexOf("_") != -1)
        {
            idStr = idStr.split("_")[0];
            id = idMapper.getGuid(idStr);
        }
        try
        {
            id = contentDsSrv.getItemGuid(id);
            //FB: NP_GUARANTEED_DEREF  - NC - 1/16/16
            if(id == null){
            	throw new Exception("Cannot get guid for nav-id - " + idStr);
            }
        }
        catch (Exception e)
        {
            throw new PSSiteSectionException("Cannot get guid for nav-id = " + idStr, e);
        }
        List<String> childIds = new ArrayList<>();
        List<String> tempChildIds = idMapper.getStrings(navSrv.findChildNavonIds(id));
        for(String ids : tempChildIds) {
            if(!childIds.contains(ids)){
                childIds.add(ids);
            }
        }
        List<String> temp = new ArrayList<>();
        temp.add(NAVON_FIELD_DISPLAYTITLE);
        temp.add(NAVON_FIELD_TYPE);
        temp.add(NAVON_FIELD_TARGET);
        temp.add(NAVON_FIELD_EXTERNALURL);
        temp.add(NAVON_FIELD_REQUIRESLOGIN);
        temp.add(NAVON_FIELD_ALLOWACCESSTO);
        temp.add(NAVON_FIELD_CSSCLASSNAMES);
        Map<String, String> navProps = navSrv.getNavonProperties(id, temp);
        String title = navProps.get(NAVON_FIELD_DISPLAYTITLE);
        String sectionType = navProps.get(NAVON_FIELD_TYPE);
        PSSectionTypeEnum stEnum = PSSectionTypeEnum.section;
        try
        {
            stEnum = PSSectionTypeEnum.valueOf(sectionType);
        }
        catch (Exception e)
        {
            stEnum = PSSectionTypeEnum.section;
        }
        if (id != null && parentGuid != null && stEnum == PSSectionTypeEnum.section)
        {
            if (navSrv.isSectionLink(id, parentGuid))
            {
                stEnum = PSSectionTypeEnum.sectionlink;

            }
        }
        PSSectionTargetEnum stgtEnum = PSSectionTargetEnum._self;
        String target = navProps.get(NAVON_FIELD_TARGET);
        try
        {
            stgtEnum = PSSectionTargetEnum.valueOf(target);
        }
        catch (Exception e)
        {
            stgtEnum = PSSectionTargetEnum._self;
        }

        if (folderPath == null && resolvePath)
        {
            String[] paths = contentSrv.findFolderPaths(id);
            if (paths == null || paths.length == 0)
                throw new PSSiteSectionException("Cannot find folder for nav-id = " + id.toString());

            folderPath = paths[0];
        }

        PSSiteSection section = new PSSiteSection();
        section.setTitle(title);
        section.setFolderPath(folderPath);
        section.setChildIds(childIds);
        section.setSectionType(stEnum);
        section.setTarget(stgtEnum);
        section.setExternalLinkUrl(navProps.get(NAVON_FIELD_EXTERNALURL));
        section.setRequiresLogin(Boolean.valueOf(navProps.get(NAVON_FIELD_REQUIRESLOGIN)));
        section.setAllowAccessTo(navProps.get(NAVON_FIELD_ALLOWACCESSTO));
        section.setCssClassNames(navProps.get(NAVON_FIELD_CSSCLASSNAMES));
        
        id = resetRevision(id);
        if (!StringUtils.isEmpty(parentDisplayPath))
        {
            StringBuffer path = new StringBuffer(parentDisplayPath);
            section.setDisplayTitlePath(path.append("/").append(section.getTitle()).toString());
        }
        
        if (stEnum == PSSectionTypeEnum.sectionlink)
        {
            section.setId(idMapper.getString(id) + "_" + idMapper.getString(parentGuid));
            if (resolveDisplayPath)
            {
                if (StringUtils.isEmpty(parentDisplayPath))
                {
                    section.setDisplayTitlePath(buildDisplayTitlePath(id));
                } 
            }
        }
        else
        {
            section.setId(idMapper.getString(id));
        }

        return section;
    }

    /**
     * @param navonId
     * @return
     */
    private String buildDisplayTitlePath(IPSGuid navonId)
    {
        List<IPSGuid> ids = navSrv.findAncestorNavonIds(navonId);
        String displayPath = "";
        for(IPSGuid id : ids)
        {
            displayPath += "/" + navSrv.getNavTitle(id);
        }
        
        displayPath += "/" + navSrv.getNavTitle(navonId);
        
        return displayPath;
    }
    
    /*
     * //see base interface method for details
     */
    public PSSectionNode loadTree(String siteName) throws PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
        PSSiteSection root = loadRoot(siteName);
        PSSectionNode tree = loadSectionTree(root);
        return tree;
    }

    /**
     * Loads the section node and all its descendant nodes.
     * 
     * @param section the node in question, assumed not <code>null</code>.
     * 
     * @return the section node and its descendant nodes, never
     *         <code>null</code> .
     */
    private PSSectionNode loadSectionTree(PSSiteSection section) throws PSSiteSectionException {
        PSSectionNode node = new PSSectionNode();
        node.setId(section.getId());
        node.setTitle(section.getTitle());
        node.setSectionType(section.getSectionType());
        node.setRequiresLogin(section.isRequiresLogin());
        node.setAllowAccessTo(section.getAllowAccessTo());
        node.setFolderPath(section.getFolderPath());
        List<PSSectionNode> childNodes = new ArrayList<>();
        if (section.getSectionType() == PSSectionTypeEnum.section)
        {
            IPSGuid sectionGuid = idMapper.getGuid(section.getId());
            String displayPath = section.getDisplayTitlePath();
            if (StringUtils.isEmpty(displayPath))
            {
                displayPath = buildDisplayTitlePath(sectionGuid);
            }

            for (String id : section.getChildIds())
            {
                PSSiteSection cSection = loadSiteSection(idMapper.getGuid(id), sectionGuid, null,
                        true, true, displayPath);
                PSSectionNode cNode = loadSectionTree(cSection);
                cNode.setFolderPath(cSection.getFolderPath());
                childNodes.add(cNode);
            }
        }
        node.setChildNodes(childNodes);

        return node;
    }

    /**
     * Resets the revision of the specified ID to <code>-1</code> if it is not.
     * This is used to keep the ID consistent for all the parent and child nodes
     * otherwise the revision of the child IDs will be <code>-1</code>, but it
     * may be a different values for the parent nodes
     * 
     * @param id the ID in question, assumed not <code>null</code>.
     * 
     * @return the ID with revision equals to <code>-1</code>.
     */
    private IPSGuid resetRevision(IPSGuid id)
    {
        // always set revision to -1 so that it is consistent with the child IDs
        // which is needed by the UI.
        if (((PSLegacyGuid) id).getRevision() != -1)
        {
            id = new PSLegacyGuid(((PSLegacyGuid) id).getContentId(), -1);
        }
        return id;
    }
  
    /**
     * Loads all sections for the provided site it provided, not only immediate child sections but 
     * as many level as it is.
     * @param siteName
     * @return
     */
    private List<PSSiteSection> loadAllSectionsForSingleSite(String siteName) throws PSSiteSectionException, com.percussion.services.error.PSNotFoundException {
        PSSiteSection  rootSection = new PSSiteSection();
        List<PSSiteSection> allSections = new ArrayList<>();
        List<PSSiteSection> childSections = new ArrayList<>();
        rootSection = loadRoot(siteName);
        childSections = loadChildSections(rootSection);
        for(PSSiteSection section :childSections)
        {
            allSections.add(section);
            List<PSSiteSection> sections = new ArrayList<>();
            sections = getAllDescendantSections(sections,section);
            allSections.addAll(sections);
        }
        return allSections;
    }
    
    /**
     * Traverse recursively and returns the list of all descendant sections (any level) for the
     * provided section.
     * @param sections list used to collect descendant sections.
     * @param section 
     * @return list of all descendant sections.
     */
    private List<PSSiteSection> getAllDescendantSections(List<PSSiteSection> sections, PSSiteSection section)
    {
        List<PSSiteSection> childSections = loadChildSections(section);
        for(PSSiteSection child: childSections)
        {
            sections.add(child);
            getAllDescendantSections(sections,child);
        }
        return sections;
    }

    public PSSiteSection load(String sId) throws PSSiteSectionException {
        IPSGuid id = idMapper.getGuid(sId);
        PSSiteSection section = loadSiteSection(id, null, null, true, true, null);
        return section;
    }

    /**
     * Creates a child folder.
     * 
     * @param name the name of the to be created folder, assumed not blank.
     * @param parentPath the parent folder path that contains the created
     *            folder, assumed not blank.
     * @param templateId id of the template used for new blog posts, will be <code>null</code> for regular sections.
     *            This will be stored as a property on the folder.
     * 
     * @return the created folder, never <code>null</code>.
     */
    private PSFolder createFolder(String name, String parentPath, String templateId) throws PSSiteSectionException {
        if (contentSrv.isChildExistInFolder(parentPath, name))
        {
            throw new PSSiteSectionException("Cannot create a folder (name=" + name + ") under parent folder: "
                    + parentPath);
        }

        try
        {
            PSFolder folder = contentSrv.addFolder(name, parentPath, false);
            
            if (templateId != null)
            {
                folder.setProperty(BLOG_POST_TEMPLATE_PROP, templateId);
            }
            else
            {
                // delete the inherited property if it exists
                folder.deleteProperty(BLOG_POST_TEMPLATE_PROP);
            }

            // set permission to WRITE
            setFolderPermission(folder, Access.WRITE);
            contentSrv.saveFolder(folder);

            return folder;
        }
        catch (PSErrorException e)
        {
            log.error("Failed to create a folder name=" + name + ", under folder-path=\"" + parentPath + "\"", e);
            throw new PSSiteSectionException(e);
        }
    }

    /**
     * Creates a landing page from the specified parameters.
     * 
     * @param req the request for creating the landing page, assumed not
     *            <code>null</code>.
     * @param folderPath the folder path that contains the created landing page,
     *            not blank.
     * @param navonId the ID of the navigation node that will link to the
     *            landing page, assumed not <code>null</code>.
     * 
     * @return the created landing page, never <code>null</code>.
     */
    private PSPage createLandingPage(PSCreateSiteSection req, String folderPath, IPSGuid navonId) throws PSDataServiceException {
        PSPage page = new PSPage();
        page.setFolderPath(folderPath);
        
        if (StringUtils.isEmpty(req.getPageName()))
		{
        	req.setPageName(getDefaultDocument(folderPath));
		}
        page.setName(req.getPageName());
        page.setTitle(req.getPageTitle());
        page.setTemplateId(req.getTemplateId());
        page.setLinkTitle(req.getPageLinkTitle());

        pageDaoHelper.setWorkflowAccordingToParentFolder(page);
        
        page = pageDao.save(page);
        IPSGuid pageId = idMapper.getGuid(page.getId());

        // attach landing page to navon
        PSItemStatus status = contentSrv.prepareForEdit(navonId);
        navSrv.addLandingPageToNavnode(pageId, navonId, asmBridge.getDispatchTemplate());
        contentSrv.releaseFromEdit(status, false);

        contentSrv.checkinItems(Collections.singletonList(pageId), null);

        return page;
    }

    /*
     * //see base interface method for details
     */
    public void delete(String id) throws PSValidationException, DataServiceSaveException {
        IPSGuid guid = idMapper.getGuid(id);
        IPSGuid folderId = folderHelper.getParentFolderId(guid);
        
        List<IPSSite> sites = publishingWs.getItemSites(guid);
        
        if(sites.get(0).isSecure())
        {
            removeSiteTouchedFile(sites.get(0).getName());
        }
       	contentSrv.deleteFolders(Collections.singletonList(folderId), true);
    }

    public void convertToFolder(String id) throws PSValidationException {
        PSSiteSection section = null;
        try{
            section = load(id);
        }
        catch(Exception e){
            log.error("Failed to load section for the supplied id: " + id, e);
            throw new RuntimeException("Failed to find the selected section, it might have been deleted in another session.",e);
        }
        //Group all child sections as different type of sections require different treatment
        List<String> externalLinkSections = new ArrayList<>();
        Map<String, String> sectionLinks = new HashMap<>();
        List<String> sections = new ArrayList<>();
        groupChildSectionsByType(section, externalLinkSections,sectionLinks,sections);
        
        //For external links we need to delete the parent folders
        List<IPSGuid> parentFolderIds = new ArrayList<>();
        for (String guid : externalLinkSections) {
            parentFolderIds.add(folderHelper.getParentFolderId(idMapper.getGuid(guid)));
        }
        if(parentFolderIds.size()>0)
            contentSrv.deleteFolders(parentFolderIds, true);
        
        //For section links we just need to delete the links
        for (Entry<String, String> entry : sectionLinks.entrySet()) {
            deleteSectionLink(entry.getKey(), entry.getValue());
        }
        
        //For sections we need to delete the navons
        List<IPSGuid> sectionIds = new ArrayList<>();
        for (String guid : sections) {
            sectionIds.add(idMapper.getGuid(guid));
        }
        sectionIds.add(idMapper.getGuid(section.getId()));
        contentSrv.deleteItems(sectionIds);
        psContentEvent=new PSContentEvent(section.getId(),section.getId().substring(section.getId().lastIndexOf("-")+1,section.getId().length()),section.getFolderPath(), PSContentEvent.ContentEventActions.delete, PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
        psAuditLogService.logContentEvent(psContentEvent);
    }
    
    /**
     * Groups the child sections by type. Recursively calls the function to load the children and group them
     * @param parentSection assumed not <code>null</code>.
     * @param externalLinkSections list of external link guids, assumed not <code>null</code>
     * @param sectionLinks map of section links, first argument is guid of the section and second argument is parent guid, assumed not <code>null</code>
     * @param sections list of section guids, assumed not <code>null</code>
     */
    private void groupChildSectionsByType(PSSiteSection parentSection, List<String> externalLinkSections, Map<String, String> sectionLinks, List<String> sections)
    {
        List<PSSiteSection> childSections = loadChildSections(parentSection);
        if(childSections.size()<1)
            return;
        for (PSSiteSection section : childSections) {
            if(section.getSectionType().equals(PSSectionTypeEnum.externallink))
                externalLinkSections.add(section.getId());
            else if(section.getSectionType().equals(PSSectionTypeEnum.sectionlink))
                sectionLinks.put(section.getId(), parentSection.getId());
            else
                sections.add(section.getId());
            groupChildSectionsByType(section, externalLinkSections, sectionLinks, sections);
        }
    }
    
    public PSSiteSection find(String id)
    {
        throw new UnsupportedOperationException("find(String) is not supported.");
    }

    public PSSiteSection save(PSSiteSection section)
    {
        throw new UnsupportedOperationException("save(PSSiteSection) is not supported.");
    }

    public List<PSSiteSection> findAll() throws DataServiceLoadException,
            DataServiceNotFoundException
    {
        throw new UnsupportedOperationException("findAll() is not supported.");
    }

    /**
     * Renames the specified landing page to original(n), where n is > 0 such
     * that the name is unique within the section folder. A landing page named
     * "index" will become "index(1), index(2), etc." until a unique name is
     * found.
     * 
     * @param landingPage assumed not <code>null</code>.
     * 
     * @return the new name of the landing page, never blank.
     */
    private String renameLandingPage(PSPage landingPage) throws PSDataServiceException {
        String landingPageName = landingPage.getName();
        String sectionFolderPath = landingPage.getFolderPath();

        // calculate the new name, from "foo" to "foo (#)" or from "foo.htm" to
        // "foo (#).htm"
        int i = 1;
        int lastDot = landingPageName.lastIndexOf('.');
        String name = (lastDot == -1) ? landingPageName : landingPageName.substring(0, lastDot);
        String suffix = (lastDot == -1) ? "" : landingPageName.substring(lastDot);

        String newLandingPageName;
        String path;
        do
        {
            newLandingPageName = name + "-" + i++ + suffix;
            path = folderHelper.concatPath(sectionFolderPath, newLandingPageName);
        }
        while (contentSrv.getIdByPath(path) != null);

        // update the page
        landingPage.setName(newLandingPageName);
        pageDao.save(landingPage);

        return newLandingPageName;
    }

    /**
     * Creates and saves a template based on the specified blog name and source template.  The name of the template will
     * use the following convention: {blog name} - {source template name} - (n).  The increment will be added if
     * necessary to ensure a unique name under the specified site.  It will start at 2.
     * 
     * @param name of the blog.
     * @param srcId source template.
     * @param siteId destination site.
     * 
     * @return id of the new template.
     */
    private String createBlogTemplate(String name, String srcId, String siteId) throws PSDataServiceException {
       String tempId = null;
       
       PSTemplateSummary tempSrc = templateSrv.find(srcId);
       if (tempSrc != null)
       {
          String templateName = name.replaceAll("[\\\\\\\\|/<>?\":*#;% ]", "");
          String tempBaseName = templateName + "-" + tempSrc.getName();
          String tempName = tempBaseName;

          boolean tempExists = false;
          int i = 2;

          List<PSTemplateSummary> siteTemps = siteTemplateSrv.findTemplatesBySite(siteId);

          while (!tempExists)
          {
             for (PSTemplateSummary siteTempSum : siteTemps)
             {
                if (siteTempSum.getName().equals(tempName))
                {
                   tempExists = true;
                   break;
                }
             }

             if (tempExists)
             {
                tempName = tempBaseName + "-" + i++;
                tempExists = false;
             }
             else
             {
                break;
             }
          }

          tempId = templateSrv.createTemplate(tempName, srcId, siteId).getId();
       }
       else
       {
          log.warn("Could not find template for id: " + srcId);
       }
       
       return tempId;
    }
    
    private List<IPSNode> getPostPages(String path, String templateId)
    {
        Map<String, String> whereFields = new HashMap<>();
        whereFields.put("templateid", templateId);
        List<IPSNode> nodes = jcrNodeFinder.find(path, whereFields);
        return nodes;
    }
    
   
    
    /**
     * Validating the request of updating a site section.
     * 
     * @author YuBingChen
     */
    public class PSUpdateSectionValidator extends PSAbstractBeanValidator<PSSiteSectionProperties>
    {
    	 /***
         * If a missing nav landing page is detected - see if the default exists and add it back if it does.
         * @param navonId
         * @return
         */
        private PSLegacyGuid fixMissingLandingPage(IPSGuid navonId){
        	//TODO: Implement ME
        	return null;
        }
        
        @Override
        protected void doValidation(PSSiteSectionProperties req, PSBeanValidationException e) throws PSValidationException {
            IPSGuid sectionFolderId = folderHelper.getParentFolderId(idMapper.getGuid(req.getId()));
            String newFolderName = req.getFolderName();
            List<PSItemSummary> sums = contentSrv.findItems(Collections.singletonList(sectionFolderId), false);
            String oldFolderName = sums.get(0).getName();

            // check to see if landing page is checked out to another user
            IPSGuid navonId = idMapper.getGuid(req.getId());
            PSLegacyGuid landingPageId = (PSLegacyGuid)navSrv.getLandingPageFromNavnode(navonId);
            
            if(landingPageId == null){
            	log.warn("No Landing Page detected for Section {} attempting to auto-detect Landing Page.",  newFolderName );
            	landingPageId = fixMissingLandingPage(navonId);
            
            }
            //If we weren't able to auto detect / repair the landing page log an error but let the save proceed.
            if(landingPageId == null){
            	log.error("Unable to locate the Landing Page for section " + req.getId() + req.getFolderName());
            }else{
            
	            PSComponentSummary summary = PSWebserviceUtils.getItemSummary(landingPageId.getContentId());
	            boolean isCheckedOut = !summary.getCheckoutUserName().isEmpty(); 
	            if (isCheckedOut && !PSWebserviceUtils.isItemCheckedOutToUser(summary))
	            {
	                e.reject("updateSiteSection.checkedOutOtherUser", new Object[]
	                {oldFolderName, summary.getCheckoutUserName()}, "Cannot modify the section \"{0}\" "
	                        + "because its landing page is checked out by the user \"{1}\".");
	                return;
	            }
	            
	            // see if renaming the section
	            if (newFolderName.equalsIgnoreCase(oldFolderName))
	            {
	                // no change to folder name, so there is no need to validate
	                return;
	            }
	
	            // get the path of the section's parent folder
	            String[] itemPaths = contentSrv.findItemPaths(folderHelper.getParentFolderId(sectionFolderId));
	
	            // see if something exists with same name
	            IPSGuid existingId = contentSrv.getIdByPath(itemPaths[0] + '/' + newFolderName);
	            if (existingId != null)
	            {
	                e.reject("updateSiteSection.nameAlreadyExists", new Object[]
	                {oldFolderName, newFolderName}, "Cannot rename site section \"{0}\" to \"{1}\" "
	                        + "because an object with the same name already exist.");
	            }
            }
        }
    }

    /**
     * The validator used to validate a request of updating a site section.
     */
    private PSAbstractBeanValidator<PSSiteSectionProperties> updateRequestValidator = new PSUpdateSectionValidator();

    /**
     * Validating the request of moving a site section.
     * 
     * @author YuBingChen
     */
    public class PSMoveSectionValidator extends PSAbstractBeanValidator<PSMoveSiteSection>
    {
        @Override
        protected void doValidation(PSMoveSiteSection req,
                PSBeanValidationException e)
        {
        }
    }

    /**
     * The validator used to validate a request of moving a site section.
     */
    private PSAbstractBeanValidator<PSMoveSiteSection> moveRequestValidator = new PSMoveSectionValidator();

    /**
     * Validating the request of creating a site section.
     * 
     * @author YuBingChen
     */
    public class PSCreateSectionValidator extends PSAbstractBeanValidator<PSCreateSiteSection>
    {
        @Override
        protected void doValidation(PSCreateSiteSection req, PSBeanValidationException e) throws PSValidationException {
            String sectionName = req.getPageUrlIdentifier();
            PSSectionTypeEnum sectionType = req.getSectionType();
            
            IPSGuid existingId = contentSrv.getIdByPath(req.getFolderPath() + '/' + req.getPageUrlIdentifier());
            if (existingId != null)
            {
                e.reject("createSiteSection.alreadyExists", new Object[]
                {sectionName}, "Cannot create a section with the name \"{0}\" "
                        + "because an object with the same name already exists.");
            }
            PSTemplateSummary template  = null;
            try {
                 template = templateSrv.find(req.getTemplateId());
            } catch (PSDataServiceException psDataServiceException) {
                throw new PSBeanValidationException(e);
            }
            if (template == null)
            {
                String msg;
                
                if (sectionType == PSSectionTypeEnum.blog)
                {
                    msg = "Cannot create a blog with the name \"{0}\" because the selected blog index template has "
                        + " been deleted.  Please select a different blog index template.";
                }
                else
                {
                    msg = "Cannot create a section with the name \"{0}\" because the selected template has been "
                        + "deleted.  Please select a different template.";
                }
                
                e.reject("createSiteSection.missingTemplate", new Object[]{sectionName}, msg);
            }
            
            if (sectionType == PSSectionTypeEnum.blog)
            {
                try {
                    template = templateSrv.find(req.getBlogPostTemplateId());
                }catch (PSDataServiceException psDataServiceException) {
                     throw new PSBeanValidationException(e);
                }
                if (template == null)
                {
                    e.reject("createSiteSection.missingTemplate", new Object[]{sectionName}, "Cannot create a blog "
                            + "with the name \"{0}\" because the selected blog post template has been deleted.  Please "
                            + "select a different blog post template.");
                }
            }
        }
    }

    /**
     * Validating the request of creating an external site section.
     * 
     * @author YuBingChen
     */
    public class PSCreateExternalLinkSectionValidator extends PSAbstractBeanValidator<PSCreateExternalLinkSection>
    {
        @Override
        protected void doValidation(PSCreateExternalLinkSection req, PSBeanValidationException e)
        {
            IPSGuid existingId = contentSrv.getIdByPath(req.getFolderPath());
            if (existingId == null)
            {
                String sectionName = req.getLinkTitle();
                e.reject("createExternalSection.invalidParentSection", new Object[]
                {sectionName}, "Cannot create a section with the name \"{0}\" "
                        + "because an invalid parent section is supplied.");
            }
        }
    }
    
    /**
     * Validating the request of creating an external site section.
     */
    public class PSCreateSectionFromFolderValidator extends PSAbstractBeanValidator<PSCreateSectionFromFolderRequest>
    {
        @Override
        protected void doValidation(PSCreateSectionFromFolderRequest req, PSBeanValidationException e)
        {
            String sourcePath = req.getSourceFolderPath();
            if(sourcePath.endsWith("/"))
            	sourcePath = sourcePath.substring(0, sourcePath.length() - 1);
        	try{
            	IPSItemSummary itemSum = folderHelper.findFolder(sourcePath);
            	if(!itemSum.getCategory().equals(Category.FOLDER)){
                    e.reject("createSectionFromFolder.invalidFolderPath", new Object[]
                            {req.getSourceFolderPath()}, "Cannot create a section from the folder with path \"{0}\" "
                                    + "as the path does not correspond to a folder.");
            	}
            }
            catch(Exception ex){
                e.reject("createSectionFromFolder.invalidFolderPath", new Object[]
                {req.getSourceFolderPath()}, "Cannot create a section from the folder with path \"{0}\" "
                        + "because a folder with that path cannot be found.");
            }
            try{
            	IPSItemSummary itemSum = folderHelper.findFolder(req.getParentFolderPath());
            	if(!itemSum.getCategory().equals(Category.SECTION_FOLDER)){
                    e.reject("createSectionFromFolder.invalidFolderPath", new Object[]
                            {req.getSourceFolderPath(), req.getParentFolderPath()}, "Cannot create a section from the folder with path \"{0}\" "
                                    + "because the parent path \"{1}\" is not a section.");
            	}
            }
            catch(Exception ex){
                e.reject("createSectionFromFolder.invalidFolderPath", new Object[]
                {req.getSourceFolderPath(), req.getParentFolderPath()}, "Cannot create a section from the folder with path \"{0}\" "
                        + "because a section with parent path \"{1}\" cannot be found.");
            }
            IPSGuid pageId = contentSrv.getIdByPath(sourcePath + "/" + req.getPageName());
            if (StringUtils.isNotBlank(req.getPageName()) && pageId == null)
            {
                e.reject("createSectionFromFolder.invalidPageName", new Object[]
                {req.getSourceFolderPath(), req.getPageName()}, "Cannot create a section from the folder with path \"{0}\" "
                        + "and a page named \"{1}\" because a page with that name cannot be found in that folder.");
            }            
        }
    }

    private void updateExternalLinkSection(String title, String navonId, String url, PSSectionTargetEnum target, String cssClassNames)
    {
        notNull(navonId);
        String tgt = target == null || target == PSSectionTargetEnum._self ? "" : target.name();
        Map<String, String> map = new HashMap<>();
        map.put(NAVON_FIELD_DISPLAYTITLE, title);
        map.put(NAVON_FIELD_TYPE, PSSectionTypeEnum.externallink.name());
        map.put(NAVON_FIELD_EXTERNALURL, (url == null ? "" : url));
        map.put(NAVON_FIELD_TARGET, tgt);
        map.put(NAVON_FIELD_CSSCLASSNAMES, (cssClassNames == null ? "" : cssClassNames));
        IPSGuid nodeId = idMapper.getGuid(navonId);
        navSrv.setNavonProperties(nodeId, map);
    }

    /**
     * The validator used to validate a request of creating a site section.
     */
    private PSAbstractBeanValidator<PSCreateSiteSection> createRequestValidator = new PSCreateSectionValidator();

    /**
     * The validator used to validate a request of creating an external site
     * section.
     */
    private PSAbstractBeanValidator<PSCreateExternalLinkSection> createExternalLinkRequestValidator = new PSCreateExternalLinkSectionValidator();

    /**
     * The logger.
     */
    private static final Logger log = LogManager.getLogger(PSSiteSectionService.class);

    public PSValidationErrors validate(PSSiteSection object)
    {
        return null;
    }

    private static final String EXTERNAL_SECTION_NAME_PREFIX = "percEs-";
    
    private static final String BLOG_POST_TEMPLATE_PROP = "blogPostTemplate";
    
    private static final String BLOG_INDEX_TEMPLATE_PROPS ="blogIndexTemplate";

}
