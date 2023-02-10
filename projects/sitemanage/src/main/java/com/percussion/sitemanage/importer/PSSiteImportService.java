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
package com.percussion.sitemanage.importer;

import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageCatalogService;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.error.PSTemplateImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.dao.IPSImportLogDao;
import com.percussion.sitemanage.importer.helpers.impl.PSImportHelper;
import com.percussion.sitemanage.service.IPSSiteImportService;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;
@Component("siteImportService")
@Lazy
@Transactional
public class PSSiteImportService implements IPSSiteImportService
{
    private static final Logger log = LogManager.getLogger(PSSiteImportService.class);
    
    private List<PSImportHelper> mandatoryHelpers;

    private List<PSImportHelper> optionalHelpers;
    
    private List<PSImportHelper> executedHelpers;
    
    private IPSImportLogDao logDao;
    
    private IPSPageDao pageDao;
    
    private IPSPageCatalogService pageCatalogService;
    
    private IPSSiteImportSummaryService siteImportSummaryService;
    
    @Override
    public PSSiteImportCtx importSiteFromUrl(PSSite site, String userAgent) throws PSSiteImportException
    {
        PSSiteImportCtx siteImportCtx = new PSSiteImportCtx();
        
        try
        {
            siteImportCtx.setSite(site);
            siteImportCtx.setLogger(new PSSiteImportLogger(PSLogObjectType.TEMPLATE));
            siteImportCtx.setSiteUrl(PSSiteImporter.getRedirectedUrl(site.getBaseUrl(), siteImportCtx.getLogger(), userAgent));
            siteImportCtx.setUserAgent(userAgent);
            siteImportCtx.setSummaryService(siteImportSummaryService);
            
            // Import page content from URL
            PSPageContent importedPageContent = PSSiteImporter.getPageContentFromSite(siteImportCtx);

            // List to keep the executed helpers in case and rollback is needed.
            executedHelpers = new ArrayList<>();
            
            // Run Helpers.
            runHelpers(siteImportCtx, importedPageContent);
            
            return siteImportCtx;
        }
        catch (IOException | PSDataServiceException e)
        {
            throw new PSSiteImportException("The URL is invalid or unreachable.", e);
        }
        finally
        {
            // if a site has been saved, use the IPSImportLogDao to persist the log w/template id
            PSSiteImportLogger logger = (PSSiteImportLogger) siteImportCtx.getLogger();
            String templateId = siteImportCtx.getTemplateId();
            if (logger != null && templateId != null)
            {
                saveImportLog(templateId, logger, siteImportCtx.getSite().getSiteId().toString(), siteImportCtx.getSite().getFolderPath() + "/" + siteImportCtx.getPageName());
            }
        }
    }

    @Override
    public PSSiteImportCtx importCatalogedPage(PSSite site, String pageId, String userAgent, PSSiteImportCtx context)
            throws PSSiteImportException
    {   
        context.setSite(site);
        context.setCatalogedPageId(pageId);
        context.setUserAgent(userAgent);
        IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.PAGE);
        logger.logErrors();
        logger.setWaitCount(1);
        context.setLogger(logger);
        IPSSiteImportSummaryService summaryService = (IPSSiteImportSummaryService) getWebApplicationContext().getBean("siteImportSummaryService");
        context.setSummaryService(summaryService);
        String pagePath = null;
        
        try
        {
            PSPage page = pageDao.find(context.getCatalogedPageId());
            if (page == null)
                throw new PSSiteImportException("Failed to import page id:" + pageId + ". It does not exist.");
            
            if (pageCatalogService.doesImportedPageExist(page))
            {
                throw new PSSiteImportException("Skip import page (id=" + pageId + ", name=" + page.getName() + ", folder=" + page.getFolderPath() + "). The page already exists under the site.");
            }
            
            pagePath = pageCatalogService.convertToImportedFolderPath(page.getFolderPath() + "/" + page.getName());            
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, "Import Page", "Starting import for page: " + pagePath);
            
            context.setSiteUrl(PSSiteImporter.getRedirectedUrl(page.getDescription(), context.getLogger(), context.getUserAgent()));
            context.setTemplateId(page.getTemplateId());
            
            // Import page content from URL
            PSPageContent importedPageContent = PSSiteImporter.getPageContentFromSite(context);
            
            // List to keep the executed helpers in case and rollback is needed.
            executedHelpers = new ArrayList<>();
            
            // Run Helpers.
            runHelpers(context, importedPageContent);

            return context;
        }
        catch (PSDataServiceException e)
        {
            throw new PSSiteImportException("The page doesn't exist in the system.");
        }
        catch (IOException e)
        {
            throw new PSSiteImportException("The URL is invalid or unreachable.", e);
        }
        finally
        {
            // if a site has been saved, use the IPSImportLogDao to persist the log w/page id
            if (logger != null && pageId != null)
            {
                saveImportLog(pageId, logger, context.getSite().getSiteId().toString(), pagePath);
            }
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
    
    public void setPageDao(IPSPageDao pageDao)
    {
        this.pageDao = pageDao;
    }

    public void setLogDao(IPSImportLogDao logDao)
    {
        this.logDao = logDao;
    }
    
    public void setPageCatalogService(IPSPageCatalogService pageCatalogService) 
    {
        this.pageCatalogService = pageCatalogService;
    }

    /**
     * 
     * @param siteImportCtx
     * @param importedPageContent
     */
    private void runHelpers(PSSiteImportCtx siteImportCtx, PSPageContent importedPageContent) throws PSDataServiceException, PSSiteImportException {
        // Run helpers
        for (PSImportHelper mandatoryHelper : mandatoryHelpers)
        {
            try
            {
                executedHelpers.add(mandatoryHelper);
                mandatoryHelper.process(importedPageContent, siteImportCtx);
            }
            catch (PSSiteImportException | PSTemplateImportException | IPSPageService.PSPageException e)
            {
                for (int i = executedHelpers.size() - 1; i < 0; i--)
                {
                    executedHelpers.get(i).rollback(importedPageContent, siteImportCtx);
                }

                throw new PSSiteImportException(
                        "An unexpected error occurred while processing the imported page. Please check the import log for more information.", e);
            }
        }

        for (PSImportHelper optionalHelper : optionalHelpers)
        {
            optionalHelper.process(importedPageContent, siteImportCtx);
        }
    }

    /**
     * Saves the log in another thread, waits for any other threads to complete work before 
     * 
     * @param objectId The object id to use
     * @param logger the logger to use, assumed not <code>null</code>.
     * @param desc The description of the object being imported, assumed not <code>null<code/> or empty.
     */
    private void saveImportLog(String objectId, IPSSiteImportLogger logger, String siteId, String desc)
    {
        try
        {
            PSDeferredLogWriter writer = new PSDeferredLogWriter(siteId, desc, logger, objectId, logDao);
            writer.saveWhenReady();
        }
        catch (Exception e)
        {
            log.error("Failed to save import log for ID " + objectId + " and type " + logger.getType().name() + ": " + e.getLocalizedMessage(), e);
        }        
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
