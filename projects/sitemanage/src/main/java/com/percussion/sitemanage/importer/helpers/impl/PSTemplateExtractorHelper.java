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

import static java.util.Arrays.asList;

import com.percussion.pagemanagement.data.IPSHtmlMetadata;
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.data.PSWidgetItem;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * @author LucasPiccoli
 * 
 */
@Component("templateExtractorHelper")
@Lazy
public class PSTemplateExtractorHelper extends PSGenericMetadataExtractorHelper
{

    private final String STATUS_MESSAGE = "changing template information";

    @Autowired
    public PSTemplateExtractorHelper(IPSTemplateService templateService)
    {
        super(templateService);
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.impl.PSGenericMetadataExtractorHelper#getTargetItem(com.percussion.sitemanage.data.PSSiteImportCtx)
     */
    @Override
    protected IPSHtmlMetadata getTargetItem(PSSiteImportCtx context) throws PSDataServiceException {
        // Load site's home page template
        return templateService.load(context.getTemplateId());
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.impl.PSGenericMetadataExtractorHelper#addHtmlWidgetToTemplate(com.percussion.sitemanage.data.PSSiteImportCtx)
     */
    @Override
    protected void addHtmlWidgetToTemplate(PSSiteImportCtx context) throws PSDataServiceException {
        // Load site's home page template
        PSTemplate template = templateService.load(context.getTemplateId());
        
        // Set Theme
        template.setTheme(context.getThemeSummary().getName());
        
        // Create Raw HTML widget and add the widget to the template
        PSWidgetItem rawHtmlWidget = PSPageManagementUtils.createRawHtmlWidgetItem("1");
        template.getRegionTree().setRegionWidgets(REGION_CONTENT, asList(rawHtmlWidget));

        context.getLogger().appendLogMessage(PSLogEntryType.STATUS, ADD_HTML_WIDGET,
                "The HTML widget was successfully added to the template.");

        // Save template and finish
        templateService.save(template);
        context.getLogger().appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                "Metadata was successfully saved to the template.");
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.impl.PSGenericMetadataExtractorHelper#saveTargetItem(com.percussion.pagemanagement.data.IPSHtmlMetadata)
     */
    @Override
    protected void saveTargetItem(IPSHtmlMetadata targetItem) throws PSDataServiceException {
        // targetItem is a PSTemplate object here
        templateService.save((PSTemplate) targetItem);       
    }
}
