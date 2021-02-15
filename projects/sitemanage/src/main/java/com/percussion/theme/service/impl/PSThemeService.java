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

package com.percussion.theme.service.impl;

import com.percussion.pagemanagement.data.PSTemplate;
import com.percussion.pagemanagement.service.IPSTemplateService;
import com.percussion.server.PSRequest;
import com.percussion.share.service.IPSDataService.*;
import com.percussion.share.service.exception.PSValidationException;
import com.percussion.theme.data.*;
import com.percussion.theme.service.IPSThemeService;
import com.percussion.utils.request.PSRequestInfo;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

import static com.percussion.share.service.exception.PSParameterValidationUtils.rejectIfNull;
import static org.apache.commons.lang.Validate.notEmpty;
import static org.apache.commons.lang.Validate.notNull;

/**
 * Implementation of the {@link IPSThemeService}.
 *
 * @author YuBingChen
 */
@Component("themeService")
@Lazy
public class PSThemeService implements IPSThemeService
{
    @PostConstruct
    public void init()
    {
        String tempThemeDir = getThemesTempRootDirectory();
        File tempDir = new File(tempThemeDir);
        FileUtils.deleteQuietly(tempDir);
    }
    
    /*
     * //see base interface method for details
     */
    public List<PSThemeSummary> findAll()
    {
        List<PSThemeSummary> themes = new ArrayList<>();
        File root = getThemesRoot();
        if (!root.exists())
        {
            return themes;
        }

        for (File thFile : Objects.requireNonNull(root.listFiles())) {
            if (thFile.isDirectory()) {
                try {
                    themes.add(find(thFile.getName()));
                } catch (DataServiceLoadException | DataServiceNotFoundException | PSValidationException e) {
                    log.error("Failed to load theme: {}" ,thFile.getName());
                    log.debug(e);
                }
            }
        }

        return themes;
    }
    
    protected void loadThemeSummary(File file, PSThemeSummary summary) throws PSThemeNotFoundException {
        File root = getThemesRoot();
        String themeName = file.getName();
        String url = getThumbUrl(root, themeName);
        
        summary.setName(file.getName());
        summary.setThumbUrl(url);
        File cssFile = getCssFile(themeName);
        if (cssFile != null)
        {
            summary.setCssFilePath(themeName +  "/" + cssFile.getName());
        }
        File regionCssFile = getRegionCssFileOrNull(themeName);
        if (regionCssFile != null)
        {
            summary.setRegionCssFilePath(themeName +  "/" + THEME_REGION_CSS_PATH);
        }
    }

    private File getRegionCssFileOrNull(String themeName) throws PSThemeNotFoundException {
        File regionCss = getRegionCssFile(themeName);
        if (regionCss.exists())
            return regionCss;
        else
            return null;
    }

    private File getRegionCssFile(String themeName) throws PSThemeNotFoundException {
        File themeFolder = getThemeFolder(themeName);
        return  new File(themeFolder, THEME_REGION_CSS_PATH);
    }

    /**
     * Gets the cached region CSS URL that is relative to (all) theme root.
     * The cached region CSS file will be copied from the theme's region CSS file
     * or created an empty one if the theme's region CSS file does not exist.
     *  
     * @param theme the theme name, not blank.
     * 
     * @return the URL, not blank.
     */
    public String getCachedRegionCSSRelativeURL(String theme) throws PSThemeNotFoundException {
        // in server environment, make sure to cache the region CSS (or copy it to the temp location
        getCachedRegionCSSFile(theme, false);
        return getCachedRegionCSSRelativePath(theme);
    }


    private String getCurrentSessionId()
    {
        PSRequest request = (PSRequest) PSRequestInfo.getRequestInfo(PSRequestInfo.KEY_PSREQUEST);
        return (request == null) ? "pssession" : request.getUserSessionId();
    }
    
    private String getCachedRegionCSSRelativePath(String theme)
    {
        String psSession = getCurrentSessionId();
        return psSession + "/" + theme +  "/" + THEME_REGION_CSS_PATH;
    }

    private File getCachedRegionCSSFileOnly(String theme)
    {
        String path = getCachedRegionCSSRelativePath(theme);
        return new File(getThemesTempRootDirectory() + File.separator  + path);

    }
    private File getCachedRegionCSSFile(String theme, boolean overrideCachedFile) throws PSThemeNotFoundException {
        File tempFile = getCachedRegionCSSFileOnly(theme);
        if (tempFile.exists() && (!overrideCachedFile))
            return tempFile;
        
        File cssFile = getRegionCssFileOrNull(theme);
        if (cssFile != null)
            cssFileService.copyFile(cssFile.getAbsolutePath(), tempFile.getAbsolutePath());
        else
            cssFileService.copyFile(null, tempFile.getAbsolutePath());
        return tempFile;
    }
    
    /**
     * Gets the File pointer to the default theme root
     *  
     * @author federicoromanelli 
     * 
     * @return the File pointer to the default_theme folder, never <code>null</code>
     */  
    protected File getOriginalThemeFolder() 
    {
        return getDefaultThemeRoot();
    }
    
    /**
     * Calculates the newThemeName if the folder already exists in
     * <INSTALL_DIR>/web_resources/themes.
     * 
     * The new name is the first available folder (non existing one)
     * using the following pattern: <themeName>-# (where # starts with 1)
     *  
     * @author federicoromanelli 
     * @param themeName the original name of the theme, not blank.
     * 
     * @return the File pointer to the new theme folder, never <code>null</code>
     */    
    protected File getNewThemeFolder(String themeName) 
    {
        File root = getThemesRoot();
        File themeFolder = new File(root, themeName);
        int i = 0;
        while (themeFolder.exists())
        {
            i++;
            themeFolder = new File(root, themeName + "-" + i);
        }

        return themeFolder;
    
    }
    
    protected File getThemeFolder(String themeName) throws PSThemeNotFoundException {
        File root = getThemesRoot();
        File themeFolder = new File(root, themeName);
        if (! themeFolder.isDirectory() )
            throw new PSThemeNotFoundException("Cannot find theme folder for theme: \"" + themeName + "\".");
        
        return themeFolder;
    }
    
    /**
     * Gets the CSS file for the specified theme. The CSS file must be under the
     * theme's directory. If there is only one CSS file in the directory, this
     * file will be returned, otherwise, the directory will be searched for a
     * file named "{theme name}".css". This file will be returned if it exists.
     * If the file is not found, the first file (alphabetically) with extension
     * ".css" will be returned.
     * 
     * @param themeName the name of the theme, assumed not blank.
     * 
     * @return the CSS file. Never <code>null</code>.
     * @throws PSThemeNotFoundException If the css file cannot be found
     */
    private File getCssFile(String themeName) throws PSThemeNotFoundException {
        File themeFolder = getThemeFolder(themeName);
        ThemeFileFilter filter = new ThemeFileFilter(new String[]{THEME_CSS_EXTENSION});
        File[] cssFiles = themeFolder.listFiles(filter);
        if (cssFiles.length == 1)
        {
            return cssFiles[0];
        }
        
        File namedCssFile = new File(themeFolder, themeName + THEME_CSS_EXTENSION);
        if (namedCssFile.exists())
        {
            return namedCssFile;
        }
        
        if (cssFiles.length > 0)
        {
            Arrays.sort(cssFiles);
            return cssFiles[0];
        }
        
        String msg = "Cannot find CSS file for theme: \"" + themeName + "\".";
        log.warn(msg);
        throw new PSThemeNotFoundException(msg);
    }

    /*
     * //see base interface method for details
     */
    public PSTheme load(String name) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        PSTheme themeCSS = new PSTheme();
        themeCSS.setTheme(name);
        
        // if the css file cannot be found, the PSThemeNotFoundException is thrown
        PSThemeSummary sum = find(name);
        File cssFile = getCssFile(sum.getName());
        
        String css;
        try
        {
            css = FileUtils.readFileToString(cssFile, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new DataServiceLoadException("Failed to load theme: " + name, e);
        }
        
        themeCSS.setCSS(css);
        
        return themeCSS;
    }
    
    /**
     * The filter used to look for the thumb image or CSS files for a given theme.
     *
     * @author YuBingChen
     */
    private class ThemeFileFilter implements FilenameFilter
    {
        private String[] validExtensions;
        
        /**
         * Creates an instance of the theme name filter.
         * 
         * @param validExtensions the valid file extensions for the filter, it
         * is either {@link #THEME_CSS_EXTENSION} or
         * {@link #THEME_THUMB_EXTENSIONS}.
         */
        public ThemeFileFilter(String[] validExtensions)
        {
            notNull(validExtensions);
            notEmpty(validExtensions);
            
            this.validExtensions = validExtensions;
        }
        
        /*
         * //see base interface method for details
         */
        public boolean accept(File dir, String nameAndExtension)
        {
            for (String extension : validExtensions)
            {
                if (nameAndExtension.endsWith(extension))
                    return true;
            }

            return false;
        }
    }
    
    /**
     * Gets the URL of the thumb image of the given theme.
     * The image file is the 1st file find under the theme directory and
     * the file extension must be one of the {@link #THEME_THUMB_EXTENSIONS}.
     *  
     * @param themesRoot the root of all themes, assumed not <code>null</code>.
     * @param themeName the name of the theme, not blank.
     * 
     * @return the URL of the thumb image, may be <code>null</code> if the
     * thumb image does not exist for the specified theme.
     */
    private String getThumbUrl(File themesRoot, String themeName)
    {
        String imgDirPath = File.separator  + themeName;
        File imgDir = new File(themesRoot, imgDirPath);
        if (!imgDir.exists())
            return null;
        
        ThemeFileFilter filter = new ThemeFileFilter(THEME_THUMB_EXTENSIONS);
        File[] imgs = imgDir.listFiles(filter);
        if(imgs != null) {
            if (imgs.length > 0) {
                File imgFile = imgs[0];
                return getThemesRootRelativeUrl() + imgDirPath + "/" + imgFile.getName();
            }
        }
        log.debug("Cannot find thumbnail image for theme '{}'", themeName );
        
        return null;
    }
    
    @Override
    public PSThemeSummary find(String id) throws DataServiceLoadException, DataServiceNotFoundException, PSValidationException {
        rejectIfNull("find", "id", id);
    
        File themeFolder = getThemeFolder(id);
        PSThemeSummary sum = new PSThemeSummary();
        loadThemeSummary(themeFolder, sum);
        return sum;
    }
    
    public PSThemeSummary create(String newTheme,
            String existingTheme) throws DataServiceLoadException,
            DataServiceNotFoundException, DataServiceSaveException
    {
        notEmpty(newTheme);
        notEmpty(existingTheme);
        
        // get the existing theme directory
        File existingThemeFolder = getThemeFolder(existingTheme);
        
        // get the new theme directory
        File newThemeFolder = new File(getThemesRoot(), newTheme);
        
        try
        {
            // create the new theme directory and copy the theme
            FileUtils.copyDirectory(existingThemeFolder, newThemeFolder, false);
            
            return find(newTheme);
        }
        catch (IOException | PSValidationException e)
        {
            throw new DataServiceSaveException("Could not create theme : " + newTheme, e);
        }
    }
    
    /* (non-Javadoc)
     * @see com.percussion.theme.service.IPSThemeService#createFromDefault(java.lang.String)
     */   
    public PSThemeSummary createFromDefault(String newTheme) throws DataServiceLoadException,
            DataServiceNotFoundException, DataServiceSaveException
    {
        notEmpty(newTheme);
        
        // get the existing theme directory
        File existingThemeFolder = getOriginalThemeFolder();
        
        // get the new theme directory
        File newThemeFolder = getNewThemeFolder(newTheme);
        
        try
        {
            // create the new theme directory and copy the theme
            FileUtils.copyDirectory(existingThemeFolder, newThemeFolder, false);
            
            return find(newThemeFolder.getName());
        }
        catch (IOException | PSValidationException e)
        {
            throw new DataServiceSaveException("Could not create theme : " + newTheme, e);
        }
    }
    

    public void delete(String theme) throws DataServiceNotFoundException, DataServiceDeleteException {
        notEmpty(theme);
        
        // check if the theme folder exists or not
        File themeFolder = null;
        
        try 
        {
            // get the theme directory
            themeFolder = getThemeFolder(theme);
        }
        catch(PSThemeNotFoundException e)
        {
            /*
             * This means that the folder does not exist. So we can silently
             * ignore this exception and return. Issue CM-276
             */
            return;
        }
        
        try
        {
            // At this point we can be sure that the folder exists, so we can delete it
            FileUtils.deleteDirectory(themeFolder);
        }
        catch (IOException e)
        {
            throw new DataServiceDeleteException("Could not delete theme : " + theme, e);
        }
    }
    
    public String getThemeRootUrl(String themeName)
    {
        notEmpty(themeName);
        
        return getThemesRootRelativeUrl() + "/" + themeName;
    }
    
    public String getThemeRootDirectory(String themeName)
    {
        notEmpty(themeName);
        
        return getThemesRootDirectory() + "/" + themeName;
    }

 
    public PSRegionCSS getRegionCSS(String theme, String templatename,
           String outerregion,  String region) throws PSThemeNotFoundException {
        File cssFile = getCachedRegionCSSFile(theme, false);        
        PSRegionCSS regionCSS = cssFileService.findRegionCSS(outerregion, region, cssFile.getAbsolutePath());
        if (regionCSS == null)
            return new PSRegionCSS();
        
        return regionCSS;
    }

  
    public void saveRegionCSS( String theme,  String templatename,
            PSRegionCSS regionCSS) throws PSThemeNotFoundException {
        log.debug("save region CSS: " + regionCSS.getOuterRegionName() + ", " + regionCSS.getRegionName());
        
        File cssFile = getCachedRegionCSSFile(theme, false);
        cssFileService.save(regionCSS, cssFile.getAbsolutePath());
    }

    
    public void deleteRegionCSS(String theme, String templatename,
             String outerregion, String region) throws PSThemeNotFoundException {
        log.debug("delete region CSS:{} , {}",outerregion ,region);

        File cssFile = getCachedRegionCSSFile(theme, false);
        cssFileService.delete(outerregion, region, cssFile.getAbsolutePath());
    }


    public void mergeRegionCSS( String theme, String templateId, PSRegionCssList deletedRegions) throws PSThemeNotFoundException {
        log.debug("merge region CSS: {} {} ", theme , templateId);
        
        File tempFile = getCachedRegionCSSFile(theme, false);
        File cssFile = getRegionCssFile(theme); 
        if (templateService != null)
        {
            // this is in server environment
            PSTemplate template = templateService.load(templateId);
            cssFileService.mergeFile(template.getRegionTree(), tempFile.getAbsolutePath(), cssFile.getAbsolutePath());
            // Check for deleted regions
            for (PSRegionCSS deletedRegion : deletedRegions.getRegions())
            {
                String outerregion = deletedRegion.getOuterRegionName();
                String region = deletedRegion.getRegionName();
                PSRegionCSS regionCSS = cssFileService.findRegionCSS(outerregion, region, tempFile.getAbsolutePath());
                if (regionCSS == null)
                {
                    cssFileService.delete(outerregion, region, cssFile.getAbsolutePath());
                }
            }
        }
        else
        {
            // this is in unit test environment
            cssFileService.copyFile(tempFile.getAbsolutePath(), cssFile.getAbsolutePath());
        }
    }

    public void prepareForEditRegionCSS(String theme, String templatename) throws PSThemeNotFoundException {
        log.debug("prepareForEdit for '{}'", theme );
        
        getCachedRegionCSSFile(theme, true);
    }

    
    public void clearCacheRegionCSS( String theme, String templatename)
    {
        log.debug("clearCache for '{}", theme );
        
        File sessionDir = new File(getThemesTempRootDirectory() + File.separator + getCurrentSessionId());
        if (sessionDir.exists())
        {
            FileUtils.deleteQuietly(sessionDir);
        }
    }
    
    @Override
    public List<PSRichTextCustomStyle> getRichTextCustomStyles()
    {
        return getCustomStyles();
    }

    /**
     * Builds the list of rich text custom styles by loading the properties file, optimizes it by checking the
     * lastModified date, the style file is locally cached in the service.
     * If there is any error loading the file logs the error and returns an empty list.
     * @return List of PSRichTextCustomStyle never <code>null</code>, may be empty.
     */
    private List<PSRichTextCustomStyle> getCustomStyles()
    {
        List<PSRichTextCustomStyle> rtStyles = new ArrayList<>();
        if(richTextStylesFile == null)
        {
            try
            {
                richTextStylesFile = new File(getCustomStylesFolderPath() + File.separator + "PercRichTextCustomStyles.properties");
            }
            catch(IllegalArgumentException ie)
            {
                log.error("PercRichTextCustomStyles.properties file does not exist under rx_resources\\css folder, " +
                		"custom formats for rich text editors will be blank.", ie);
            }
        }
        //If the file doesn't exist return empty styles list
        if(richTextStylesFile == null)
            return rtStyles;
        
        //Load the file if not loaded or modified after the last load
        if(richTextStylesLastModified == null || richTextStylesFile.lastModified() > richTextStylesLastModified)
        {
            Properties props = new Properties();
            try
            {
                try(FileInputStream fis = new FileInputStream(richTextStylesFile)) {
                    props.load(fis);
                }

                richTextStylesLastModified = richTextStylesFile.lastModified();
                for(Entry<Object,Object> prop : props.entrySet())
                {
                    PSRichTextCustomStyle rtStyle = new PSRichTextCustomStyle();
                    rtStyle.setClassName((String) prop.getKey());
                    rtStyle.setClassLabel((String) prop.getValue());
                    rtStyles.add(rtStyle);
                    rtCustomStyles.clear();
                    rtCustomStyles.addAll(rtStyles);
                }
            }
            catch (FileNotFoundException e)
            {
                log.error("PercRichTextCustomStyles.properties file does not exist under rx_resources\\css folder, " +
                        "custom formats for rich text editors will be blank.", e);
            }
            catch (IOException e)
            {
                log.error("Exception occurred while reading PercRichTextCustomStyles.properties file from rx_resources\\css folder, " +
                        "custom formats for rich text editors will be blank.", e);
            }
        }
        else
        {
            rtStyles.addAll(rtCustomStyles);
        }
        class CustomComparator implements Comparator<PSRichTextCustomStyle> {
            @Override
            public int compare(PSRichTextCustomStyle o1, PSRichTextCustomStyle o2) {
                return o1.getClassLabel().compareTo(o2.getClassLabel());
            }
        }
        Collections.sort(rtStyles, new CustomComparator());
        return rtStyles;
    }

    public void setTemplateService(IPSTemplateService templateServce)
    {
        this.templateService = templateServce;
    }
    

    /**
     * The root of all themes.
     * 
     * @return the root, never <code>null</code>.
     */
    private File getThemesRoot()
    {
        if (themesRoot == null)
            return new File(getThemesRootDirectory());
        
        return themesRoot;
    }
    
    private File getDefaultThemeRoot()
    {
        return new File(getDefaultThemeRootDirectory());
    }    
    
    public String getThemesRootRelativeUrl()
    {
        return themesRootRelativeUrl;
    }

    @Value("/web_resources/themes")
    public void setThemesRootRelativeUrl(String themesRootRelativeUrl)
    {
        this.themesRootRelativeUrl = themesRootRelativeUrl;
    }

    public String getThemesRootDirectory()
    {
        return themesRootDirectory;
    }
    @Value("${rxdeploydir}/web_resources/themes")
    public void setThemesRootDirectory(String themesRootDirectory)
    {
        this.themesRootDirectory = themesRootDirectory;
    }

    public String getDefaultThemeRootDirectory()
    {
        return defaultThemeRootDirectory;
    }

    @Value("${rxdeploydir}/rx_resources/default_theme")
    public void setDefaultThemeRootDirectory(String defaultThemesRootDirectory)
    {
        this.defaultThemeRootDirectory = defaultThemesRootDirectory;
    }

    public String getThemesTempRootDirectory()
    {
        return themesTempRootDirector;
    }
    @Value("${rxdeploydir}/sys_resources/temp/themes")
    public void setThemesTempRootDirectory(String tempRootDir)
    {
        themesTempRootDirector = tempRootDir;
    }
    
    public String getThemesTempRootRelativeUrl()
    {
        return themesTempRootRelativeUrl;
    }

    @Value("/sys_resources/temp/themes")
    public void setThemesTempRootRelativeUrl(String url)
    {
        themesTempRootRelativeUrl = url;
    }
    public String getCustomStylesFolderPath()
    {
    	return customStylesFolderPath;
    }
    
    @Value("${rxdeploydir}/rx_resources/css")
    public void setCustomStylesFolderPath(String csFolderPath)
    {
    	this.customStylesFolderPath = csFolderPath;
    }
    
    private String themesRootRelativeUrl;
    private String themesRootDirectory;
    private String defaultThemeRootDirectory;
    private String themesTempRootDirector;
    private String themesTempRootRelativeUrl;
    private Long richTextStylesLastModified;
    private File richTextStylesFile;
    private List<PSRichTextCustomStyle> rtCustomStyles = new ArrayList<>();
    private String customStylesFolderPath;
    
    private PSRegionCSSFileService cssFileService = new PSRegionCSSFileService();
    
    /**
     * The root directory of all themes, initialized by {@link #getThemesRoot()},
     * never modified after that.
     */
    private File themesRoot = null;
    
    /**
     * Template service, expected to be set (or wired) by sprint.
     */
    private IPSTemplateService templateService = null;
        
    /**
     * The file extension for the master CSS file of a theme.
     */
    private static final String THEME_CSS_EXTENSION = ".css";
    
    /**
     * The set file extensions that can be used for the thumb-nail image of a theme.
     */
    private static final String[] THEME_THUMB_EXTENSIONS = new String[]{".png", ".gif", ".jpg", ".jpeg"};
    
    /**
     * The relative path to the region CSS file. This is relative to current theme folder.  
     */
    public static final String THEME_REGION_CSS_PATH = "perc/perc_region.css";
    
    /**
     * Logger for this service.
     */
    public static final Logger log = LogManager.getLogger(PSThemeService.class);

    


}
