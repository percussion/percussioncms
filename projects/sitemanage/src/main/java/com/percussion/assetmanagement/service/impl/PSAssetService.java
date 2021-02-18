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

import com.percussion.assetmanagement.dao.IPSAssetDao;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest;
import com.percussion.assetmanagement.data.PSAbstractAssetRequest.AssetType;
import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetEditUrlRequest;
import com.percussion.assetmanagement.data.PSAssetEditor;
import com.percussion.assetmanagement.data.PSAssetFolderRelationship;
import com.percussion.assetmanagement.data.PSAssetSummary;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.data.PSBinaryAssetRequest;
import com.percussion.assetmanagement.data.PSContentEditCriteria;
import com.percussion.assetmanagement.data.PSExtractedAssetRequest;
import com.percussion.assetmanagement.data.PSFileAssetReportLine;
import com.percussion.assetmanagement.data.PSHtmlAssetData;
import com.percussion.assetmanagement.data.PSImageAssetReportLine;
import com.percussion.assetmanagement.data.PSInspectedElementsData;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.auditlog.PSActionOutcome;
import com.percussion.auditlog.PSAuditLogService;
import com.percussion.auditlog.PSContentEvent;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSCoreItem;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.PSItemDefinition;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.design.objectstore.PSContentEditor;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.itemmanagement.data.PSAssetSiteImpact;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService.PSItemWorkflowServiceException;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageChangeEvent.PSPageChangeEventType;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.data.PSWidgetContentType;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.data.PSWidgetProperties.PSWidgetProperty;
import com.percussion.pagemanagement.data.PSWidgetSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pathmanagement.data.PSFolderProperties;
import com.percussion.pathmanagement.service.impl.PSAssetPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.catalog.data.PSObjectSummary;
import com.percussion.services.contentmgr.IPSContentPropertyConstants;
import com.percussion.services.guidmgr.PSGuidUtils;
import com.percussion.services.legacy.IPSCmsObjectMgr;
import com.percussion.services.legacy.PSCmsObjectMgrLocator;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.workflow.IPSWorkflowService;
import com.percussion.services.workflow.data.PSAssignmentTypeEnum;
import com.percussion.services.workflow.data.PSState;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.dao.PSHtmlUtils;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.data.PSAbstractPersistantObject;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.data.PSNoContent;
import com.percussion.share.service.IPSDataItemSummaryService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.service.PSAbstractFullDataService;
import com.percussion.share.service.exception.PSBeanValidationException;
import com.percussion.share.service.exception.PSBeanValidationUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSExtractHTMLException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.share.validation.PSAbstractBeanValidator;
import com.percussion.share.validation.PSValidationErrors;
import com.percussion.share.validation.PSValidationErrorsBuilder;
import com.percussion.user.data.PSAccessLevel;
import com.percussion.user.data.PSAccessLevelRequest;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.exceptions.PSORMException;
import com.percussion.utils.guid.IPSGuid;
import com.percussion.utils.tools.PSCopyStream;
import com.percussion.webservices.content.IPSContentDesignWs;
import com.percussion.webservices.content.IPSContentWs;
import com.percussion.workflow.service.IPSSteppedWorkflowMetadata;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import static com.percussion.pathmanagement.service.impl.PSPathUtils.getFinderPath;
import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfNull;
import static com.percussion.share.service.exception.PSParameterValidationUtils.validateParameters;
import static java.util.Arrays.asList;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.apache.commons.lang.StringUtils.startsWith;
import static org.apache.commons.lang.StringUtils.substringAfterLast;
import static org.apache.commons.lang.Validate.isTrue;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 *
 * @author PeterFrontiero
 *
 */
@Component("assetService")
@Lazy
public class PSAssetService extends PSAbstractFullDataService<PSAsset, PSAssetSummary>  implements IPSAssetService
{
    private IPSItemService itemService;
    private PSAuditLogService psAuditLogService=PSAuditLogService.getInstance();
    private PSContentEvent psContentEvent;

	/**
     * Constructs an instance of the class.
     *
     * @param idMapper the id mapper, never <code>null</code>.
     * @param pageService the page service, never <code>null</code>.
     * @param templateService the template service, never <code>null</code>.
     * @param widgetService the widget service, never <code>null</code>.
     * @param contentDs the content design service, never <code>null</code>.
     * @param assetDao the dao for asset item manipulation, never <code>null</code>.
     * @param dataItemSummaryService the service for data item summary manipulation, never <code>null</code>.
     * @param widgetAssetRelationshipService the service for manipulating widget asset relationships,
     * never <code>null</code>.
     * @param itemDefManager used for item def manipulation, never <code>null</code>.
     * @param workflowService
     * @param nameGenerator used for content item name generation, never <code>null</code>.
     * @param folderHelper used for folder manipulation, never <code>null</code>.
     * @param contentWs used for item manipulation, never <code>null</code>.
     * @param assetUploadFolderPathMap used for figuring out the folder path for new assets.
     */
    @Autowired
    public PSAssetService(IPSIdMapper idMapper, IPSPageService pageService,
            @Qualifier("sys_templateService") IPSTemplateService templateService,
            IPSWidgetService widgetService,
            IPSContentDesignWs contentDs,
            IPSAssetDao assetDao,
            IPSDataItemSummaryService dataItemSummaryService,
            IPSWidgetAssetRelationshipService widgetAssetRelationshipService,
            PSItemDefManager itemDefManager,
            IPSWorkflowService workflowService,
            IPSNameGenerator nameGenerator,
            IPSFolderHelper folderHelper,
            IPSContentWs contentWs,
            IPSItemWorkflowService itemWorkflowService,
            PSAssetUploadFolderPathMap assetUploadFolderPathMap,
            IPSUserService userService,
            IPSSteppedWorkflowMetadata steppedWfMetadata,
            IPSItemService itemService)
    {
        super(dataItemSummaryService, assetDao);

        //PXA Why is this even a thing, don't we use DI where we'd get an exception if these couldn't be created?
        notNull(idMapper, "idMapper may not be null.");
        notNull(pageService, "Page service may not be null.");
        notNull(templateService, "Template service may not be null.");
        notNull(widgetService, "Widget service may not be null.");
        notNull(contentDs, "Content Design service may not be null.");
        notNull(widgetAssetRelationshipService, "Widget asset relationship service may not be null.");
        notNull(workflowService, "workflowService may not be null.");
        notNull(nameGenerator, "nameGenerator may not be null.");
        notNull(folderHelper, "folderHelper may not be null.");
        notNull(contentWs, "contentWs may not be null.");
        notNull(itemWorkflowService, "itemWorkflowService may not be null.");
        notNull(userService, "userService may not be null.");
        notNull(steppedWfMetadata, "steppedWfMetadata may not be null.");

        this.idMapper = idMapper;
        this.pageService = pageService;
        this.templateService = templateService;
        this.widgetService = widgetService;
        this.contentDs = contentDs;
        this.widgetAssetRelationshipService = widgetAssetRelationshipService;
        this.assetWidgetRelationshipValidator = new PSAssetWidgetRelationshipValidator();
        this.itemDefManager = itemDefManager;
        this.workflowService = workflowService;
        this.nameGenerator = nameGenerator;
        this.assetDao = assetDao;
        this.folderHelper = folderHelper;
        this.contentWs = contentWs;
        this.itemWorkflowService = itemWorkflowService;
        this.assetUploadFolderPathMap = assetUploadFolderPathMap;
        this.userService = userService;
        this.steppedWfMetadata = steppedWfMetadata;
        this.itemService = itemService;
    }


	@Override
	public List<PSImageAssetReportLine> findNonCompliantImageAssets() throws PSReportFailedToRunException {

		List<PSImageAssetReportLine> ret = new ArrayList<>();
		List<PSAsset> assets = assetDao.findAllNonADACompliantImageAssets();
		List<String> assetIds =  new ArrayList<>();

		if(log.isDebugEnabled())
			log.debug("Collecting Asset Ids...");

		for(PSAsset a : assets){
			assetIds.add(a.getId());
		}

		if(log.isDebugEnabled())
			log.debug("Analyzing Site Impact for Assets...");

		List<PSAssetSiteImpact> impact = itemService.getAssetSiteImpact(assetIds);

		//Now that we have all of our Assets and Site Impacts we can start pumping out lines
		int i = 0;
		for(PSAsset a : assets) {
			try {
				PSAssetSiteImpact si = impact.get(i);
				PSImageAssetReportLine row = new PSImageAssetReportLine();
				String sites = "";
				String pages = "";
				String pagePaths = "";
				String templateNames = "";

				row.setId(Integer.parseInt((String) a.getFields().get(IPSHtmlParameters.SYS_CONTENTID)));
				row.setGuid(a.getId());
				row.setAltText((String) a.getFields().get("alttext"));
				row.setTitle((String) a.getFields().get("displaytitle"));
				row.setResourceLinkTitle((String) a.getFields().get("resource_link_title"));
				row.setBulkImportAction("UPDATE");
				row.setFilename((String) a.getFields().get("filename"));
				row.setName((String) a.getFields().get(IPSHtmlParameters.SYS_TITLE));
				row.setContentCreatedBy((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDBY.replace("rx:", "")));
				row.setContentCreatedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDDATE.replace("rx:", "")));
				row.setContentModifiedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIEDATE.replace("rx:", "")));
				row.setContentLastModifier((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIER.replace("rx:", "")));
				row.setContentPostDate((String) a.getFields().get("sys_contentpostdate"));
				row.setContentStartDate((String) a.getFields().get("sys_contentstartdate"));
				row.setPubDate((String) a.getFields().get("sys_pubdate"));


				PSWorkflow wf = workflowService.loadWorkflow(PSGuidUtils.makeGuid(
						Long.parseLong((String) a.getFields().get(IPSHtmlParameters.SYS_WORKFLOWID)), PSTypeEnum.WORKFLOW));

				PSState state = workflowService.loadWorkflowState(PSGuidUtils.makeGuid(Long.parseLong((String) a.getFields().get(IPSHtmlParameters.SYS_CONTENTSTATEID)), PSTypeEnum.WORKFLOW_STATE), wf.getGUID());


				row.setWorkflowName(wf.getLabel());

				if (state != null) {
					row.setWorkflowState(state.getLabel());
				}
				//Now process fields that might require more rows.
				if (a.getFolderPaths() != null && a.getFolderPaths().size() == 1) {
					//most likely just one path.
					row.setFolderPath(a.getFolderPaths().get(0));
				} else {
					log.warn("Asset has more than one Folder Path:" + a.getId());
					// Do not include the record if folder is blank/ asset moved to Recycle bin | CMS-3216
					continue;
				}

				if (si != null) {
					for (PSItemProperties p : si.getOwnerPages()) {
						if (pages.equals(""))
							pages = p.getName();
						else
							pages = pages + "\r\n" + p.getName();

						if (pagePaths.equals(""))
							pagePaths = p.getPath();
						else
							pagePaths = pagePaths + "\r\n" + p.getPath();

						if (pages.length() > MAX_MULTINE_CHARACTERS || pagePaths.length() > MAX_MULTINE_CHARACTERS) {
							break;
						}
					}
					row.setPageNames(pages);
					row.setPagePaths(pagePaths);

					for (IPSSite s : si.getOwnerSites()) {

						if (sites.equals(""))
							sites = s.getName();
						else
							sites = sites + "\r\n" + s.getName();

						if (sites.length() < MAX_MULTINE_CHARACTERS) {
							break;
						}
					}

					row.setSiteNames(sites);

					for (PSTemplateSummary t : si.getOwnerTemplates()) {
						if (templateNames.equals(""))
							templateNames = t.getName();
						else
							templateNames = templateNames + "\r\n" + t.getName();

						if (templateNames.length() < MAX_MULTINE_CHARACTERS) {
							break;
						}

					}
					row.setTemplateNames(templateNames);
				}


				ret.add(row);
				i++;
			}catch(Exception e){
				log.warn("Skipping asset due to error: " + e.getMessage());
				log.debug(e);
			}

		}


		return ret;
	}

	@Override
	public List<PSImageAssetReportLine> findAllImageAssets() throws PSReportFailedToRunException {
		List<PSImageAssetReportLine> ret = new ArrayList<>();
		List<PSAsset> assets = assetDao.findAllImageAssets();
		List<String> assetIds =  new ArrayList<>();

		if(log.isDebugEnabled())
			log.debug("Collecting Asset Ids...");

		for(PSAsset a : assets){
			assetIds.add(a.getId());
		}

		if(log.isDebugEnabled())
			log.debug("Analyzing Site Impact for Assets...");

		List<PSAssetSiteImpact> impact = itemService.getAssetSiteImpact(assetIds);

		//Now that we have all of our Assets and Site Impacts we can start pumping out lines
		int i = 0;
		for(PSAsset a : assets){
			PSAssetSiteImpact si = impact.get(i);
			PSImageAssetReportLine row = new PSImageAssetReportLine();
			String sites = "";
			String pages = "";
			String pagePaths = "";
			String templateNames = "";

			row.setId(Integer.parseInt((String)a.getFields().get(IPSHtmlParameters.SYS_CONTENTID)));
			row.setGuid(a.getId());
			row.setAltText((String)a.getFields().get("alttext"));
			row.setTitle((String) a.getFields().get("displaytitle"));
			row.setResourceLinkTitle((String) a.getFields().get("resource_link_title"));
			row.setBulkImportAction("UPDATE");
			row.setFilename((String)a.getFields().get("filename"));
			row.setName((String) a.getFields().get(IPSHtmlParameters.SYS_TITLE));
			row.setContentCreatedBy((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDBY.replace("rx:", "")));
			row.setContentCreatedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDDATE.replace("rx:", "")));
			row.setContentModifiedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIEDATE.replace("rx:", "")));
			row.setContentLastModifier((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIER.replace("rx:", "")));
			row.setContentPostDate((String) a.getFields().get("sys_contentpostdate"));
			row.setContentStartDate((String) a.getFields().get("sys_contentstartdate"));
			row.setPubDate((String) a.getFields().get("sys_pubdate"));


				PSWorkflow wf = workflowService.loadWorkflow(PSGuidUtils.makeGuid(
		                Long.parseLong((String)a.getFields().get(IPSHtmlParameters.SYS_WORKFLOWID)), PSTypeEnum.WORKFLOW));

				PSState state = workflowService.loadWorkflowState(PSGuidUtils.makeGuid(Long.parseLong((String)a.getFields().get(IPSHtmlParameters.SYS_CONTENTSTATEID)),PSTypeEnum.WORKFLOW_STATE), wf.getGUID());

				row.setWorkflowName(wf.getLabel());

				if(state!=null) {
					row.setWorkflowState(state.getLabel());
				}

				//Now process fields that might require more rows.
				if(a.getFolderPaths()!=null && a.getFolderPaths().size()==1){
					//most likely just one path.
					row.setFolderPath(a.getFolderPaths().get(0));
				}else{
					log.warn("Asset has more than one Folder Path:" + a.getId());
					// Do not include the record if folder is blank/ asset moved to Recycle bin | CMS-3216
					continue;
				}

				if(si != null){
					for(PSItemProperties p : si.getOwnerPages()){
						if(pages.equals(""))
							pages = p.getName();
						else
							pages = pages + "\r\n" + p.getName();

						if(pagePaths.equals(""))
							pagePaths = p.getPath();
						else
							pagePaths = pagePaths + "\r\n" + p.getPath();


						if(pages.length()>MAX_MULTINE_CHARACTERS || pagePaths.length()>MAX_MULTINE_CHARACTERS){
							break;
						}
					}
					row.setPageNames(pages);
					row.setPagePaths(pagePaths);

					for(IPSSite s : si.getOwnerSites()){

						if(sites.equals(""))
							sites = s.getName();
						else
							sites = sites + "\r\n" + s.getName();


						if(sites.length()<MAX_MULTINE_CHARACTERS){
							break;
						}
					}

					row.setSiteNames(sites);

					for(PSTemplateSummary t: si.getOwnerTemplates()){
						if(templateNames.equals(""))
							templateNames = t.getName();
						else
							templateNames = templateNames + "\r\n" + t.getName();

						if(templateNames.length()<MAX_MULTINE_CHARACTERS){
							break;
						}
					}
					row.setTemplateNames(templateNames);
				}



			ret.add(row);
			i++;
		}


		return ret;
	}

	@Override
	public List<PSFileAssetReportLine> findNonCompliantFileAssets() throws PSReportFailedToRunException {
		List<PSFileAssetReportLine> ret = new ArrayList<>();
		List<PSAsset> assets = assetDao.findAllNonADACompliantFileAssets();
		List<String> assetIds =  new ArrayList<>();

		log.debug("Collecting Asset Ids...");

		assetIds = assets.stream().map(PSAsset::getId).collect(Collectors.toList());

		if(log.isDebugEnabled())
			log.debug("Analyzing Site Impact for Assets...");

		List<PSAssetSiteImpact> impact = itemService.getAssetSiteImpact(assetIds);

		//Now that we have all of our Assets and Site Impacts we can start pumping out lines
		int i = 0;
		for(PSAsset a : assets){
			PSAssetSiteImpact si = impact.get(i);
			PSFileAssetReportLine row = new PSFileAssetReportLine();
			String sites = "";
			String pages = "";
			String pagePaths = "";
			String templateNames = "";

				row.setId(Integer.parseInt((String)a.getFields().get(IPSHtmlParameters.SYS_CONTENTID)));
				row.setGuid(a.getId());
				row.setBulkImportAction("UPDATE");
			    // Added mising AltText column in non-ada-files report | CMS-3216
				row.setAltText((String)a.getFields().get("alttext"));
				row.setTitle((String)a.getFields().get("displaytitle"));
				row.setFilename((String)a.getFields().get("filename"));
				row.setName((String) a.getFields().get(IPSHtmlParameters.SYS_TITLE));
				row.setContentCreatedBy((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDBY.replace("rx:", "")));
				row.setContentCreatedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDDATE.replace("rx:", "")));
				row.setContentModifiedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIEDATE.replace("rx:", "")));
				row.setContentLastModifier((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIER.replace("rx:", "")));
				row.setContentPostDate((String) a.getFields().get("sys_contentpostdate"));



				PSWorkflow wf = workflowService.loadWorkflow(PSGuidUtils.makeGuid(
		                Long.parseLong((String)a.getFields().get(IPSHtmlParameters.SYS_WORKFLOWID)), PSTypeEnum.WORKFLOW));

				PSState state = workflowService.loadWorkflowState(PSGuidUtils.makeGuid(Long.parseLong((String)a.getFields().get(IPSHtmlParameters.SYS_CONTENTSTATEID)),PSTypeEnum.WORKFLOW_STATE), wf.getGUID());

				row.setWorkflowName(wf.getLabel());
				row.setWorkflowState(state.getLabel());

				//Now process fields that might require more rows.
				if(a.getFolderPaths()!=null && a.getFolderPaths().size()==1){
					//most likely just one path.
					row.setFolderPath(a.getFolderPaths().get(0));
				}else{
					log.warn("Asset has more than one Folder Path:" + a.getId());
					// Do not include the record if folder is blank/ asset moved to Recycle bin | CMS-3216
					continue;
				}

				if(si != null){
					for(PSItemProperties p : si.getOwnerPages()){
						if(pages.equals(""))
							pages = p.getName();
						else
							pages = pages + "\r\n" + p.getName();

						if(pagePaths.equals(""))
							pagePaths = p.getPath();
						else
							pagePaths = pagePaths + "\r\n" + p.getPath();


						if(pages.length()>MAX_MULTINE_CHARACTERS || pagePaths.length()>MAX_MULTINE_CHARACTERS){
							break;
						}
					}
					row.setPageNames(pages);
					row.setPagePaths(pagePaths);

					for(IPSSite s : si.getOwnerSites()){

						if(sites.equals(""))
							sites = s.getName();
						else
							sites = sites + "\r\n" + s.getName();


						if(sites.length()<MAX_MULTINE_CHARACTERS){
							break;
						}
					}

					row.setSiteNames(sites);

					for(PSTemplateSummary t: si.getOwnerTemplates()){
						if(templateNames.equals(""))
							templateNames = t.getName();
						else
							templateNames = templateNames + "\r\n" + t.getName();

						if(templateNames.length()<MAX_MULTINE_CHARACTERS){
							break;
						}
					}
					row.setTemplateNames(templateNames);
				}



			ret.add(row);
			i++;
		}


		return ret;
	}

	@Override
	public List<PSFileAssetReportLine> findAllFileAssets() throws PSReportFailedToRunException {
		List<PSFileAssetReportLine> ret = new ArrayList<>();
		List<PSAsset> assets = assetDao.findAllFileAssets();
		List<String> assetIds =  new ArrayList<>();

		if(log.isDebugEnabled())
			log.debug("Collecting Asset Ids...");

		for(PSAsset a : assets){
			assetIds.add(a.getId());
		}

		if(log.isDebugEnabled())
			log.debug("Analyzing Site Impact for Assets...");

		List<PSAssetSiteImpact> impact = itemService.getAssetSiteImpact(assetIds);

		//Now that we have all of our Assets and Site Impacts we can start pumping out lines
		int i = 0;
		for(PSAsset a : assets){
			PSAssetSiteImpact si = impact.get(i);
			PSFileAssetReportLine row = new PSFileAssetReportLine();
			String sites = "";
			String pages = "";
			String pagePaths = "";
			String templateNames = "";

				row.setId(Integer.parseInt((String)a.getFields().get(IPSHtmlParameters.SYS_CONTENTID)));
				row.setGuid(a.getId());
			    // Added missing AltText column in non-ada-files report | CMS-3216
				row.setAltText((String)a.getFields().get("alttext"));
				row.setTitle((String) a.getFields().get("displaytitle"));
				row.setBulkImportAction("UPDATE");
				row.setFilename((String)a.getFields().get("filename"));
				row.setName((String) a.getFields().get(IPSHtmlParameters.SYS_TITLE));
				row.setContentCreatedBy((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDBY.replace("rx:", "")));
				row.setContentCreatedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTCREATEDDATE.replace("rx:", "")));
				row.setContentModifiedDate((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIEDATE.replace("rx:", "")));
				row.setContentLastModifier((String) a.getFields().get(IPSContentPropertyConstants.RX_SYS_CONTENTLASTMODIFIER.replace("rx:", "")));
				row.setContentPostDate((String) a.getFields().get("sys_contentpostdate"));
				row.setContentStartDate((String) a.getFields().get("sys_contentstartdate"));
				row.setPubDate((String) a.getFields().get("sys_pubdate"));
				row.setExtension((String) a.getFields().get("item_file_attachment_ext"));

				PSWorkflow wf = workflowService.loadWorkflow(PSGuidUtils.makeGuid(
		                Long.parseLong((String)a.getFields().get(IPSHtmlParameters.SYS_WORKFLOWID)), PSTypeEnum.WORKFLOW));

				PSState state = workflowService.loadWorkflowState(PSGuidUtils.makeGuid(Long.parseLong((String)a.getFields().get(IPSHtmlParameters.SYS_CONTENTSTATEID)),PSTypeEnum.WORKFLOW_STATE), wf.getGUID());

				row.setWorkflowName(wf.getLabel());
				row.setWorkflowState(state.getLabel());

				//Now process fields that might require more rows.
				if(a.getFolderPaths()!=null && a.getFolderPaths().size()==1){
					//most likely just one path.
					row.setFolderPath(a.getFolderPaths().get(0));
				}else{
					log.warn("Asset has more than one Folder Path:" + a.getId());
					// Do not include the record if folder is blank/ asset moved to Recycle bin | CMS-3216
					continue;
				}

				if(si != null){
					for(PSItemProperties p : si.getOwnerPages()){
						if(pages.equals(""))
							pages = p.getName();
						else
							pages = pages + "\r\n" + p.getName();

						if(pagePaths.equals(""))
							pagePaths = p.getPath();
						else
							pagePaths = pagePaths + "\r\n" + p.getPath();

						if(pages.length()>MAX_MULTINE_CHARACTERS || pagePaths.length()>MAX_MULTINE_CHARACTERS){
							break;
						}
					}
					row.setPageNames(pages);
					row.setPagePaths(pagePaths);

					for(IPSSite s : si.getOwnerSites()){

						if(sites.equals(""))
							sites = s.getName();
						else
							sites = sites + "\r\n" + s.getName();


						if(sites.length()<MAX_MULTINE_CHARACTERS){
							break;
						}
					}

					row.setSiteNames(sites);

					for(PSTemplateSummary t: si.getOwnerTemplates()){
						if(templateNames.equals(""))
							templateNames = t.getName();
						else
							templateNames = templateNames + "\r\n" + t.getName();

						if(templateNames.length()<MAX_MULTINE_CHARACTERS){
							break;
						}
					}
					row.setTemplateNames(templateNames);
				}



			ret.add(row);
			i++;
		}


		return ret;
	}

    public String updateAssetWidgetRelationship(PSAssetWidgetRelationship awRel) throws PSAssetServiceException, IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException, PSValidationException {
        rejectIfNull("updateAssetWidgetRelationship", "awRel", awRel);
        return widgetAssetRelationshipService.updateAssetWidgetRelationship(awRel);
    }

    /**
     *
     * {@inheritDoc}
     */
    public String createAssetWidgetRelationship(PSAssetWidgetRelationship awRel) throws PSDataServiceException {
        return createAssetWidgetRelationship(awRel, true);
    }

    private String createAssetWidgetRelationship(PSAssetWidgetRelationship awRel, boolean notify) throws PSDataServiceException {
        rejectIfNull("createAssetWidgetRelationship", "awRel", awRel);
        if (log.isDebugEnabled())
            log.debug("Associated asset to widget: {}",  awRel);

        validateAssetWidgetRelationship(awRel);

        String assetId = awRel.getAssetId();
        String ownerId = awRel.getOwnerId();
        PSAssetSummary asset =  find(assetId);

        String relId =  widgetAssetRelationshipService.createAssetWidgetRelationship(loadOwner(ownerId),
                asset, String.valueOf(awRel.getWidgetId()),
                awRel.getAction(), awRel.getResourceType(), awRel.getAssetOrder(), awRel.getWidgetInstanceName(), awRel.getReplacedRelationshipId());

        /*
         * Below is post processing done after the relationship has been created.
         */
        if (awRel.getResourceType() == PSAssetResourceType.local) {
            log.trace("Related local asset: {}", assetId);

            IPSItemSummary owner = find(ownerId);
            /*
             * We will lock the local content if its parent (page) has revision
             * lock turned on (The page has been in the pending state before).
             */
            if (owner.isRevisionable() && !asset.isRevisionable()) {
                assetDao.revisionControlOn(asset.getId());
            }
        }
        else if (awRel.getResourceType() == PSAssetResourceType.shared) {
            //TODO: STORY-93 Review No longer need to add the asset to a folder here.

        }
        else {
            throw new IllegalStateException("Cannot handle resource type: " + awRel.getResourceType());
        }
        if(notify)
        {
            PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
            pageChangeEvent.setPageId(ownerId);
            pageChangeEvent.setItemId(assetId);
            pageChangeEvent.setType(PSPageChangeEventType.ITEM_ADDED);
            pageService.notifyPageChange(pageChangeEvent);
        }

        return relId;

    }
    /*
     * (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSAssetService#updateInspectedElements(com.percussion.assetmanagement.data.PSInspectedElementsData)
     */
    public List<PSAsset> updateInspectedElements(PSInspectedElementsData newHtmlAssetData) throws PSDataServiceException {
        List<PSHtmlAssetData> assets = newHtmlAssetData.getNewAssets();
        List<PSAsset> results = new ArrayList<PSAsset>();
        String updatedPageId = null;
        if(!assets.isEmpty())
        {
            for (PSHtmlAssetData assetData: assets)
            {
                PSAsset asset = new PSAsset();
                asset.setType(HTML_ASSET_TYPE);
                String newName = nameGenerator.generateLocalContentName();
                asset.setName(newName);
                Map<String, Object> fields = asset.getFields();
                fields.put(HTML_FIELD, assetData.getContent());
                try {
					fields.put(SYS_WORKFLOWID, "" + itemWorkflowService.getLocalContentWorkflowId());
				} catch (PSItemWorkflowServiceException e) {
					log.error(e.getMessage());
					log.debug(e.getMessage(),e);
				}
				fields.put(IPSHtmlParameters.SYS_TITLE, newName);
                PSAsset newAsset = save(asset);
                results.add(newAsset);

                String ownerId = assetData.getOwnerId();
                PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(ownerId, Long.parseLong(assetData.getWidgetId()), "percRawHtml", newAsset.getId(), 0);
                createAssetWidgetRelationship(awRel, false);
                if (updatedPageId == null && pageService.isPageItem(ownerId))
                    updatedPageId = ownerId;
            }
        }

        // if we added assets to a page, update it w/ the template migration version
        if (updatedPageId != null)
            pageService.updateTemplateMigrationVersion(updatedPageId);

        List<PSAssetWidgetRelationship> awRels = newHtmlAssetData.getClearAssets();
        if(!awRels.isEmpty())
        {
            for (PSAssetWidgetRelationship awRel : awRels)
            {
                clearAssetWidgetRelationship(awRel);
            }
        }
        return results;
    }

    /**
     * Clone the asset and then promote the new asset to the template level.
     */
    public PSNoContent promoteAssetWidget(PSAssetWidgetRelationship awRel) throws PSDataServiceException, PSItemWorkflowServiceException {
        rejectIfNull("createAssetWidgetRelationship", "awRel", awRel);
        if (log.isDebugEnabled())
            log.debug("Associated asset to widget: {}" , awRel);

        String relId = null;
        String assetId = awRel.getAssetId();
        if (assetId != null && !assetId.equals(""))
        {
            // is local asset?
            if (awRel.getResourceType().equals(PSAssetResourceType.local))
            {
                PSAsset clonAsset = copy(assetId);

                if (clonAsset != null)
                {
                    // update awRel with clone asset Id
                    awRel.setAssetId(clonAsset.getId());
                    relId = createAssetWidgetRelationship(awRel);
                }
            }
            else
            {
                relId = createAssetWidgetRelationship(awRel);
            }

        }
        return new PSNoContent("Successfully promoted the content");
    }

    /**
     *    Copies an asset given its ID. Returns ID of copied asset
     *    @param id Asset Id, can not be <code>null</code>
     *
     *    @return PSAsset
     */
    private PSAsset copy(String id) throws PSDataServiceException, PSItemWorkflowServiceException {
        PSAsset asset = null;
        try
        {
            asset = load(id);
            if(asset==null)
            	throw new DataServiceNotFoundException();
        }
        catch (DataServiceNotFoundException notFound)
        {
            PSValidationErrorsBuilder builder = validateParameters("copy");
            String msg = "Cannot copy ASSET_NAME. Asset no longer exists.";
            builder.reject("id", msg).throwIfInvalid();
            log.error(msg, notFound);
            return null;
        }

        String path = "";
        if(asset.getFolderPaths() != null
                && ! asset.getFolderPaths().isEmpty()) {
            path = asset.getFolderPaths().get(0);
        }
        String base = asset.getName();
        IPSGuid guid = idMapper.getGuid(id);
        String newName = nameGenerator.generateLocalContentName();
        List<PSCoreItem> items;
        try
        {
            items = contentWs.newCopies(Collections.singletonList(guid), Collections.singletonList(path), "NewCopy", false);
        }
        catch (Exception ae)
        {
            String msg = "Failed to copy asset  \"" + base + "\".";
            log.error(msg, ae);
            return null;
        }

        PSCoreItem newAssetCoreItem = items.get(0);

        PSLocator locator = (PSLocator)newAssetCoreItem.getLocator();
        String newAssetId = idMapper.getString(locator);

        itemWorkflowService.checkOut(newAssetId);
        PSAsset newAsset = load(newAssetId);
        newAsset.getFields().put(IPSHtmlParameters.SYS_TITLE, newName);
        newAsset.setName(newName);
        save(newAsset);
        itemWorkflowService.checkIn(newAssetId);
        log.info("newAssetId: {}", newAssetId);

        return newAsset;
    }

    public void clearAssetWidgetRelationship(PSAssetWidgetRelationship awRel) throws PSAssetServiceException, PSValidationException, IPSWidgetAssetRelationshipService.PSWidgetAssetRelationshipServiceException {
        rejectIfNull("clearAssetWidgetRelationship", "awRel", awRel);

        validateAssetWidgetRelationship(awRel);

        widgetAssetRelationshipService.clearAssetFromWidget(awRel.getOwnerId(), awRel.getAssetId(),
                String.valueOf(awRel.getWidgetId()));
        PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
        pageChangeEvent.setPageId(awRel.getOwnerId());
        pageChangeEvent.setItemId(awRel.getAssetId());
        pageChangeEvent.setType(PSPageChangeEventType.ITEM_REMOVED);
        pageService.notifyPageChange(pageChangeEvent);

    }

    /**
     * Helper to return URL for content Editor.
     *
     * @param itemId asset id, can be <code>null</code>.
     * @param ctName content type name.  Assumed not blank.
     * @param editView view for editing the asset.  Assumed not blank.
     *
     * @return URL
     */
    private String getUrl(IPSGuid itemId, String ctName, String editView, boolean readonly)
    {
        String url = readonly
           ? contentDs.getItemViewUrl(itemId, ctName, editView)
           : contentDs.getItemEditUrl(itemId, ctName, editView);
        // TODO -- need to replace the hard coded "/Rhythmyx/" later
        if(isNotBlank(url))
        {
            if (url.startsWith("../")) {
				url = "/Rhythmyx/" + url.substring(3);
			}
        }

        return url;
    }

    /*
     * //see base interface method for details
     */
	public PSContentEditCriteria getContentEditCriteria(PSAssetEditUrlRequest request) throws PSDataServiceException, PSItemWorkflowServiceException {
        rejectIfNull("getContentEditCriteria", "request", request);
        PSContentEditCriteria ceCrit = new PSContentEditCriteria();
        boolean isPage = validateEditUrlRequest(request);
        PSWidgetItem w = getWidget(request, isPage);
        PSWidgetDefinition wdef = widgetService.load(w.getDefinitionId());
        String ctName = wdef.getWidgetPrefs().getContenttypeName();
        String editView = getEditView(w, Collections.singletonList(IPSHtmlParameters.SYS_TITLE));
        IPSGuid itemId = null;
        if (!isBlank(request.getAssetId()))
            itemId = idMapper.getGuid(request.getAssetId());
        ceCrit.setUrl(getUrl(itemId, ctName, editView, false));
        //If there is an itemId then set its type.
        if(itemId != null)
        {
            IPSItemSummary item = itemSummaryService.find(itemId.toString());
            List<String> fPaths = item.getFolderPaths();
            boolean shared = isSharedAsset(fPaths);
            ceCrit.setAssetType(shared?PSContentEditCriteria.ASSET_TYPE_SHARED:PSContentEditCriteria.ASSET_TYPE_LOCAL);
        }

        boolean producesResource = producesResource(ctName);
        ceCrit.setProducesResource(producesResource);

        //generate a name if it is a new item request and if the content type
        //does not produce a resource
        if (isBlank(request.getAssetId()) && !producesResource)
        {
           ceCrit.setContentName(nameGenerator.generateLocalContentName());
        }

        //Set preferred height and widths
        int prefHt = wdef.getWidgetPrefs().getPreferredEditorHeight() == null ?
              PSContentEditCriteria.DEFAULT_PREFERRED_EDITOR_HEIGHT
            : wdef.getWidgetPrefs().getPreferredEditorHeight().intValue();

        int prefWd = wdef.getWidgetPrefs().getPreferredEditorWidth() == null ?
              PSContentEditCriteria.DEFAULT_PREFERRED_EDITOR_WIDTH
            : wdef.getWidgetPrefs().getPreferredEditorWidth().intValue();
        ceCrit.setPreferredEditorHeight(prefHt);
        ceCrit.setPreferredEditorWidth(prefWd);

        boolean createSharedAsset = wdef.getWidgetPrefs().getCreateSharedAsset();
        ceCrit.setCreateSharedAsset(createSharedAsset);

        /*
         * Add the folder id of where we should create the asset into return object.
         * The UI can then decided whether or not to use it.
         */
        if (isNotBlank(request.getAssetId())) {
            PSAssetSummary sum = find(request.getAssetId());
            ceCrit.setLegacyFolderId(
                    assetUploadFolderPathMap.getLegacyFolderIdForAsset(sum).intValue());
            if (sum.getFolderPaths() != null && (!sum.getFolderPaths().isEmpty())) {
                String folderPath = getFinderPath(sum.getFolderPaths().get(0));
                ceCrit.setFolderPath(folderPath);
            }
        }
        else {
            ceCrit.setLegacyFolderId(getUploadLegacyFolderIdForType(ctName));

            // Set the workflow id only for new items.
            String parentFolderForAsset = assetUploadFolderPathMap.getFolderPathForType(ctName);
            int workflowId = getWorkflowId(ctName, producesResource, parentFolderForAsset);
            if(workflowId == -1)
            {
               throw new PSAssetServiceException("Failed to obtain the workflow for content.");
            }
            //Everyone must have access to local content workflow, if not we need to fix the code to provide it.
            if(workflowId != itemWorkflowService.getLocalContentWorkflowId())
            {
                throwIfUserNotAllowedToCreateAsset(workflowId);
            }
            ceCrit.setWorkflowId(workflowId);
        }

        return ceCrit;
    }

    private Integer getUploadLegacyFolderIdForType(String type) throws PSAssetServiceException {
        return assetUploadFolderPathMap.getLegacyFolderIdForType(type).intValue();

    }

    /*
     * //see base interface method for details
     */
    public List<PSAssetEditor> getAssetEditors(String parentFolderPath) throws PSDataServiceException, PSItemWorkflowServiceException {
        return getAssetEditors(parentFolderPath, null);
    }

    /*
     * //see base interface method for details
     */
    public List<PSAssetEditor> getAssetEditors(String parentFolderPath, String filterDisabledWidgets) throws PSDataServiceException, PSItemWorkflowServiceException {
        List<PSAssetEditor> assetEditors = new ArrayList<>();
        List<PSWidgetSummary> widgetList = widgetService.findByType("All", filterDisabledWidgets);

        //Loop all widgets
        for(PSWidgetSummary widget:widgetList)
        {
            if(widget != null) {
                PSAssetEditor assetEditor = getAssetEditor(parentFolderPath, widget);
                if (assetEditor != null && isNotBlank(assetEditor.getUrl())) {
                    assetEditors.add(assetEditor);
                }
            }
        }

        return assetEditors;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSAssetService#getAssetEditor(java.lang.String)
     */
    public PSAssetEditor getAssetEditor(String widgetId) throws PSDataServiceException, PSItemWorkflowServiceException {
        rejectIfNull("getAssetEditor", "widgetId", widgetId);
        PSWidgetSummary widget = widgetService.find(widgetId);
        String parentFolderPath = assetUploadFolderPathMap.getFolderPathForType(widget.getType());
        PSAssetEditor assetEditor = getAssetEditor(parentFolderPath, widget);
        return assetEditor;
    }

    /*
     * (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSAssetService#getAssetEditor(java.lang.String, java.lang.String)
     */
    public PSAssetEditor getAssetEditor(String widgetId, String folderPath) throws PSDataServiceException, PSItemWorkflowServiceException {
        rejectIfNull("getAssetEditor", "widgetId", widgetId);
        rejectIfNull("getAssetEditor", "folderPath", folderPath);
        PSWidgetSummary widget = widgetService.find(widgetId);
        PSAssetEditor assetEditor = getAssetEditor(folderPath, widget);
        return assetEditor;

    }
    /**
     * Helper function that generates PSAssetEditor object for the supplied widget.
     * @param parentFolderPath may be <code>null</code>
     * @param widget assumed not <code>null</code>
     * @return PSAssetEditor for the supplied widget.
     */
    private PSAssetEditor getAssetEditor(String parentFolderPath, PSWidgetSummary widget) throws PSDataServiceException, PSItemWorkflowServiceException {
        boolean createSharedAsset = true;
        PSAssetEditor assetEditor = new PSAssetEditor();
        PSWidgetDefinition widgetDef = widgetService.load(widget.getId());
        String ctName = widgetDef.getWidgetPrefs().getContenttypeName();
        createSharedAsset = widgetDef.getWidgetPrefs().getCreateSharedAsset();
        if (isNotEmpty(ctName) && (createSharedAsset))
        {
            assetEditor.setTitle(widgetDef.getWidgetPrefs().getTitle());
            assetEditor.setIcon(widget.getIcon());
            assetEditor.setWorkflowId(getWorkflowId(ctName, true, parentFolderPath));
            assetEditor.setUrl(getUrl(null, ctName, getEditView(ctName), false));
            assetEditor.setLegacyFolderId(getUploadLegacyFolderIdForType(ctName));
        }
        return assetEditor;
    }

    @Override
    public List<PSWidgetContentType> getAssetTypes(String filterDisabledWidgets) throws PSDataServiceException {
        List<PSWidgetContentType> results = new ArrayList<>();
        List<PSWidgetSummary> widgetList = widgetService.findByType("All", filterDisabledWidgets);
        //Loop all widgets
        for(PSWidgetSummary widget:widgetList)
        {
            PSWidgetDefinition widgetDef = widgetService.load(widget.getId());
            String ctName = widgetDef.getWidgetPrefs().getContenttypeName();
            if (isNotEmpty(ctName) && widgetDef.getWidgetPrefs().getCreateSharedAsset())
            {
                try
                {
                    PSItemDefinition ceDef = itemDefManager.getItemDef(ctName,
                            PSItemDefManager.COMMUNITY_ANY);
                    long id = ceDef.getGuid().longValue();
                    PSWidgetContentType wc = new PSWidgetContentType();
                    wc.setContentTypeId("" + id);
                    wc.setContentTypeName(ctName);
                    wc.setWidgetId(widgetDef.getId());
                    wc.setWidgetLabel(widgetDef.getWidgetPrefs().getTitle());
                    wc.setIcon(widget.getIcon());
                    results.add(wc);
                }
                catch (PSInvalidContentTypeException e)
                {
                    log.error("Error getting the details for the Content type (" + ctName +") of widget (" + widgetDef.getId() + ")", e);
                }
            }
        }
        return results;
    }

    /*
     * //see base interface method for details
     */
    public String getAssetUrl(String assetId, boolean readonly) throws PSDataServiceException {
        String url = "";
        PSAssetSummary asset = find(assetId);
        String type = asset.getType();
        url = getUrl(idMapper.getGuid(assetId), type, getEditView(type), readonly);

        return url;
    }

    /**
     * Gets the workflow ID associated with the folder path given as argument.
     *
     * @param parentFolderPath Assumed not <code>null</code>.
     * @return The workflow ID associated with the folder path.
     */
    private int getWorkflowIdFromFolder(String parentFolderPath) throws PSAssetServiceException, PSValidationException {
        // I need to load an IPSItemSummary here, as the path pointed by parentFolderPath may not be
        // a folder, but an asset path.
        IPSItemSummary itemSummary;
        try {
			String processedParentFolderPath = parentFolderPath;

			if (!startsWith(parentFolderPath, PSAssetPathItemService.ASSET_ROOT_SUB)) {
				processedParentFolderPath = PSAssetPathItemService.ASSET_ROOT_SUB + "/" + parentFolderPath;
			}

			itemSummary = folderHelper.findItem(processedParentFolderPath);
		}catch (Exception e)
		{
			throw new PSAssetServiceException("There was an error in getting the folder from path: " + parentFolderPath, e);
		}


		PSFolderProperties parentFolder = null;

        if (StringUtils.equals("Folder", itemSummary.getType()))
        {
            parentFolder = folderHelper.findFolderProperties(itemSummary.getId());
        }
        else
        {
            // The parentFolderPath variable points to an item, not a folder. Get its
            // parent folder.
            IPSGuid parentFolderGuid = folderHelper.getParentFolderId(idMapper.getGuid(itemSummary.getId()), true);
            parentFolder = folderHelper.findFolderProperties(idMapper.getString(parentFolderGuid));
        }

        if (parentFolder != null)
        {
            return folderHelper.getValidWorkflowId(parentFolder);
        }
        else
            return -1;
    }

    /**
     * Helper method to get the appropriate workflow id based on the content type
     * name and whether it produces a resource or not. If the content type
     * does not produce a resource then id of the workflow with name
     * LocalContent is returned otherwise the generic workflow is returned.
     *
     * @param ctypeName assumed not <code>null</code>.
     * @param producesResource assumed not <code>null</code>.
     * @param parentFolderPath The parent folder path where the asset will be created.
     * @return workflow id, may return -1 if not found.
     * @throws Exception
     */
    private int getWorkflowId(String ctypeName, boolean producesResource, String parentFolderPath) throws PSAssetServiceException, PSValidationException, PSItemWorkflowServiceException {
        int wfid = -1;

        if (isNotEmpty(parentFolderPath))
        {
            wfid = getWorkflowIdFromFolder(parentFolderPath);
        }

        if (wfid == -1)
        {
            if(!producesResource)
            {
                wfid = itemWorkflowService.getLocalContentWorkflowId();
            }
            else
            {
                List<PSObjectSummary> wfs = workflowService.findWorkflowSummariesByName(workflowService.getDefaultWorkflowName());
                if (!wfs.isEmpty())
                {
                    wfid = (int) wfs.get(0).getGUID().longValue();
                }
            }
        }

        return wfid;
    }

    /*
       * //see base interface method for details
       */
   public List<PSAssetDropCriteria> getWidgetAssetCriteria(String id, Boolean isPage) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
       validateParameters("getWidgetAssetCriteria")
           .rejectIfBlank("id", id)
           .rejectIfNull("isPage", isPage)
           .throwIfInvalid();
      List<PSAssetDropCriteria> criteriaList = new ArrayList<>();

      if (isPage)
      {
          PSPage page = pageService.load(id);
          criteriaList = widgetAssetRelationshipService.getWidgetAssetCriteriaForPage(page, templateService.load(
                  page.getTemplateId()));
      }
      else
      {
          criteriaList = widgetAssetRelationshipService.getWidgetAssetCriteriaForTemplate(templateService.load(id));
      }

      return criteriaList;
   }

    /**
       * Gets the sys_view for editing the widget asset.
       *
       * @param w the widget instance, assumed not <code>null</code>.
       * @param additionalFields list of additional fields to be hidden from the view.  Assumed not <code>null</code>.
       *
       * @return the view, never blank.
       */
    private String getEditView(PSWidgetItem w, List<String> additionalFields)
    {
        List<String> hiddenFields = new ArrayList<>();
        for (Entry<String, Object> p : w.getProperties().entrySet())
        {
            if (p.getKey().startsWith(PSWidgetProperty.HIDE_FIELD_PREFIX))
            {
                boolean bvalue = (Boolean) p.getValue();
                if (bvalue)
                {
                    hiddenFields.add(p.getKey().substring(PSWidgetProperty.HIDE_FIELD_PREFIX.length()));
                }
            }
        }

        for (String field : additionalFields)
        {
            if (!hiddenFields.contains(field))
            {
                hiddenFields.add(field);
            }
        }

        if (hiddenFields.isEmpty())
        {
            return IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME;
        }
        Collections.sort(hiddenFields);
        StringBuilder buffer = new StringBuilder();
        for (String field : hiddenFields)
        {
            if (buffer.length() > 0)
            {
                buffer.append(',');
            }

            buffer.append(field);
        }

        return IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME + buffer.toString();
    }

    /**
     * Gets the sys_view for editing the widget asset.  The name field will be hidden for resource types.
     *
     * @param type the content type of the asset, assumed not <code>null</code>.
     *
     * @return the view, never blank.
     */
    private String getEditView(String type)
    {
        String editView = IPSConstants.SYS_HIDDEN_FIELDS_VIEW_NAME;
        if (producesResource(type))
        {
            editView += IPSHtmlParameters.SYS_TITLE;
        }

        return editView;
    }

    /**
     * Finds the requested widget instance.
     *
     * @param request the request info, assumed not <code>null</code>.
     * @param isPage <code>true</code> if the parent is a page; otherwise the
     *            parent is a template.
     *
     * @return the requested widget, never <code>null</code>.
     */
    private PSWidgetItem getWidget(PSAssetEditUrlRequest request, boolean isPage) throws PSAssetServiceException, DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        String templateId = null;
        Set<PSRegionWidgets> regionSet;
        if (isPage)
        {
            PSPage page = pageService.load(request.getParentId());
            regionSet = page.getRegionBranches().getRegionWidgetAssociations();

            PSWidgetItem w = getWidget(request.getWidgetId(), regionSet);
            if (w != null)
                return w;

            templateId = page.getTemplateId();
        }
        else
        {
            templateId = request.getParentId();
        }
        PSTemplate template = templateService.load(templateId);
        regionSet = template.getRegionTree().getRegionWidgetAssociations();
        PSWidgetItem w = getWidget(request.getWidgetId(), regionSet);
        if (w != null)
            return w;

        // If still there is no widget item found, try to get it given the request
        if (isNotBlank(request.getWidgetDefinition()))
        {
            String assetType = request.getWidgetDefinition();
			try {
				List<PSWidgetSummary> widgetList = widgetService.findAll();
				if (widgetList != null) {
					for (PSWidgetSummary widgetSummary : widgetList) {
						if (widgetSummary.getId().equalsIgnoreCase(assetType)
								|| widgetSummary.getName().equalsIgnoreCase(assetType)) {
							w = new PSWidgetItem();
							w.setDefinitionId(widgetSummary.getId());
							if (w != null)
								return w;
						}
					}
				}
			} catch (PSDataServiceException e) {
				log.error("Error loading asset with id: {} Error: {}",
						request.getWidgetId(),
						e.getMessage());
			}
		}

        throw new PSAssetServiceException("Cannot find widget id=" + request.getWidgetId() + " in " + request.getType()
                + " parent id=" + request.getParentId());
    }

    /**
     * Finds the widget from the specified association.
     *
     * @param id the ID of the widget instance, assumed not blank.
     * @param regionSet the region/widget association, assumed not
     *            <code>null</code>.
     *
     * @return the specified widget, it may be <code>null</code> if does not
     *         exist in the given association.
     */
    private PSWidgetItem getWidget(String id, Set<PSRegionWidgets> regionSet)
    {
        for (PSRegionWidgets rw : regionSet)
        {
            for (PSWidgetItem w : rw.getWidgetItems())
            {
                if (w.getId().equals(id))
                    return w;
            }
        }
        return null;
    }

    /**
     * Used to load the Page or Template owner object represented by the specified id.
     *
     * @param ownerId assumed not <code>null</code>.
     *
     * @return data object for the id, never <code>null</code>.
     */
    private PSAbstractPersistantObject loadOwner(String ownerId) throws PSAssetServiceException {
        try
        {
            // see if it's a page
            return pageService.load(ownerId);
        }
        catch (Exception e1)
        {
            try
            {
                // must be a template
                return templateService.load(ownerId);
            }
            catch (Exception e2)
            {
                throw new PSAssetServiceException("owner is not a template or page.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public PSValidationErrors validateAssetWidgetRelationship(PSAssetWidgetRelationship awr) throws PSValidationException {
        return assetWidgetRelationshipValidator.validate(awr)
            .throwIfInvalid()
            .getValidationErrors();
    }

    public void addAssetToFolder(PSAssetFolderRelationship assetFolderRelationship) throws PSDataServiceException {
        PSBeanValidationUtils.validate(assetFolderRelationship).throwIfInvalid();
        addAssetToFolder(assetFolderRelationship.getFolderPath(), assetFolderRelationship.getAssetId());
    }

    public void addAssetToFolder(String folderPath, String assetId) throws PSDataServiceException {
        IPSItemSummary item = itemSummaryService.find(assetId);
        try
        {
            assetDao.addItemToPath(item, folderPath);

            psContentEvent=new PSContentEvent(assetId,assetId.substring(assetId.lastIndexOf("-")+1,assetId.length()),folderPath, PSContentEvent.ContentEventActions.create,PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.SUCCESS);
            psAuditLogService.logContentEvent(psContentEvent);
        }
        catch (Exception e)
        {
			psContentEvent=new PSContentEvent(assetId,assetId.substring(assetId.lastIndexOf("-")+1,assetId.length()),folderPath, PSContentEvent.ContentEventActions.create,PSSecurityFilter.getCurrentRequest().getServletRequest(), PSActionOutcome.FAILURE);
			psAuditLogService.logContentEvent(psContentEvent);
            throw new PSAssetServiceException("Failed to add asset to folder", e);
        }
    }

    public void removeAssetFromFolder(PSAssetFolderRelationship assetFolderRelationship) throws PSDataServiceException {
        PSBeanValidationUtils.validate(assetFolderRelationship).throwIfInvalid();

        IPSItemSummary item = itemSummaryService.find(assetFolderRelationship.getAssetId());
        try
        {
            assetDao.removeItemFromPath(item, assetFolderRelationship.getFolderPath());
        }
        catch (Exception e)
        {
            throw new PSAssetServiceException("Failed to remove asset from folder", e);
        }
    }

    /**
     * Validates the request info.
     *
     * @param req the request info in question, may not <code>null</code>.
     *
     * @return <code>true</code> if the given parent is a page; otherwise the
     *         given parent is a template.
     */
    private boolean validateEditUrlRequest(PSAssetEditUrlRequest req)
    {
        notNull(req, "Request must not be null.");
        notEmpty(req.getParentId(), "Request parent ID must not be empty.");
        notEmpty(req.getWidgetId(), "Request widget ID must not be empty.");
        notEmpty(req.getType(), "Request type must not be empty.");
        return (req.getType().equals(PSAssetEditUrlRequest.PAGE_PARENT));

    }

    protected class PSAssetWidgetRelationshipValidator extends PSAbstractBeanValidator<PSAssetWidgetRelationship> {

        @Override
        protected void doValidation(PSAssetWidgetRelationship obj, PSBeanValidationException e) {
            try {
				PSTemplateSummary t = templateService.find(obj.getOwnerId());
				if (t == null) {
					PSPage p = pageService.find(obj.getOwnerId());
					if (p == null) {
						e.rejectValue("ownerId", "asset.template_or_page_not_exist", "The template or page does not exist for ownerid.");
					}
				}
			} catch (DataServiceLoadException | DataServiceNotFoundException | PSValidationException dataServiceLoadException) {
				e.addSuppressed(dataServiceLoadException);
			}

		}

    }

    @Override
    protected PSAssetSummary createSummary(String id)
    {
        PSAssetSummary sum =  new PSAssetSummary();
        sum.setId(id);
        return sum;
    }

    public List<PSAssetSummary> findAll()
            throws com.percussion.share.service.IPSDataService.DataServiceLoadException,
            com.percussion.share.service.IPSDataService.DataServiceNotFoundException
    {
        // TODO Auto-generated method stub
        //return null;
        throw new UnsupportedOperationException("findAll is not yet supported");
    }

    public PSAsset createAsset(PSAbstractAssetRequest request) throws PSAssetServiceException, PSValidationException {
        notNull(request, "request must not be null.");

        if (!(request instanceof PSBinaryAssetRequest) && !(request instanceof PSExtractedAssetRequest))
        {
            throw new IllegalArgumentException("unsupported request type : " + request.getClass());
        }

        String internalFolderPath = PSPathUtils.getFolderPath(request.getFolderPath());
        try
        {
            // create the folder (including parent folders) if necessary
            folderHelper.createFolder(internalFolderPath);
        }
        catch (Exception e)
        {
            log.error("Could not create folder : " + request, e);
            throw new PSAssetServiceException(e);
        }

        if (request instanceof PSBinaryAssetRequest)
        {
            return createBinaryAsset((PSBinaryAssetRequest) request);
        }
        return createExtractedAsset((PSExtractedAssetRequest) request);
    }

    public PSAsset updateAsset(String itemId, PSAbstractAssetRequest request, boolean forceCheckOut) throws PSAssetServiceException
    {
        notNull(request, "request must not be null.");

        if (!(request instanceof PSBinaryAssetRequest))
        {
            throw new IllegalArgumentException("unsupported request type : " + request.getClass());
        }

        return updateBinaryAsset(itemId, (PSBinaryAssetRequest) request, forceCheckOut);
    }

    public PSAsset load(String id, boolean isSummary) throws PSAssetServiceException {
    	try {
			return assetDao.find(id, isSummary);
		} catch (IPSGenericDao.LoadException | DataServiceLoadException | PSValidationException | DataServiceNotFoundException e) {
			throw new PSAssetServiceException(e.getMessage(),e);
		}
	}

    public Collection<PSAsset> findByTypeAndWf(String type, String workflow, String state) throws PSAssetServiceException, IPSGenericDao.LoadException {
        notEmpty(type);
        notEmpty(workflow);

        // find the workflow, state id's
        int workflowId = -1;
        int stateId = -1;
        try
        {
            workflowId = itemWorkflowService.getWorkflowId(workflow);
            if (state != null)
            {
                stateId = itemWorkflowService.getStateId(workflow, state);
            }
        }
        catch (PSItemWorkflowServiceException | PSValidationException e)
        {
            throw new PSAssetServiceException(e);
        }

        return assetDao.findByTypeAndWf(type, workflowId, stateId);
    }

    public Collection<PSAsset> findLocalByType(String type) throws PSAssetServiceException, PSValidationException, PSItemWorkflowServiceException, IPSGenericDao.LoadException {
        notEmpty(type);

        return assetDao.findByTypeAndWf(type, getWorkflowId(type, false, null), -1);
    }

    /**
     * @param wfid
     */
    private void throwIfUserNotAllowedToCreateAsset(int wfid) throws PSAssetServiceException {
        PSAccessLevelRequest accessLevelRequest = new PSAccessLevelRequest();
        accessLevelRequest.setWorkflowId(wfid);

        PSAccessLevel accessLevel = userService.getAccessLevel(accessLevelRequest);

        if (!PSAssignmentTypeEnum.ADMIN.name().equals(accessLevel.getAccessLevel()) &&
                !PSAssignmentTypeEnum.ASSIGNEE.name().equals(accessLevel.getAccessLevel()))
            throw new PSAssetServiceException("You are not authorized to create a new asset.");
    }

    /**
	 * @param pageId
	 * @param assetId
     */
    public void updateAsset(String pageId, String assetId)
    {
        if((pageId != null)&& (assetId != null))
        {
            PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
            pageChangeEvent.setPageId(pageId);
            pageChangeEvent.setItemId(assetId);
            pageChangeEvent.setType(PSPageChangeEventType.ITEM_SAVED);
            pageService.notifyPageChange(pageChangeEvent);
        }
    }

    public String shareLocalContent(String name, String path, PSAssetWidgetRelationship awRel) throws PSAssetServiceException
    {
        try
        {
            String methodName = "shareLocalContent";
            validateParameters(methodName)
            .rejectIfBlank("name", name)
            .rejectIfBlank("path", path)
            .rejectIfNull("src", awRel)
            .throwIfInvalid();

            if (log.isDebugEnabled())
                log.debug("shareLocalContent: " + awRel);

            String relId = null;
            String assetId = awRel.getAssetId();
            String newAssetItemId = "";
            if (assetId != null && !StringUtils.isEmpty(assetId))
            {
                // is local asset?
                if (!awRel.getResourceType().equals(PSAssetResourceType.local))
                {
                    validateParameters(methodName).rejectField("src", "src must specify local content", awRel).throwIfInvalid();
                }

                // copy the asset
                PSAsset newAsset = copyAsset(assetId, name, path);

                // update workflow
                updateWorkflow(newAsset.getId(), path);

                // update the relationship with the new asset Id
                clearAssetWidgetRelationship(awRel);
                awRel.setAssetId(newAsset.getId());
                newAssetItemId = idMapper.getItemGuid(newAsset.getId()).toString();
                awRel.setResourceType(PSAssetResourceType.shared);
                relId = createAssetWidgetRelationship(awRel);
            }

            return newAssetItemId;
        }
        catch (Exception e)
        {
            log.error(e.getLocalizedMessage(), e);
            throw new PSAssetServiceException("An error occurred while trying to share the local content.");
        }
    }

    /* (non-Javadoc)
     * @see com.percussion.assetmanagement.service.IPSAssetService#convertHtmlIntoRichTextAsset(com.percussion.pagemanagement.data.PSConvertHtmlWidgetRequest)
     */
    @Override
    public PSAsset createAssetFromSourceAsset(String srcAssetId, String targetAssetType) throws PSAssetServiceException
    {
        notNull(srcAssetId);
        notEmpty(targetAssetType);

        try
        {
            PSAsset srcAsset = load(srcAssetId);

            // TODO Validate the targetAssetType when supporting multiple types of asset.
            PSAsset richTextAsset = createRichTextAssetFromHtmlAsset(srcAsset);

            richTextAsset = save(richTextAsset);
            return richTextAsset;
        } catch (PSDataServiceException e) {
        	log.error(CREATE_ASSET_ERROR_MESSAGE);
        	log.debug(e.getMessage(),e);
			throw new PSAssetServiceException(CREATE_ASSET_ERROR_MESSAGE,e);
		}
    }

    /**
     * Generates an Rich Text Asset from a specified HTML asset.
     *
     * @param htmlAsset the HTML asset, assumed not <code>null</code> and the asset type must be HTML asset.
     * @return the create asset, never <code>null</code>.
     */
    private PSAsset createRichTextAssetFromHtmlAsset(PSAsset htmlAsset)
    {
        isTrue(HTML_ASSET_TYPE.equals(htmlAsset.getType()), "htmlAsset must be \"percRawHtmlAsset\" type.");

        PSAsset asset = new PSAsset();
        asset.setType(RICH_TEXT_ASSET_TYPE);
        asset.setFolderPaths(htmlAsset.getFolderPaths());

        Map<String, Object> htmlFields = htmlAsset.getFields();
        Map<String, Object> fields = asset.getFields();
        String htmlContent = (String) htmlFields.get(HTML_FIELD);
        fields.put(TEXT_FIELD, htmlContent);
        fields.put(IPSHtmlParameters.SYS_WORKFLOWID, htmlFields.get(IPSHtmlParameters.SYS_WORKFLOWID));
        asset.getFields().put(IPSHtmlParameters.SYS_TITLE, getCopyAssetName(htmlAsset));
        return asset;
    }

    /**
     * Gets an asset name from the specified asset. For a shared asset, the returned name will be in the format
     * of "<sourc-name>-copy", "<source-name>-copy-2", ...etc. The created name will be unique within the specified
     * asset.
     * @param srcAsset the source asset, assumed not <code>null</code>.
     * @return the create asset name, not blank.
     */
    private String getCopyAssetName(PSAsset srcAsset)
    {
        if (!isSharedAsset(srcAsset.getFolderPaths()))
            return nameGenerator.generateLocalContentName();

        // return the shared name as  ../<source-name>-copy, ../<source-name>-copy-2, ...etc
        String baseName = srcAsset.getName() + "-copy";
        String name = baseName;
        String basePath = srcAsset.getFolderPaths().get(0) + "/";
        String path = basePath + name;
        IPSGuid id = contentWs.getIdByPath(path);
        int i = 2;
        while (id != null)
        {
            name = baseName + "-" + i++;
            path = basePath + name;
            id = contentWs.getIdByPath(path);
        }
        return name;
    }

    /**
     * Checks if the given folder paths correspond to a shared asset or not.
     *
     * @param folderPaths {@link List}<{@link String}> with the folder paths.
     *            Can be <code>null</code>.
     * @return <code>true</code> if the asset path belongs to shared asset,
     *         <code>false</code> otherwise.
     */
    private boolean isSharedAsset(List<String> folderPaths)
    {
        boolean shared = false;
        if (isNotEmpty(folderPaths))
        {
           for (String path : folderPaths)
           {
              if (path.startsWith(PSAssetPathItemService.ASSET_ROOT))
              {
                 shared = true;
                 break;
              }
           }
        }
        return shared;
    }


    /**
     * Change the worklfow of the supplied asset to match the workflow assigned to the supplied folder path.
     *
     * @param assetId The id of the asset to change.
     * @param path The folder path to use for workflow assignement.
     *
     * @throws PSAssetServiceException If there are any errors.
     */
    private void updateWorkflow(String assetId, String path) throws PSAssetServiceException, PSValidationException {
        int wfId = getWorkflowIdFromFolder(path);
        IPSCmsObjectMgr objMgr = PSCmsObjectMgrLocator.getObjectManager();
        try
        {
            objMgr.changeWorkflowForItem(idMapper.getGuid(assetId).getUUID(), wfId, steppedWfMetadata.getSystemStatesList());
        }
        catch (PSORMException e)
        {
            String msg = "Failed to assign workflow for asset with ID \"" + assetId + "\" in folder \"" + path + "\".";
            throw new PSAssetServiceException(msg, e);
        }
    }

    /**
     * Copy the specified asset using the supplied name and path
     *
     * @param id The id of the asset to copy, must exist
     * @param name The new name to use for the asset copy
     * @param path The folder path in which to place the copy
     *
     * @return The new asset, never <code>null</code>.
     *
     * @throws PSAssetServiceException
     */
    private PSAsset copyAsset(String id, String name, String path) throws PSAssetServiceException, DataServiceLoadException, DataServiceNotFoundException, PSValidationException, PSItemWorkflowServiceException {
        List<IPSGuid> guids = Arrays.asList(idMapper.getGuid(id));
        List<String> paths = Arrays.asList(PSPathUtils.getFolderPath("/" + path));

        List<PSCoreItem> items;
        try
        {
            items = contentWs.newCopies(guids, paths, null, false, true);
        }
        catch (Exception ae)
        {
            String msg = "Failed to create asset \"" + name + "\" in folder \"" + path + "\".";
            throw new PSAssetServiceException(msg, ae);
        }

        PSCoreItem newAssetCoreItem = items.get(0);

        PSLocator locator = (PSLocator)newAssetCoreItem.getLocator();
        String newAssetId = idMapper.getString(locator);

        itemWorkflowService.checkOut(newAssetId);
        PSAsset newAsset = load(newAssetId);
        newAsset.getFields().put(IPSHtmlParameters.SYS_TITLE, name);

        try
        {
            save(newAsset);
        }
        catch (Exception e)
        {
            String msg = "Failed to name asset with \"" + name + "\" in folder \"" + path + "\".";
            throw new PSAssetServiceException(msg, e);
        }

        itemWorkflowService.checkIn(newAssetId);

        return newAsset;
    }

    /**
     * Creates a binary asset (file, flash, image) for the specified request.
     *
     * @param request the asset request, assumed not <code>null</code>.
     *
     * @return the created asset, never <code>null</code>.
     * @throws PSAssetServiceException if an error occurs creating the asset.
     */
    private PSAsset createBinaryAsset(PSBinaryAssetRequest request) throws PSAssetServiceException, PSValidationException {
        PSAsset asset = new PSAsset();
        String type;
        String fieldBase;
        AssetType aType = request.getType();
        if (aType == AssetType.IMAGE)
        {
            type = "percImageAsset";
            fieldBase = "img";
        }
        else
        {
            type = (aType == AssetType.FLASH) ? "percFlashAsset" : "percFileAsset";
            fieldBase = "item_file_attachment";
        }
        asset.setType(type);
        String internalFolderPath = PSPathUtils.getFolderPath(request.getFolderPath());
        asset.setFolderPaths(asList(internalFolderPath));
        Map<String, Object> fieldsMap = asset.getFields();
        String fileName = request.getFileName();
        fieldsMap.put("sys_workflowid", String.valueOf(getWorkflowIdFromFolder(internalFolderPath)));
        fieldsMap.put(IPSHtmlParameters.SYS_TITLE, fileName);
        fieldsMap.put("displaytitle", fileName);
        fieldsMap.put("filename", fileName);
        fieldsMap.put(fieldBase + "_filename", fileName);
        String fileType = request.getFileType();
        fieldsMap.put(fieldBase + "_type", fileType);
        String extension = '.' + substringAfterLast(fileName, ".");

        try
        {
            PSPurgableTempFile ptf = new PSPurgableTempFile("tmp", extension, null, fileName, fileType, null);
            try(FileOutputStream fos = new FileOutputStream(ptf)) {
				PSCopyStream.copyStream(request.getFileContents(), fos);
			}
            fieldsMap.put(fieldBase, ptf);

            return assetDao.save(asset);
        }
        catch(Exception e)
        {
            log.error("Could not create asset : " + fileName, e);
            throw new PSAssetServiceException(e);
        }
    }

    private PSAsset updateBinaryAsset(String itemId, PSBinaryAssetRequest request, boolean forceCheckOut) throws PSAssetServiceException
    {
        PSAsset asset = new PSAsset();
        String type;
        String fieldBase;
        AssetType aType = request.getType();
        if (aType == AssetType.IMAGE)
        {
            type = "percImageAsset";
            fieldBase = "img";
        }
        else
        {
            type = (aType == AssetType.FLASH) ? "percFlashAsset" : "percFileAsset";
            fieldBase = "item_file_attachment";
        }
        asset.setType(type);
        String internalFolderPath = PSPathUtils.getFolderPath(request.getFolderPath());
        asset.setFolderPaths(asList(internalFolderPath));
        asset.setId(itemId);
        Map<String, Object> fieldsMap = asset.getFields();
        String fileName = request.getFileName();
        String fileType = request.getFileType();
        String extension = '.' + substringAfterLast(fileName, ".");

        try
        {
			PSPurgableTempFile ptf = new PSPurgableTempFile("tmp", extension, null, fileName, fileType, null);

            try(FileOutputStream fos = new FileOutputStream(ptf)) {
				PSCopyStream.copyStream(request.getFileContents(), fos);
			}
            fieldsMap.put(fieldBase, ptf);

            IPSGuid guid = idMapper.getGuid(itemId);

            if(forceCheckOut) {
            	List<IPSGuid> ids = new ArrayList<>();
            	ids.add(guid);
            	contentWs.checkinItems(ids, "Force check-in to update the binary in the asset.", true);
            }
            contentWs.prepareForEdit(guid);
            return assetDao.save(asset);
        }
        catch(Exception e)
        {
            log.error("Could not create asset : " + fileName, e);
            throw new PSAssetServiceException(e);
        }
    }

    /**
     * Creates an extracted asset (html, rich text, simple text) for the specified request.
     *
     * @param request the asset request, assumed not <code>null</code>.
     *
     * @return the created asset, never <code>null</code>.
     * @throws PSAssetServiceException if an error occurs creating the asset.
     * @throws PSExtractHTMLException if fail to create asset due to error on extracting content
     */
    private PSAsset createExtractedAsset(PSExtractedAssetRequest request) throws PSAssetServiceException, PSValidationException {
        PSAsset asset = new PSAsset();
        String type;
        String contentFieldName;
        AssetType aType = request.getType();
        if (aType == AssetType.HTML)
        {
            type = "percRawHtmlAsset";
            contentFieldName = "html";
        }
        else
        {
            type = (aType == AssetType.RICH_TEXT) ? RICH_TEXT_ASSET_TYPE : "percSimpleTextAsset";
            contentFieldName = "text";
        }
        asset.setType(type);
        String internalFolderPath = PSPathUtils.getFolderPath(request.getFolderPath());
        asset.setFolderPaths(asList(internalFolderPath));
        Map<String, Object> fieldsMap = asset.getFields();
        String name = getUniqueName(internalFolderPath, request.getFileName());
        fieldsMap.put(IPSHtmlParameters.SYS_TITLE, name);
        fieldsMap.put("sys_workflowid", String.valueOf(getWorkflowIdFromFolder(internalFolderPath)));

        try
        {
            String extractedContent = extractContent(request);
            fieldsMap.put(contentFieldName, extractedContent);
            return assetDao.save(asset);
        }
        catch (IOException | PSDataServiceException e)
        {
            log.error("Could not create asset : \"" + name + "\"", e);
            throw new PSAssetServiceException(e);
        }
	}

    /**
     * Extracts the HTML content for the specified request.
     *
     * @param request the request of the extraction, assumed not <code>null</code>.
     *
     * @return the extracted HTML content, never <code>null</code> or empty.
     *
     * @throws IOException if IO error occurs.
     * @throws PSExtractHTMLException if extraction error occurs.
     */
    private String extractContent(PSExtractedAssetRequest request) throws IOException
    {
        String content = IOUtils.toString(request.getFileContents());
        String selector = request.getSelector();
        String extractedContent = null;

        extractedContent = PSHtmlUtils.extractHtml(selector, content, request.getFileName(), request.shouldIncludeOuterHtml());

        if (isBlank(extractedContent))
        {
            String warning = "CSS Selector \"" + selector + "\" does not exist in file '" + request.getFileName() + ".'";
            throw new PSExtractHTMLException(warning);
        }

        return extractedContent;
    }

    /**
     * Determines if the specified content type produces resource assets.
     *
     * @param ctName the content type name, assumed not <code>null</code>.
     *
     * @return <code>true</code> if the content type produces resource assets, <code>false</code> otherwise.
     */
    private boolean producesResource(String ctName)
    {
        try
        {
            PSItemDefinition ceDef = itemDefManager.getItemDef(ctName,
                  PSItemDefManager.COMMUNITY_ANY);
            PSContentEditor editor = ceDef.getContentEditor();
            return editor.doesProduceResource();
        }
        catch (PSInvalidContentTypeException e)
        {
            log.warn("Invalid content type: " + ctName);
        }

        return false;
    }

    /**
     * Generates a unique name for an asset under the specified folder path.  See
     * {@link PSFolderPathUtils#addEnumeration(String, Number)} for the name generation algorithm.
     *
     * @param folderPath the containing folder, assumed not blank.
     * @param name the asset name from which to begin, assumed not blank.
     *
     * @return a unique name under the specified folder.
     */
    private String getUniqueName(String folderPath, String name)
    {
        int counter = 1;
        String newName = PSFolderPathUtils.replaceInvalidItemNameCharacters(name);
        while (contentWs.getIdByPath(folderPath + '/' + newName) != null)
        {
            newName = PSFolderPathUtils.addEnumeration(PSFolderPathUtils.replaceInvalidItemNameCharacters(name), counter++);
        }

        return newName;
    }

    public PSAbstractBeanValidator<PSAssetWidgetRelationship> assetWidgetRelationshipValidator;


    /**
     * The item def manager for accessing content types etc... Initialized by
     * constructor, never <code>null</code> after that.
     */
    private PSItemDefManager itemDefManager;

    /**
     * The workflow service for accessing the workflow information.Initialized by
     * constructor, never <code>null</code> after that.
     */
    private IPSWorkflowService workflowService;

    /**
     * The id mapper, initialized by constructor, never <code>null</code> after
     * that.
     */
    private IPSIdMapper idMapper;

    /**
     * The page service, initialized by constructor, never <code>null</code>
     * after that.
     */
    private IPSPageService pageService;

    /**
     * The template service, initialized by constructor, never <code>null</code>
     * after that.
     */
    private IPSTemplateService templateService;


    /**
     * The widget service, initialized by constructor, never <code>null</code>
     * after that.
     */
    private IPSWidgetService widgetService;

    /**
     * The content design service, initialized by constructor, never
     * <code>null</code> after that.
     */
    private IPSContentDesignWs contentDs;

    /**
     * The widget asset relationship service, initialized by constructor, never <code>null</code> after that.
     */
    private IPSWidgetAssetRelationshipService widgetAssetRelationshipService;

    /**
     * The name generator, initialized by constructor, never <code>null</code> after that.
     */
    private IPSNameGenerator nameGenerator;

    /**
     * The asset dao, initialized by constructor, never <code>null</code> after that.
     */
    private IPSAssetDao assetDao;

    /**
     * The folder helper, initialized by constructor, never <code>null</code> after that.
     */
    private IPSFolderHelper folderHelper;

    /**
     * The content web service, initialized by constructor, never <code>null</code> after that.
     */
    private IPSContentWs contentWs;

    /**
     * The service used for workflow operations on items.  Initialized by constructor, never <code>null</code> after
     * that.
     */
    private IPSItemWorkflowService itemWorkflowService;

    private PSAssetUploadFolderPathMap assetUploadFolderPathMap;

    /**
     * User service. Initialized by constructor, never <code>null</code> after that.
     */
    private IPSUserService userService;

    /**
     * Stepped wf metadata. Initialized by constructor, never <code>null</code> after that.
     */
    private IPSSteppedWorkflowMetadata steppedWfMetadata;

    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSAssetService.class);

    private static int MAX_MULTINE_CHARACTERS=32000;



}
