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
package com.percussion.sitemanage.data;

import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitesummaryservice.service.IPSSiteImportSummaryService;
import com.percussion.theme.data.PSThemeSummary;

import java.util.Map;

/**
 * @author LucasPiccoli
 *
 */
public class PSSiteImportCtx
{

    String siteUrl;
    
    PSSite site;
    
    IPSSiteImportLogger logger;
    
    IPSSiteImportSummaryService summaryService;
    
    Map<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer> summaryStats;

    PSThemeSummary themeSummary;
    
    String themesRootDirectory;
    
    String templateId = null;
    
    String pageName;
    
    String catalogedPageId;

    String templateName;
	
	String statusMessagePrefix;
	 
    String userAgent;

    boolean isCanceled = false;
    
    /**
     * @return the siteUrl
     */
    public String getSiteUrl()
    {
        return siteUrl;
    }

    /**
     * @param siteUrl the siteUrl to set
     */
    public void setSiteUrl(String siteUrl)
    {
        this.siteUrl = siteUrl;
    }

    /**
     * @return the site
     */
    public PSSite getSite()
    {
        return site;
    }

    /**
     * @param site the site to set
     */
    public void setSite(PSSite site)
    {
        this.site = site;
    }
    
    /**
     * Set logger on the context.
     * 
     * @param logger The logger, never <code>null</code>
     */
    public void setLogger(IPSSiteImportLogger logger)
    {
        this.logger = logger;        
    }
    
    /**
     * Get the current logger.
     * 
     * @return The logger, never <code>null</code>.
     * @throws IllegalStateException if no logger has been set.
     */
    public IPSSiteImportLogger getLogger()
    {
        if (logger == null)
            throw new IllegalStateException("logger has not been set");
        
        return logger;
    }

    public IPSSiteImportSummaryService getSummaryService()
    {
        return summaryService;
    }

    public void setSummaryService(IPSSiteImportSummaryService summaryService)
    {
        this.summaryService = summaryService;
    }

    
    /**
     * @return the theme summary
     */
    public PSThemeSummary getThemeSummary()
    {
        return themeSummary;
    }
    
    /**
     * @param themeSummary the new summary to assign
     */
    public void setThemeSummary(PSThemeSummary themeSummary)
    {
        this.themeSummary = themeSummary;
    }
    
    /**
     * @return the themes root directory absolute path
     */
    public String getThemesRootDirectory()
    {
        return themesRootDirectory;
    }

    /**
     * @param themesRootDirectory the themes root directory absolute path
     */
    public void setThemesRootDirectory(String themesRootDirectory)
    {
        this.themesRootDirectory = themesRootDirectory;
    }

    /**
     * Get the id of the template if one was created during the import process.
     * 
     * @return The id, or <code>null</code> if a template was not created.
     */
    public String getTemplateId()
    {
        return templateId;
    }

    /**
     * Set the id of the template if one was created during the import process, must
     * be called in order for an import log to be saved.
     * 
     * @param templateId The template id.
     */
    public void setTemplateId(String templateId)
    {
        this.templateId = templateId;
    }

    /**
     * @return the pageName
     */
    public String getPageName()
    {
        return pageName;
    }

    /**
     * @param pageName the pageName to set
     */
    public void setPageName(String pageName)
    {
        this.pageName = pageName;
    }

    /**
     * @return the templateName
     */
    public String getTemplateName()
    {
        return templateName;
    }

    /**
     * @param templateName the templateName to set
     */
    public void setTemplateName(String templateName)
    {
        this.templateName = templateName;
    }   
    
    /**
     * @return the statusMessagePrefix
     */
    public String getStatusMessagePrefix()
    {
        return statusMessagePrefix;
    }

    /**
     * @param statusMessagePrefix the statusMessagePrefix to set
     */
    public void setStatusMessagePrefix(String statusMessagePrefix)
    {
        this.statusMessagePrefix = statusMessagePrefix;
    }  
    
    /**
     * @return the userAgent
     */
    public String getUserAgent()
    {
        return userAgent;
    }

    /**
     * @param userAgent the userAgent to set
     */
    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    /**
     * Used when importing cataloged pages. Is the id of the page being
     * imported.
     * 
     * @return {@link String} may be <code>null</code>.
     */
    public String getCatalogedPageId()
    {
        return catalogedPageId;
    }

    public void setCatalogedPageId(String catalogedPageId)
    {
        this.catalogedPageId = catalogedPageId;
    }
    
    public void setCanceled(boolean cancelFlag)
    {
        isCanceled = cancelFlag;
    }
    
    /**
     * Determines if the current import process has been canceled.
     * 
     * @return <code>true</code> if the import process has been canceled. 
     */
    public boolean isCanceled()
    {
        return isCanceled;
    }
    /**
     * @return May be null if not set.
     */
    public Map<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer> getSummaryStats()
    {
        return summaryStats;
    }

    public void setSummaryStats(Map<IPSSiteImportSummaryService.SiteImportSummaryTypeEnum, Integer> summaryStats)
    {
        this.summaryStats = summaryStats;
    }


}
