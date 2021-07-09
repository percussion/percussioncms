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

import com.percussion.pagemanagement.dao.IPSPageDao;
import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.pagemanagement.service.IPSPageService;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.pagemanagement.service.impl.PSPageManagementUtils;
import com.percussion.services.assembly.IPSAssemblyService;
import com.percussion.services.assembly.PSAssemblyException;
import com.percussion.services.error.PSNotFoundException;
import com.percussion.share.IPSSitemanageConstants;
import com.percussion.share.service.IPSIdMapper;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSTemplateImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.service.IPSSiteTemplateService;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author LucasPiccoli
 * 
 *         Helper responsible for creating a template, and a new page using that
 *         template.
 * 
 */
@Component("templateCreationHelper")
@Lazy
public class PSTemplateCreationHelper extends PSImportHelper
{
    private IPSTemplateService templateService;

    private IPSPageDao pageDao;

    private IPSAssemblyService assemblyService;

    private IPSIdMapper idMapper;

    private IPSSiteTemplateService siteTemplateService;

    private IPSPageService pageService;




    public static final String LOG_ENTRY_PREFIX = "Import Template From Url";

    private static final String STATUS_MESSAGE = "creating template";

    /**
     * Server logger for the helper (It's a mandatory helper so context log will
     * be erased if an error occurs).
     */
    public static final Logger log = LogManager.getLogger(PSTemplateCreationHelper.class);

    @Autowired
    public PSTemplateCreationHelper(IPSTemplateService templateService, IPSPageDao pageDao,
            IPSAssemblyService assemblyService, IPSIdMapper idMapper, IPSSiteTemplateService siteTemplateService,
            IPSPageService pageService)
    {
        this.templateService = templateService;
        this.pageDao = pageDao;
        this.assemblyService = assemblyService;
        this.idMapper = idMapper;
        this.siteTemplateService = siteTemplateService;
        this.pageService = pageService;
    }

    @SuppressWarnings("unused")
    @Override
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSTemplateImportException, IPSPageService.PSPageException {
        startTimer();
        // Initial names, using site-wide naming conventions.
        String pageName = PSPageManagementUtils.PAGE_NAME;
        String templateName = PSPageManagementUtils.TEMPLATE_NAME;

        // If possible, extract name from URL to use for page and template,
        // instead.
        String extractedName = extractPageNameFromUrl(context.getSiteUrl());
        if (!StringUtils.isEmpty(extractedName))
        {
            pageName = extractedName;
            templateName = extractedName;
        }
        else
        {
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, LOG_ENTRY_PREFIX,
                    "Template and page name couldn't be extracted from URL. Defaulting to " + PSPageManagementUtils.TEMPLATE_NAME);
        }

        // Generate names to avoid collision with existing pages and templates.
        pageName = pageService.generateNewPageName(pageName, context.getSite().getFolderPath());
        templateName = siteTemplateService.generateNewTemplateName(PSPageManagementUtils.TEMPLATE_NAME, context.getSite().getId());

        try
        {
            // TODO Replace plain template with new perc.base.empty that will be
            // later added to base package.
            // Create template
            context.setTemplateName(templateName);
            PSTemplateSummary newTemplate = templateService.createNewTemplate(IPSSitemanageConstants.PLAIN_BASE_TEMPLATE_NAME,
                    templateName, context.getSite().getId());
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, LOG_ENTRY_PREFIX,
                    "Template was successfully created with name: " + templateName);

            // Create page
            context.setPageName(pageName);
            PSPage newPage = createNewPage(pageName, newTemplate.getId(), context.getSite().getFolderPath());
            context.getLogger().appendLogMessage(PSLogEntryType.STATUS, LOG_ENTRY_PREFIX,
                    "Page was successfully created with name: " + templateName);

            // Assign the new template id to the context object
            context.setTemplateId(newTemplate.getId());
        } catch (PSAssemblyException | PSDataServiceException e)
        {
            String message = "There was an unexpected error importing the template from the provided URL.";
            log.error(message + ". Caused by: " + e.getMessage());
            throw new PSTemplateImportException(message, e);
        }
        endTimer();
    }

    @SuppressWarnings("unused")
    @Override
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context) throws PSDataServiceException {
        if (context.getSite() == null)
        {
            return;
        }

        // Delete page if it was created
        if (StringUtils.isNotEmpty(context.getPageName()))
        {
            PSPage page = pageService.findPage(context.getPageName(), context.getSite().getFolderPath());
            if (page != null)
            {
                pageService.delete(page.getId());
            }
        }

        // Delete template if it was created
        if (StringUtils.isNotEmpty(context.getTemplateName()))
        {
            List<PSTemplateSummary> siteTemplates = siteTemplateService.findTemplatesBySite(context.getSite().getId());
            for (PSTemplateSummary template : siteTemplates)
            {
                if (template.getName().equals(context.getTemplateName()))
                {
                    try {
                        templateService.delete(template.getId());
                    } catch (PSNotFoundException e) {
                        log.warn(e.getMessage());
                        log.debug(e.getMessage(),e);
                    }
                    break;
                }
            }
        }
    }

    /**
     * It gets the text of the URL behind the last bar "/" and before the URL
     * parameters or query section in the URL.
     * 
     * @param url The URL to process (can include http:// prefix or not). No
     *            syntax restrictions.
     * @return extracted name if it was possible to extract, or an empty string
     *         if it couldn't be extracted. Never null.
     */
    public String extractPageNameFromUrl(String url)
    {
        if (StringUtils.isEmpty(url))
        {
            return new String("");
        }

        // Clean URL before processing
        // Unify \ bars to /
        String cleanUrl = url.replace('\\', '/');

        // Remove protocol prefix - http://, https://, etc ;
        String protocolSeparator = "://";
        int protocolPosition = cleanUrl.indexOf(protocolSeparator);
        if (protocolPosition != -1)
        {
            cleanUrl = cleanUrl.substring(protocolPosition + protocolSeparator.length(), cleanUrl.length());
        }

        // Remove / at the start
        if (cleanUrl.length() > 0 && cleanUrl.charAt(0) == '/')
        {
            cleanUrl = cleanUrl.substring(1);
        }

        // Remove trailing bar
        if (cleanUrl.endsWith("/"))
        {
            cleanUrl = cleanUrl.substring(0, cleanUrl.length() - 1);
        }

        // Start from the last bar found, without including it.
        int startIndex = cleanUrl.lastIndexOf("/");

        if (startIndex == -1 || startIndex == cleanUrl.length() - 1)
        {
            return new String("");
        }
        else
        {
            startIndex++;
        }

        // End at the first ?, ; or . found.
        int endIndex = cleanUrl.replace('?', '.').replace(';', '.').indexOf('.', startIndex);

        if (endIndex == -1)
        {
            return cleanUrl.substring(startIndex);
        }
        else
        {
            return cleanUrl.substring(startIndex, endIndex);
        }
    }


    /**
     * It creates a new page with specified name in folderPath, using template
     * with templateId
     * 
     * @param name The name that this page will have. Will also be used to set
     *            link title and page title.
     * @param templateId The template id of the template that this page will
     *            use.
     * @param folderPath The folder path inside the site where the page will be
     *            created.
     * @return PSPage Class that holds information of the created page.
     * 
     */
    public PSPage createNewPage(String name, String templateId, String folderPath) throws PSDataServiceException {
        PSPage page = new PSPage();
        page.setName(name);
        page.setFolderPath(folderPath);
        page.setTitle(name);
        page.setTemplateId(templateId);
        page.setLinkTitle(name);
        return pageService.save(page);
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }

}
