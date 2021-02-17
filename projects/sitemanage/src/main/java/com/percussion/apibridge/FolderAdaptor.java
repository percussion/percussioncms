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

package com.percussion.apibridge;

import com.percussion.cms.objectstore.PSCloningOptions;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.fastforward.managednav.IPSManagedNavService;
import com.percussion.fastforward.managednav.PSNavException;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.pagemanagement.assembler.IPSRenderAssemblyBridge;
import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.dao.IPSPageDaoHelper;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pathmanagement.data.PSDeleteFolderCriteria;
import com.percussion.pathmanagement.data.PSFolderPermission;
import com.percussion.pathmanagement.data.PSFolderPermission.Access;
import com.percussion.pathmanagement.data.PSFolderPermission.Principal;
import com.percussion.pathmanagement.data.PSFolderPermission.PrincipalType;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.data.PSMoveFolderItem;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.data.PSRenameFolderItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.IPSPathService.PSPathNotFoundServiceException;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.redirect.data.PSCreateRedirectRequest;
import com.percussion.redirect.data.PSRedirectStatus;
import com.percussion.redirect.service.IPSRedirectService;
import com.percussion.rest.LinkRef;
import com.percussion.rest.errors.FolderNotFoundException;
import com.percussion.rest.errors.NotAuthorizedException;
import com.percussion.rest.folders.Folder;
import com.percussion.rest.folders.IFolderAdaptor;
import com.percussion.rest.folders.SectionInfo;
import com.percussion.rest.folders.SectionLinkRef;
import com.percussion.rest.pages.Page;
import com.percussion.server.webservices.PSServerFolderProcessor;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.content.data.PSItemStatus;
import com.percussion.services.guidmgr.data.PSGuid;
import com.percussion.services.guidmgr.data.PSLegacyGuid;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.PSWorkflowServiceLocator;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSParametersValidationException;
import com.percussion.sitemanage.data.PSCreateExternalLinkSection;
import com.percussion.sitemanage.data.PSCreateSectionFromFolderRequest;
import com.percussion.sitemanage.data.PSCreateSiteSection;
import com.percussion.sitemanage.data.PSSiteSection;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTargetEnum;
import com.percussion.sitemanage.data.PSSiteSection.PSSectionTypeEnum;
import com.percussion.sitemanage.data.PSSiteSectionProperties;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.PSSiteManageBean;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.webservices.PSErrorException;
import com.percussion.webservices.content.IPSContentWs;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import javax.ws.rs.NotFoundException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.percussion.webservices.PSWebserviceUtils.getItemSummary;
import static com.percussion.webservices.PSWebserviceUtils.isItemCheckedOutToUser;
import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.apache.commons.lang.Validate.notNull;

@PSSiteManageBean
@Lazy
public class FolderAdaptor implements IFolderAdaptor {

	public static final String ASSETS = "Assets";

	public static final String EXTERNAL_SECTION_NAME_PREFIX = "percEs-";

	public static final String ASSETS_JCRPATH = "//Folders/$System$/Assets";

	@Autowired
	private IPSPathService pathService;

    @Autowired
	private IPSFolderHelper folderHelper;

    @Autowired
	private IPSSiteSectionService sectionService;

    @Autowired
	private IPSPageService pageService;

    @Autowired
	private IPSTemplateService templateService;

	/**
	 * The ID map service, initialized by constructor.
	 */
	private IPSIdMapper idMapper;

	/**
	 * The navigation service, initialized by constructor.
	 */
    @Autowired
	private IPSManagedNavService navSrv;

    @Autowired
	private IPSPageDaoHelper pageDaoHelper;

    @Autowired
	private IPSPageDao pageDao;

    @Autowired
	private IPSUserService userService;

    @Autowired
    private IPSContentWs contentService;

    @Autowired
    private IPSRedirectService redirectService;

    @Autowired
    private IPSSiteDataService siteDataService;

    @Autowired
    private IPSRecentService recentService;

    @Autowired
    private IPSRenderAssemblyBridge asmBridge;

    @Autowired
    private IPSItemService itemService;

	/**
	 * Logger for this service.
	 */
	public static Log log = LogFactory.getLog(FolderAdaptor.class);


	@Autowired
	public FolderAdaptor(IPSPathService pathService, IPSFolderHelper folderHelper,
						 @Qualifier("siteSectionService") IPSSiteSectionService sectionService, IPSManagedNavService navSrv, IPSIdMapper idMapper,
						 IPSPageService pageService, @Qualifier("sys_templateService") IPSTemplateService templateService, IPSPageDaoHelper pageDaoHelper,
						 IPSPageDao pageDao, IPSContentWs contentService, @Qualifier("renderAssemblyBridge") IPSRenderAssemblyBridge asmBridge,
						 IPSUserService userService, IPSRedirectService redirectService, IPSSiteDataService siteDataService, IPSRecentService recentService)
	{
		this.pathService = pathService;
		this.folderHelper = folderHelper;
		this.sectionService = sectionService;
		this.navSrv = navSrv;
		this.idMapper = idMapper;
		this.pageService = pageService;
		this.templateService = templateService;
		this.pageDaoHelper = pageDaoHelper;
		this.pageDao = pageDao;
		this.contentService = contentService;
		this.asmBridge = asmBridge;
		this.userService = userService;
		this.redirectService = redirectService;
		this.siteDataService = siteDataService;
		this.recentService = recentService;

	}

	@Override
	public Folder getFolder(URI baseUri, String site, String path, String folderName) {
		checkAPIPermission();

		return getFolder(baseUri, null, site, path, folderName);
	}

	private Folder getFolder(URI baseUri, String folderGuid, String site, String path, String folderName) {
		PSSectionTypeEnum sectionType = PSSectionTypeEnum.section;
		PSPathItem pathItem = null;
		String fullPath = null;
		String pathUtilsPath = null;
		UrlParts folderSections = null;

		if (StringUtils.isNotEmpty(folderGuid) && site == null && path == null && folderName == null) {
			try {
				PSLocator loc = idMapper.getLocator(folderGuid);
				loc.setRevision(-1);
				pathItem = folderHelper.findItemById(idMapper.getString(loc));
			} catch (PSParametersValidationException e) {
				throw new FolderNotFoundException();
			} catch (PSPathNotFoundServiceException e) {
				throw new FolderNotFoundException();
			}

			pathUtilsPath = pathItem.getFolderPath();
			if (pathUtilsPath == null && CollectionUtils.isNotEmpty(pathItem.getFolderPaths())) {
				fullPath = pathItem.getFolderPaths().get(0) + "/" + pathItem.getName();
				pathUtilsPath = StringUtils.substringAfter(fullPath, "/");
			} else {
				fullPath = "/" + pathUtilsPath;
			}

			folderSections = new UrlParts(fullPath);
			site = folderSections.getSite();
			path = folderSections.getPath();
			folderName = folderSections.getName();
			if (folderName.startsWith(EXTERNAL_SECTION_NAME_PREFIX)) {
				sectionType = PSSectionTypeEnum.externallink;
				folderName = StringUtils.substringAfter(folderName, EXTERNAL_SECTION_NAME_PREFIX);
			}
		} else {

			fullPath = generateFullSitePath(site, path, folderName);

			//Handle sites pointed at folders that don't match site name
			fullPath = PSPathUtils.fixSiteFolderPath(siteDataService,fullPath);

			pathUtilsPath = StringUtils.substringAfter(fullPath, "/");
			folderSections = new UrlParts(site, path, folderName);

			try {

				pathItem = pathService.find(pathUtilsPath);

			} catch (PSPathNotFoundServiceException e) {
				// Check if this is an external link
				String extLinkPath = generateFullSitePath(site, path, EXTERNAL_SECTION_NAME_PREFIX + folderName);

				pathUtilsPath = StringUtils.substringAfter(fullPath, "/");
				folderSections = new UrlParts(site, path, folderName);

			}
		}

		Folder folder = new Folder();

		folder.setSiteName(site);
		folder.setPath(path);
		folder.setName(StringUtils.remove(folderName, EXTERNAL_SECTION_NAME_PREFIX));

		// Get Folder Guid
		IPSItemSummary folderSummary = null;
		try {
			if(folderGuid == null)
				folderSummary = folderHelper.findFolder(pathItem.getFolderPath());
			else
				folderSummary = folderHelper.findItemById(folderGuid);

		} catch (Exception e) {
			throw new FolderNotFoundException();
		}

		boolean isSiteRoot = false;
		if (StringUtils.isEmpty(path) && StringUtils.isEmpty(folderName)) {
			isSiteRoot = true;
		}
		folder.setId(folderSummary.getId());

		// Folder properties for workflow
		PSFolderProperties folderProperties = folderHelper.findFolderProperties(folderSummary.getId());

		int wfid = folderProperties.getWorkflowId();
		if (wfid > 0) {
			PSWorkflow wf = loadWorkflow(wfid);
			// Invalid Workflow
			folder.setWorkflow(wf.getName());
		} else {
			folder.setWorkflow("[default]");
		}

		// Security
		extractSecurityInfo(folder, folderProperties);

		folder.setCommunityId(folderProperties.getCommunityId());
        folder.setCommunityName(folderProperties.getCommunityName());
        folder.setLocale(folderProperties.getLocale());
        folder.setDefaultDisplayFormatName(folderProperties.getDisplayFormatName());


        // SubSections
		if (!site.equals("Assets"))
			extractSectionInfo(baseUri, fullPath, folder, sectionType);

		// Sub Pages and Folders
		if (sectionType.equals(PSSectionTypeEnum.section)) {
			extractSubPagesFolders(baseUri, pathItem, pathUtilsPath, folder);
		}

		if (StringUtils.startsWith(folder.getName(), EXTERNAL_SECTION_NAME_PREFIX))
			folder.setName(StringUtils.substringAfter(folder.getName(), EXTERNAL_SECTION_NAME_PREFIX));

		folder.setRecentUsers(new ArrayList<>());
		
		return folder;
	}

	private void checkAPIPermission() {
		if (!userService.isAdminUser(userService.getCurrentUser().getName()))
			throw new NotAuthorizedException();
	}

	private void extractSubPagesFolders(URI baseUri, PSPathItem pathItem, String pathUtilsPath, Folder folder) {
		List<LinkRef> folderChildren = new ArrayList<>();
		folder.setSubfolders(folderChildren);
		List<LinkRef> folderPages = new ArrayList<>();
		List<LinkRef> folderAssets = new ArrayList<>();

		List<String> existingFolders = new ArrayList<>();
		if (folder.getSubsections() != null) {
			for (SectionLinkRef section : folder.getSubsections()) {
				existingFolders.add(section.getName());
			}
		}
		folder.setPages(folderPages);
		folder.setAssets(folderAssets);

		if (!pathItem.isLeaf()) {
			List<PSPathItem> children = pathService.findChildren(pathUtilsPath);

			for (PSPathItem child : children) {
				if (child.isFolder()) {
					String name = child.getName();
					// We do not include sections in list of subfolders
					if (!existingFolders.contains(name)) {
						// Generate LinkRef from folder path.
						LinkRef subfolder = new LinkRef();
						subfolder.setName(name);
						UrlParts parts = new UrlParts(pathUtilsPath + "/" + name);
						URI folderUri = Folder.getFolderURI(baseUri, parts.getSite(), parts.getPath(), parts.getName());

						subfolder.setHref(folderUri.toASCIIString());
						folderChildren.add(subfolder);
					}
				} else if (child.isPage()) {
					String name = child.getName();
					// Generate LinkRef from folder path.
					LinkRef page = new LinkRef();
					page.setName(name);
					UrlParts parts = new UrlParts(pathUtilsPath);

					URI pageUri = Page.getPageUri(baseUri, parts.getSite(), parts.getPath(), parts.getName());

					page.setHref(pageUri.toASCIIString());
					folderPages.add(page);
				} else if (!child.getType().equalsIgnoreCase("NavOn")) {
					String name = child.getName();
					// Generate LinkRef from folder path.
					LinkRef asset = new LinkRef();
					asset.setName(name);
					UrlParts parts = new UrlParts(pathUtilsPath);

					URI assetUri = Page.getPageUri(baseUri, parts.getSite(), parts.getPath(), parts.getName());

					asset.setHref(assetUri.toASCIIString());
					folderAssets.add(asset);
				}
			}
		}
	}

	private void extractSecurityInfo(Folder folder, PSFolderProperties folderProperties) {
		PSFolderPermission permissions = folderProperties.getPermission();
		Access accessLevel = permissions.getAccessLevel();
		folder.setAccessLevel(accessLevel.toString());

		if (accessLevel.equals(Access.READ)) {
			List<String> editUsers = new ArrayList<>();
			folder.setEditUsers(editUsers);
			if (permissions.getWritePrincipals() != null) {
				for (Principal principal : permissions.getWritePrincipals()) {
					if (!principal.getType().equals(PrincipalType.USER))
						throw new RuntimeException("Currently only support User type");

					editUsers.add(principal.getName());

				}
			}
		}
	}

	private void extractSectionInfo(URI baseUri, String fullPath, Folder folder, PSSectionTypeEnum sectionType) {
		PSComponentSummary navSummary = navSrv.findNavSummary(fullPath);
		if (navSummary != null) {
			PSLegacyGuid id = new PSLegacyGuid(navSummary.getCurrentLocator());
			// normalize id, remove revision
			PSLocator loc = idMapper.getLocator(id);
			loc.setRevision(-1);
			PSSiteSection section = sectionService.load(idMapper.getString(loc));

			SectionInfo sectionInfo = new SectionInfo();
			folder.setSectionInfo(sectionInfo);
			sectionInfo.setNavClass(section.getCssClassNames());
			sectionInfo.setTargetWindow(section.getTarget().name());
			sectionInfo.setDisplayTitle(section.getTitle());

			if (sectionType == PSSectionTypeEnum.section) {
				List<SectionLinkRef> sectionChildren = new ArrayList<>();
				folder.setSubsections(sectionChildren);
				List<PSSiteSection> childSections = sectionService.loadChildSections(section);

				if (!childSections.isEmpty()) {

					for (PSSiteSection subSection : childSections) {

						SectionLinkRef sectionLink = new SectionLinkRef();
						sectionChildren.add(sectionLink);
						sectionLink.setType(subSection.getSectionType().name());

						sectionLink.setName(
								StringUtils.remove(StringUtils.substringAfterLast(subSection.getFolderPath(), "/"),
										EXTERNAL_SECTION_NAME_PREFIX));

						String subSectionfolder = subSection.getFolderPath();
						UrlParts parts = new UrlParts(subSectionfolder);
						URI folderUri = Folder.getFolderURI(baseUri, parts.getSite(), parts.getPath(), parts.getName());

						sectionLink
								.setHref(StringUtils.remove(folderUri.toASCIIString(), EXTERNAL_SECTION_NAME_PREFIX));

					}
				}

				IPSGuid landingPageId = navSrv.getLandingPageFromNavnode(id);
				if (landingPageId != null) {
					PSPage landingPage = pageService.find(idMapper.getString(landingPageId));
					String landingTemplateId = landingPage.getTemplateId();
					PSTemplateSummary template = templateService.find(landingTemplateId);
					String templateName = template.getName();

					sectionInfo.setTemplateName(templateName);
					String landingPagePath = landingPage.getFolderPath() + "/" + landingPage.getName();
					LinkRef linkRef = new LinkRef();
					linkRef.setName(landingPage.getName());

					UrlParts parts = new UrlParts(landingPagePath);

					URI pageUri = Page.getPageUri(baseUri, parts.getSite(), parts.getPath(), parts.getName());
					linkRef.setHref(pageUri.toASCIIString());
					sectionInfo.setLandingPage(linkRef);
				}
			} else if (sectionType == PSSectionTypeEnum.externallink) {
				sectionInfo.setType(sectionType.name());
				sectionInfo.setExternalLinkUrl(section.getExternalLinkUrl());
			}

		}
	}

	private PSWorkflow loadWorkflow(int workflowId) {
		IPSGuid id = new PSGuid(PSTypeEnum.WORKFLOW, workflowId);
		IPSWorkflowService srv = PSWorkflowServiceLocator.getWorkflowService();
		return srv.loadWorkflow(id);
	}

	private int workflowIdByName(String wfName) {
		IPSWorkflowService srv = PSWorkflowServiceLocator.getWorkflowService();
		List<PSObjectSummary> summary = srv.findWorkflowSummariesByName(wfName);
		return (summary == null) ? -1 : (int) (summary.get(0).getId());
	}

	@Override
	public Folder updateFolder(URI baseUri, Folder folder) {
		checkAPIPermission();

		Folder existingFolder = null;
		boolean newFolder = false;
		boolean byId = StringUtils.isNotEmpty(folder.getId()) && folder.getSiteName() == null
				&& folder.getPath() == null && folder.getName() == null;
		try {
			if (byId) {

				existingFolder = getFolder(baseUri, folder.getId());
			} else {
				existingFolder = getFolder(baseUri, folder.getSiteName(), folder.getPath(), folder.getName());
			}
		} catch (FolderNotFoundException e) {
			newFolder = true;
			if (StringUtils.isEmpty(folder.getPath()) && StringUtils.isEmpty(folder.getName()))
				throw new RuntimeException(
						"Trying to create site root folder.  Create site first : " + folder.getSiteName());
		}

		if (!byId && existingFolder == null && folder.getId() != null) {
			// If id is passed into update and it looks as if the item does not
			// already exist, we are probably trying to update a purged item, or
			// the id is from a different item.
			throw new FolderNotFoundException();
		}

		if (existingFolder != null && folder.getId() != null && existingFolder.getId() != null
				&& !existingFolder.getId().equals(folder.getId()))
			throw new RuntimeException("Id " + folder.getId()
					+ " passed into update but Id does not match item referenced by site and path "
					+ existingFolder.getId());

		if (newFolder) {
			String folderId = createNewFolder(baseUri, folder);
			folder = getFolder(baseUri, folderId);
			
			
		} else {
			folder = modifyExistingFolder(baseUri, existingFolder, folder);
		}
		return folder;
	}

	private Folder modifyExistingFolder(URI baseUri, Folder existingFolder, Folder folder) {

		String realName = folder.getName();
		if (existingFolder.getSectionInfo() != null && existingFolder.getSectionInfo().getType() != null
				&& existingFolder.getSectionInfo().getType().equalsIgnoreCase(PSSectionTypeEnum.externallink.name()))
			realName = EXTERNAL_SECTION_NAME_PREFIX + realName;

		PSPathItem pathItem = null;
		String fullPath = null;
		String pathUtilsPath = null;
		UrlParts folderSections = null;
		
		try {
		PSLocator loc = idMapper.getLocator(existingFolder.getId());
		loc.setRevision(-1);
		pathItem = folderHelper.findItemById(idMapper.getString(loc));
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		}
		pathUtilsPath = pathItem.getFolderPath();
		if (pathUtilsPath == null && CollectionUtils.isNotEmpty(pathItem.getFolderPaths())) {
			fullPath = pathItem.getFolderPaths().get(0) + "/" + pathItem.getName();
			pathUtilsPath = StringUtils.substringAfter(fullPath, "/");
		} else {
			fullPath = "/" + pathUtilsPath;
		}

		folderSections = new UrlParts(fullPath);

		if(folder.getRecentUsers()!=null && !folder.getRecentUsers().isEmpty()){
			updateRecentUsers(fullPath,folder.getRecentUsers());
		}
		
		updateFolderProperties(folder, pathItem.getId(), existingFolder);

		// Skip the section stuff for Asset folders.
		if (!folder.getSiteName().equals("Assets")) {
			if (folder.getSectionInfo() != null && existingFolder.getSectionInfo() == null) {
				convertFolderToSection(folderSections.getUrl(), folder.getName(), folder.getSiteName(), folder.getSectionInfo());
			}

			updateLandingPage(folder, existingFolder, folderSections.getUrl());

			updateSectionInfo(folder, folderSections.getUrl(), existingFolder);

			reorderSiteSection(baseUri, existingFolder.getId(), folder, existingFolder);
		}
		
		// PSSiteSectionProperties request = new PSSiteSectionProperties();
		// create patch item so do not need to get everything again.
		return getFolder(baseUri, existingFolder.getId());
	}

	/***
	 * Adds this folder to the recent list for the list of user names.
	 * @param folderPath
	 * @param userNames
	 */
	private void updateRecentUsers(String folderPath, List<String> userNames){
	
		if(folderPath.startsWith("//"))
			folderPath = folderPath.substring(1);
		folderPath = folderPath.replace("/Folders/$System$","");
		
		for(String u : userNames){
			try{
				if(folderPath.startsWith("/Assets/"))
					recentService.addRecentAssetFolderByUser(u, folderPath);
				else if(folderPath.startsWith("/Sites/")){
					recentService.addRecentSiteFolderByUser(u,folderPath);
				}else{
						recentService.addRecentSiteFolderByUser(u, "/Sites/" + folderPath);					
				}
			}catch(Exception e){
				log.warn("Error adding " + "/Sites/" + folderPath + " to the Recent list for user" + u );
			}
		}
		
	}
	
	
	private void updateLandingPage(Folder folder, Folder existingFolder, String folderPath) {
		SectionInfo reqSectionInfo = folder.getSectionInfo();
		SectionInfo currSectionInfo = existingFolder.getSectionInfo();

		if (reqSectionInfo != null && currSectionInfo != null && reqSectionInfo.getLandingPage() != null
				&& reqSectionInfo.getLandingPage().getName() != null) {
			String reqName = reqSectionInfo.getLandingPage().getName();
			String existingName = null;
			if (existingFolder.getSectionInfo().getLandingPage() != null)
				existingName = existingFolder.getSectionInfo().getLandingPage().getName();

			if (!reqName.equals(existingName)) {
				// get child folders
				boolean foundPage = false;
				for (LinkRef page : existingFolder.getPages()) {
					if (page.getName().equals(reqName)) {
						foundPage = true;
						break;
					}
				}

				PSComponentSummary navSummary = navSrv.findNavSummary(folderPath);
				PSLegacyGuid sectionId = new PSLegacyGuid(navSummary.getCurrentLocator());
				PSItemStatus navonStatus = prepareForEdit(sectionId, false);

				if (foundPage) {

					PSPathItem pathItem = pathService.find(StringUtils.substring(folderPath + "/" + reqName, 1));
					navSrv.addLandingPageToNavnode(idMapper.getGuid(pathItem.getId()), sectionId,
							asmBridge.getDispatchTemplate());

				} else {

					IPSGuid landingPageId = navSrv.getLandingPageFromNavnode(sectionId);
					PSItemStatus itemStatus = prepareForEdit(landingPageId, true);
					PSPage landingPage = pageDao.find(idMapper.getString(landingPageId));
					landingPage.setName(reqName);
					pageService.save(landingPage);
					releaseFromEdit(itemStatus);
				}
				releaseFromEdit(navonStatus);

			}

		}
	}

	private void updateSectionInfo(Folder folder, String folderUrl, Folder existingFolder) {
		PSComponentSummary navSummary = navSrv.findNavSummary(folderUrl);
		if (navSummary != null) {
			PSLegacyGuid id = new PSLegacyGuid(navSummary.getCurrentLocator());
			PSLocator loc = new PSLocator(id.getUUID(), -1);
			String normId = idMapper.getString(loc);

			PSSiteSection currentSection = sectionService.load(idMapper.getString(id));

			PSFolderProperties folderProps = folderHelper.findFolderProperties(existingFolder.getId());

			if (currentSection.getSectionType() == PSSectionTypeEnum.section) {
				PSSiteSectionProperties req = new PSSiteSectionProperties();

				req.setId(normId);
				req.setAllowAccessTo(currentSection.getAllowAccessTo());
				req.setCssClassNames(currentSection.getCssClassNames());
				req.setFolderName(folderProps.getName());
				req.setFolderPermission(folderProps.getPermission());
				req.setRequiresLogin(currentSection.isRequiresLogin());
				req.setTarget(currentSection.getTarget());
				req.setTitle(currentSection.getTitle());

				SectionInfo toSection = folder.getSectionInfo();
				if (toSection != null) {
					if (toSection.getDisplayTitle() != null)
						req.setTitle(toSection.getDisplayTitle());

					if (toSection.getNavClass() != null)
						req.setCssClassNames(toSection.getNavClass());

					if (toSection.getTargetWindow() != null) {
						req.setTarget(PSSectionTargetEnum.valueOf(toSection.getTargetWindow()));
					}
				}
				// update landing page
				// no update template

				sectionService.update(req);
			} else if (currentSection.getSectionType() == PSSectionTypeEnum.externallink) {
				PSCreateExternalLinkSection req = new PSCreateExternalLinkSection();
				req.setCssClassNames(currentSection.getCssClassNames());
				req.setExternalUrl(currentSection.getExternalLinkUrl());
				req.setFolderPath(currentSection.getFolderPath());
				req.setLinkTitle(currentSection.getTitle());
				req.setSectionType(PSSectionTypeEnum.externallink);
				PSSectionTargetEnum target = (currentSection.getTarget() == null) ? PSSectionTargetEnum._self
						: currentSection.getTarget();
				req.setTarget(target);

				SectionInfo toSection = folder.getSectionInfo();
				if (toSection != null) {
					if (toSection.getDisplayTitle() != null)
						req.setLinkTitle(toSection.getDisplayTitle());

					if (toSection.getNavClass() != null)
						req.setCssClassNames(toSection.getNavClass());

					if (toSection.getTargetWindow() != null) {
						req.setTarget(PSSectionTargetEnum.valueOf(toSection.getTargetWindow()));
					}

					if (toSection.getExternalLinkUrl() != null) {
						req.setExternalUrl(toSection.getExternalLinkUrl());
					}
				}

				sectionService.updateExternalLink(normId, req);

			}

		}
	}

	private void convertFolderToSection(String folderPath, String folderName, String siteName,
			SectionInfo sectionInfo) {

		folderPath = folderPath.startsWith("//") ? StringUtils.substring(folderPath, 1) : folderPath;
		String parentFolderPath = StringUtils.substringBeforeLast(folderPath, "/" + folderName);

		String name = "index.html";
		if (sectionInfo.getLandingPage() != null && sectionInfo.getLandingPage().getName() != null)
			name = sectionInfo.getLandingPage().getName();

		// CMS-3210 - NC
		boolean exists = checkIfPageExists(folderPath + "/" + name);

		String templateId = getTemplateIdForName(sectionInfo.getTemplateName(), siteName);

		if (!exists) {
			PSPage page = new PSPage();
			page.setFolderPath("/" + folderPath);

			String title = StringUtils.defaultString(sectionInfo.getDisplayTitle(), folderName);

			page.setName(name);
			page.setTitle(title);
			page.setTemplateId(templateId);
			page.setLinkTitle(title);

			pageDaoHelper.setWorkflowAccordingToParentFolder(page);

			page = pageDao.save(page);
		}

		PSCreateSectionFromFolderRequest req = new PSCreateSectionFromFolderRequest();

		// Check if parent of this folder is a section.
		req.setPageName(name);
		req.setParentFolderPath("/" + parentFolderPath);
		req.setSourceFolderPath("/" + folderPath);

		sectionService.createSectionFromFolder(req);

	}

	private String getTemplateIdForName(String templateName, String siteName) {
		IPSGuid template = null;
		if (templateName == null) {
			throw new RuntimeException("templateName is required");
		}
		try {
			template = templateService.findUserTemplateIdByName(templateName, siteName);
		} catch (PSParametersValidationException e) {
			throw new RuntimeException("Cannot find template " + templateName);
		}
		return idMapper.getString(template);
	}

	private boolean checkIfPageExists(String folderPath) {
		PSPathItem pathItem = null;
		try {
			pathItem = pathService.find(folderPath);
			if (pathItem != null)
				return true;

		} catch (PSPathNotFoundServiceException e) {
			// Not an error we want to create if not found.
		}
		return false;
	}

	private void reorderSiteSection(URI baseUri, String id, Folder folder, Folder existingFolder) {
		List<String> existingSubSections = new ArrayList<>();
		List<String> requestedSubSections = new ArrayList<>();

		if (folder.getSubsections() != null) {
			if (folder.getSectionInfo() == null && existingFolder.getSectionInfo() == null)
				throw new RuntimeException("non-section folders cannot have subsections");
			String nameCheck = null;
			for (SectionLinkRef subsection : folder.getSubsections()) {
				if (subsection.getType().equals(PSSectionTypeEnum.sectionlink.name()))
					nameCheck = subsection.getName() + "-" + subsection.getHref();
				else
					nameCheck = subsection.getName();

				if (requestedSubSections.contains(nameCheck))
					throw new RuntimeException(
							"Cannot specify more than one subsection with the same name, other than sectionlinks which must have unique name and href");

				requestedSubSections.add(nameCheck);

			}

			if (existingFolder.getSubsections() != null) {

				for (SectionLinkRef subsection : existingFolder.getSubsections()) {
					if (subsection.getType().equals(PSSectionTypeEnum.sectionlink.name()))
						nameCheck = subsection.getName() + "-" + subsection.getHref();
					else
						nameCheck = subsection.getName();

					existingSubSections.add(nameCheck);
				}
			}

			Collection<String> subSectionsToCreate = CollectionUtils.subtract(requestedSubSections,
					existingSubSections);

			for (String createName : subSectionsToCreate) {
				for (SectionLinkRef subsection : folder.getSubsections()) {
					if (subsection.getName().equals(createName)
							|| (subsection.getType().equals(PSSectionTypeEnum.sectionlink.name())
									&& createName.equals(subsection.getName() + "-" + subsection.getHref()))) {
						createSubSection(baseUri, existingFolder, folder, subsection.getName(), subsection.getType(),
								subsection.getHref());
					}
				}
			}

			existingSubSections.addAll(subSectionsToCreate);

			int pos = 0;
			/*
			 * TODO Re-order sections.
			 */
		}
	}

	private String createNewFolder(URI baseUri, Folder folder) {

		UrlParts fullUrl = new UrlParts(folder.getSiteName(), folder.getPath(), folder.getName());
		UrlParts parentUrl = new UrlParts(folder.getSiteName(), folder.getPath(), "");

		PSPathItem newFolder = null;

		// Create a folder with a section.
		if (!folder.getSiteName().equals("Assets") && folder.getSectionInfo() != null) {
			newFolder = createNewSection(folder, parentUrl);
		} else
		// Create a regular folder
		{

			// Will create all ancestor folders that do no already exist.
			// PSPathItem parent =
			// pathService.find(StringUtils.substringAfter(parentUrl.getUrl(),"/"));

			newFolder = pathService.addFolder(
							fullUrl.getUrl());

		}

		if(folder.getRecentUsers()!=null && !folder.getRecentUsers().isEmpty()){
			updateRecentUsers(fullUrl.getUrl(),folder.getRecentUsers());
		}
		updateFolderProperties(folder, newFolder.getId(), null);

		// create subsections
		if (!folder.getSiteName().equals("Assets"))
			createSubSections(baseUri, folder, folder);

		return newFolder.getId();
	}

	private void updateFolderProperties(Folder folder, String folderId, Folder existingFolder) {

		// Folder properties for workflow
		PSFolderProperties folderProperties = folderHelper.findFolderProperties(folderId);

		boolean changedProperties = updatePermissions(folder, folderProperties, existingFolder);
		changedProperties |= updateWorkflow(folder, folderProperties);

		if (changedProperties)
			folderHelper.saveFolderProperties(folderProperties);
	}

	private void createSubSections(URI baseUri, Folder existingFolder, Folder folder) {

		if (folder.getSubsections() != null) {
			if (folder.getSectionInfo() == null)
				throw new RuntimeException("non-section folders cannot have subsections");

			for (SectionLinkRef subSection : folder.getSubsections()) {
				String name = subSection.getName();
				createSubSection(baseUri, existingFolder, folder, name, subSection.getType(), subSection.getHref());
			}

		}
	}

	private void createSubSection(URI baseUri, Folder existingFolder, Folder folder, String name, String type,
			String extUrl) {
		notNull(folder);
		notNull(existingFolder);

		String newSubSection = null;

		// FB: RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE NC 1-17-16
		if (type == null) {
			type = PSSectionTypeEnum.section.name();
		}

		if (!type.equalsIgnoreCase(PSSectionTypeEnum.sectionlink.name())) {
			Folder subFolder = new Folder();
			subFolder.setSiteName(folder.getSiteName());
			subFolder.setPath(folder.getPath() + "/" + folder.getName());
			subFolder.setName(name);

			if (folder.getWorkflow() != null)
				subFolder.setWorkflow(folder.getWorkflow());
			else
				subFolder.setWorkflow(existingFolder.getWorkflow());

			SectionInfo subSectionInfo = new SectionInfo();
			subFolder.setSectionInfo(subSectionInfo);

			subSectionInfo.setType(type);
			if (type.equalsIgnoreCase(PSSectionTypeEnum.externallink.name()))
				subSectionInfo.setExternalLinkUrl(extUrl);
			subSectionInfo.setDisplayTitle(name);
			String templateName = null;
			if (folder.getSectionInfo() != null && folder.getSectionInfo().getTemplateName() != null)
				templateName = folder.getSectionInfo().getTemplateName();
			else if (existingFolder.getSectionInfo() != null
					&& existingFolder.getSectionInfo().getTemplateName() != null)
				templateName = existingFolder.getSectionInfo().getTemplateName();

			subSectionInfo.setTemplateName(templateName);

			newSubSection = createNewFolder(baseUri, subFolder);

		} else {
			// Deal with section links;
			String path = "//Sites/" + StringUtils.substringAfter(extUrl, "by-path/");

			String toGuid = getNavGuidByPath(path);
			if (toGuid == null)
				throw new RuntimeException("Cannot create section link to section that does not yet exist " + path);
			UrlParts fromFolder = new UrlParts(folder.getSiteName(), folder.getPath(), folder.getName());
			String fromGuid = getNavGuidByPath(fromFolder.getUrl());
			sectionService.createSectionLink(toGuid, fromGuid);

		}
	}

	private String getNavGuidByPath(String path) {
		String guid = null;
		PSComponentSummary navSummary = navSrv.findNavSummary(path);
		if (navSummary != null) {
			PSLegacyGuid id = new PSLegacyGuid(navSummary.getCurrentLocator());
			// normalize id, remove revision
			PSLocator loc = idMapper.getLocator(id);
			loc.setRevision(-1);

			guid = idMapper.getString(loc);

		}
		return guid;
	}

	private PSPathItem createNewSection(Folder folder, UrlParts parentUrl) {
		PSPathItem newFolder = null;

		String parentFolderUrl = parentUrl.getUrl();
		PSComponentSummary parentSummary = null;
		try {
			parentSummary = navSrv.findNavSummary(parentFolderUrl);
		} catch (PSNavException e) {
			// Cannot find folder
		}
		if (parentSummary == null)
			throw new RuntimeException("can only create section folders that are child folders of other sections.");
		SectionInfo section = folder.getSectionInfo();
		PSSiteSection newSection = null;
		if (section.getType() != null && section.getType().equalsIgnoreCase(PSSectionTypeEnum.externallink.name())) {
			PSCreateExternalLinkSection req = new PSCreateExternalLinkSection();
			req.setCssClassNames(section.getNavClass());
			req.setExternalUrl(section.getExternalLinkUrl());
			req.setFolderPath(parentFolderUrl);
			req.setLinkTitle(section.getDisplayTitle());
			req.setSectionType(PSSectionTypeEnum.externallink);
			PSSectionTargetEnum target = (section.getTargetWindow() == null) ? PSSectionTargetEnum._self
					: PSSectionTargetEnum.valueOf(section.getTargetWindow());
			req.setTarget(target);
			newSection = sectionService.createExternalLinkSection(req);

		} else if (section.getType() == null || section.getType().equalsIgnoreCase(PSSectionTypeEnum.section.name())) {
			PSCreateSiteSection request = new PSCreateSiteSection();
			request.setCopyTemplates(false);

			request.setFolderPath(parentFolderUrl);

			// get specified landing page name or use index if does not exist.
			if (section.getLandingPage() != null && section.getLandingPage().getName() != null) {
				request.setPageName(section.getLandingPage().getName());
			} else {
				request.setPageName("index.html");
			}
			String displayTitle = section.getDisplayTitle();
			if (displayTitle == null)
				displayTitle = folder.getName();

			request.setPageLinkTitle(displayTitle);
			request.setPageTitle(displayTitle);
			request.setPageUrlIdentifier(folder.getName());
			request.setSectionType(PSSectionTypeEnum.section);
			IPSGuid template = null;

			if (section.getTemplateName() == null) {
				throw new RuntimeException("templateName is required for section");
			}
			try {
				template = templateService.findUserTemplateIdByName(section.getTemplateName(), folder.getSiteName());
			} catch (PSParametersValidationException e) {
				throw new RuntimeException("Cannot find template " + section.getTemplateName());
			}

			request.setTemplateId(idMapper.getString(template));

			newSection = sectionService.create(request);

		}
		if (newSection != null)
			newFolder = pathService.find(StringUtils.substring(newSection.getFolderPath(), 1));
		return newFolder;
	}

	private boolean updateWorkflow(Folder folder, PSFolderProperties folderProperties) {
		String requestWorkflow = folder.getWorkflow();
		if (requestWorkflow != null) {
			int wfid;
			if (requestWorkflow.equalsIgnoreCase("[default]"))
				// Real strange code, wf is removed by passing this.
				wfid = Integer.MIN_VALUE;

			else
				wfid = workflowIdByName(requestWorkflow);

			if (wfid != folderProperties.getWorkflowId()) {
				folderProperties.setWorkflowId(wfid);
				return true;
			}
		}
		return false;

	}

	private boolean updatePermissions(Folder folder, PSFolderProperties folderProperties, Folder existingFolder) {
		boolean changed = false;
		PSFolderPermission permission = folderProperties.getPermission();
		Access accessLevel = permission.getAccessLevel();

		boolean isSection = ((existingFolder != null && existingFolder.getSectionInfo() != null)
				|| folder.getSectionInfo() != null);

		// Need to change to write if we are converting to a section and the
		// original access level was ADMIN

		if (existingFolder != null && isSection && existingFolder.getSectionInfo() == null
				&& existingFolder.getAccessLevel().equals("ADMIN")){
			folder.setAccessLevel("WRITE");
			changed = true;
		}

		// FB: EC_UNRELATED_TYPES NC 1-16-16
		if (folder.getAccessLevel() != null && !folder.getAccessLevel().equals(accessLevel.name())) {
			// default for section is write, does not allow admin. default for
			// non section is admin. Unknown will be set to correct default
			String level = folder.getAccessLevel();
			if (isSection) {
				if (!(level.equals("READ") || level.equals("WRITE")))
					level = "WRITE";
			} else {
				if (!(level.equals("READ") || level.equals("WRITE") || level.equals("ADMIN")))
					level = "ADMIN";
			}
			permission.setAccessLevel(Access.valueOf(level));
			changed = true;
		}

		List<String> editUsers = folder.getEditUsers();
		if (editUsers != null) {
			List<String> serverUsers = new ArrayList<>();
			List<Principal> writePrincipals = permission.getWritePrincipals();

			if (writePrincipals == null)
				writePrincipals = new ArrayList<>();

			if (writePrincipals.size() > 0) {
				for (Principal principal : writePrincipals) {
					if (principal.getType().equals(PrincipalType.USER))
						serverUsers.add(principal.getName());
				}
			}
			if (!CollectionUtils.isEqualCollection(editUsers, serverUsers)) {

				changed = true;
				writePrincipals.clear();
				for (String user : editUsers) {
					Principal p = new Principal();
					p.setName(user);
					p.setType(PrincipalType.USER);
					writePrincipals.add(p);
				}
				permission.setWritePrincipals(writePrincipals);
				
				//Clear any Read principals as CM1 doesn't support them in the UI, remove this if that feature is ever enabled.
				permission.setReadPrincipals(new ArrayList<>());
			}
		}
		folderProperties.setPermission(permission);
		return changed;
	}

	protected String generateFullSitePath(String siteName, String path, String name) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotEmpty(siteName) && !siteName.equals(FolderAdaptor.ASSETS)) {
			sb.append("//Sites/").append(siteName);
		} else {
			sb.append("//Assets");
		}
		if (StringUtils.isNotEmpty(path))
			sb.append("/").append(path);
		if (StringUtils.isNotEmpty(name))
			sb.append("/").append(name);
		return sb.toString();
	}

	private void releaseFromEdit(PSItemStatus itemStatus) {
		contentService.releaseFromEdit(itemStatus, false);
	}

	private PSItemStatus prepareForEdit(IPSGuid id, boolean isPage) {
		try {
			return contentService.prepareForEdit(id);
		} catch (PSErrorException e) {

			String type = isPage ? "page" : "asset";
			PSComponentSummary summary = getItemSummary(((PSLegacyGuid) id).getContentId());
			String msg;
			if (!isEmpty(summary.getCheckoutUserName()) && !isItemCheckedOutToUser(summary)) {
				msg = "The " + type + " '" + summary.getName() + "' is being edited by user '"
						+ summary.getCheckoutUserName() + "', please open it to override.";
			} else {
				msg = "You are not authorized to modify " + type + " \"" + summary.getName()
						+ "\", please open it to override.";
			}

			throw new RuntimeException(msg);
		}
	}

	public IPSPathService getPathService() {
		return pathService;
	}

	public void setPathService(IPSPathService pathService) {
		this.pathService = pathService;
	}

	@Override
	public void deleteFolder(URI baseUri, String siteName, String path, String folderName, boolean includeSubFolders) {
		checkAPIPermission();

		UrlParts urlParts = new UrlParts(siteName, path, folderName);
		String folderUrl = urlParts.getUrl();

		//Fix for sites with mismatched sitefolders
		folderUrl = PSPathUtils.fixSiteFolderPath(siteDataService, folderUrl);

		PSPathItem folderPathItem = null;
		try {
			folderPathItem = pathService.find(folderUrl);
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		}
		boolean hasChildren = false;

        List<PSPathItem> children = pathService.findChildren(StringUtils.substring(folderUrl, 1));

        hasChildren = !CollectionUtils.sizeIsEmpty(children);

		if (hasChildren && !includeSubFolders)
			throw new RuntimeException(
					"Folder includes subfolder, use includeSubFolders=true html parameter to delete all");
		PSDeleteFolderCriteria criteria = new PSDeleteFolderCriteria();
		criteria.setPath(StringUtils.substring(folderUrl, 1));
		pathService.deleteFolder(criteria);
	}

	@Override
	public Folder getFolder(URI baseUri, String id) {
		checkAPIPermission();

		return getFolder(baseUri, id, null, null, null);
	}

	@Override
	public void moveFolderItem(URI baseURI, String itemPath, String targetFolderPath) {
		checkAPIPermission();

		itemPath = PSPathUtils.fixSiteFolderPath(siteDataService, itemPath);
		targetFolderPath = PSPathUtils.fixSiteFolderPath(siteDataService,targetFolderPath);

		PSPathItem folderPathItem = null;
		try {
			folderPathItem = pathService.find(itemPath);
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		}

		PSMoveFolderItem request = new PSMoveFolderItem();
		request.setItemPath(itemPath);
		request.setTargetFolderPath(targetFolderPath);

		getPathService().moveItem(request);

		PSPathItem targetPathItem = null;
		try {
			targetPathItem = pathService.find(targetFolderPath + "/" + folderPathItem.getName());
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		}

		// Generate a redirect from the old path to the new path.
		if (redirectService.status().getStatusCode() == PSRedirectStatus.SERVICE_OK) {
			try {

				PSSiteSummary site = siteDataService.findByPath("/" + targetFolderPath + folderPathItem.getName());
				site.setPubInfo(siteDataService.getS3PubServerInfo(site.getSiteId()));

				PSModuleLicense lic = redirectService.getLicense();

				if (lic != null && site.getPubInfo() != null) {
					PSCreateRedirectRequest req = new PSCreateRedirectRequest();
					req.setCategory(IPSRedirectService.REDIRECT_CATEGORY_AUTOGEN);
					req.setCondition(PSPathUtils.getBaseFolderFromPath(itemPath) + "/" + folderPathItem.getName());
					req.setEnabled(true);
					req.setKey(lic.getKey());
					req.setPermanent(true);
					req.setRedirectTo(
							PSPathUtils.getBaseFolderFromPath(targetFolderPath) + "/" + folderPathItem.getName());
					req.setSite(site.getPubInfo().getBucketName());
					req.setType(IPSRedirectService.REDIRECT_TYPE_DEFAULT);
					redirectService.createRedirect(req);
				}
			} catch (Exception e) {
				log.error("An error occurred generating a Redirect while moving Item: " + itemPath, e);
			}

		}

	}

	@Override
	public void moveFolder(URI baseURI, String folderPath, String targetFolderPath) {
		checkAPIPermission();

		folderPath = PSPathUtils.fixSiteFolderPath(siteDataService, folderPath);
		targetFolderPath = PSPathUtils.fixSiteFolderPath(siteDataService, targetFolderPath);

		PSPathItem folderPathItem = null;
		try {
			folderPathItem = pathService.find(folderPath);
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		}

		PSMoveFolderItem request = new PSMoveFolderItem();
		request.setItemPath(folderPath);
		request.setTargetFolderPath(targetFolderPath);

		getPathService().moveItem(request);

	}

	@Override
	public Folder renameFolder(URI baseURI, String site, String path, String folderName, String newName) {
		checkAPIPermission();

		UrlParts urlParts = new UrlParts(site, path, folderName);
		String folderUrl = urlParts.getUrl();
		PSPathItem folderPathItem = null;
		try {
			folderPathItem = pathService.find(StringUtils.substring(folderUrl, 1));
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		}

		PSRenameFolderItem request = new PSRenameFolderItem();
		request.setName(newName);
		request.setPath(StringUtils.substring(folderUrl, 1));

		PSPathItem ret = pathService.renameFolder(request);

		return getFolder(baseURI, site, path, newName);
	}

	private IPSItemSummary getFolderPathItem(String path){

		try {
			return  folderHelper.findFolder(path);
		} catch (PSParametersValidationException e) {
			throw new FolderNotFoundException();
		} catch (PSPathNotFoundServiceException e) {
			throw new FolderNotFoundException();
		} catch (Exception e) {
			throw new FolderNotFoundException();
		}
	}

    @Override
    public void copyFolderItem(URI baseURI, String itemPath, String targetFolderPath) throws Exception {
        checkAPIPermission();

        String correctedItemPath = PSPathUtils.fixSiteFolderPath(siteDataService,itemPath );
        String correctedTargetPath = PSPathUtils.fixSiteFolderPath(siteDataService,targetFolderPath );

		IPSItemSummary sourceItem = this.folderHelper.findItem(correctedItemPath);

		//If it is a page, treat it special
		if(sourceItem.isPage()) {
			pageService.copy(sourceItem.getId(), targetFolderPath, true);
		}else if(sourceItem.isFolder()){
			throw new Exception("Bad call to copyFolderItem.  copyFolder must be called to copy a folder.");
		}else{
			PSLegacyGuid guid = (PSLegacyGuid)idMapper.getGuid(sourceItem.getId());

			List<IPSGuid> guids = new ArrayList<>();

			guids.add(guid);

			List<String> paths = new ArrayList<>();

			paths.add(correctedTargetPath);

			//Copy it like an item
			List<PSCoreItem> items = contentService.newCopies(guids,
					paths,
					"NewCopy",
					false);

		}
	}

    @Override
    public void copyFolder(URI baseURI, String folderPath, String targetFolderPath) throws Exception {
        checkAPIPermission();

        String correctedTarget = PSPathUtils.fixSiteFolderPath(siteDataService,targetFolderPath);
        String correctedSource = PSPathUtils.fixSiteFolderPath(siteDataService,PSPathUtils.getFolderPath(folderPath));
        String targetFolderName = PSPathUtils.getFolderName(correctedSource);

		IPSItemSummary item = getFolderPathItem(correctedSource);
		PSLocator sourceLoc = idMapper.getLocator(item.getId());

		if(item == null)
		    throw new NotFoundException("Source Folder " + folderPath + " was not found.  Please check the path and try again.");

		IPSGuid guid = idMapper.getGuid(item.getId());
		List<IPSGuid> guids = new ArrayList<>();

		IPSItemSummary targetItem = getFolderPathItem(correctedTarget);

		PSLocator destLoc = null;
        String folderName = null;
        destLoc = idMapper.getLocator(targetItem.getId());

		IPSGuid targetGuid = idMapper.getGuid(targetItem.getId());

        folderName = folderHelper.getUniqueFolderName(correctedTarget,targetFolderName);

		PSServerFolderProcessor folderProc = PSServerFolderProcessor.getInstance();
		String result = folderProc.copyFolder(sourceLoc,destLoc, new PSCloningOptions(
				PSCloningOptions.TYPE_SITE_SUBFOLDER, folderName,
				PSCloningOptions.COPY_ALL_CONTENT, PSCloningOptions.COPYCONTENT_AS_NEW_COPY,
				null
		) );

		if(result != null)
		    throw new Exception("Failed to copy folder");
        else
		    log.info("Copied folder:" + folderPath + " to " + targetFolderPath);

	}

    @Override
    public void deleteFolderItem(URI baseURI, String itemPath) {
        checkAPIPermission();

        PSPathItem ps=null;
        try {
            ps = pathService.find(itemPath);
        }catch(Exception e){
            throw new NotFoundException(itemPath + " Not Found");
        }

        if(ps == null)
            throw new NotFoundException(itemPath + " Not Found");

        ArrayList<IPSGuid> guids = new ArrayList<>();
        guids.add(idMapper.getGuid(ps.getId()));

        this.contentService.deleteItems(guids);

    }

}
