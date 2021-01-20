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
package com.percussion.sitemanage.importer;

import com.percussion.search.PSSearchIndexEventQueue;
import com.percussion.share.async.IPSAsyncJob;
import com.percussion.share.async.impl.PSAsyncJob;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.helpers.impl.PSImportHelper;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component("siteImportJob")
@Lazy
public class PSImportFromUrlJob extends PSAsyncJob
{
    private static final Log log = LogFactory.getLog(PSImportFromUrlJob.class);

    private List<PSImportHelper> mandatoryHelpers;

    private List<PSImportHelper> optionalHelpers;

    private List<PSImportHelper> executedHelpers;

    private IPSImportLogDao logDao;

    private PSSiteImportCtx importContext;

    private IPSSiteImportSummaryService siteImportSummaryService;


    @Override
    protected void doInit(Object config)
    {
        Validate.isTrue(config instanceof PSSiteImportCtx);
        importContext = (PSSiteImportCtx) config;
        
        IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.TEMPLATE);
        logger.logErrors();
        importContext.setLogger(logger);
        importContext.setSummaryService(siteImportSummaryService);
        setStatusMessage("Initializing");
    }
    
    /**
     * Runs this Job when JobService starts it.
     */
    @Override
    public void doRun()
    {
        importFromUrl();
    }
    
    private void importFromUrl()
    {
        IPSSiteImportLogger logger = importContext.getLogger();
        logger.appendLogMessage(PSLogEntryType.STATUS, "Import", "Importing from " + importContext.getSiteUrl());
        
        
        // handle case where called within a unit test
        if (!PSSearchIndexEventQueue.isInitialized())
            return;
        PSSearchIndexEventQueue searchQueue = PSSearchIndexEventQueue.getInstance();
           
        searchQueue.pause();
        
        try
        {
            // Get the final url (after redirections) and use it as base url
            importContext.setSiteUrl(PSSiteImporter.getRedirectedUrl(importContext.getSiteUrl(), importContext.getLogger(), importContext.getUserAgent()));

            // Import page content from URL
            PSPageContent importedPageContent = PSSiteImporter.getPageContentFromSite(importContext);

            // List to keep the executed helpers in case and rollback is
            // needed.
            executedHelpers = new ArrayList<PSImportHelper>();

            // Run helpers
            for (PSImportHelper mandatoryHelper : mandatoryHelpers)
            {
                try
                {
                    // Set status for status message
                    executedHelpers.add(mandatoryHelper);
                    setStatusMessage(mandatoryHelper.getStatusMessage(importContext.getStatusMessagePrefix()));

                    mandatoryHelper.process(importedPageContent, importContext);

                    setStatus(getImportProgress());
                }
                catch (PSSiteImportException e)
                {
                    setStatusMessage("An unexpected error occurred, cleaning up.");
                    for (int i = executedHelpers.size() - 1; i < 0; i--)
                    {
                        executedHelpers.get(i).rollback(importedPageContent, importContext);
                    }
                    setStatus(IPSAsyncJob.ABORT_STATUS);
                    setStatusMessage("Clean process finished. Please check the import log for more information.");
                    
                    String msg = "Unexpected error importing from " + importContext.getSiteUrl();
                    logger.appendLogMessage(PSLogEntryType.ERROR, "Import", msg + ": " + e.toString());
                    log.error(msg, e);
                    
                    //End all processing.
                    return;
                }
            }

            for (PSImportHelper optionalHelper : optionalHelpers)
            {
                executedHelpers.add(optionalHelper);
                setStatusMessage(optionalHelper.getStatusMessage(importContext.getStatusMessagePrefix()));

                optionalHelper.process(importedPageContent, importContext);

                setStatus(getImportProgress());
            }

            setResult(importContext);
        }
        catch (IOException e)
        {
            setStatus(IPSAsyncJob.ABORT_STATUS);
            setStatusMessage("The URL is invalid or unreachable.");
            logger.appendLogMessage(PSLogEntryType.ERROR, "Import", e.toString());
            log.error("Failed to import site " + importContext.getSiteUrl(), e);
        }
        finally
        {
            searchQueue.resume();
            
            // Always set the job as completed, once process is finished,
            // whether it failed or succeeded.
            setCompleted();

            // If import was done successfully, use the IPSImportLogDao to
            // persist the log w/template id
            String templateId = importContext.getTemplateId();
            if (logger != null && templateId != null)
            {
                saveImportLog(templateId, logger, importContext.getSite().getSiteId().toString(), importContext.getSite().getFolderPath() + "/" + importContext.getPageName());
            }
        }
    }

    private int getImportProgress()
    {
        return ((executedHelpers.size() * 100) / (mandatoryHelpers.size() + optionalHelpers.size()));
    }

    /**
     * Saves the log
     * 
     * @param objectId The object id to use
     * @param logger the logger to use, assumed not <code>null</code>.
     * @param siteId The site being used, if <code>null</code> or empty, additional error logging is not
     * performed.
     * @param desc The description of the object being imported, assumed not <code>null<code/> or empty.
     */
    private void saveImportLog(String objectId, IPSSiteImportLogger logger, String siteId, String desc)
    {
        try
        {
            PSSiteImporter.saveImportLog(objectId, logger, logDao, siteId, desc);
        }
        catch (Exception e)
        {
            log.error("Failed to save import log for ID " + objectId + " and type " + logger.getType().name() + ": "
                    + e.getLocalizedMessage(), e);
        }
    }

    /**
     * @return the mandatoryHelpers
     */
    public List<PSImportHelper> getMandatoryHelpers()
    {
        return mandatoryHelpers;
    }

    /**
     * @param mandatoryHelpers the mandatoryHelpers to set
     */
    public void setMandatoryHelpers(List<PSImportHelper> mandatoryHelpers)
    {
        this.mandatoryHelpers = mandatoryHelpers;
    }

    /**
     * @return the optionalHelpers
     */
    public List<PSImportHelper> getOptionalHelpers()
    {
        return optionalHelpers;
    }

    /**
     * @param optionalHelpers the optionalHelpers to set
     */
    public void setOptionalHelpers(List<PSImportHelper> optionalHelpers)
    {
        this.optionalHelpers = optionalHelpers;
    }

    public void setLogDao(IPSImportLogDao logDao)
    {
        this.logDao = logDao;
    }
    public IPSSiteImportSummaryService getSiteImportSummaryService()
    {
        return siteImportSummaryService;
    }

    public void setSiteImportSummaryService(IPSSiteImportSummaryService siteImportSummaryService)
    {
        this.siteImportSummaryService = siteImportSummaryService;
    }
}
