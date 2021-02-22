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

package com.percussion.theme.service;

import com.percussion.share.service.IPSCatalogService;
import com.percussion.share.service.IPSDataService;
import com.percussion.share.service.IPSDataService.DataServiceLoadException;
import com.percussion.share.service.IPSDataService.DataServiceNotFoundException;
import com.percussion.share.service.IPSDataService.DataServiceSaveException;
import com.percussion.share.service.IPSReadOnlyDataService;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.theme.data.PSRegionCSS;
import com.percussion.theme.data.PSRegionCssList;
import com.percussion.theme.data.PSRichTextCustomStyle;
import com.percussion.theme.data.PSTheme;
import com.percussion.theme.data.PSThemeSummary;

import java.util.List;

/**
 * 
 * Finds theme information.
 * 
 * @see PSThemeSummary
 * @see PSTheme
 * 
 * @author YuBingChen
 * @author adamgent
 */
public interface IPSThemeService extends IPSCatalogService<PSThemeSummary, String>, IPSReadOnlyDataService<PSTheme, String>
{
    
    /**
     * Loads the content of a single css files associated with the specified theme.
     * 
     * @param name the Theme name, never blank.
     * 
     * @return a {@link PSTheme} object with css content for the Theme, never <code>null</code>.
     * @throws DataServiceLoadException if the theme does not have a CSS file or multiple css files.
     * @throws DataServiceNotFoundException if the theme is not found.
     */
    PSTheme load(String name) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException;
    
    /**
     * Creates a new theme from an existing theme.  The newly created theme will be a copy of the existing theme.
     * 
     * @param newTheme the new Theme name, never blank.
     * @param existingTheme the existing Theme name, never blank.
     * 
     * @return a {@link PSThemeSummary} object with information for the new Theme, never <code>null</code>.
     * @throws DataServiceLoadException if the new theme does not have a CSS file or multiple css files.
     * @throws DataServiceNotFoundException if the existing theme is not found.
     * @throws DataServiceSaveException if the new theme could not be saved.
     */
    PSThemeSummary create(String newTheme, String existingTheme) throws DataServiceLoadException,
          DataServiceNotFoundException, DataServiceSaveException;
    
    /**
     * Creates a new theme from the default one when importing site from URL.
     * The newly created theme will be a copy of the existing default_theme located
     * in <INSTALL_DIR>/rx_resources/default_theme 
     * 
     * @param newTheme the new Theme name, never blank.
     * 
     * @return a {@link PSThemeSummary} object with information for the new Theme, never <code>null</code>.
     * @throws DataServiceLoadException if the new theme does not have a CSS file or multiple css files.
     * @throws DataServiceNotFoundException if the existing theme is not found.
     * @throws DataServiceSaveException if the new theme could not be saved.
     */    
    PSThemeSummary createFromDefault(String newTheme) throws DataServiceLoadException,
            DataServiceNotFoundException, DataServiceSaveException;
    
    /**
     * Deletes the specified theme.
     * 
     * @param name the Theme name, never blank.
     *
     * @throws DataServiceNotFoundException if the theme does not exist.
     */
    void delete(String name) throws DataServiceNotFoundException, IPSDataService.DataServiceDeleteException;
    
    /**
     * Gets the root URL of the specified theme.
     * @param themeName the theme name, may not be <code>null</code> or empty.
     * @return the URL that is relative to any site that uses this theme.
     * It never <code>null</code> or empty.
     */
    String getThemeRootUrl(String themeName);
    
    /**
     * Gets the root directory of the specified theme on the file system.
     * @param themeName the theme name, not <code>null</code> or empty.
     * @return the root directory, never <code>null</code> or empty.
     */
    String getThemeRootDirectory(String themeName);

    /**
     * Gets the specified region CSS properties
     * @param theme the theme name, not blank
     * @param templatename the template name, not blank
     * @param outerregion the most outer region name, not blank
     * @param region the name of the region that contains the CSS properties, not blank
     * @return the region CSS, never <code>null</code>.
     */
    public PSRegionCSS getRegionCSS(String theme, String templatename, String outerregion, String region) throws IPSDataService.PSThemeNotFoundException;

    /**
     * Saves the specified region CSS
     * @param theme the theme name, not blank
     * @param templatename the template name, not blank
     * @param regionCSS the saved region CSS, not <code>null</code>.
     */
    public void saveRegionCSS(String theme, String templatename, PSRegionCSS regionCSS) throws IPSDataService.PSThemeNotFoundException;

    /**
     * Deletes the specified region CSS.
     * @param theme the theme name, not blank
     * @param templatename the template name, not blank
     * @param outerregion the most outer region name, not blank
     * @param region the name of the region that contains the CSS properties, not blank
     */
    public void deleteRegionCSS(String theme, String templatename, String outerregion, String region) throws IPSDataService.PSThemeNotFoundException;

    /**
     * Merges the cached region CSS file from the temporary location into the theme's region CSS file.
     * This should be called when save the "Layout" of the template.
     *  
     * @param theme the theme name, not blank
     * @param templatename the template name, not blank
     * @param deletedRegions
     */
    public void mergeRegionCSS(String theme, String templatename, PSRegionCssList deletedRegions) throws PSDataServiceException;
    
    /**
     * Prepare for editing the region CSS file. It copies the file from the location of the theme
     * to a temporary location. This should be called before modifying the "Layout" of the template.
     * 
     * @param theme the theme name, not blank
     * @param templatename the template name, not blank
     */
    public void prepareForEditRegionCSS(String theme, String templatename) throws IPSDataService.PSThemeNotFoundException;
    
    /**
     * Clear the temporary cached region CSS file if exist. This file is created by calling
     * {@link #prepareForEditRegionCSS(String, String)}. 
     * This should be called when canceling the changes of template "Layout" and 
     * should be called when entering the edit mode of the template (to make sure to clear
     * any previously cached file if there is any).
     * 
     * @param theme the theme name, not blank
     * @param templatename the template name, not blank
     */
    public void clearCacheRegionCSS(String theme, String templatename);
    
    /**
     * Returns the list of custom styles that are stored in a property file, checks for the file modification time
     * and loads the properties if the file is modified from last read.  
     * 
     * @return List of PSRichTextCustomStyles never <code>null</code> may be empty.
     */
    public List<PSRichTextCustomStyle> getRichTextCustomStyles();
    
}
