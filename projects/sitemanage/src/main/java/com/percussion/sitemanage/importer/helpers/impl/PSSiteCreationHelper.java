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

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.Validate.notNull;


/**
 * @author LucasPiccoli
 *
 */
@Component("siteCreationHelper")
@Lazy
public class PSSiteCreationHelper extends PSImportHelper
{

    /**
     * Server logger for the helper (It's a mandatory helper so context log will
     * be erased if an error occurs).
     */
    public static final Logger log = LogManager.getLogger(PSSiteCreationHelper.class);
    
    private IPSiteDao siteDao;
    
    private IPSPageService pageService;
    
    private final String DEFAULT_TEMPLATE_NAME = "Home";

    private final String DEFAULT_LANDING_PAGE_NAME = "Home";

    private final String STATUS_MESSAGE = "creating site";
    
    @Autowired
    public PSSiteCreationHelper(IPSiteDao siteDao, IPSPageService pageService){
        this.siteDao = siteDao;
        this.pageService = pageService;
    }
    
    @Override
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException
    {
        startTimer();
        context.getLogger().appendLogMessage(PSLogEntryType.STATUS, "Create Site", "The site creation has started.");

        // Create site and related elements
        PSSite newSite = context.getSite();
     
        //Set plain template as base template
        newSite.setBaseTemplateName(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME);
        newSite.setTemplateName(DEFAULT_TEMPLATE_NAME);
        
        //If page title could be extracted get it from pageContent. 
        //Otherwise default to page name and log a message.
        String importedPageTitle = pageContent.getTitle();
        if (StringUtils.isBlank(importedPageTitle))
        {
            importedPageTitle = DEFAULT_LANDING_PAGE_NAME;
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS,
                    "Extract page title",
                    "No title could be extracted from the page. Defaulting to page name.");
        }
        newSite.setHomePageTitle(importedPageTitle);
        newSite.setNavigationTitle(importedPageTitle);
        
        try
        {
            // save and create related elements
            context.setSite(siteDao.save(newSite));
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, "Create site",
                    "The site was created successfully.");
            
            // set the template id on the context
            PSPage homePage = pageService.findPage(PSSiteContentDao.HOME_PAGE_NAME, context.getSite().getFolderPath());
            if (homePage != null)
            {
                context.setTemplateId(homePage.getTemplateId());
                context.setPageName(PSSiteContentDao.HOME_PAGE_NAME);
            }

            //Create site import summary entry
            context.getSummaryService().create(context.getSite().getSiteId().intValue());
            
            //Update the template count
            Map<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer> summaryStats = 
                    new HashMap<>();
            if(context.getSummaryStats()!=null)
            {
                summaryStats.putAll(context.getSummaryStats());
                context.setSummaryStats(null);
            }
            summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.TEMPLATES, new Integer(1));
            summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.PAGES, new Integer(1));
            context.getSummaryService().update(context.getSite().getSiteId().intValue(), summaryStats);        
            
        }
        catch (RuntimeException | PSDataServiceException e)
        {
            // Errors in mandatory helpers are not logged in siteImportLogger,
            // because that log is discarded. Log the error in the server log.
            String message = "There was an unexpected error creating the new site.";
            log.error(message + ". Caused by: " + e.getMessage() + ExceptionUtils.getFullStackTrace(e));
            throw new PSSiteImportException(message, e);
        }
        endTimer();
    }
    
    @Override
    @SuppressWarnings("unused")
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context)
    {
        notNull(context.getSite());
        try
        {
            // Delete site and related content
            siteDao.delete(context.getSite().getId());
        }
        catch (PSDataServiceException e)
        {
            context.getLogger().appendLogMessage(PSLogEntryType.ERROR, "Delete Site",
                    "Failed to roll back site creation: " + e.getLocalizedMessage());
        }
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }
}
