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

import static com.percussion.share.spring.PSSpringWebApplicationContextUtils.getWebApplicationContext;

import static org.apache.commons.lang.Validate.notNull;

import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.theme.IPSFileDownloader;
import com.percussion.sitemanage.importer.theme.PSFileDownloader;
import com.percussion.sitemanage.importer.theme.PSHTMLHeaderImporter;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import com.percussion.theme.service.IPSThemeService;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Helper class that will handle the import of the site theme files.
 * 
 * @author Santiago M. Murchio
 * 
 */
@Component("importThemeHelper")
@Lazy
public class PSImportThemeHelper extends PSImportHelper
{

    private IPSThemeService themeService;
    
    private static final Logger log = LogManager.getLogger(PSImportThemeHelper.class);
    
    private PSHTMLHeaderImporter headerImporter;
    
    private final String STATUS_MESSAGE = "importing theme furniture";
    
    @Autowired
    public PSImportThemeHelper(IPSThemeService themeService)
    {
        this.themeService = themeService;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * com.percussion.sitemanage.importer.helpers.IPSSiteImportHelper#process
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
        IPSSiteImportSummaryService summaryService = (IPSSiteImportSummaryService) getWebApplicationContext().getBean("siteImportSummaryService");
        context.setSummaryService(summaryService);
        
        Map<String, String> linkPaths = new HashMap<>();
        Map<String, String> scriptPaths = new HashMap<>();
        Map<String, String> resources = new HashMap<>();
        Map<String, String> assets = new HashMap<>();
        Map<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer> summaryStats = 
                new HashMap<>();
        
        IPSFileDownloader fileDownloader = new PSFileDownloader();
        
        // Call the URL converter to get the full path for the resource
        try
        {
            Document sourceDoc = pageContent.getSourceDocument();
            
            String baseUrl = getBaseUrl(context, sourceDoc);
            if (baseUrl.equals(""))
                baseUrl = context.getSiteUrl();
            
            String siteName = context.getSite().getName();
            String themeRootDirectory = themeService.getThemeRootDirectory(context.getThemeSummary().getName());
            String themeRootUrl = themeService.getThemeRootUrl(context.getThemeSummary().getName());
            
            headerImporter = new PSHTMLHeaderImporter(sourceDoc, baseUrl, siteName, themeRootDirectory, themeRootUrl,
                    context.getLogger());

            // Get the map of link paths
            linkPaths = headerImporter.getLinkPaths();
            removeIfExists(linkPaths);

            // Get the map of script paths
            scriptPaths = headerImporter.getScriptPaths();
            resources.putAll(scriptPaths);
            
            // Call the downloader by passing the linkPaths and scriptPaths
            List<PSPair<Boolean, String>> linkResults = fileDownloader.downloadFiles(linkPaths, context, false);
            int linksCount = 0;
            for (PSPair<Boolean, String> linkResult : linkResults)
            {
                if(linkResult.getFirst())
                    linksCount++;
            }
            
            // Process the inline images
            resources.putAll(headerImporter.processInlineStyles());
            
            // Process the images referenced in <img> and <input type=image>
            // tags and create assets for them
            assets.putAll(headerImporter.processHeaderAndBodyImages());
            
            // Process the images referenced in embed tags and create assets for them
            assets.putAll(headerImporter.processFlashFiles(context.getSite().getName()));
            
            // The css files are downloaded, get the images
            resources.putAll(headerImporter.processCssFiles(linkPaths));
            
            // download the resources
            fileDownloader.downloadFiles(resources, context, false);
            
            // download the resources to a temp file and create assets for them
            List<PSPair<Boolean, String>> assetResults = fileDownloader.downloadFiles(assets, context, true);
            int assetsCount = 0;
            for (PSPair<Boolean, String> assetResult : assetResults)
            {
                if(assetResult.getFirst())
                    assetsCount++;
            }
            if(linksCount > 0 || assetsCount > 0)
            {
                summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.STYLESHEETS, new Integer(linksCount));
                summaryStats.put(IPSSiteImportSummaryService.SiteImportSummaryTypeEnum.FILES, new Integer(assetsCount));
                if(context.getSite() != null && context.getSite().getSiteId() != null)
                    context.getSummaryService().update(context.getSite().getSiteId().intValue(), summaryStats);
                else
                    context.setSummaryStats(summaryStats);
            }
            
        }
        catch (Exception e)
        {
            String msg = "Failed to process jsoup document from url: " + context.getSiteUrl();
            log.warn(msg, e);
        }
        endTimer();
    }

    @SuppressWarnings("unused")
    @Override
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context)
    {
    }
    
    /**
     * Helper method to get the base url if it is defined. If it is not present,
     * the site url is used as the base url. 
     * 
     * @param context The context object containing logger, site data and common
     *            information to be shared among all helpers.
     * @param sourceDoc the source code of the page.
     * @return the base url.
     */
    private String getBaseUrl(PSSiteImportCtx context, Document sourceDoc)
    {
        Elements bases = sourceDoc.getElementsByTag("base");
        if (bases.size() > 0)
        {
            String baseUrl = "";
            
            for (Element b : bases)
            {
                // Finds the first base element with href attribute
                if (b.hasAttr("href"))
                {
                    baseUrl = b.attr("href");
                    break;
                }
            }
            
            // Remove all base elements with href attribute
            for (Element b : bases)
            {
                if (b.hasAttr("href"))
                    b.remove();
            }
            
            return baseUrl;
        }
        else
        {
            return context.getSiteUrl();
        }
    }

    /**
     * Categories used by this helper to log content.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public static enum LogCategory {
        ParseCSS("Parse CSS"), ConvertURL("Convert URL"), DownloadFile("Download File"), ImportHeader(
                "Import Document Header");

        private final String name;

        LogCategory(String name)
        {
            this.name = name;
        }

        public String getName()
        {
            return name;
        }
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }
    
    /**
     * Helper method to avoid downloading and processing duplicated css files.
     * @param linkPaths
     */
    private void removeIfExists(Map<String, String> linkPaths)
    {
        Set<String> cssURLs = new HashSet<>(linkPaths.keySet());
        for (String cssURL : cssURLs)
        {
            String cssFile = linkPaths.get(cssURL);
            
            File f = new File(cssFile);
            
            if(f.exists())
            {
                linkPaths.remove(cssURL);
            }
        }
        
        
    }
}
