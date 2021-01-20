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

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.dao.IPSGenericDao.DeleteException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSite;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;


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
    public static Log log = LogFactory.getLog(PSSiteCreationHelper.class);
    
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
                    new HashMap<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer>();
            if(context.getSummaryStats()!=null)
            {
                summaryStats.putAll(context.getSummaryStats());
                context.setSummaryStats(null);
            }
            summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.TEMPLATES, new Integer(1));
            summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.PAGES, new Integer(1));
            context.getSummaryService().update(context.getSite().getSiteId().intValue(), summaryStats);        
            
        }
        catch (RuntimeException e)
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
        catch (DeleteException e)
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
