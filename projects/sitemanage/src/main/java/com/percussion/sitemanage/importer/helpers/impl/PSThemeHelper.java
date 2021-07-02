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

import com.percussion.sitemanage.data.PSPageContent;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.error.PSSiteImportException;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.theme.data.PSThemeSummary;
import com.percussion.theme.service.IPSThemeService;

import java.io.File;

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
@Component("themeHelper")
@Lazy
public class PSThemeHelper extends PSImportHelper
{
    
    private final String STATUS_MESSAGE = "creating new theme";
    
    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.IPSSiteImportHelper#process(PSPageContent, PSSiteImportCtx)
     */    
    @Override
    public void process(PSPageContent pageContent, PSSiteImportCtx context) throws PSSiteImportException
    {
        startTimer();
        logger = context.getLogger();
        PSThemeSummary newThemeSummary = null;
        try
        {
            String newSiteName = processName(context.getSite().getName());
            newThemeSummary = themeService.createFromDefault(newSiteName);
            logger.appendLogMessage(PSLogEntryType.STATUS, helperCategory + " - " + themeCreationCategory,
                 "Create theme: " + newThemeSummary.getName());            
            renameBasicFiles(newThemeSummary.getName(), newSiteName);
            context.setThemeSummary(newThemeSummary);
            context.setThemesRootDirectory(themesRootDirectory);
        }
        catch (Exception e)
        {
            // Couldn't create template
            // Log Error and call delete theme just in case
            log.info("PSCreateThemeHelper: Couldn't create theme.");
            logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + themeCreationCategory,
                    "Couldn't create theme folder.");
            
            if (newThemeSummary != null && newThemeSummary.getName() != null)
            {
                deleteTheme(newThemeSummary.getName());
            }
            
            throw new PSSiteImportException("Couldn't create new theme folder", e);
        }
        endTimer();
    }

    /* (non-Javadoc)
     * @see com.percussion.sitemanage.importer.helpers.IPSSiteImportHelper#rollback(PSPageContent, PSSiteImportCtx)
     */  
    @Override
    public void rollback(PSPageContent pageContent, PSSiteImportCtx context)
    {
        PSThemeSummary newThemeSummary = context.getThemeSummary();
        if (newThemeSummary != null)
        {
            deleteTheme(newThemeSummary.getName());
        }
    }
    
    /**
     * Helper method used to rename the 2 basic files from the new theme:
     * theme.css, theme.png into the corresponding name created from site name.
     *
     * @author federicoromanelli
     * @param newThemeName, the name of the new theme (with collision detected and avoided).
     * Never <code> null </code>
     * @param newSiteName, the name of the new theme (using the original site name).
     * Never <code> null </code>
     */
    protected void renameBasicFiles(String newThemeName, String newSiteName)
    {
        String newThemeRoot = themesRootDirectory + "/" + newThemeName;
        File oldCSSfile = new File(newThemeRoot, "theme.css");
        File newCSSfile = new File(newThemeRoot, newSiteName + ".css");
        File oldImagefile = new File(newThemeRoot, "theme.png");
        File newImagefile = new File(newThemeRoot, newSiteName + ".png");        

        logger.appendLogMessage(PSLogEntryType.STATUS, helperCategory + " - " + fileRenamingCategory,
              "Renaming theme files");
        if(!oldCSSfile.renameTo(newCSSfile))
        {
            logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + fileRenamingCategory,
                    "Couldn't rename file: " + oldCSSfile);
        }
        
        if(!oldImagefile.renameTo(newImagefile))
        {
            logger.appendLogMessage(PSLogEntryType.ERROR, helperCategory + " - " + fileRenamingCategory,
                    "Couldn't rename file: " + oldImagefile);            
        }
    }
    
    /**
     * Helper method used to perform transformations on the site name to make it suitable for 
     * theme name.
     *
     * @author federicoromanelli
     * @param siteName, original site name to be transformed. Never <code> null </code>
     * @return String - the site name with the transformations applied
     */    
    protected String processName (String siteName)
    {
        return siteName.replace(".", "-");
    }

    /**
     * Deletes the theme calling the corresponding method in themes service
     * 
     * @author federicoromanelli
     * @param newThemeName, the name of the new theme (with collision detected
     *            and avoided). Never <code> null </code>
     */
    protected void deleteTheme (String newThemeName)
    {
        try
        {
            logger.appendLogMessage(PSLogEntryType.STATUS, helperCategory + " - " + themeDeletionCategory,
                    "Delete new theme: " + newThemeName);            
            themeService.delete(newThemeName);
        }
        catch (Exception e)
        {
            log.info("PSCreateThemeHelper: Couldn't delete theme template. The theme might not have been created");
        }
    }
    
    private static final Logger log = LogManager.getLogger(PSThemeHelper.class);
    
    private IPSThemeService themeService;
    private String themesRootDirectory;
    private IPSSiteImportLogger logger;
    
    public static final String helperCategory = "Theme Creator";
    
    // Categories used for theme helper logger
    public static final String themeCreationCategory = "Theme Creation";
    public static final String themeDeletionCategory = "Theme Deletion";
    public static final String fileRenamingCategory = "Theme Files Rename";

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
    public PSThemeHelper(IPSThemeService themeService)
    {
        this.themeService = themeService;
    }

    @Override
    public String getHelperMessage()
    {
        return STATUS_MESSAGE;
    }

}
