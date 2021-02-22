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

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship.PSAssetResourceType;
import com.percussion.assetmanagement.data.PSOrphanedAssetSummary;
import com.percussion.assetmanagement.data.PSReportFailedToRunException;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.IPSWidgetAssetRelationshipService;
import com.percussion.assetmanagement.service.impl.PSPreviewPageUtils;
import com.percussion.cms.IPSConstants;
import com.percussion.cms.objectstore.PSComponentSummary;
import com.percussion.contentmigration.service.IPSContentMigrationService;
import com.percussion.contentmigration.service.PSContentMigrationException;
import com.percussion.design.objectstore.PSLocator;
import com.percussion.design.objectstore.PSRelationship;
import com.percussion.itemmanagement.data.PSItemStateTransition;
import com.percussion.itemmanagement.service.IPSItemService;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.itemmanagement.service.IPSWorkflowHelper;
import com.percussion.licensemanagement.data.PSModuleLicense;
import com.percussion.pagemanagement.assembler.PSAbstractMergedRegionTree;
import com.percussion.pagemanagement.assembler.PSMergedRegion;
import com.percussion.pagemanagement.assembler.PSMergedRegion.PSMergedRegionOwner;
import com.percussion.pagemanagement.assembler.PSMergedRegionTree;
import com.percussion.pagemanagement.assembler.PSWidgetInstance;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSPageChangeEvent;
import com.percussion.pagemanagement.data.PSPageChangeEvent.PSPageChangeEventType;
import com.percussion.pagemanagement.data.PSPageReportLine;
import com.percussion.pagemanagement.data.PSRegion;
import com.percussion.pagemanagement.data.PSRegionWidgets;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetDefinition;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.IPSWidgetService;
import com.percussion.pagemanagement.service.impl.PSPageChangeHandler;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.IPSPathService;
import com.percussion.pathmanagement.service.impl.PSPathUtils;
import com.percussion.recent.service.rest.IPSRecentService;
import com.percussion.redirect.data.PSCreateRedirectRequest;
import com.percussion.redirect.data.PSRedirectStatus;
import com.percussion.redirect.service.IPSRedirectService;
import com.percussion.rest.assets.Asset;
import com.percussion.rest.assets.IAssetAdaptor;
import com.percussion.rest.errors.AssetNotFoundException;
import com.percussion.rest.errors.BackendException;
import com.percussion.rest.errors.ContentMigrationException;
import com.percussion.rest.errors.FolderNotFoundException;
import com.percussion.rest.errors.PageNotFoundException;
import com.percussion.rest.errors.TemplateNotFoundException;
import com.percussion.rest.pages.CalendarInfo;
import com.percussion.rest.pages.CodeInfo;
import com.percussion.rest.pages.IPageAdaptor;
import com.percussion.rest.pages.Page;
import com.percussion.rest.pages.Region;
import com.percussion.rest.pages.SeoInfo;
import com.percussion.rest.pages.Widget;
import com.percussion.rest.pages.WorkflowInfo;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.services.workflow.data.PSWorkflow;
import com.percussion.share.dao.IPSContentItemDao;
import com.percussion.share.dao.IPSFolderHelper;
import com.percussion.share.dao.PSDateUtils;
import com.percussion.share.dao.PSFolderPathUtils;
import com.percussion.share.dao.impl.PSContentItem;
import com.percussion.share.data.PSItemProperties;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.service.IPSSiteDataService;
import com.percussion.sitemanage.service.IPSSiteSectionService;
import com.percussion.user.service.IPSUserService;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.util.PSSiteManageBean;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static com.percussion.assetmanagement.service.impl.PSPreviewPageUtils.getEmptyTemplateWidgets;
import static com.percussion.assetmanagement.service.impl.PSPreviewPageUtils.getUsedPageAssets;

@PSSiteManageBean
@Lazy
public class PageAdaptor extends SiteManageAdaptorBase implements IPageAdaptor
{

	 private enum WorkflowStates{
	    	APPROVE,
	    	ARCHIVE,
	    	REVIEW
	    }
	 
    private IPSPathService pathService;

    private IPSFolderHelper folderHelper;

    private IPSSiteSectionService sectionService;

    private IPSPageService pageService;

    private IPSTemplateService templateService;

    private IPSWorkflowHelper wfHelper;

    private IPSContentItemDao contentItemDao;

    private IPSWidgetService widgetService;

    private IPSWidgetAssetRelationshipService widgetAssetService;

    private IPSNameGenerator nameGenerator;

    private IPSAssetService assetService;
    
    private IAssetAdaptor assetAdaptor;
    private IPSRedirectService redirectService;
    
    private IPSSiteDataService siteDataService;

    private IPSContentMigrationService migrationService;
    
    private IPSRecentService recentService;
    
    private IPSItemService itemService;

    /**
     * The ID map service, initialized by constructor.
     */
    private IPSIdMapper idMapper;
    
    /**
     * Logger for this service.
     */
    public static Log log = LogFactory.getLog(PageAdaptor.class);

    @Autowired
    public PageAdaptor(@Qualifier("pathService") IPSPathService pathService, IPSFolderHelper folderHelper, IPSSiteSectionService sectionService,
                       IPSIdMapper idMapper, IPSPageService pageService, IPSTemplateService templateService,
                       IPSWorkflowHelper wfHelper, @Qualifier(value = "contentItemDao") IPSContentItemDao contentItemDao, IPSWidgetService widgetService,
                       IPSWidgetAssetRelationshipService widgetAssetService, IPSNameGenerator nameGenerator,
                       IPSItemWorkflowService itemWorkflowService, IPSAssetService assetService, IPSUserService userService, IAssetAdaptor assetAdaptor)
    {
        super(userService, itemWorkflowService);
        this.pathService = pathService;
        this.folderHelper = folderHelper;
        this.sectionService = sectionService;
        this.idMapper = idMapper;
        this.pageService = pageService;
        this.templateService = templateService;
        this.wfHelper = wfHelper;
        this.contentItemDao = contentItemDao;
        this.widgetService = widgetService;
        this.widgetAssetService = widgetAssetService;
        this.nameGenerator = nameGenerator;
        this.assetService = assetService;
        this.assetAdaptor = assetAdaptor;
        this.redirectService = redirectService;
        this.siteDataService = siteDataService;
        this.migrationService = migrationService;
        this.recentService = recentService;
        this.itemService = itemService;
    }

    @Override
    public Page getPage(URI baseUri, String site, String path, String pageName) throws BackendException, PSDataServiceException {
        checkAPIPermission();

        UrlParts url = new UrlParts(site, path, pageName);
        String pathServicePath = StringUtils.substring(url.getUrl(), 1);
        PSPathItem pathItem = null;
        try
        {
            pathItem = pathService.find(pathServicePath);

        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            // Change to say that path is not a page
            throw new PageNotFoundException();
        }

        if (!pathItem.getType().equals("percPage"))
        {
            throw new PageNotFoundException();
        }

        Page page = new Page();
        PSPage psPage;
        try {
             psPage = pageService.findPageByPath(url.getUrl());
        } catch (IPSPageService.PSPageException | PSValidationException e) {
            throw new BackendException(e.getMessage(),e);
        }

        PSItemProperties itemProperties = null;
        try {
            itemProperties = pathService.findItemProperties(StringUtils.substring(url.getUrl(), 1));
        } catch (PSDataServiceException | IPSPathService.PSPathServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }
        PSComponentSummary summ = wfHelper.getComponentSummary(itemProperties.getId());

        page.setName(psPage.getName());
        page.setSiteName(site);
        page.setFolderPath(path);
        page.setId(psPage.getId());
        // page.setDisplayName(psPage.getLabel());
        page.setDisplayName(psPage.getLinkTitle());
        // linkTitle?

        // Only set if auto flag off. page.setSummary(psPage.getSummary());

        PSTemplate template = templateService.load(psPage.getTemplateId());

        page.setTemplateName(template.getName());

        PSWorkflow wf = loadWorkflow(psPage.getWorkflowId());
        WorkflowInfo wfInfo = new WorkflowInfo();
        page.setWorkflow(wfInfo);
        wfInfo.setName(wf.getName());

        wfInfo.setCheckedOutUser(StringUtils.defaultString(summ.getCheckoutUserName()));
        if(summ.getCheckoutUserName()!= null && !summ.getCheckoutUserName().isEmpty())
        	wfInfo.setCheckedOut(true);
        else
        	wfInfo.setCheckedOut(false);

        wfInfo.setState(itemProperties.getStatus());

        SeoInfo seo = new SeoInfo();

        seo.setBrowserTitle(psPage.getTitle());

        // Other fields from internal content type

        // Find the content item for the given id
        PSContentItem contentItem;
        try {
             contentItem = contentItemDao.find(psPage.getId());
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }

        Map<String, Object> fields = contentItem.getFields();
        CalendarInfo calInfo = new CalendarInfo();
        page.setCalendar(calInfo);
        List<String> calendarsValue = (List<String>) fields.get("page_calendar");
        calInfo.setCalendars(calendarsValue);
        String startDateStr = (String) fields.get("page_start_date");
        Date startDate = null;
        if (StringUtils.isNotEmpty(startDateStr))
        {
            startDate = PSDateUtils.parseSystemDateString(startDateStr);
        }
        calInfo.setStartDate(startDate);

        String endDateStr = (String) fields.get("page_end_date");
        Date endDate = null;
        if (StringUtils.isNotEmpty(endDateStr))
        {
            endDate = PSDateUtils.parseSystemDateString(endDateStr);
        }

        calInfo.setEndDate(endDate);

        String overridePostDateStr = (String) fields.get("sys_contentpostdate");
        Date overridePostDate = null;
        if (StringUtils.isNotEmpty(overridePostDateStr))
        {
            overridePostDate = PSDateUtils.parseSystemDateString(overridePostDateStr);
        }

        page.setOverridePostDate(overridePostDate);

        List<String> pageCategories = (List<String>) fields.get("page_categories_tree");
        if (pageCategories != null)
            seo.setCategories(pageCategories);

        String autoGenerate = (String) fields.get("auto_generate_summary");

        if ("1".equals(autoGenerate))
        {
            page.setSummary("");
        }
        else
        {
            page.setSummary((String) fields.get("page_summary"));
        }

        boolean hideSearch = (psPage.getNoindex() != null && psPage.getNoindex().equalsIgnoreCase("true"));
        seo.setHideSearch(hideSearch);
        seo.setMetaDescription(psPage.getDescription());
        seo.setTags(new ArrayList<>(psPage.getTags()));

        page.setSeo(seo);

        CodeInfo code = new CodeInfo();
        page.setCode(code);
        code.setAfterStart(psPage.getAfterBodyStartContent());
        code.setBeforeClose(psPage.getBeforeBodyCloseContent());
        code.setHead(psPage.getAdditionalHeadContent());

        getRegionInfo(baseUri, page, psPage, template);

        // page.setBody(body);

        // TODO: Populate Page
        return page;
    }

    private boolean updateRegionInfo(Page page, PSPage psPage, PSTemplate template) throws PSDataServiceException {
        boolean savePage = false;

        PSAbstractMergedRegionTree tree = new PSMergedRegionTree(widgetService, template.getRegionTree(),
                psPage.getRegionBranches());

        List<Region> updateRegions = page.getBody();
        if (CollectionUtils.isEmpty(updateRegions))
            return false;

        Map<String, PSMergedRegion> regionMap = tree.getMergedRegionMap();

        for (Region updateRegion : updateRegions)
        {
            PSMergedRegion existingRegion = regionMap.get(updateRegion.getName());
            List<Widget> updateWidgets = updateRegion.getWidgets();
            if (existingRegion != null && updateWidgets != null
                    && CollectionUtils.isEmpty(existingRegion.getSubRegions()))
            {

                PSMergedRegionOwner owner = existingRegion.getOwner();

                List<PSWidgetInstance> existingInstances = existingRegion.getWidgetInstances();
                if (existingInstances == null)
                    existingInstances = new ArrayList<>();

                int instanceSize = (existingInstances == null) ? 0 : existingInstances.size();

                // Can modify region if it is owned by the page, or there are
                // not widgets in it from the template
                boolean regionEditable = (instanceSize == 0 || owner.equals(PSMergedRegionOwner.PAGE));

                boolean clearRegion = updateRegion.getWidgets().isEmpty();
                List<PSWidgetItem> updateWidgetList = new ArrayList<>();
                List<PSWidgetItem> newWidgetList = new ArrayList<>();

                for (Widget updateWidget : updateRegion.getWidgets())
                {

               
                    
                    PSWidgetItem existingWidget = findWidget(existingInstances, updateWidget);

                    if (existingWidget == null && regionEditable)
                    {
                        if (StringUtils.isNotEmpty(updateWidget.getId()))
                            throw new RuntimeException("Cannot find widget id " + updateWidget.getId() + " on region "
                                    + updateRegion.getName());

                        if (StringUtils.isEmpty(updateWidget.getId()) && StringUtils.isEmpty(updateWidget.getName()))
                            throw new RuntimeException("Must specify either id or name on widget");

                        existingWidget = new PSWidgetItem();
                        existingWidget.setDefinitionId(updateWidget.getType());
                        existingWidget.setId(updateWidget.getId());
                        existingWidget.setName(updateWidget.getName());

                    }
                    // will add placeholder nulls for non editable regions
                    newWidgetList.add(existingWidget);

                    updateWidgetList.add(existingWidget);

                }

                // update region widgets

                if (regionEditable && ! (updateWidgets.isEmpty() && existingInstances.isEmpty()))
                {

                    Set<PSRegionWidgets> widgetAssoc = psPage.getRegionBranches().getRegionWidgetAssociations();

                    // Manually remove items. remove method does not work due to
                    // hashcode/equals not correct D-01991
                    Set<PSRegionWidgets> newWidgetAssoc = new HashSet<>();
                    for (Iterator<PSRegionWidgets> it = widgetAssoc.iterator(); it.hasNext();)
                    {
                        PSRegionWidgets widgetToAdd = it.next();
                        if (!widgetToAdd.getRegionId().equals(updateRegion.getName()))
                            newWidgetAssoc.add(widgetToAdd);
                    }

                    if (!clearRegion)
                        pullTemplateRegionToPage(psPage, template, updateRegion.getName());

                    psPage.getRegionBranches().setRegionWidgetAssociations(newWidgetAssoc);

                    psPage.getRegionBranches().setRegionWidgets(updateRegion.getName(), updateWidgetList);

                    savePage = true;
                }

            }
            else
            {
                // log ignoring unknown region.
            }

        }

        return savePage;
    }

    private void getRegionInfo(URI baseUri, Page page, PSPage psPage, PSTemplate template) throws BackendException, PSDataServiceException {

        boolean existingPage = StringUtils.isNotEmpty(psPage.getId());

        List<Region> itemRegions = new ArrayList<>();
        page.setBody(itemRegions);

        PSAbstractMergedRegionTree tree = new PSMergedRegionTree(widgetService, template.getRegionTree(),
                psPage.getRegionBranches());

        Map<String, PSRelationship> assetWidgets = (psPage.getId() == null)
                ? new HashMap<>()
                : getUsedPageAssets(psPage, template);

        Set<PSWidgetItem> emptyTemplateWidgets = getEmptyTemplateWidgets(template);
        HashSet<String> emptyTemplateWidgetIds = new HashSet<>();
        for (PSWidgetItem emptyTemplateWidgetId : emptyTemplateWidgets)
        {
            emptyTemplateWidgetIds.add(emptyTemplateWidgetId.getId());
        }

        for (Entry<String, PSMergedRegion> regionEntry : tree.getMergedRegionMap().entrySet())
        {

            String regionName = regionEntry.getKey();
            PSMergedRegion mergedRegion = regionEntry.getValue();
            if (!CollectionUtils.isEmpty(mergedRegion.getSubRegions()))
                continue;
            Region itemRegion = new Region();
            itemRegion.setName(regionName);
            itemRegions.add(itemRegion);

            itemRegion.setType(mergedRegion.getOwner().name());

            boolean readOnlyRegion = false;

            if (mergedRegion.getOwner() == PSMergedRegionOwner.TEMPLATE)
            {

                List<PSMergedRegion> subRegions = mergedRegion.getSubRegions();
                List<PSWidgetInstance> widgetInstances = mergedRegion.getWidgetInstances();

                if ((subRegions != null && !subRegions.isEmpty())
                        || (widgetInstances != null && !widgetInstances.isEmpty()))
                {
                    readOnlyRegion = true;
                }

            }

            List<PSWidgetItem> existingRegionWidgets = getMergedWidgetItemsForRegion(tree, regionName);

            // end update regions

            itemRegion.setEditable(!readOnlyRegion);
            List<Widget> regionWidgets = new ArrayList<>();
            itemRegion.setWidgets(regionWidgets);

            if (existingRegionWidgets != null)
            {

                for (PSWidgetItem widget : existingRegionWidgets)
                {

                    String name = widget.getName();
                    String id = widget.getId();
                    Widget itemWidget = new Widget();

                    itemWidget.setId(id);
                    itemWidget.setName(name);
                    itemWidget.setType(widget.getDefinitionId());
                    regionWidgets.add(itemWidget);
                    PSRelationship assetRels = assetWidgets.get(id);

                    itemWidget.setScope(Widget.SCOPE_LOCAL);

                    // we can edit a widget if it is from a template and empty
                    // or from the page.
                    if (emptyTemplateWidgetIds.contains(id) || !readOnlyRegion)
                    {
                        itemWidget.setEditable(true);
                    }
                    else
                    {
                        itemWidget.setEditable(false);
                    }

                   
                    if (assetRels != null)
                    {
                       
                        Asset asset = new Asset();
                        itemWidget.setAsset(asset);

                        boolean isLocal = assetRels.getConfig().getName().equals("LocalContent");

                        itemWidget.setScope(isLocal ? Widget.SCOPE_LOCAL : Widget.SCOPE_SHARED);

                        PSLocator assetLocator = assetRels.getDependent();
                        assetLocator.setRevision(-1);

                        String guidString = idMapper.getString(assetLocator);

                        if (!isLocal)
                        {
                            itemWidget.setAsset(assetAdaptor.getSharedAsset(baseUri, guidString));
                        }
                        else
                        {
                            PSAsset assetItem;
                            try {
                                assetItem = assetService.load(guidString, true);
                            } catch (IPSAssetService.PSAssetServiceException e) {
                                throw new BackendException(e.getMessage(),e);
                            }
                            if (assetItem != null)
                            {

                                HashMap<String, String> restAssetFields = new HashMap<>();
                                asset.setFields(restAssetFields);

                                Map<String, Object> assetFields = assetItem.getFields();

                                for (Entry<String, Object> field : assetFields.entrySet())
                                {
                                    String fieldName = field.getKey();
                                    if (!fieldName.startsWith("sys_") && !fieldName.equals("revision")
                                            && !fieldName.startsWith("jcr:"))
                                    {
                                        String value = (field.getValue() == null) ? null : field.getValue().toString();
                                        restAssetFields.put(field.getKey(), value);
                                    }
                                }

                            }
                        }
                    } 
                    

                    // get content for asset.
                }

            }

        }
    }

    private PSWidgetItem findWidget(List<PSWidgetInstance> existingInstances, Widget updateWidget)
    {
        PSWidgetItem existingWidget = null;
        for (PSWidgetInstance instance : existingInstances)
        {
            PSWidgetItem widget = instance.getItem();

            if (updateWidget.getId() != null && widget.getId() != null && updateWidget.getId().equals(widget.getId()))
                existingWidget = widget;
            if (updateWidget.getName() != null && widget.getName() != null
                    && updateWidget.getName().equals(widget.getName()))
                existingWidget = widget;

        }
        return existingWidget;
    }

    private void pullTemplateRegionToPage(PSPage psPage, PSTemplate template, String regionName)
    {
        List<PSRegion> currentRegions = psPage.getRegionBranches().getRegions();

        if (currentRegions == null)
            currentRegions = new ArrayList<>();

        boolean found = false;
        for (PSRegion checkRegion : currentRegions)
        {
            if (checkRegion != null && checkRegion.getRegionId().equals(regionName))
            {
                found = true;
                break;
            }
        }

        if (!found)
        {
            PSRegion tempRegion = null;

            for (PSRegion tempRegionLoop : template.getRegionTree().getDescendentRegions())
            {
                if (regionName.equals(tempRegionLoop.getRegionId()))
                {
                    tempRegion = tempRegionLoop;
                    tempRegion.setStartTag(null);
                    tempRegion.setEndTag(null);
                    break;
                }
            }

            currentRegions.add(tempRegion);
        }
        psPage.getRegionBranches().setRegions(currentRegions);
    }

    private List<PSWidgetItem> getMergedWidgetItemsForRegion(PSAbstractMergedRegionTree tree, String regionName)
    {
        List<PSWidgetInstance> existingRegionWidgetInstances = tree.getMergedRegionMap().get(regionName)
                .getWidgetInstances();
        List<PSWidgetItem> existingRegionWidgets = new ArrayList<>();
        if (existingRegionWidgetInstances != null)
        {
            for (PSWidgetInstance existingRegionWidgetInstance : existingRegionWidgetInstances)
            {
                existingRegionWidgets.add(existingRegionWidgetInstance.getItem());
            }
        }
        return existingRegionWidgets;
    }
    @Override
    public Page renamePage(URI baseUri, String siteName, String path, String pageName, String newName) throws BackendException, PSDataServiceException {
    	
    	  checkAPIPermission();

          UrlParts url = new UrlParts(siteName, path, pageName);

          UrlParts folderUrl = new UrlParts(url.getSite(), url.getPath(), null);
          String pathServicePath = StringUtils.substring(folderUrl.getUrl(), 1);
          String originalName = pageName;
          try
          {
        	  pathService.find(pathServicePath);
          }
          catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
          {
              throw new FolderNotFoundException();
          }
          
          // If required state is not set then get current state to return to.
          String endState = "Draft";
          String currentState = "Draft"; // PXA This seems mostly for debugging
                                         // purposes?
        PSPage psPage;
        try {
            psPage = pageService.findPageByPath(url.getUrl());
        } catch (IPSPageService.PSPageException | PSValidationException e) {
            throw new BackendException(e.getMessage(),e);
        }

        if(psPage == null){
        	  throw new PageNotFoundException("Unable to rename Page: " + url.getUrl() + " as the target Page was not found." );
          }
          currentState = getWorkflowState(psPage);
          endState = currentState;

          if (!wfHelper.isCheckedOutToCurrentUser(psPage.getId()))
          {
              try {
                  itemWorkflowService.forceCheckOut(psPage.getId());
              } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                  throw new BackendException(e.getMessage(),e);
              }
          }
          
          psPage.setName(newName);
          
          //Save the Page
        try {
            psPage = pageService.save(psPage);
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }

        //Make sure the metadata change event is queued as well
          pageService.savePageMetadata(psPage.getId());
          
          if (wfHelper.isCheckedOutToCurrentUser(psPage.getId()))
          {
              try {
                  itemWorkflowService.checkIn(psPage.getId());
              } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                  throw new BackendException(e.getMessage(),e);
              }
          }
          
          //Check if Redirects are turned on - if they are - generate a redirect.
          if(redirectService!=null && redirectService.status().getStatusCode() == PSRedirectStatus.SERVICE_OK){
        	  try{
        		  PSSiteSummary site = siteDataService.findByPath(psPage.getFolderPath() +"/" + newName);
        		  
          		  site.setPubInfo(siteDataService.getS3PubServerInfo(site.getSiteId()));
        		  PSModuleLicense lic = redirectService.getLicense();
        		  
        		  if(lic != null && site.getPubInfo()!=null){
	        		  PSCreateRedirectRequest request = new PSCreateRedirectRequest();
	        		  request.setCategory(IPSRedirectService.REDIRECT_CATEGORY_AUTOGEN);
	        		  request.setCondition(url.getPath() + "/" + url.getName() );
	        		  request.setEnabled(true);
	        		  request.setKey(lic.getKey());
	        		  request.setPermanent(true);
	        		  request.setRedirectTo(url.getPath() + "/" + newName);
	        		  request.setSite(site.getPubInfo().getBucketName());
	        		  request.setType(IPSRedirectService.REDIRECT_TYPE_DEFAULT);
	        		  redirectService.createRedirect(request);
        		  }
        	  }catch(Exception e){
        		  log.error("An error occurred generating a Redirect while renaming Page: " + psPage.getId(),e);
        	  }
        	  
          }
          
          
          return getPage(baseUri, url.getSite(), url.getPath(), newName);
          
    }
    
    @Override
    public Page updatePage(URI baseUri, Page toPage) throws BackendException, PSDataServiceException {
        checkAPIPermission();

        UrlParts url = new UrlParts(toPage.getSiteName(), toPage.getFolderPath(), toPage.getName());

        UrlParts folderUrl = new UrlParts(toPage.getSiteName(), toPage.getFolderPath(), null);
        String pathServicePath = StringUtils.substring(folderUrl.getUrl(), 1);
       
        try
        {
           pathService.find(pathServicePath);

        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            throw new FolderNotFoundException();
        }
        PSPage psPage;
        try {
             psPage = pageService.findPageByPath(url.getUrl());
        } catch (IPSPageService.PSPageException | PSValidationException e) {
           throw new BackendException(e.getMessage(),e);
        }
        if (psPage == null)
        { 
            if (PSFolderPathUtils.testHasInvalidChars(toPage.getName()))
                throw new IllegalArgumentException("Cannot create a page with following chars in the name" +IPSConstants.INVALID_ITEM_NAME_CHARACTERS);
            
            
            createNewPage(baseUri, toPage, null);
        }
        else
        {
        	
            createNewPage(baseUri, toPage, psPage);
        }

        Page currentPage = getPage(baseUri, toPage.getSiteName(), toPage.getFolderPath(), toPage.getName());

        return currentPage;
    }

    private void updateBookmarkedUsers(String pageId, List<String> users){
    	for(String u : users){
			try{
				itemService.addToMyPages(u,pageId);
			}catch(Exception e){
				log.warn("Error adding Page " + pageId + " to the Recent list for user " + u );
			}
		}
    }
    
    private void updateRecentUsers(String pageId, List<String> users){
    	for(String u : users){
			try{
				recentService.addRecentItemByUser(u, pageId);
			}catch(Exception e){
				log.warn("Error adding Page " + pageId + " to the Recent list for user " + u );
			}
		}
    }
    
    private void createNewPage(URI baseUri, Page toPage, PSPage page) throws BackendException, PSDataServiceException {
        boolean newPage = (page == null);

        if (page == null)
            page = new PSPage();

        // If required state is not set then get current state to return to.
        String endState = "Draft";
        String currentState = "Draft"; // PXA This seems mostly for debugging
                                       // purposes?
        if (!newPage)
        {
            currentState = getWorkflowState(page);
        }

        if (toPage.getWorkflow() != null && !StringUtils.isEmpty(toPage.getWorkflow().getState()))
            endState = toPage.getWorkflow().getState();
        else
            endState = currentState;

        if (!newPage && wfHelper.isItemInApproveState(idMapper.getContentId(page.getId())))
            currentState = setWorkflowState(page.getId(), "Quick Edit", new ArrayList<>());

        if (!newPage && !wfHelper.isCheckedOutToCurrentUser(page.getId()))
        {
            try {
                itemWorkflowService.forceCheckOut(page.getId());
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                throw new BackendException(e.getMessage(),e);
            }
        }

        UrlParts folderUrl = new UrlParts(toPage.getSiteName(), toPage.getFolderPath(), null);

        // set default values
        if (newPage)
        {
            page.setTitle(toPage.getName());
            page.setLinkTitle(toPage.getName());
        }

        if (toPage.getCode() != null)
        {
            if (toPage.getCode().getHead() != null)
                page.setAdditionalHeadContent(toPage.getCode().getHead());
            if (toPage.getCode().getAfterStart() != null)
                page.setAfterBodyStartContent(toPage.getCode().getAfterStart());
            if (toPage.getCode().getBeforeClose() != null)
                page.setBeforeBodyCloseContent(toPage.getCode().getBeforeClose());

        }

        if (toPage.getSeo() != null)
        {
            if (toPage.getSeo().getMetaDescription() != null)
                page.setDescription(toPage.getSeo().getMetaDescription());

            // default title to page name;
            if (toPage.getSeo().getBrowserTitle() != null)
                page.setTitle(toPage.getSeo().getBrowserTitle());

            Boolean hideSearch = toPage.getSeo().getHideSearch();
            page.setNoindex((hideSearch != null && hideSearch) ? "true" : "false");

            List<String> tags = toPage.getSeo().getTags();
            if (tags != null)
            {
                page.setTags(tags);
            }

            // toPage.getSeo().getCategories();

        }

        page.setFolderPath(folderUrl.getUrl());
        // default linktitle to name
        if (toPage.getDisplayName() != null)
            page.setLinkTitle(toPage.getDisplayName());

        if (toPage.getName() != null)
            page.setName(toPage.getName());

        String templateName = toPage.getTemplateName();

        PSTemplate template = null;
        String templateId = null;

        if (templateName != null && !templateName.isEmpty())
        {
            templateId = idMapper
                    .getString(templateService.findUserTemplateIdByName(templateName, toPage.getSiteName()));

            page.setTemplateId(templateId);
        }
        else
        {

            templateId = page.getTemplateId();
            if (StringUtils.isEmpty(templateId))
                throw new RuntimeException("templateName is required");
        }

        template = templateService.load(templateId);

        if (toPage.getDisplayName() != null)
            page.setLinkTitle(toPage.getDisplayName());

        updateRegionInfo(toPage, page, template);
        try {
            page = pageService.save(page);
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }


        if(toPage.getBookmarkedUsers()!= null && !toPage.getBookmarkedUsers().isEmpty()){
        	updateBookmarkedUsers(page.getId(), toPage.getBookmarkedUsers());
        }
        
        if(toPage.getRecentUsers()!=null && !toPage.getBookmarkedUsers().isEmpty()){
        	updateRecentUsers(page.getId(),toPage.getBookmarkedUsers());
        }

        // getRegionInfo(toPage, page, template, false);

        updateAssetInfo(baseUri, toPage, page, template);

        CalendarInfo calInfo = toPage.getCalendar();
        List<String> categories = (toPage.getSeo() == null) ? null : toPage.getSeo().getCategories();

        boolean isUpdated = false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        PSContentItem contentItem;
        try {
             contentItem = contentItemDao.find(page.getId());
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }
        Map<String, Object> fields = contentItem.getFields();
        if(calInfo != null){
            if (calInfo.getCalendars() != null)
                fields.put("page_calendar", calInfo.getCalendars());
            if (calInfo.getStartDate() != null)
                fields.put("page_start_date", dateFormat.format(calInfo.getEndDate()));
            if (calInfo.getEndDate() != null)
                fields.put("page_end_date", dateFormat.format(calInfo.getEndDate()));
            isUpdated = true;
        }
        if (toPage.getOverridePostDate() != null){
            fields.put("sys_contentpostdate", dateFormat.format(toPage.getOverridePostDate()));
            isUpdated = true;
        }
        if (categories != null){
            fields.put("page_categories_tree", categories);
            isUpdated = true;
        }
        // Setting to null or empty string is the same as auto generate.
        if(toPage.getSummary() != null){
            if (StringUtils.isNotBlank(toPage.getSummary()))
            {
                fields.put("auto_generate_summary", "0");
                fields.put("page_summary", toPage.getSummary());
            }
            else{
                fields.put("auto_generate_summary", "1");
            }
            isUpdated = true;
        }

        if(isUpdated){
            try {
                contentItemDao.save(contentItem);
            } catch (PSDataServiceException e) {
                throw new BackendException(e.getMessage(),e);
            }
            PSPageChangeHandler ph = new PSPageChangeHandler();
            PSPageChangeEvent pageChangeEvent = new PSPageChangeEvent();
            pageChangeEvent.setPageId(page.getId());
            pageChangeEvent.setType(PSPageChangeEventType.PAGE_SAVED);
            ph.pageChanged(pageChangeEvent);
        }

        boolean shouldCheckin = true;

        if (toPage.getWorkflow() != null)
        {
            shouldCheckin = !Boolean.TRUE.equals(toPage.getWorkflow().getCheckedOut());
        }

        currentState = setWorkflowState(page.getId(), endState, new ArrayList<>());

        if (wfHelper.isCheckedOutToCurrentUser(page.getId()))
        {
            try {
                itemWorkflowService.checkIn(page.getId());
            } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                throw new BackendException(e.getMessage(),e);
            }
        }
    }

    private String getWorkflowState(PSPage page)
    {
        PSItemStateTransition transitions = itemWorkflowService.getTransitions(page.getId());

        String currentState = transitions.getStateName();

        return currentState;
    }

    /**
     * 
     * Widgets to update must already have been created and saved. Widget asset
     * will not be updated if a widget name, or widget id has not been set.
     * 
     * @param toPage
     * @param page
     * @param template
     */
    private void updateAssetInfo(URI baseUri, Page toPage, PSPage page, PSTemplate template) throws BackendException, PSDataServiceException {
        if (toPage.getBody()==null)
            return;
        
        Map<String, PSRelationship> assetWidgets = getUsedPageAssets(page, template);

        PSAbstractMergedRegionTree tree = new PSMergedRegionTree(widgetService, template.getRegionTree(),
                page.getRegionBranches());

        Set<PSWidgetItem> emptyTemplateWidgets = getEmptyTemplateWidgets(template);
        HashSet<String> emptyTemplateWidgetIds = new HashSet<>();
        for (PSWidgetItem emptyTemplateWidgetId : emptyTemplateWidgets)
        {
            emptyTemplateWidgetIds.add(emptyTemplateWidgetId.getId());
        }

        for (Region region : toPage.getBody())
        {
            PSMergedRegion systemRegion = tree.getMergedRegionMap().get(region.getName());
            List<Widget> widgets = region.getWidgets();
            List<PSWidgetInstance> systemRegionWidgets = systemRegion.getWidgetInstances();

            if (widgets != null)
            {
                for (Widget widget : widgets)
                {
                
                  
                    PSWidgetItem widgetItem = findWidget(systemRegionWidgets, widget);

               
                    boolean isEditable = widgetItem != null
                            && (emptyTemplateWidgetIds.contains(widgetItem.getId()) || systemRegion.getOwner().equals(
                                    PSMergedRegionOwner.PAGE));

                    if (isEditable && widget.getAsset() != null)
                    {
                 
                        
                        String id = widgetItem.getId();

                        PSRelationship assetRels = assetWidgets.get(id);
                        Asset asset = widget.getAsset();

                        //
                        if (assetRels != null && Boolean.TRUE.equals(asset.getRemove())) 
                        {
                            try {
                                //delete asset
                                String toRemoveGuid = idMapper.getString(assetRels.getDependent());
                                if (assetRels.getConfig().getName().equals("LocalContent")) {
                                    assetService.delete(toRemoveGuid);
                                } else {
                                    // clear shared asset relationship but do not remove item
                                    PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(page.getId(),
                                            Long.parseLong(widget.getId()), widget.getType(), toRemoveGuid, 0);

                                    assetService.clearAssetWidgetRelationship(awRel);
                                }
                            } catch (PSDataServiceException | PSNotFoundException e) {
                               throw new BackendException(e.getMessage(),e);
                            }
                        }
                        
                        else 
                        {
                            boolean requestSharedAsset = (asset.getFolderPath() != null && asset.getName() != null);
                         
                            if (requestSharedAsset)
                            {
                                
                                updateSharedAsset(page, widget, assetRels, asset);
                              
                            }
                            else
                            {
                                updateLocalAsset(page, template, widget,  id, assetRels, asset);
                            }
                        }
                    } // if ediable widget
                } // end loop widgets
            } // end if widgets
        }// end for body
    }

    private void updateLocalAsset(PSPage page, PSTemplate template, Widget widget,
            String id, PSRelationship assetRels, Asset asset) throws BackendException {
        // LOCAL ASSET
        
        HashMap<String, String> fields = null;
        String guidString = null;
        
        if (asset.getFields() != null)
        {

            fields = new HashMap<>();
            for (Entry<String, String> field : asset.getFields().entrySet())
            {
                fields.put(field.getKey(), field.getValue());
            }

            if (assetRels == null)
            {

                // Re Associate to unused asset if one
                // exists with same widget name.
                Set<PSOrphanedAssetSummary> unusedAssets = PSPreviewPageUtils
                        .getOrphanedAssetsSummaries(page, template);
                Iterator<PSOrphanedAssetSummary> iterator = unusedAssets.iterator();
                boolean foundOrphan = false;
                
                while (iterator.hasNext())
                {

                    PSOrphanedAssetSummary orphan = iterator.next();
                    if (StringUtils.equals(widget.getName(), orphan.getName())
                            && StringUtils.equals(widget.getType(), orphan.getType()))
                    {
                        foundOrphan = true;
                        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(
                                page.getId(), Long.parseLong(widget.getId()), widget.getType(),
                                orphan.getId(), 0);
                        try {
                            assetService.createAssetWidgetRelationship(awRel);

                            guidString = orphan.getId();
                            PSAsset assetItem = assetService.load(guidString, true);
                            assetItem.getFields().putAll(fields);
                            PSAsset savedAsset = assetService.save(assetItem);
                            guidString = savedAsset.getId();
                        } catch (PSDataServiceException e) {
                            throw new BackendException(e.getMessage(),e);
                        }
                    }
                }

                if (!foundOrphan)
                    guidString = createAndAssociateLocalAsset(page.getId(), id, widget.getType(),
                            fields);
            }
            else  // existing asset
            {
                try {
                    guidString = idMapper.getString(assetRels.getDependent());
                    PSAsset assetItem = assetService.load(guidString, true);
                    itemWorkflowService.checkOut(guidString);
                    assetItem.getFields().putAll(fields);
                    PSAsset savedAsset = assetService.save(assetItem);
                    guidString = savedAsset.getId();
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
                    throw new BackendException(e.getMessage(),e);
                }
            }
            
            if (guidString!=null) {
                try {
                    itemWorkflowService.checkIn(guidString);
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    throw new BackendException(e.getMessage(),e);
                }
            }
           
        }
       
    }

    private void updateSharedAsset(PSPage page, Widget widget, PSRelationship assetRels, Asset asset) throws BackendException {
        // existing asset.
        PSPathItem pathItem = null;
        boolean pathNotFound = true;
        String fullPath = asset.getFolderPath() + "/" + asset.getName();
        try
        {
            pathItem = pathService.find(fullPath);
            pathNotFound = pathItem==null;
        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            // We rethrow if the path not found later.
        }

        try {
            if (assetRels == null) {
                // new shared asset
                if (pathNotFound)
                    throw new AssetNotFoundException();
                PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(page.getId(),
                        Long.parseLong(widget.getId()), widget.getType(), pathItem.getId(), 0);
                awRel.setResourceType(PSAssetResourceType.shared);
                assetService.createAssetWidgetRelationship(awRel);
            } else if (assetRels.getConfig().getName().equals("LocalContent")) {
                if (!pathNotFound)
                    throw new BackendException("Cannot convert local asset to shared asset, asset already exists at " + fullPath);
                PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(page.getId(),
                        Long.parseLong(widget.getId()), widget.getType(), idMapper.getString(assetRels.getDependent()), 0);
                assetService.shareLocalContent(asset.getName(), StringUtils.substringAfter(asset.getFolderPath(), "/"),
                        awRel);

            } else {
                if (idMapper.getContentId(pathItem.getId()) != assetRels.getDependent().getId()) {
                    if (pathNotFound)
                        throw new AssetNotFoundException();

                    PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(page.getId(),
                            Long.parseLong(widget.getId()), widget.getType(), pathItem.getId(), 0);
                    awRel.setResourceType(PSAssetResourceType.shared);
                    awRel.setReplacedRelationshipId(assetRels.getId());
                    assetService.updateAssetWidgetRelationship(awRel);
                }

                // else same so nothing to do.

            }
        } catch (PSDataServiceException e) {
            throw new BackendException(e.getMessage(),e);
        }
    }
    public IPSPathService getPathService()
    {
        return pathService;
    }

    public void setPathService(IPSPathService pathService)
    {
        this.pathService = pathService;
    }

    @Override
    public void deletePage(URI baseUri, String siteName, String path, String pageName) throws BackendException {
        checkAPIPermission();

        UrlParts url = new UrlParts(siteName, path, pageName);
        String pathServicePath = StringUtils.substring(url.getUrl(), 1);
        PSPathItem pathItem = null;
        try
        {
            pathItem = pathService.find(pathServicePath);

        }
        catch (IPSPathService.PSPathServiceException | PSDataServiceException e)
        {
            // Change to say that path is not a page
            throw new PageNotFoundException();
        }

        if (!pathItem.getType().equals("percPage"))
        {
            throw new PageNotFoundException();
        }

        try {
            pageService.delete(pathItem.getId());
        } catch (PSValidationException e) {
            throw new BackendException(e);
        }
    }

    @Override
    public Page getPage(URI baseUri, String id) throws BackendException {
        checkAPIPermission();

        try {

            Page page = new Page();

            PSPage psPage = pageService.load(id);

            if (psPage == null)
                throw new PageNotFoundException(id);

            PSItemProperties itemProperties = pathService.findItemProperties(psPage.getFolderPath());
            PSComponentSummary summ = wfHelper.getComponentSummary(itemProperties.getId());

            page.setName(psPage.getName());

            PSSiteSummary site = siteDataService.findByPath(psPage.getFolderPath() + psPage.getName());
            page.setSiteName(site.getName());
            page.setFolderPath(PSPathUtils.getBaseFolderFromPath(psPage.getFolderPath()));
            page.setId(psPage.getId());
            page.setDisplayName(psPage.getLinkTitle());

            // Only set if auto flag off. page.setSummary(psPage.getSummary());

            PSTemplate template = templateService.load(psPage.getTemplateId());

            page.setTemplateName(template.getName());

            PSWorkflow wf = loadWorkflow(psPage.getWorkflowId());
            WorkflowInfo wfInfo = new WorkflowInfo();
            page.setWorkflow(wfInfo);
            wfInfo.setName(wf.getName());

            wfInfo.setCheckedOutUser(StringUtils.defaultString(summ.getCheckoutUserName()));
            if (summ.getCheckoutUserName() != null && !summ.getCheckoutUserName().isEmpty())
                wfInfo.setCheckedOut(true);
            else
                wfInfo.setCheckedOut(false);

            wfInfo.setState(itemProperties.getStatus());

            SeoInfo seo = new SeoInfo();

            seo.setBrowserTitle(psPage.getTitle());

            // Other fields from internal content type

            // Find the content item for the given id
            PSContentItem contentItem = contentItemDao.find(psPage.getId());
            Map<String, Object> fields = contentItem.getFields();
            CalendarInfo calInfo = new CalendarInfo();
            page.setCalendar(calInfo);
            List<String> calendarsValue = (List<String>) fields.get("page_calendar");
            calInfo.setCalendars(calendarsValue);
            String startDateStr = (String) fields.get("page_start_date");
            Date startDate = null;
            if (StringUtils.isNotEmpty(startDateStr)) {
                startDate = PSDateUtils.parseSystemDateString(startDateStr);
            }
            calInfo.setStartDate(startDate);

            String endDateStr = (String) fields.get("page_end_date");
            Date endDate = null;
            if (StringUtils.isNotEmpty(endDateStr)) {
                endDate = PSDateUtils.parseSystemDateString(endDateStr);
            }

            calInfo.setEndDate(endDate);

            String overridePostDateStr = (String) fields.get("sys_contentpostdate");
            Date overridePostDate = null;
            if (StringUtils.isNotEmpty(overridePostDateStr)) {
                overridePostDate = PSDateUtils.parseSystemDateString(overridePostDateStr);
            }

            page.setOverridePostDate(overridePostDate);

            List<String> pageCategories = (List<String>) fields.get("page_categories_tree");
            if (pageCategories != null)
                seo.setCategories(pageCategories);

            String autoGenerate = (String) fields.get("auto_generate_summary");

            if ("1".equals(autoGenerate)) {
                page.setSummary("");
            } else {
                page.setSummary((String) fields.get("page_summary"));
            }

            boolean hideSearch = (psPage.getNoindex() != null && psPage.getNoindex().equalsIgnoreCase("true"));
            seo.setHideSearch(hideSearch);
            seo.setMetaDescription(psPage.getDescription());
            seo.setTags(new ArrayList<>(psPage.getTags()));

            page.setSeo(seo);

            CodeInfo code = new CodeInfo();
            page.setCode(code);
            code.setAfterStart(psPage.getAfterBodyStartContent());
            code.setBeforeClose(psPage.getBeforeBodyCloseContent());
            code.setHead(psPage.getAdditionalHeadContent());

            getRegionInfo(baseUri, page, psPage, template);

            // page.setBody(body);

            // TODO: Populate Page
            return page;
        } catch (PSDataServiceException | IPSPathService.PSPathServiceException e) {
           throw  new BackendException(e.getMessage(),e);
        }
    }

    public String createAndAssociateLocalAsset(String pageId, String widgetId, String widgetDefId,
            Map<String, String> setFields) throws BackendException {
        try {
            PSWidgetDefinition widgetDef = widgetService.load(widgetDefId);
            PSAsset asset = new PSAsset();
            PSAsset newAsset = null;
            // some assets do not have supporting type we currently do not update
            // widget properties
            if (widgetDef.getWidgetPrefs() != null && !StringUtils.isEmpty(widgetDef.getWidgetPrefs().getContenttypeName())) {
                asset.setType(widgetDef.getWidgetPrefs().getContenttypeName());
                String newName = nameGenerator.generateLocalContentName();
                asset.setName(newName);
                Map<String, Object> fields = asset.getFields();
                // Only currently support single string value fields in local asset
                for (Entry<String, String> field : setFields.entrySet()) {
                    fields.put(field.getKey(), field.getValue());
                }
                fields.putAll(setFields);
                fields.put(IPSHtmlParameters.SYS_WORKFLOWID, "" + itemWorkflowService.getLocalContentWorkflowId());
                fields.put(IPSHtmlParameters.SYS_TITLE, newName);
                newAsset = assetService.save(asset);
                PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, Long.parseLong(widgetId),
                        widgetDefId, newAsset.getId(), 0);
                assetService.createAssetWidgetRelationship(awRel);
            }
            if (newAsset != null)
                return newAsset.getId();
            else
                return null;
        } catch (PSDataServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException e) {
            throw new BackendException(e.getMessage(), e);
        }
    }

	private int recursivelyWorkflowAllPages(WorkflowStates state, int counter, String path) throws IPSPathService.PSPathServiceException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
		int ctr = counter;
		
		if(path == null)
			path = "";
		
		PSPathItem pi = pathService.find(path);
		
		Set<String> itemList = new HashSet<>();
		if(pi != null){
		
			List<PSPathItem> children = pathService.findChildren(path);
			
			for(PSPathItem child: children){
				if(child.isFolder()){
					ctr += recursivelyWorkflowAllPages(state, ctr,path + "/" + child.getName());
				}else if( child.isPage() && ! wfHelper.isArchived(child.getId())){
					this.itemWorkflowService.checkIn(child.getId());
					itemList.add(child.getId());
					ctr++;
			}
		}
			if(state.equals(WorkflowStates.APPROVE))
				wfHelper.transitionToPending(itemList);
			else if(state.equals(WorkflowStates.ARCHIVE))
				wfHelper.transitionToArchive(itemList);
			else if(state.equals(WorkflowStates.REVIEW))
				wfHelper.transitionToReview(itemList);
				
	}
		
		return ctr;
	}
	
	@Override
	public int approveAllPages(URI baseUri, String folderPath) throws BackendException {
        try {
            int counter = 0;

            if (folderPath == null)
                folderPath = "";

            String path = folderPath;

            counter = recursivelyWorkflowAllPages(WorkflowStates.APPROVE, 0, path);
            return counter;
        } catch (IPSPathService.PSPathServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public Page changePageTemplate(URI baseUri, Page p) throws BackendException {
        try {
            Page ret = null;

            if (p != null && p.getTemplateName() != null && !p.getTemplateName().isEmpty() && p.getId() != null && !p.getId().isEmpty()) {

                if (!wfHelper.isCheckedOutToCurrentUser(p.getId())) {
                    itemWorkflowService.forceCheckOut(p.getId());
                }

                String templateName = p.getTemplateName();
                String templateId = null;


                templateId = idMapper
                        .getString(templateService.findUserTemplateIdByName(templateName, p.getSiteName()));

                if ((templateId != null) && p.getId() != null) {
                    ArrayList<String> pageIds = new ArrayList<>();
                    pageIds.add(p.getId());
                    try {
                        migrationService.migrateContentOnTemplateChange(templateId, null, pageIds);
                    } catch (PSContentMigrationException e) {
                        log.error("An error occurred change Page " + p.getId() + " to template " + templateName, e);
                        throw new ContentMigrationException();
                    } catch (PSDataServiceException e) {
                        throw new BackendException(e.getMessage(), e);
                    }

                } else {
                    throw new TemplateNotFoundException(p.getTemplateName());
                }

            } else {
                throw new PageNotFoundException();
            }

            if (wfHelper.isCheckedOutToCurrentUser(p.getId())) {
                itemWorkflowService.checkIn(p.getId());
            }
            ret = getPage(baseUri, p.getId());

            return ret;
        } catch (IPSItemWorkflowService.PSItemWorkflowServiceException | BackendException | PSValidationException | IPSDataService.DataServiceLoadException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public List<String> allPagesReport(URI baseUri, String siteFolderPath) throws BackendException {
			checkAPIPermission();
			 
			List<String> ret = new ArrayList<>();
			List<PSPageReportLine> pages;
			
			try {
				pages = pageService.findAllPages(siteFolderPath);
		
			 for(PSPageReportLine row : pages){
	       	  String csvData = row.toCSVRow();
	       	  
	       	  if(ret.size()==0)
	 			  ret.add(row.getHeaderRow());
	       	
	       	  if(csvData != null)
	       		  ret.add(row.toCSVRow());
	         }
			} catch (PSReportFailedToRunException | PSDataServiceException e) {
				log.error("An error occurred while running the All Pages Report", e);
		        throw new BackendException(e);
			}
				
			return ret;

	}

	@Override
	public int archiveAllPages(URI baseUri, String folderPath) throws BackendException {
		try {
            int counter = 0;

            if (folderPath == null)
                folderPath = "";

            String path = folderPath;

            counter = recursivelyWorkflowAllPages(WorkflowStates.ARCHIVE, 0, path);
            return counter;
        } catch (IPSPathService.PSPathServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new BackendException(e);
        }
    }

	@Override
	public int submitForReviewAllPages(URI baseUri, String folderPath) throws BackendException {
		try {
            int counter = 0;

            if (folderPath == null)
                folderPath = "";

            String path = folderPath;

            counter = recursivelyWorkflowAllPages(WorkflowStates.REVIEW, 0, path);
            return counter;
        } catch (IPSPathService.PSPathServiceException | IPSItemWorkflowService.PSItemWorkflowServiceException | PSDataServiceException e) {
            throw new BackendException(e);
        }
    }

}
