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
package com.percussion.sitemanage.importer.helpers.impl;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.IPSHtmlMetadata;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Helper used to extract the metadata and save it into the page.
 * 
 * @author Santiago M. Murchio
 * 
 */
@Component("pageMetadataExtractorHelper")
@Lazy
public class PSPageMetadataExtractorHelper extends PSGenericMetadataExtractorHelper
{
    private IPSPageService pageService;

    private IPSPageCatalogService catalogService;

    private IPSItemWorkflowService itemWorkflowService;

    private static HashMap<String, PSTemplate> unassignedTemplateCache = new HashMap<>();

    @Autowired
    public PSPageMetadataExtractorHelper(IPSTemplateService templateService, IPSPageService pageService,
            IPSPageCatalogService catalogService, IPSItemWorkflowService itemWorkflowService)
    {
        super(templateService);
        this.pageService = pageService;
        this.catalogService = catalogService;
        this.itemWorkflowService = itemWorkflowService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.importer.helpers.impl.
     * PSGenericMetadataExtractorHelper
     * #saveTargetItem(com.percussion.pagemanagement.data.IPSHtmlMetadata)
     */
    @Override
    protected void saveTargetItem(IPSHtmlMetadata targetItem) throws IPSItemWorkflowService.PSItemWorkflowServiceException, PSDataServiceException {
        PSPage page = (PSPage) targetItem;
        // the target Item in this case is a PSPage
        
        long workflowTimer = System.nanoTime();
                
        itemWorkflowService.checkOut(page.getId());
        pageService.save(page);
        itemWorkflowService.checkIn(page.getId());
        
        PSHelperPerformanceMonitor.updateStats("PSPageMetaDataExtractor:PageWorkflow", ((System.nanoTime() - workflowTimer)/1000000));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.importer.helpers.impl.
     * PSGenericMetadataExtractorHelper
     * #addHtmlWidgetToTemplate(com.percussion.sitemanage.data.PSSiteImportCtx)
     */
    @Override
    protected void addHtmlWidgetToTemplate(PSSiteImportCtx context) throws PSDataServiceException, PSSiteImportException {
        PSTemplate template = unassignedTemplateCache.get(context.getSite().getName());

        if (template == null)
        {
            if (unassignedTemplateCache.size() > 5)
            {
                unassignedTemplateCache.clear();
            }
            
            // Load site's home page template
            template = templateService.load(catalogService.getCatalogTemplateIdBySite(context.getSite()
                    .getName()));
            // Set Theme (Only first time)
            if (isBlank(template.getTheme()))
            {
                template.setTheme(context.getThemeSummary().getName());
            }
            

            // add the widget only if it is not already created
            if (isEmpty(template.getWidgets()))
            {
                // Create Raw HTML widget and add the widget to the template
                PSWidgetItem rawHtmlWidget = PSPageManagementUtils.createRawHtmlWidgetItem("1");
                template.getRegionTree().setRegionWidgets(REGION_CONTENT, asList(rawHtmlWidget));

                context.getLogger().appendLogMessage(PSLogEntryType.STATUS, ADD_HTML_WIDGET,
                        "The HTML widget was successfully added to the Unassigned template.");

                // Save template and finish
                templateService.save(template);

                context.getLogger().appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                        "Metadata was successfully saved to the Unassigned template.");
            }
            unassignedTemplateCache.put(context.getSite().getName(), template);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.importer.helpers.impl.
     * PSGenericMetadataExtractorHelper
     * #getTargetItem(com.percussion.sitemanage.data.PSSiteImportCtx)
     */
    @Override
    protected IPSHtmlMetadata getTargetItem(PSSiteImportCtx context) throws IPSDataService.DataServiceLoadException, PSValidationException, IPSDataService.DataServiceNotFoundException {
        return pageService.find(context.getCatalogedPageId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.importer.helpers.impl.PSImportHelper#
     * getHelperMessage()
     */
    @Override
    public String getHelperMessage()
    {
        return "changing cataloged page information";
    }
}
