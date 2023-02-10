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
import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.dao.IPSiteDao;
import com.percussion.sitemanage.dao.impl.PSSiteContentDao;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.data.PSSiteSummary;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.IPSThemeService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Helper class to create new theme folder while importing an external site
 * from URL.
 * @author federicoromanelli
 * 
 */
@Component("themeTemplateHelper")
@Lazy
public class PSThemeTemplateHelper extends PSImportHelper
{
    
    private final String STATUS_MESSAGE = "retrieving theme information";
    
    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.IPSSiteImportHelper#process(PSPageContent, PSSiteImportCtx)
     */    
    @Override
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException, PSDataServiceException {
        startTimer();
        if(context.isCanceled())
        {
            return;
        }
        
        logger = context.getLogger();
        PSThemeSummary themeSummary = null;
        String templateId = null;
        String siteName = context.getSite().getName();
        PSSiteSummary siteSummary = siteDao.findSummary(siteName);
        
        PSPage homePage = null;
        if (siteSummary != null)
        {
            try
            {
                homePage = siteContentDao.getHomePage(siteSummary);
            }
            catch (Exception e)
            {
                log.debug("PSThemeTemplateHelper: Couldn't find home page for site " + siteName);
                logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + themeFetchCategory,
                    "Unable to determine theme folder to use, unable to find home page for site " + siteName);
                
                throw new PSSiteImportException("Couldn't find home page for site" + siteName, e);
            }
            templateId = homePage.getTemplateId();
        }
        else
        {
            log.debug("PSThemeTemplateHelper: Couldn't find site " + siteName);
            logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + themeFetchCategory,
                    "Unable to determine theme folder to use, unable to find site " + siteName);
            
            throw new PSSiteImportException("Couldn't find site" + siteName);
        }
        
        if (templateId != null)
        {
            PSTemplate templateObj = templateService.load(templateId);
            if (templateObj != null)
            {
                if (templateObj.getTheme() != null)
                {
                    themeSummary = themeService.find(templateObj.getTheme());
                }
                else
                {
                    log.debug("PSThemeTemplateHelper: Couldn't find theme associated to home template");
                    logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + themeFetchCategory,
                        "Unable to determine theme folder to use, template might not have a theme associated");
                    
                    throw new PSSiteImportException("Couldn't find theme associated to home template");
                }
            }
            else
            {
                log.debug("PSThemeTemplateHelper: Couldn't find template associated to home page");
                logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + themeFetchCategory,
                        "Unable to determine theme folder to use, unable to find template associated to home page");
                
                throw new PSSiteImportException("Couldn't find template associated to home page");
            }
        }
        context.setThemeSummary(themeSummary);
        context.setThemesRootDirectory(themesRootDirectory);
        endTimer();
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.IPSSiteImportHelper#rollback(PSPageContent, PSSiteImportCtx)
     */  
    @Override
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context)
    {
        // No rollback needed, nothing was created by the helper
    }
    
    private static final Logger log = LogManager.getLogger(PSThemeTemplateHelper.class);
    
    private IPSThemeService themeService;
    private String themesRootDirectory;
    private IPSSiteImportLogger logger;
    private IPSTemplateService templateService;
    private IPSiteDao siteDao;
    private PSSiteContentDao siteContentDao;
       
    public static final String helperCategory = "Theme";
    
    // Categories used for theme helper logger
    public static final String themeFetchCategory = "Theme Fetch";

    public String getThemesRootDirectory()
    {
        return themesRootDirectory;
    }

    @Value("${rxdeploydir}/web_resources/themes")
    public void setThemesRootDirectory(String themesRootDirectory)
    {
        this.themesRootDirectory = themesRootDirectory;
    }

    @Autowired
    public PSThemeTemplateHelper(IPSThemeService themeService, IPSiteDao siteDao, IPSPageService pageService,
            IPSTemplateService templateService, PSSiteContentDao siteContentDao)
    {
        this.themeService = themeService;
        this.siteDao = siteDao;
        this.templateService = templateService;
        this.siteContentDao = siteContentDao;
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }  
    
}
