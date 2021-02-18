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

import static org.apache.commons.lang.StringUtils.containsIgnoreCase;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.Validate.notNull;

import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.helpers.impl.PSImportThemeHelper.LogCategory;
import com.percussion.utils.types.PSPair;

import java.net.MalformedURLException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Importer class that will be get the links and scripts from header of a given
 * document. Also the header is updated properly with the new paths for links
 * and scripts.
 * 
 * @author Leonardo Hildt
 * 
 */
public class PSHTMLHeaderImporter
{
    private Document docSource;
    
    private Element docHeader;
    
    private Element docBody;
    
    private String siteUrl;
    
    private String siteName;
    
    PSURLConverter urlConverter;
    
    PSCSSParser cssParser;
    
    IPSSiteImportLogger logger;
    
    private static String CONVERTED_CSS_URL = "Replaced CSS URL from ''{0}'' to ''{1}''.";
    
    private static String CONVERTED_SCRIPT_URL = "Replaced script URL from ''{0}'' to ''{1}''.";
    
    private static String CSS_REL_ATTRIBUTE = "stylesheet";
    private static String END_SUFFIX_FLASH_FILE = ".swf";
    private static String SHORT_ICON_REL_ATTRIBUTE = "shortcut icon";
    
    private static String ICON_REL_ATTRIBUTE = "icon";

    /**
     * Constructor, builds an instance of the importer with the given
     * parameters.
     * 
     * @param docHeader {@link Element} which holds the <header> element from
     *            the imported site. Must not be <code>null</code>.
     * @param siteUrl {@link String} with the url for the site the user is
     *            trying to import. Must not be <code>null</code>.
     * @param siteName {@link String} the name of the site the user is
     *            trying to import. Must not be <code>null</code>.
     * @param themeRootDirectory the theme root on the file system. Cannot be
     *            <code>null</code> or empty.
     * @param themeRootUrl the theme root URL that is relative to any site that
     *            references the theme. Must not be <code>null</code>.
     * @param logger {@link IPSSiteImportLogger} to use for logging.
     */
    public PSHTMLHeaderImporter(Document sourceDoc, String siteUrl, String siteName, String themeRootDirectory, String themeRootUrl,
            IPSSiteImportLogger logger)
    {
        notNull(sourceDoc);
        notNull(siteUrl);
        notNull(siteName);
        notNull(themeRootDirectory);
        notNull(themeRootUrl);
        notNull(logger);
        
        this.docSource = sourceDoc;
        this.docHeader = sourceDoc.head();
        this.docBody = sourceDoc.body();
        
        this.siteName = siteName;
        this.siteUrl = siteUrl;
        this.logger = logger;
        
        urlConverter = new PSURLConverter(this.siteUrl, siteName, themeRootDirectory, themeRootUrl, logger);
        cssParser = new PSCSSParser(siteName, themeRootDirectory, themeRootUrl, logger);
    }
    
    /**
     * Gets the absolute paths of scripts inside the header. The absolute
     * remote URL as well the path to download the CSS file are added into a map
     * object. Also the source link is updated with the new link, to match the
     * correct path once the source document is saved later. If the href
     * attribute is empty or null, then it was ignored and not added into the
     * map.
     * 
     * @return {@link Map}<{@link String}, {@link String}> where the key is the
     *         url of the file, and the value is the absolute local path where
     *         the file is going to be saved to. Never <code>null</code>, but
     *         may be empty if there is no link to add.
     */
    public Map<String, String> getLinkPaths()
    {
        Elements links = this.docSource.select("link");
        
        Map<String, String> linkPaths = new HashMap<>();

        appendHeaderImporterMessage("Starting to process CSS links in the document header.");

        for (Element link : links)
        {
            if (isValidLinkElement(link))
            {
                // Add the paths into the map
                String remoteUrl = urlConverter.getFullUrl(link.attr("href"));
                String fullThemePath = "";
                String convertedLink = "";

                // only append ".css" for stylesheets
                if (isValidCssLinkElement(link))
                {
                    fullThemePath = urlConverter.getFileSystemPathForCss(remoteUrl);
                    convertedLink = urlConverter.convertToThemeLinkForCss(remoteUrl);
                }
                else
                {
					fullThemePath = urlConverter.getFileSystemPath(remoteUrl);
					convertedLink = urlConverter.convertToThemeLink(remoteUrl);
                }
                // Add the link path into the map
                linkPaths.put(remoteUrl, fullThemePath);

                // Log the information related to the processed element
                appendHeaderImporterMessage(MessageFormat.format(CONVERTED_CSS_URL, link.attr("href"), convertedLink));

                // Set the new path for the link being processed
                link.attr("href", convertedLink);
            }
        }
        appendHeaderImporterMessage("Finished the processing for link paths. Processed: " + linkPaths.size()
                + " elements.");
        
        return linkPaths;
    }

    /**
     * Gets the absolute paths for the list of scripts inside the header. The absolute
     * remote URL as well the path to download the CSS file are added into a map
     * object. Also the source link is updated with the new link, to match the
     * correct path once the source document is saved later. If the scr
     * attribute is empty or null, then it was ignored and not added into the
     * map.
     * 
     * @return {@link Map}<{@link String}, {@link String}> where the key is the
     *         URL of the script, and the value is the absolute local path where
     *         the file is going to be saved to. Never <code>null</code>, but
     *         may be empty if there is no scripts to add.
     */
    public Map<String, String> getScriptPaths()
    {
        // Get the script elements from Header and Body
        Elements scripts = docSource.select("script");
        
        Map<String, String> scriptPaths = new HashMap<>();

        appendHeaderImporterMessage("Starting to process script paths in the document.");
        
        for (Element script : scripts)
        {
            if (!isBlank(script.attr("src")))
            {
                // Add the paths into the map
                String remoteUrl = urlConverter.getFullUrl(script.attr("src"));
                
                if(isNotBlank(remoteUrl))
                {
                    String fullThemePath = urlConverter.getFileSystemPath(remoteUrl);
                    scriptPaths.put(remoteUrl, fullThemePath);
                    String convertedLink = urlConverter.convertToThemeLink(remoteUrl);
                    
                    // Log the information related to the processed element
                    appendHeaderImporterMessage(MessageFormat.format(CONVERTED_SCRIPT_URL, script.attr("src"), convertedLink));
                    
                    // Set the new path for the script being processed
                    script.attr("src", convertedLink);
                }
            }
        }
        appendHeaderImporterMessage("Finished the processing for script paths. Processed: " + scriptPaths.size()
                + " elements.");
        
        return scriptPaths;
    }

    /**
     * Processes any inline images inside the Style tag. To get those images
     * replaced properly, calls the CSS parser with the style content, and the
     * updates the Style tag inside the document header. A map is returned so the images
     * 
     * @throws MalformedURLException. If the path for the link cannot be
     *             determined.
     * @return {@link Map}<{@link String}, {@link String}> that holds the images
     *         that need to be downloaded. Never <code>null</code> but may be
     *         empty.
     */
    public Map<String, String> processInlineStyles()
    {
        Map<String, String> processedInlineImages = new HashMap<>();
        
        // Process the URL for images in style tag from Header
        processedInlineImages.putAll(processHeaderInlineStyles());
        
        // Process the URL for images in style tag from Body
        processedInlineImages.putAll(processBodyInlineStyles());
        
        // Process the URL for images in body style attributes
        processedInlineImages.putAll(processBodyStyleAtributes());

        return processedInlineImages;  
    }
    
    /**
     * Uses the {@link PSCSSParser css parser} to process the downloaded css
     * files, to get the images that need to be downloaded.
     * 
     * @param cssFilesMap {@link Map}<{@link String}, {@link String}> map where
     *            the key is the url of the css file, and the value is the local
     *            path. Must not be <code>null</code>.
     * @return {@link Map}<{@link String}, {@link String}> where the key is the
     *         url of the image to be downloaded, and the value is the local
     *         path where it should be downloaded to. Never <code>null</code>
     *         but may be empty.
     */
    public Map<String, String> processCssFiles(Map<String, String> cssFilesMap)
    {
        appendHeaderImporterMessage("Starting to process images included in CSS files.");

        notNull(cssFilesMap);
        Map<String, String> images = cssParser.parse(cssFilesMap);

        appendHeaderImporterMessage("Completed the processing for images in CSS files. Processed: " + images.size()
                + " elements.");
        
        return images;
    }
    
    /**
     * Processes the image URLS referenced in img tags and input type=image that
     * are present in the HTML header and body. In order to get the correct URLs
     * if needed, calls to the PSURLConvert. Then the original URL is replaced
     * in the img tag, and the image is added into the map, to be downloaded.
     * 
     * @throws MalformedURLException. If the path for the link cannot be
     *             determined.
     * @return {@link Map}<{@link String}, {@link String}> that holds the images
     *         that need to be downloaded. Never <code>null</code> but may be
     *         empty.
     */
    public Map<String, String> processHeaderAndBodyImages()
    {
        Map<String, String> imagesMapInline = new HashMap<>();

        // Get all existing img tag elements in the header and body
        Elements imgElements = docSource.getElementsByTag("img");
        
        //Add all the images referenced in input type = image.
        imgElements.addAll(docSource.select("input[type=image]"));

        appendHeaderImporterMessage("Starting to process the images referenced in <img> and <input type=image> tags.");

        for (Element imgElement : imgElements)
        {
            if (!isBlank(imgElement.attr("src")))
            {
                // Add the paths into the map
                String remoteUrl = urlConverter.getFullUrl(imgElement.attr("src"));

                String fullThemePath = urlConverter.getCmsFolderPathForImageAsset(remoteUrl, this.siteName);
                imagesMapInline.put(remoteUrl, fullThemePath);

                // Log the information related to the processed element
                appendHeaderImporterMessage(MessageFormat.format(CONVERTED_SCRIPT_URL, imgElement.attr("src"),
                        fullThemePath));

                // Set the new path for the script being processed
                imgElement.attr("src", fullThemePath);
            }
        }
        appendHeaderImporterMessage("Finished the processing for images referenced in <img> and <input type=image> tags. Processed: "
                + imagesMapInline.size() + " elements.");

        return imagesMapInline;
    }
    
    /**
     * Gets the absolute paths for the list of swf files inside the header. The absolute
     * remote URL as well the path to download the CSS file are added into a map
     * object. Also the source link is updated with the new link, to match the
     * correct path once the source document is saved later. If the scr
     * attribute is empty or null, then it was ignored and not added into the
     * map.
     * 
     * @return {@link Map}<{@link String}, {@link String}> where the key is the
     *         URL of the script, and the value is the absolute local path where
     *         the file is going to be saved to. Never <code>null</code>, but
     *         may be empty if there is no scripts to add.
     */
    public Map<String, String> processFlashFiles(String siteName)
    {
        Elements flashObjects = docSource.select("object");
        
        Map<String, String> embedFlashPaths = new HashMap<>();

        appendHeaderImporterMessage("Starting to process swf files in <object> tags in the document.");
        
        for (Element flash : flashObjects)
        {
            if(!isValidObjectFlash(flash))
                continue;
            
            // Process the data attribute
            embedFlashPaths.putAll(processDataAttribute(flash, siteName));
            
            // Process param movie and embed object
            embedFlashPaths.putAll(processFlashObject(flash, "param[name=movie]", "value", siteName));
            
            // Process embed tag 
            embedFlashPaths.putAll(processFlashObject(flash, "embed", "src", siteName));
        }
        
        appendHeaderImporterMessage("Finished the processing for swf files in <object> tags. Processed: " + embedFlashPaths.size()
                + " elements.");
        
        return embedFlashPaths;
    }
    
    /**
     * Log a new entry in the logger, using the message supplied for the caller of this method.
     * 
     * @param message {@link String} the message to log. Must not be <code>null</code> or empty.
     * 
     */
    private void appendHeaderImporterMessage(String message)
    {
        // Append a new message related to the import header
        this.logger.appendLogMessage(IPSSiteImportLogger.PSLogEntryType.STATUS, LogCategory.ImportHeader.getName(),
                message);
    }
    
    /**
     * Validate if a link element has the attributes to be accepted as a valid
     * link. 
     * 
     * @param link {@link Element} the link element to be validated.
     * 
     * @return {@link Boolean} true if is a valid link. Otherwise false.
     * 
     */
    private boolean isValidLinkElement(Element link)
    {
        if(isValidCssLinkElement(link))
        {
            return true;
        }

        String relAttribute = link.attr("rel");

        if (equalsIgnoreCase(relAttribute, ICON_REL_ATTRIBUTE)
                || equalsIgnoreCase(relAttribute, SHORT_ICON_REL_ATTRIBUTE))
        {
            return true;
        }

        return false;
    }

    /**
     * Validates if the {@link Element} is a valid css link element.
     * 
     * @param link {@link Element} assumed not <code>null</code>.
     * @return <code>true</code> if the link is a valid css link.
     *         <code>false</code> otherwise.
     */
    private boolean isValidCssLinkElement(Element link) 
    {
        String hrefAttribute = link.attr("href");
        String relAttribute = link.attr("rel");

        // Check if the link element is 
        if (!isBlank(hrefAttribute) && containsIgnoreCase(relAttribute, CSS_REL_ATTRIBUTE))
        {
            return true;
        }
        return false;
    }
    
    /**
     * Validates if the {@link Element} is a valid flash object element. In
     * order to validate the supplied element is a valid flash file, the method
     * checks the param movie, as it is a required param for flash object.
     * 
     * @param flashObject {@link Element} assumed not <code>null</code>.
     * @return <code>true</code> if the link is a valid flash file.
     *         <code>false</code> otherwise.
     */
    private boolean isValidObjectFlash(Element flashObject)
    {
        // Check the application type
        String dataAttribute = flashObject.attr("data");

        // Check if the type attribute is flash
        if (!isBlank(dataAttribute) && dataAttribute.endsWith(END_SUFFIX_FLASH_FILE))
        {
            return true;
        }

        // Check the movie param
        Elements movies = flashObject.select("param[name=movie]");

        // At this point we should have only one elements
        for (Element movie : movies)
        {
            String valueAttribute = movie.attr("value");

            // Check if the link element is
            if (!isBlank(valueAttribute) && valueAttribute.endsWith(END_SUFFIX_FLASH_FILE))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Processes the images referenced in the style tag inside the document header.
     * To get those images replaced properly, calls the CSS parser and passes
     * the style content to process and replace the URLs as needed. Then updates
     * the style in the header with the modified content. A map is returned with
     * the images that need to be downloaded.
     * 
     * @return {@link Map}<{@link String}, {@link String}> that holds the images
     *         that need to be downloaded. Never <code>null</code> but may be
     *         empty.
     */
    private Map<String, String> processHeaderInlineStyles()
    {
        Elements styleElements = docHeader.select("style");
        Map<String, String> inlineImagesHeader = new HashMap<>();
        
        appendHeaderImporterMessage("Processing inline stypes included in document header.");
        
        for (Element el : styleElements)
        {
            appendHeaderImporterMessage("Style content before replacing: " + el.data());
            
            // process the content of style tag
            PSPair<Map<String, String>, String> parserResult = cssParser.parse(this.siteUrl, el.data());
            
            
            // modify header content
            el.empty();
            el.appendText(parserResult.getSecond().replace("\"", "'"));
            
            // log the the updated content
            appendHeaderImporterMessage("Style content after replacing: " + parserResult.getSecond());
            
            // save the images we need to download
            inlineImagesHeader.putAll(parserResult.getFirst());
        }

        return inlineImagesHeader;
    }
    
    /**
     * Processes the images referenced in the style tags inside the document body.
     * To get those images replaced properly, calls the CSS parser and passes
     * the style content to process and replace the URLs as needed. Then updates
     * the style in the body with the modified content. A map is returned with
     * the images that need to be downloaded.
     * 
     * @return {@link Map}<{@link String}, {@link String}> that holds the images
     *         that need to be downloaded. Never <code>null</code> but may be
     *         empty.
     */
    private Map<String, String> processBodyInlineStyles()
    {
        Elements styleElements = docBody.getElementsByTag("style");
        Map<String, String> imagesMapInline = new HashMap<>();
        
        appendHeaderImporterMessage("Processing inline styles included in document body.");
        
        for (Element el : styleElements)
        {
            appendHeaderImporterMessage("Style content before replacing: " + el.data());
            
            // process the content of style tag
            PSPair<Map<String, String>, String> parserResult = cssParser.parse(this.siteUrl, el.data());
            
            // modify header content
            el.empty();
            el.appendText(parserResult.getSecond().replace("\"", "'"));
            
            // log the the updated content
            appendHeaderImporterMessage("Style content after replacing: " + parserResult.getSecond());
            
            // save the images we need to download
            imagesMapInline.putAll(parserResult.getFirst());
        }
        
        return imagesMapInline;
    }
    
    /**
     * Processes the images referenced in the style attributes referenced in the
     * document body. To get those images replaced properly, calls the CSS
     * parser and passes the style content to process and replace the URLs as
     * needed. Then updates the style in the body with the modified content. A
     * map is returned with the images that need to be downloaded.
     * 
     * @return {@link Map}<{@link String}, {@link String}> that holds the images
     *         that need to be downloaded. Never <code>null</code> but may be
     *         empty.
     */
    private Map<String, String> processBodyStyleAtributes()
    {
        Elements styleElements = docBody.getElementsByAttribute("style");
        Map<String, String> imagesMapInline = new HashMap<>();
        
        appendHeaderImporterMessage("Processing inline styles attributes in document body.");
        
        for (Element el : styleElements)
        {
            appendHeaderImporterMessage("Style content before replacing: " + el.attr("style"));
            
            // process the content of style tag
            PSPair<Map<String, String>, String> parserResult = cssParser.parse(this.siteUrl, el.attr("style"));
            
            // modify header content
            el.attr("style", parserResult.getSecond());
            
            // log the the updated content
            appendHeaderImporterMessage("Style content after replacing: " + parserResult.getSecond());
            
            // save the images we need to download
            imagesMapInline.putAll(parserResult.getFirst());
        }
        
        return imagesMapInline;
    }
    
    /**
     * Process the attributes of a flash object and update the attribute value
     * with the new location for the swf file. A map is returned with the swf
     * files that need to be downloaded.
     * 
     * @param flash {@link Element} the flash object element to be processed.
     * @param cssQuerr {@link String} the jsoup query used to get the elements
     *            from flash object.
     * @param attribValue {@link String} the attribute value to get and update.
     * @param embedFlashPaths {@link Map} the map of processed paths.
     * @param siteName {@link String} the name of the site.
     * 
     * @return {@link Map}<{@link String}, {@link String}> that holds the swf
     *         files that need to be downloaded. Never <code>null</code> but may
     *         be empty.
     */
    private Map<String, String> processFlashObject(Element flash, String cssQuery, String attribValue, String siteName)
    {
        Elements flashElements = flash.select(cssQuery);
        Map<String, String> flashPaths = new HashMap<>();

        for (Element movie : flashElements)
        {
            if (!isBlank(movie.attr(attribValue)))
            {
                // Add the paths into the map
                String remoteUrl = urlConverter.getFullUrl(movie.attr(attribValue));

                String fullThemePath = urlConverter.getCmsFolderPathForImageAsset(remoteUrl, siteName);
                flashPaths.put(remoteUrl, fullThemePath);

                // Log the information related to the processed element
                appendHeaderImporterMessage(MessageFormat.format(CONVERTED_SCRIPT_URL, movie.attr(attribValue),
                        fullThemePath));

                // Set the new path for the swf movie being processed
                movie.attr(attribValue, fullThemePath);
            }
        }
        return flashPaths;
    }
    
    /**
     * Process the data attribute of a flash object if exists.
     * 
     * @param flash {@link Element} the flash object element to be processed.
     * @param embedFlashPaths {@link Map} the map of processed paths.
     * @param siteName {@link String} the name of the site.
     * 
     * @return {@link Map}<{@link String}, {@link String}> that holds the swf
     *         files that need to be downloaded. Never <code>null</code> but may
     *         be empty.
     */
    private Map<String, String> processDataAttribute(Element flash, String siteName)
    {
        Map<String, String> flashPaths = new HashMap<>();

        // Process data attribute for flash object
        if (!isBlank(flash.attr("data")))
        {
            // Add the paths into the map
            String remoteUrl = urlConverter.getFullUrl(flash.attr("data"));

            String fullThemePath = urlConverter.getCmsFolderPathForImageAsset(remoteUrl, siteName);
            flashPaths.put(remoteUrl, fullThemePath);

            // Log the information related to the processed element
            appendHeaderImporterMessage(MessageFormat.format(CONVERTED_SCRIPT_URL, flash.attr("data"), fullThemePath));

            // Set the new path for the swf file being processed
            flash.attr("data", fullThemePath);
        }
        return flashPaths;
    }

}
