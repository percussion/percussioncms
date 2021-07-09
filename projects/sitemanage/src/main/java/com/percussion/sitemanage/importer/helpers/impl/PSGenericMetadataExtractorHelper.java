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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */
package com.percussion.sitemanage.importer.helpers.impl;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.itemmanagement.service.IPSItemWorkflowService;
import com.percussion.pagemanagement.data.IPSHtmlMetadata;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.share.dao.IPSGenericDao;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Santiago M. Murchio
 *
 */
public abstract class PSGenericMetadataExtractorHelper extends PSImportHelper
{

    public static final Logger log = LogManager.getLogger(PSGenericMetadataExtractorHelper.class);
    
    protected static final String ADD_HTML_WIDGET = "Add HTML Widget to Template";

    protected static final String EXTRACT_METADATA = "Extract Metadata";

    protected IPSTemplateService templateService;
    

    public PSGenericMetadataExtractorHelper(IPSTemplateService templateService)
    {
        this.templateService = templateService;
    }
    
    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.IPSImportHelper#process(com.percussion.sitemanage.data.PSPageContent, com.percussion.sitemanage.data.PSSiteImportCtx)
     */
    @Override
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException
    {
        startTimer();
        doExtractMetaData(pageContent, context);
        endTimer();
    }
    
    
    public void doExtractMetaData(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException
    {
        if(context.isCanceled())
        {
            return;
        }

        try
        {
            notNull(context.getSite());
            notNull(pageContent.getSourceDocument());

            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                    "Beginning to extract metadata.");

            PSPageManagementUtils.extractMetadata(pageContent, context.getLogger());
            
            if (isBlank(pageContent.getTitle()))
            {
                context.getLogger().appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                        "No title could be extracted from the page.");
            }

            IPSHtmlMetadata targetItem = getTargetItem(context);
            setMetadataToTargetItem(pageContent, context.getLogger(), targetItem);
            saveTargetItem(targetItem);
            
            addHtmlWidgetToTemplate(context);
        }
        catch (Exception e)
        {
            context.getLogger().appendLogMessage(PSLogEntryType.ERROR, EXTRACT_METADATA, "Page metadata could not be extracted.");
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                    "Page metadata could not be extracted: " + e.getMessage());
            log.error("Error extracting metadata while importing a page", e);
        }
    }
    
    @Override
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context)
    {
        throw new UnsupportedOperationException();
    }

    /**
     * @param targetItem
     */
    protected abstract void saveTargetItem(IPSHtmlMetadata targetItem) throws PSDataServiceException, IPSItemWorkflowService.PSItemWorkflowServiceException;

    /**
     * @param context
     */
    protected abstract void addHtmlWidgetToTemplate(PSSiteImportCtx context) throws PSDataServiceException, PSSiteImportException;

    /**
     * @param context
     * @return
     */
    protected abstract IPSHtmlMetadata getTargetItem(PSSiteImportCtx context) throws PSDataServiceException;


    /**
     * Set the meta-data from the page content to the target item
     * 
     * @param pageContent not <code>null</code>
     * @param logger not <code>null</code>
     * @param targetItem not <code>null</code>
     */
    protected void setMetadataToTargetItem(PSPageContent pageContent, IPSSiteImportLogger logger,
            IPSHtmlMetadata targetItem)
    {
        notNull(pageContent);
        notNull(logger);
        notNull(targetItem);
        
        // Update template's metadata with pageContent extracted metadata
        if (isBlank(pageContent.getHeadContent()))
        {
            logger.appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                    "No head content was extracted from the page.");
        }
        targetItem.setAdditionalHeadContent(pageContent.getHeadContent());

        if (isBlank(pageContent.getAfterBodyStart()))
        {
            logger.appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                    "No script content after body start was extracted from the page.");
        }
        targetItem.setAfterBodyStartContent(pageContent.getAfterBodyStart());

        if (isBlank(pageContent.getBeforeBodyClose()))
        {
            logger.appendLogMessage(PSLogEntryType.STATUS, EXTRACT_METADATA,
                    "No script content before body close was extracted from the page.");
        }
        targetItem.setBeforeBodyCloseContent(pageContent.getBeforeBodyClose());        
    }

}
