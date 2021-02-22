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

import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogEntryType;
import com.percussion.sitemanage.importer.helpers.impl.PSImportThemeHelper;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;

/**
 * Parser class that will read css files and update paths to a new location
 * after importing a theme.
 * 
 * @author Santiago M. Murchio
 * @author Luis Mendez
 * @author Ignacio Erro
 * 
 */
public class PSCSSParser
{
    /**
     * Match any url in the css document. The url can be in an '@import ...;'
     * statement or in a url(...) statement.
     */
    private static final String REGEX = "@import([^;]*);|url\\s*\\(([^\\)]*)\\)";

    /**
     * Matches any @import ...; statement. Starting with '@import' until the
     * next semicolon.
     */
    private static final String IMPORT_REGEX = "@import([^;]*);|@import url\\s*\\(([^\\)]*)\\)";

    /**
     * Matches any statement like url (...). Starting with 'url', followed by
     * one or more spaces, between parentheses.
     */
    private static final String URL_REGEX = "url\\s*\\(([^\\)]*)\\)";

    private static final Pattern IMPORT_PATTERN = Pattern.compile(IMPORT_REGEX);

    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEX);

    private List<String> processed = new ArrayList<>();

    private Map<String, String> imagesToDownload = new HashMap<>();

    private String siteName;
    
    private String themeRootDirectory;

    private String themeRootUrl;

    private IPSSiteImportLogger logger;

    private IPSFileDownloader fileDownloader = new PSFileDownloader();

    /**
     * Constructor, builds an instance using the given parameters.
     * 
     * @param siteName the name of the imported site. Cannot be
     *            <code>null</code> or empty.
     * @param themeRootDirectory the theme root on the file system. Cannot be
     *            <code>null</code> or empty.
     * @param themeRootUrl the theme root URL that is relative to any site that
     *            references the theme. Must not be <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to use for logging.
     */
    public PSCSSParser(String siteName, String themeRootDirectory, String themeRootUrl, IPSSiteImportLogger logger)
    {
        notNull(siteName);
        notNull(themeRootDirectory);
        notNull(themeRootUrl);
        notNull(logger);

        this.siteName = siteName;
        this.themeRootDirectory = themeRootDirectory;
        this.themeRootUrl = themeRootUrl;
        this.logger = logger;
    }

    /**
     * Parse css file.
     * 
     * @param cssFiles a <code>Map<String, String></code> of css files. The
     *            original url is passed as a key and the local location as
     *            value. Never <code>null</code>
     * @return a {@link Map}<{@link String}, {@link String}> of images to be
     *         downloaded by {@link PSFileDownloader}
     */
    public Map<String, String> parse(Map<String, String> cssFiles)
    {
        for (String cssURL : cssFiles.keySet())
        {
            String cssFile = cssFiles.get(cssURL);
            logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                    "Processing CSS file: " + cssFile + ".");

            String cssText = "";

            try
            {
                // Read from disk.
                cssText = loadFileFromDisk(cssFile);
                process(cssFile, cssText, createURLConverter(cssURL));
            }
            catch (IOException io)
            {
                logger.appendLogMessage(PSLogEntryType.ERROR, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Error loading " + cssFile + ".");
                logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Error loading " + cssFile + ": " + io.getLocalizedMessage());                
            }
            catch (Exception e)
            {
                logger.appendLogMessage(PSLogEntryType.ERROR, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Error processing css file: " + cssFile + ".");
                logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Error processing css file: " + cssFile + ": " + e.getLocalizedMessage());
            }
        }
        return imagesToDownload;
    }

    /**
     * Parse css embedded in html header.
     * 
     * @param cssUrl the base url. Never <code>null</code>
     * @param cssText the inline css from the html header. Never
     *            <code>null</code>
     * @return a {@link PSPair}<{@link Map}<{@link String}, {@link String}>}>
     *         object containing a map of images to be downloaded by
     *         {@link PSFileDownloader} and the updated css with new paths.
     */
    public PSPair<Map<String, String>, String> parse(String urlBase, String cssText)
    {
        logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                "Processing inline CSS : " + urlBase + ".");

        String cssParsed = "";

        try
        {
            cssParsed = process(cssText, createURLConverter(urlBase));
        }
        catch (Exception e)
        {
            logger.appendLogMessage(PSLogEntryType.ERROR, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                    "Failed to process inline css for " + urlBase + ".");
            logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                    "Failed to process inline css for " + urlBase + ": " + e.getLocalizedMessage());
        }

        return new PSPair<>(imagesToDownload, cssParsed);
    }

    /**
     * Creates an instance of {@link PSURLConverter} with the given base Url.
     * 
     * @param baseUrl a {@link String}, assumed not <code>null</code>.
     * @return an instance of {@link PSURLConverter}, never <code>null</code>.
     */
    private PSURLConverter createURLConverter(String baseUrl)
    {
        return new PSURLConverter(baseUrl, siteName, themeRootDirectory, themeRootUrl, logger);
    }

    /**
     * Process the css file. Loads the file, update it and then saves it back.
     * 
     * @param cssFile the file location in the filesystem. Never
     *            <code>null</code>
     * @param cssText the css file content to be parsed. Never <code>null</code>
     * @return a {@link Map}<{@link String}, {@link String}> object containing
     *         images to be downloaded.
     * @throws IOException
     */
    private Map<String, String> process(String cssFile, String cssText, PSURLConverter urlConverter)
    {
        StringBuffer sb = getCssParsed(cssText, urlConverter);
        try
        {
            // Save the css file.
            saveFile(sb, cssFile);
        }
        catch (Exception e)
        {
            logger.appendLogMessage(PSLogEntryType.ERROR, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                    "Error saving " + cssFile + ".");
            logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                    "Error saving " + cssFile + ": " + e.getLocalizedMessage());
        }

        return imagesToDownload;
    }

    /**
     * Process the inline css. Reads the content, update it and returns the
     * udpated content.
     * 
     * @param cssText the css file content to be parsed. Never <code>null</code>
     * @param urlConverter a {@link PSURLConverter} object. Never
     *            <code>null</code>
     * @return the updated css.
     * @throws IOException
     */
    private String process(String cssText, PSURLConverter urlConverter)
    {
        return getCssParsed(cssText, urlConverter).toString();
    }

    /**
     * Parse the CSS text.
     * 
     * @param cssText the css file content to be parsed. Never <code>null</code>
     * @param urlConverter a {@link PSURLConverter} object. Never
     *            <code>null</code>
     * @return the updated css.
     * @throws IOException
     */
    private StringBuffer getCssParsed(String cssText, PSURLConverter urlConverter)
    {
        // Match imports and url
        Pattern p = Pattern.compile(REGEX);
        Matcher m = p.matcher(cssText);
        StringBuffer sb = new StringBuffer();
        while (m.find())
        {
            // Calculate the new path
            String updatedPath = updatePath(m.group().toString(), urlConverter);

            m.appendReplacement(sb, Matcher.quoteReplacement(updatedPath));
        }
        m.appendTail(sb);
        return sb;
    }

    /**
     * Updates the current item's path. Uses {@link PSURLConverter} to get the
     * new path based on the new location. If path is absolute, binary image or
     * malformed, it is not modified.
     * 
     * @param quote a string which matches with any of the possible urls.
     * @param urlConverter a {@link PSURLConverter} object. Never
     *            <code>null</code>
     * @return the updated path based on the new location.
     * @throws IOException
     */
    private String updatePath(String quote, PSURLConverter urlConverter)
    {
        // If matches with @import ...;
        Matcher importMatcher = IMPORT_PATTERN.matcher(quote);

        // Handle the import match.
        if (importMatcher.matches())
        {
            String importUrl = importMatcher.group(1).trim();
            Matcher urlMatcher = URL_PATTERN.matcher(importUrl);
            if (urlMatcher.matches())
            {
                // @import url('abc.css');
                return updateImports(quote, removeQuotes(urlMatcher.group(1)).trim(), urlConverter);
            }
            else
            {
                // @import "abc.css";
                return updateImports(quote, removeQuotes(importMatcher.group(1)).trim(), urlConverter);
            }
        }

        // If matches with url(...)
        Matcher urlMatcher = URL_PATTERN.matcher(quote);

        // Handle the url match.
        if (urlMatcher.matches())
        {

            return updateUrl(quote, urlMatcher, urlConverter);
        }

        return quote;
    }

    /**
     * Updates the url path if it belongs to a import tag.
     * 
     * @param quote
     * @param importMatcher
     */
    private String updateImports(String quote, String resourceUrl, PSURLConverter urlConverter)
    {
        // Resource url can be blank if the path is absolute, binary image
        // and malformed url. In that case we don't update the path.
        String importUrl = urlConverter.getFullUrl(resourceUrl);

        if (!isBlank(importUrl) && !processed.contains(importUrl))
        {
            // Already processed css are not parsed again to prevent loops.
            processed.add(importUrl);
            
            String importPath = urlConverter.getFileSystemPathForCss(importUrl);
            
            // To avoid processing css files more than one time.
            if (fileExists(importPath))
                return getImportStatement(urlConverter.convertToThemeLinkForCss(importUrl));
            
            fileDownloader.downloadFile(importUrl, importPath);

            String cssText = "";
            try
            {
                cssText = loadFileFromDisk(importPath);
            }
            catch (IOException io)
            {
                logger.appendLogMessage(PSLogEntryType.ERROR, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Error loading " + resourceUrl + ".");
                logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Error loading " + resourceUrl + ": " + io.getLocalizedMessage());
            }

            // Don't update absolute paths.
            if (isValidURL(resourceUrl))
            {
                // process import files recursively
                imagesToDownload.putAll(process(importPath, cssText, createURLConverter(importUrl)));

                return getImportStatement(resourceUrl);
            }
            else
            {
                // process import files recursively
                imagesToDownload.putAll(process(importPath, cssText, createURLConverter(importUrl)));
    
                String updatedLink = urlConverter.convertToThemeLinkForCss(importUrl);
                logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Image url updated from: " + resourceUrl + " to " + updatedLink);
                return getImportStatement(updatedLink);
            }
        }
        else
        {
            if (processed.contains(importUrl))
            {
                // Css was already processed. Just return updated path.
                return getImportStatement(urlConverter.convertToThemeLinkForCss(importUrl));
            }
        }
        return quote;
    }

    /**
     * @param convertToThemeLinkForCss
     * @return
     */
    private String getImportStatement(String importPath)
    {
        return "@import \"" + importPath + "\";";
    }

    /**
     * @param importPath
     * @return
     */
    private boolean fileExists(String importPath)
    {
        File f = new File(importPath);
        
        return f.exists();
    }

    /**
     * Updates the url path for a url
     * 
     * @param quote
     * @param urlMatcher
     * @param urlConverter
     */
    private String updateUrl(String quote, Matcher urlMatcher, PSURLConverter urlConverter)
    {
        String resourceUrl = removeQuotes(urlMatcher.group(1));

        // Resource url can be blank if the path is absolute, binary image
        // and malformed url. In that case we don't update the path.
        String imageUrl = urlConverter.getFullUrl(resourceUrl);

        if (!isBlank(imageUrl))
        {
            String imagePath = urlConverter.getFileSystemPath(imageUrl);

            imagesToDownload.put(imageUrl, imagePath);

            // Don't update absolute paths.
            if (isValidURL(resourceUrl))
                return "url(" + resourceUrl + ")";
            else
            {
                String updatedLink = urlConverter.convertToThemeLink(imageUrl);
                logger.appendLogMessage(PSLogEntryType.STATUS, PSImportThemeHelper.LogCategory.ParseCSS.getName(),
                        "Image url updated from: " + resourceUrl + " to " + updatedLink);
                return "url(" + updatedLink + ")";
            }
        }
        return quote;
    }

    /**
     * Set fileDownloader. Mostly used by unit test. {@link PSCSSParserTest}
     * 
     * @param fileDownloader, assumed never <code>null</code>
     */
    public void setFileDownloader(IPSFileDownloader fileDownloader)
    {
        this.fileDownloader = fileDownloader;
    }

    /**
     * Saves the file to the disk.
     * 
     * @param sb
     * @param path
     * @throws IOException
     */
    private void saveFile(StringBuffer sb, String path) throws IOException
    {
        PrintWriter out = null;

        try(FileWriter fstream = new FileWriter(path)){
            out = new PrintWriter(fstream);
            out.write(sb.toString());
        }
        catch (IOException e)
        {
            System.err.println("Error: " + e.getMessage());
            throw e;
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
        return;
    }

    /**
     * Remove quotes from urls.
     * 
     * @param url
     * @return the url without quotes.
     */
    private String removeQuotes(String url)
    {
        return url.replace("\"", "").replace("'", "");
    }

    /**
     * Load the css file from filesystem.
     * 
     * @param path
     * @return
     * @throws IOException
     */
    private String loadFileFromDisk(String path) throws IOException
    {
        String cssText = "";

        try(FileInputStream in = new FileInputStream(new File(path))){

            cssText = IOUtils.toString(in);
        }
        catch (IOException e)
        {
            throw e;
        }
        return cssText;
    }
    
    /**
     * Check if the given string is a URL
     * @param path the resource path to determine if it is a local path or a URL. 
     * @return <code>true</code> if the string is a URL, <code>false</code> otherwise.
     */
    private boolean isValidURL(String path)
    {
        try
        {
            new URL(path);
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }
}
