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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Helper class that will handle the call to move a page to the actual location.
 * 
 * @author Leonardo Hildt
 * 
 */
@Component("importPageCreationHelper")
@Lazy
public class PSImportPageCreationHelper extends PSImportHelper
{

    private final String STATUS_MESSAGE = "Importing Page";

    private IPSPageCatalogService pageCatalogService;

    @Autowired
    public PSImportPageCreationHelper(final IPSPageCatalogService pageCatalogService)
    {
        this.pageCatalogService = pageCatalogService;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.percussion.sitemanage.importer.helpers.PSImportHelper#process
     * (com.percussion.sitemanage.data.PSPageContent,
     * com.percussion.sitemanage.data.PSSiteImportCtx)
     */
    @Override
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException
    {
        startTimer();
        notNull(pageContent);
        notNull(context);

        if(context.isCanceled())
        {
            return;
        }

        context.getLogger().appendLogMessage(PSLogEntryType.STATUS, STATUS_MESSAGE,
                "Starting to move imported page " + context.getPageName() + " to the actual location");

        // Call method to import the page
        try
        {
            pageCatalogService.createImportedPage(context.getCatalogedPageId());
        }
        catch (Exception e)
        {
            String errorMsg = "Could not move the imported page " + context.getPageName() + "to the matching site folder.";
            context.getLogger().appendLogMessage(PSLogEntryType.ERROR, STATUS_MESSAGE, errorMsg);
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, STATUS_MESSAGE, errorMsg + " The error was: " + e.getLocalizedMessage());
            
            throw new PSSiteImportException(errorMsg, e);
        }

        context.getLogger().appendLogMessage(PSLogEntryType.STATUS, STATUS_MESSAGE,
                "Successfully moved imported page " + context.getPageName() + " to the actual location");
        endTimer();
    }

    @Override
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context)
    {
        // NOOP - this is an optional helper
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }
}
