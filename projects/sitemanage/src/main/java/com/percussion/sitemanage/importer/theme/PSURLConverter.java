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
package com.percussion.sitemanage.importer.theme;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.Validate.notNull;
import static org.springframework.util.StringUtils.endsWithIgnoreCase;

import com.percussion.services.assembly.impl.PSReplacementFilter;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.helpers.impl.PSImportThemeHelper.LogCategory;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;

/**
 * Given an Url we can calculate his fully qualified URL, convert it to a theme
 * link and get where the resource should be saved.
 * 
 * @author Luis A. Mendez
 * 
 */
public class PSURLConverter
{
    
    private static final String IMPORT_FOLDER = "/import";
    
    private static final String ASSET_FOLDER = "/Assets/uploads/";
    
    private static int indexOfFiles;
    
    private static final String CSS_EXTENTION = ".css";
    
    private String baseUrl;
    
    private String siteName;

    private String themeRootDirectory;

    private String themeRootUrl;
    
    private IPSSiteImportLogger logger;

    /**
     * @param baseUrl Url to be used as base for relative paths.Cannot be
     *            <code>null</code> or empty.
     * @param siteName the imported site. Cannot be <code>null</code> or empty.
     * @param themeRootDirectory the theme root on the file system. Cannot be
     *            <code>null</code> or empty.
     * @param themeRootUrl the theme root URL that is relative to any site that
     *            references the theme. Must not be <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to use for logging.
     */
    public PSURLConverter(String baseUrl, String siteName, String themeRootDirectory, String themeRootUrl,
            IPSSiteImportLogger logger)
    {
        notNull(baseUrl);
        notNull(themeRootUrl);
        notNull(siteName);
        notNull(themeRootDirectory);
        notNull(logger);

        this.baseUrl = baseUrl;
        this.siteName = siteName;
        this.themeRootDirectory = themeRootDirectory;
        this.themeRootUrl = themeRootUrl;
        this.logger = logger;
        this.indexOfFiles = initializeIndex();
    }

    /**
     * Return the fully qualified URL for a specified resource link.
     * 
     * @param linkPath the resource link. Cannot be <code>null</code> or empty.
     * @return {@link String} never <code>null</code>, may be empty.
     */
    public String getFullUrl(String linkPath)
    {
        notNull(linkPath);

        if (isBlank(linkPath))
            return "";

        try
        {
            URL baseUrl = new URL(this.baseUrl);
            URL resourceUrl = new URL(baseUrl, linkPath);
            String remoteUrl = resourceUrl.toString();
            remoteUrl = remoteUrl.replace("../", "");
            
            return remoteUrl;
        }
        catch (Exception e)
        {
            this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.ERROR, LogCategory.ConvertURL.getName(), "Invalid URL: " + baseUrl);
            return "";
        }
    }

    /**
     * Converts the specified resource link (that is relative to the original
     * site) to the theme link (after the resource has been imported into the
     * theme).
     * 
     * @param resourceUrl the resource URL in question. Cannot be
     *            <code>null</code>, may be empty.
     * @return {@link String} never <code>null</code>, may be empty.
     */
    public String convertToThemeLink(String resourceUrl)
    {
        return getConvertedThemeLink(resourceUrl, false);
    }
    
    /**
     * Convenience method that converts the remote url into the theme url
     * (See {@link #convertToThemeLink(String)}), and adds the
     * {@link FileSuffixes Css suffix} to the link, if needed.
     * 
     * @param remoteUrl the resource URL in question. Cannot be
     *            <code>null</code>, may be empty.
     * @return {@link String} never <code>null</code>, may be empty.
     */
    public String convertToThemeLinkForCss(String remoteUrl)
    {
        String convertedLink = getConvertedThemeLink(remoteUrl, true);
        
        String[] linkAndParameters = convertedLink.split("\\?");
        
        Set<String> suffixes = new HashSet<String>();
        suffixes.add(FileSuffixes.Css.getSuffix());
        suffixes.add(FileSuffixes.CssGz.getSuffix());
        
        linkAndParameters[0] = addSuffixIfNeeded(linkAndParameters[0], suffixes);
        
        return StringUtils.join(linkAndParameters, "?");
    }

    /**
     * Gets the file system path for the specified resource. This is where the
     * resource should be saved.
     * 
     * @param resourceUrl Original resource Url. Cannot be <code>null</code> or
     *            empty.
     * 
     * @return {@link String} never <code>null</code>, may be empty.
     */
    public String getFileSystemPath(String resourceUrl)
    {
        return getConvertedFileSystemPath(resourceUrl, false);
    }

    /**
     * Convenience method to convert the remote url into file system path (See
     * {@link #getFileSystemPath(String)}), and adds the {@link FileSuffixes Css
     * suffix} if needed.
     * 
     * @param resourceUrl Original resource Url. Cannot be <code>null</code> or
     *            empty.
     * @return {@link String} never <code>null</code>, may be empty.
     */
    public String getFileSystemPathForCss(String remoteUrl)
    {
        String fullThemePath = getConvertedFileSystemPath(remoteUrl, true);
        
        Set<String> suffixes = new HashSet<String>();
        suffixes.add(FileSuffixes.Css.getSuffix());
        suffixes.add(FileSuffixes.CssGz.getSuffix());
        
        fullThemePath = addSuffixIfNeeded(fullThemePath, suffixes);
        return fullThemePath;
    }
    
    /**
     * Convenience method to convert the remote url into file system path (See
     * {@link #getFileSystemPath(String)}).
     * 
     * @param resourceUrl Original resource Url. Cannot be <code>null</code> or
     *            empty.
     * @return {@link String} never <code>null</code>, may be empty.
     */
    public String getCmsFolderPathForImageAsset(String resourceUrl, String siteName)
    {
        notNull(resourceUrl);

        if (isBlank(resourceUrl))
            return "";

        try
        {
            URL url = new URL(resourceUrl);
            String savePath = ASSET_FOLDER + siteName + IMPORT_FOLDER;
            return savePath + "/" + url.getHost() + validatePath(url.getPath());
        }
        catch (Exception e)
        {
            this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.ERROR, LogCategory.ConvertURL.getName(),
                    "Invalid URL: " + resourceUrl);
            return "";
        }
    }
    
    /**
     * Converts the specified resource link (that is relative to the original
     * site) to the theme link (after the resource has been imported into the
     * theme).
     * 
     * @param resourceUrl the resource URL in question. Cannot be
     *            <code>null</code>, may be empty.
     * @param getPathFromQuery <code>true</code> if the file is a css and should
     *            be renamed if it is needed. <code>false</code> otherwise.
     * @return {@link String} never <code>null</code>, may be empty.
     */
    private String getConvertedThemeLink(String resourceUrl, boolean getPathFromQuery)
    {
        notNull(resourceUrl);

        if (isBlank(resourceUrl))
            return "";

        try
        {
            URL url = new URL(resourceUrl);
            String importPath = this.themeRootUrl + IMPORT_FOLDER;
            
            if (url.getQuery() != null && getPathFromQuery)
            {
                return importPath + "/" + url.getHost() + "/" + validatePath(getPathFromQuery(false));
            }

            return importPath + "/" + url.getHost() + validatePath(PSReplacementFilter.filter(url.getFile()));
            
        }
        catch (Exception e)
        {
            this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.ERROR, LogCategory.ConvertURL.getName(),
                    "Invalid URL: " + resourceUrl);
            return "";
        }
    }
    
    /**
     * Gets the file system path for the specified resource. This is where the
     * resource should be saved.
     * 
     * @param resourceUrl Original resource Url. Cannot be <code>null</code> or
     *            empty.
     * @param getPathFromQuery <code>true</code> if the file is a css and should
     * be renamed if it is needed. <code>false</code> otherwise.
     * 
     * @return {@link String} never <code>null</code>, may be empty.
     */
    private String getConvertedFileSystemPath(String resourceUrl, boolean getPathFromQuery)
    {
        notNull(resourceUrl);

        if (isBlank(resourceUrl))
            return "";

        try
        {
            URL url = new URL(resourceUrl);
            String savePath = this.themeRootDirectory + IMPORT_FOLDER;
                       
            if (url.getQuery() != null && getPathFromQuery)
            {
                return savePath + "/" + url.getHost() + "/" + validatePath(getPathFromQuery(true));
            }
            else
            {
                return savePath + "/" + url.getHost() + validatePath(PSReplacementFilter.filter(url.getFile()));
            }
        }
        catch (Exception e)
        {
            this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.ERROR, LogCategory.ConvertURL.getName(),
                    "Invalid URL: " + resourceUrl);
            return "";
        }
    }

    /**
     * Adds the desired suffix to the given path, in case it does not already
     * have it.
     * 
     * @param path {@link String} with the path to modify, assumed not
     *            <code>null</code>.
     * @param suffix {@link Set} with the suffix to add, assumed not
     *            <code>null</code>.
     * @return {@link String} with the processed path, may be blank.
     */
    private String addSuffixIfNeeded(String path, Set<String> suffixes)
    {
        for (String suffix : suffixes)
        {
            if(endsWithIgnoreCase(path, suffix))
            {
                return path;
            }
        }
        
        return path.concat(FileSuffixes.Css.getSuffix());
    }

    /**
     * The suffixes (or file extensions) that belong to each processed file type.
     * 
     * @author Santiago M. Murchio
     * 
     */
    public enum FileSuffixes {
        Css(".css"), Js(".js"), CssGz(".css.gz");

        private final String suffix;

        private FileSuffixes(String suffix)
        {
            this.suffix = suffix;
        }
        
        public String getSuffix()
        {
            return suffix;
        }
    }
    
    /**
     * Create a new name for the file. Appends an index and increments.
     *     The index can be incremented if it is needed.
     * 
     * @param incrementIndex <code>true</code> if the index should be incremented.
     *     <code>false</code> otherwise.
     * @return the new name for the file. If it already exists, the suffix
     *     is incremented.
     */
    private String getPathFromQuery(boolean incrementIndex)
    {
        if (incrementIndex)
        {
            return this.siteName + "_" + ++indexOfFiles + CSS_EXTENTION;
        }
        else
        {
            return this.siteName + "_" + indexOfFiles + CSS_EXTENTION;
        }
    }
    
    /**
     * Validate the file path and replace invalid characters.
     * 
     * @param path the path of the file including file name.
     * @return a path including only valid characters.
     */
    private String validatePath(String path)
    {
        String decodedPath = "";
        
        try
        {
            decodedPath = URLDecoder.decode(path, "UTF-8");
        }
        catch (UnsupportedEncodingException uee)
        {
            this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.ERROR, LogCategory.ConvertURL.getName(),
                    "Unable to validate path: " + path);
        }
        
        String returnString = decodedPath.replace(":", "(colon)");
        if (returnString.contains("?"))
        {
            //Things get hairy here.  This is typically auto-generation of files
            String alteredQueryString = new String(returnString.substring(returnString.lastIndexOf("?")));
            returnString = returnString.replace(alteredQueryString, "");
            if (returnString.contains("/"))
            {
                returnString=returnString.substring(0, returnString.lastIndexOf("/") + 1);
            }
            try
            {
                //Given the permutations... the quickest path is to just Base64 the query string.
                alteredQueryString = new String(Base64.encodeBase64(alteredQueryString.getBytes("UTF-8"))); 
                returnString=returnString.substring(0, returnString.lastIndexOf("/") + 1) + alteredQueryString;
            }
            catch (Exception e)
            {
                this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.ERROR, LogCategory.ConvertURL.getName(), 
                        "Unable to properly convert URL to Path for: " + path);
            }    
        }
        return returnString.replace(" ", "-");
    }
    
    /**
     * Some resources has queries in their paths. Their names is generated
     * using the imported site name and a suffix is appended. The suffix needs
     * to be initialized because other files could be downloaded before.
     * The initialization counts the existing files in the theme directory.
     * The suffix is incremented for the next generated name.
     * 
     * @return the last index used to save files.
     */
    private int initializeIndex()
    {
        String themeDirectory = this.themeRootDirectory + IMPORT_FOLDER;
        if (StringUtils.isNotBlank(themeDirectory))
        {
            File folderPath = new File(themeDirectory);
            
            // we need to check if the folderPath is actually a folder or file
            if(folderPath.isFile())
            {
                // the path is a file, we need the parent folder
                folderPath = folderPath.getParentFile();
            }
            
            return getCurrentIndex(folderPath);
        }
        return 0;
    }

    /**
     * Retrieves the current index used as suffix for downloaded files that doesn't have names.
     * @param folderPath the path of the current theme folder.
     * @return the current index.
     */
    private int getCurrentIndex(File folderPath)
    {
        String regex = this.siteName + "[0-9]+" + CSS_EXTENTION;
        int numberOfMatches = 0;
        
        String[] files = folderPath.list();
        
        if (files != null)
        {
            for(String file : files)
            {
                if(this.siteName.equals(file) && numberOfMatches < 0)
                {
                    numberOfMatches = 0;
                }
                else if (Pattern.matches(regex, file))
                {
                    // get the integer value and see if it is the greatest
                    Integer extentionIndex = file.lastIndexOf(CSS_EXTENTION);
                    Integer number = Integer.valueOf(file.substring(this.siteName.length(), extentionIndex));
                    if(number > numberOfMatches)
                    {
                        numberOfMatches = number;
                    }
                }
            }
        }
        return numberOfMatches;
    }
}
