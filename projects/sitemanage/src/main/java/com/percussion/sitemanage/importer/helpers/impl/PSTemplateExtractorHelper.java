/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
