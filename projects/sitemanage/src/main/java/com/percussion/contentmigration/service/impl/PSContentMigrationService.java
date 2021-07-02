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

package com.percussion.contentmigration.service.impl;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.assetmanagement.data.PSAssetDropCriteria;
import com.percussion.assetmanagement.data.PSAssetWidgetRelationship;
import com.percussion.assetmanagement.data.PSOrphanedAssetSummary;
import com.percussion.assetmanagement.service.IPSAssetService;
import com.percussion.assetmanagement.service.impl.PSPreviewPageUtils;
import com.percussion.contentmigration.converters.IPSContentMigrationConverter;
import com.percussion.contentmigration.rules.IPSContentMigrationRule;
import com.percussion.contentmigration.service.IPSContentMigrationService;
import com.percussion.contentmigration.service.PSContentMigrationException;
import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.assembler.IPSRenderAssemblyBridge;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSPageTemplateService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.queue.IPSPageImportQueue;
import com.percussion.services.catalog.PSTypeEnum;
import com.percussion.services.guidmgr.IPSGuidManager;
import com.percussion.services.guidmgr.PSGuidManagerLocator;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSNameGenerator;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.util.IPSHtmlParameters;
import com.percussion.utils.service.impl.PSJsoupUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PSContentMigrationService implements IPSContentMigrationService
{
    //Other services
    private IPSRenderAssemblyBridge renderAssemblyBridge;
    private IPSPageService pageService;
    private IPSTemplateService templateService;
    private IPSAssetService assetService;
    private IPSItemWorkflowService itemWorkflowService;
    private IPSNameGenerator nameGenerator;
    private IPSPageImportQueue pageImportQueue;
    private IPSGuidManager guidMgr;
    //Memebers
    private List<IPSContentMigrationRule> migrationRules;
    private List<IPSContentMigrationConverter> migrationConverters;
    private Map<String, IPSContentMigrationConverter> converterMap = new HashMap<>();
    private IPSPageTemplateService pageTemplateService;
    
    
    public PSContentMigrationService(IPSPageService pageService, IPSRenderAssemblyBridge renderAssemblyBridge,
            IPSTemplateService templateService, IPSAssetService assetService, IPSItemWorkflowService itemWorkflowService,
            IPSNameGenerator nameGenerator, IPSPageImportQueue pageImportQueue, IPSPageTemplateService pageTemplateService)
    {
        super();
        this.pageService = pageService;
        this.renderAssemblyBridge = renderAssemblyBridge;
        this.templateService = templateService;
        this.assetService = assetService;
        this.itemWorkflowService = itemWorkflowService;
        this.nameGenerator = nameGenerator;
        this.pageImportQueue = pageImportQueue;
        guidMgr = PSGuidManagerLocator.getGuidMgr();
        this.pageTemplateService = pageTemplateService;
    }    
    
    @Override
    public void migrateContent(String siteName, String templateId, String refPageId, List<String> pageIds)
            throws PSContentMigrationException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        Validate.notEmpty(templateId, "templateId must not be empty for content migration");
        Validate.notEmpty(pageIds, "newPageIds must not be empty for content migration");
        migrateContentOnTemplateChange(templateId,refPageId, pageIds);
        //If sitename is not blank remove the page from unassigned queue.
        if(StringUtils.isNotBlank(siteName))
        {
            for (String pageId : pageIds)
            {
                pageImportQueue.removeImportPage(siteName, pageId);
            }
        }
    }

    @Override
    public void migrateContentOnTemplateChange(String templateId, String referencePageId, List<String> newPageIds)
            throws PSContentMigrationException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        Map<String,String> failedItems = new HashMap<>();
        Document refDoc = getReferenceDocument(templateId, referencePageId);
        PSTemplate template = templateService.load(templateId);
        for (String pageId : newPageIds)
        {
            if(itemWorkflowService.isCheckedOutToSomeoneElse(pageId))
            {
                failedItems.put(pageId, "Failed to process, the page is being edited by someone else.");
                continue;
            }
            
            //Check out the item if it is not checked out to the current user
            boolean checkedout = false;
            if(!itemWorkflowService.isCheckedOutToCurrentUser(pageId))
            {
                try {
                    itemWorkflowService.checkOut(pageId);
                    checkedout = true;
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    log.warn(e.getMessage());
                }
            }
            pageTemplateService.changeTemplate(pageId, templateId);
            //Find applicable widgets
            List<ApplicableWidget> applicableWidgets = findEmptyWidgets(templateId, pageId);
            if(applicableWidgets.isEmpty())
            {
                log.debug("Could not find any applicable widgets skipping migration process.");
            }
            else
            {
                PSPage page = pageService.load(pageId);
                findMatchingContent(page, template, refDoc, applicableWidgets);
                updatePage(pageId, templateId, applicableWidgets);                
            }

            
            //If the item has been checked out by this process, then check it in.
            if(checkedout)
            {
                try {
                    itemWorkflowService.checkIn(pageId);
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        if(!failedItems.isEmpty())
        {
            PSContentMigrationException cme = new PSContentMigrationException();
            cme.setFailedItems(failedItems);
            throw cme;
        }
    }

    @Override
    public void migrateSameTemplateChanges(String templateId, List<String> pageIds)
            throws PSContentMigrationException, PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        Map<String,String> failedItems = new HashMap<>();
        Document refDoc = getReferenceDocument(templateId, null);
        if(pageIds == null)
        {
            pageIds = getTemplatePages(templateId);
        }
        PSTemplate template = templateService.load(templateId);
        for (String pageId : pageIds)
        {
            PSPage page = pageService.load(pageId);
            //FB: ES_COMPARING_STRINGS_WITH_EQ  NC 1-16-16
            if(template.getContentMigrationVersion().equals(page.getTemplateContentMigrationVersion()))
            {
                log.info("Both template {} and page {} have same version skipping content migration.",templateId,pageId);
            }
            if(itemWorkflowService.isCheckedOutToSomeoneElse(pageId))
            {
                failedItems.put(pageId, "Failed to process, the page is being edited by someone else.");
                continue;
            }
            
            //Check out the item if it is not checked out to the current user
            boolean checkedout = false;
            if(!itemWorkflowService.isCheckedOutToCurrentUser(pageId))
            {
                try {
                    itemWorkflowService.checkOut(pageId);
                    checkedout = true;
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    log.warn(e.getMessage());
                }
            }
            //Find applicable widgets
            List<ApplicableWidget> applicableWidgets = findEmptyWidgets(templateId, pageId);
            if(!applicableWidgets.isEmpty())
            {
                findMatchingContent(page, template, refDoc, applicableWidgets);
                updatePage(pageId, templateId, applicableWidgets);
            }
            else
            {
                log.debug("Could not find any applicable widgets skipping migration process for page {}" ,pageId);
                pageService.updateMigrationEmptyWidgetFlag(pageId, false);
            }
            pageService.updateTemplateMigrationVersion(pageId);
            //If the item has been checked out by this process, then check it in.
            if(checkedout)
            {
                try {
                    itemWorkflowService.checkIn(pageId);
                } catch (IPSItemWorkflowService.PSItemWorkflowServiceException e) {
                    log.warn(e.getMessage());
                }
            }
        }
        if(!failedItems.isEmpty())
        {
            PSContentMigrationException cme = new PSContentMigrationException();
            cme.setFailedItems(failedItems);
            throw cme;
        }
        
    }

    @Override
    public List<String> getTemplatePages(String templateId) throws IPSPageService.PSPageException {
        List<String> pageIds = new ArrayList<>();
        List<Integer>pgIds = pageTemplateService.findPageIdsByTemplate(templateId);
        for (Integer pgId : pgIds)
        {
            pageIds.add(guidMgr.makeGuid(pgId, PSTypeEnum.LEGACY_CONTENT).toString());
        }
        return pageIds;
    }
    
    /**
     * Gets the unused content from the unused asset, if the type is either html or rich text asset.
     * TODO: this needs to be built similar to the content converters.
     * @param unusedAsset assumed not <code>null</code>
     * @return String content from the unused asset, may be null or empty.
     */
    private String getUnUsedContent(PSOrphanedAssetSummary unusedAsset) throws PSDataServiceException {
        PSAsset asset = assetService.load(unusedAsset.getId());
        String content = null;
        if(asset.getType().equalsIgnoreCase("percRawHtmlAsset"))
        {
            content = (String) asset.getFields().get("html");
        }
        else if(asset.getType().equalsIgnoreCase("percRichTextAsset"))
        {
            content = (String) asset.getFields().get("text");
        }
        return content;
    }

    /**
     * Finds the matching content using orphaned assets from page as the base for content matching.
     * @param page assumed not <code>null</code>.
     * @param template assumed not <code>null</code>
     * @param refDoc assumed not <code>null</code>
     * @param applicableWidgets assumed not <code>null</code>
     */
    private void findMatchingContent(PSPage page, PSTemplate template, Document refDoc, List<ApplicableWidget> applicableWidgets) throws IPSDataService.DataServiceLoadException, IPSDataService.DataServiceNotFoundException {
        List<Document> unusedDocuments = new ArrayList<>();
        Set<PSOrphanedAssetSummary> unusedAssets = PSPreviewPageUtils.getOrphanedAssetsSummaries(page, template);
        for (PSOrphanedAssetSummary unusedAsset : unusedAssets)
        {
            String content=null;
            try {
                content = getUnUsedContent(unusedAsset);
            } catch (PSDataServiceException e) {
                log.error(e.getMessage());
                log.debug(e.getMessage(),e);
            }
            if(StringUtils.isNotBlank(content))
            {
                Document doc = Jsoup.parseBodyFragment(content);
                unusedDocuments.add(doc);
            }
        }
        for (Document unusedDocument : unusedDocuments)
        {
            findMatchingContent(refDoc, unusedDocument, applicableWidgets);
            //@TODO optimize if all the widgets are filled then no need to loop through it, it doesn't hurt much as findMatchingContent avoids the widgets if they are filled.
        }
    }
    
    /**
     * Returns the Jsoup document of assembled page if the reference page is not blank otherwise assembled template.
     * @param templateId assumed to be a valid template id.
     * @param refPageId may be blank.
     * @return Document never <code>null</code>.
     * @throws PSContentMigrationException in case of error creating the reference document.
     */
    private Document getReferenceDocument(String templateId, String refPageId) throws PSContentMigrationException
    {
        Document refDoc = null;
        try
        {
            if(StringUtils.isNotBlank(refPageId))
            {
                String refPageHtml = renderAssemblyBridge.renderPage(refPageId, true, false);
                refDoc = Jsoup.parseBodyFragment(refPageHtml);
            }
            else
            {
                String refTemplateHtml = renderAssemblyBridge.renderTemplate(templateId, true);
                refDoc = Jsoup.parseBodyFragment(refTemplateHtml);
            }
        }
        catch(Exception e)
        {
            log.error(e);
            throw new PSContentMigrationException("Failed to migrate content, see log for more details.");
        }
        return refDoc;
    }
    
    /**
     * Finds empty page widgets whose widget type has a registered converter.
     * @param templateId assumed not <code>null</code>
     * @param pageId assumed not <code>null</code>
     * @return List of empty widgets, may be empty but never <code>null</code>.
     */
    private List<ApplicableWidget> findEmptyWidgets(String templateId, String pageId) throws PSDataServiceException {
        List<PSAssetDropCriteria> tplAssetDropCriteria = assetService.getWidgetAssetCriteria(templateId, false);
        List<ApplicableWidget> applicableWidgets = new ArrayList<>();
        List<String> tplWidgetIds = new ArrayList<>();
        List<String> tplContentWidgetIds = new ArrayList<>();
        for (PSAssetDropCriteria adc : tplAssetDropCriteria)
        {
            tplWidgetIds.add(adc.getWidgetId());
            if(adc.getExistingAsset()) {
                tplContentWidgetIds.add(adc.getWidgetId());
            }
                
        }
        List<PSAssetDropCriteria> pageAssetDropCriteria = assetService.getWidgetAssetCriteria(pageId, true);
        for (PSAssetDropCriteria adc : pageAssetDropCriteria)
        {
            //If the widget is from template and it has content skip it. 
            if(tplContentWidgetIds.contains(adc.getWidgetId())) {
                continue;
            }
            List<String> wc = new ArrayList<>(converterMap.keySet());
            wc.retainAll(adc.getSupportedCtypes());
            if(!adc.getExistingAsset() && converterMap.size() == wc.size() + adc.getSupportedCtypes().size())
            {
                applicableWidgets.add(createApplicableWidget(adc, null, !tplWidgetIds.contains(adc.getWidgetId())));
            }
        }
        return applicableWidgets;
    }
    
    /**
     * Helper method to create an applicable widget from the supplied PSAssetDropCriteria
     * @param adc assumed not <code>null</code>.
     * @param refDoc assumed not <code>null</code>.
     * @param isPageWidget boolean flag to indicate whether the widget is on page or template.
     * @return ApplicableWidget never <code>null</code>
     */
    private ApplicableWidget createApplicableWidget(PSAssetDropCriteria adc, Document refDoc, boolean isPageWidget)
    {
        ApplicableWidget applicableWidget = new ApplicableWidget();
        applicableWidget.widgetId = adc.getWidgetId();
        if(refDoc != null)
        {
            Element regElem = PSJsoupUtils.closestParentByClass(
                    refDoc,
                    PSJsoupUtils.generateAttributeSelector(IPSContentMigrationRule.ATTR_WIDGET_ID,
                            adc.getWidgetId()), IPSContentMigrationRule.CLASS_PERC_REGION);
            applicableWidget.regionId = regElem.id();
        }
        applicableWidget.widgetDefId = adc.getSupportedCtypes().get(0);
        applicableWidget.isPageWidget = isPageWidget;
        return applicableWidget;
    }

    /**
     * Finds the matching content based on the registered rules then converts the content using the content converter which results in a fields map.
     * Sets the fields map on to applicable widget. If the content is not found then the fields will be <code>null</code>.
     * 
     * @param refDoc assumed not <code>null</code>.
     * @param targetPageDoc assumed not <code>null</code>.
     * @param applicableWidgets assumed not <code>null</code>.
     */
    private void findMatchingContent(Document refDoc, Document targetPageDoc, List<ApplicableWidget> applicableWidgets)
    {
        for (ApplicableWidget widget : applicableWidgets)
        {
            if(widget.fields == null)
            {
                for (IPSContentMigrationRule rule : migrationRules)
                {
                    String content = rule.findMatchingContent(widget.widgetId, refDoc, targetPageDoc);
                    if(content!=null)
                    {
                        IPSContentMigrationConverter converter = converterMap.get(widget.widgetDefId);
                        Map<String, Object> fields = converter.convert(content);
                        widget.fields = fields;
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Changes the page template and for each applicable widget creates local asset and associates it to the page.
     * @param pageId assumed not <code>null</code>.
     * @param templateId assumed not <code>null</code>.
     * @param applicableWidgets assumed not <code>null</code> and empty.
     */
    private void updatePage(String pageId, String templateId, List<ApplicableWidget> applicableWidgets) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        boolean hasEmptyWidgets = false;
        //if the source of the widget is page, add widget to the template region
        for (ApplicableWidget widget : applicableWidgets)
        {
            if(widget.isPageWidget)
            {
                //@TODO Create a widget on page in the specified region
            }
            
            if(widget.fields != null && !widget.fields.isEmpty()) {
                createAndAssociateAsset(pageId, widget);
            }
            else {
                hasEmptyWidgets = true;
            }
        }
        pageService.updateMigrationEmptyWidgetFlag(pageId, hasEmptyWidgets);
        
    }

    /**
     * Creates a local asset and adds it to the page through the supplied widget. Uses local content workflow and generated name for local content.
     * @param pageId assumed not <code>null</code>.
     * @param applicableWidget assumed not <code>null</code>.
     */
    public void createAndAssociateAsset(String pageId, ApplicableWidget applicableWidget) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException {
        PSAsset asset = new PSAsset();
        asset.setType(converterMap.get(applicableWidget.widgetDefId).getWidgetContentType());
        String newName = nameGenerator.generateLocalContentName();
        asset.setName(newName);
        Map<String, Object> fields = asset.getFields();
        fields.putAll(applicableWidget.fields);
        fields.put(IPSHtmlParameters.SYS_WORKFLOWID, "" + itemWorkflowService.getLocalContentWorkflowId());
        fields.put(IPSHtmlParameters.SYS_TITLE, newName);
        PSAsset newAsset = assetService.save(asset);
        PSAssetWidgetRelationship awRel = new PSAssetWidgetRelationship(pageId, Long.parseLong(applicableWidget.widgetId), 
                applicableWidget.widgetDefId, newAsset.getId(), 0);
        assetService.createAssetWidgetRelationship(awRel);
    }
    
    /**
     * Data structure object to hold applicable widget information. 
     * All the members are filled in findApplicableWidgets 
     */
    private class ApplicableWidget
    {
        String widgetId;
        String regionId;
        String widgetDefId;
        boolean isPageWidget; 
        Map<String, Object> fields;
    }

    public List<IPSContentMigrationRule> getMigrationRules()
    {
        return migrationRules;
    }

    public void setMigrationRules(List<IPSContentMigrationRule> migrationRules)
    {
        this.migrationRules = migrationRules;
    }

    public List<IPSContentMigrationConverter> getMigrationConverters()
    {
        return migrationConverters;
    }

    public void setMigrationConverters(List<IPSContentMigrationConverter> migrationConverters)
    {
        this.migrationConverters = migrationConverters;
        for (IPSContentMigrationConverter converter : migrationConverters)
        {
            converterMap.put(converter.getWidgetContentType(), converter);
        }
    }
    
    private static final Logger log = LogManager.getLogger(PSContentMigrationService.class);


}
