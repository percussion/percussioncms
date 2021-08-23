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
package com.percussion.utils.service.impl;

import com.percussion.rx.publisher.PSRxPublisherServiceLocator;
import com.percussion.security.xml.PSSecureXMLUtils;
import com.percussion.server.PSServer;
import com.percussion.services.pubserver.IPSPubServer;
import com.percussion.services.sitemgr.IPSSite;
import com.percussion.services.utils.general.PSServiceConfigurationBean;
import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.sitemanage.data.PSSiteProperties;
import com.percussion.util.PSProperties;
import com.percussion.utils.string.PSStringUtils;
import com.percussion.xml.PSXmlDocumentBuilder;
import com.percussion.xml.PSXmlTreeWalker;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import static com.percussion.pathmanagement.service.impl.PSSitePathItemService.SITE_ROOT;
import static com.percussion.xml.PSXmlTreeWalker.GET_NEXT_ALLOW_CHILDREN;
import static com.percussion.xml.PSXmlTreeWalker.GET_NEXT_ALLOW_SIBLINGS;
import static org.apache.commons.lang.StringUtils.equalsIgnoreCase;
import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.splitByWholeSeparator;
import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Utility class to handle the paths for the secure sites configuration.
 *
 * @author Santiago M. Murchio
 *
 */
public class PSSiteConfigUtils
{

    private static final Logger log = LogManager.getLogger(PSSiteConfigUtils.class);

    /**
     * Flag file extension. If a file called ${sitename}.tch is found inside the
     * log folder, we know that we need to publish the configurations files.
     */
    public static final String CONFIG_FILE_TOUCHED_EXTENSION = ".tch";

    /**
     * The folder name where the configurations for every site will be held.
     */
    public static final String SITE_CONFIGS_FOLDER_NAME = "SiteConfigs";

    /**
     * The folder name for the logs folder inside SitesConfig.
     */
    public static final String LOG_FOLDER_NAME = "$log$";

    /**
     * The relative path (from the root install dir) of the source folder where
     * the default secure sites configurations files are stored.
     */
    public static final String SECURE_FILES_SOURCE_FOLDER = "sys_resources/webapps/secure/WEB-INF";

    /**
     * The relative path (from the root install dir) of the source folder where
     * the default non secure sites configurations files are stored.
     */
    public static final String NON_SECURE_FILES_SOURCE_FOLDER = "sys_resources/webapps/non-secure/WEB-INF";

    /**
     * The name of the published-date-in-ms property (it will be writing like
     * this in the touched file)
     */
    public static final String PUBLISHED_DATE_IN_MS = "published-date-in-ms";

    private static final String CONFIG_FOLDER = "config";

    private static final String SECURITY_URL_PATTERN_FILE = "security-url-pattern.xml";
    //Added this new file that is a template for security-url-pattern file.....
    private static final String BASE_SECURITY_FILE = "security.xml";
    private static final String ACCESS_GROUP_FILE = "perc-access-group.xml";

    private static final String WEB_XML_FILE = "web.xml";
    private static final String URL_PATTERN = "url-pattern";
    private static final String CACHE_CONTROL_FILTER = "PSCacheControlFilter";
    private static final String FILTER_NAME = "filter-name";
    private static final String FILTER_MAPPING = "filter-mapping";
    private static final String SERVLET_NAME = "servlet-name";
    private static final String NO_SECTIONS_SERVLET = "PSNoSecureSections";

    private static final String IS_AUTHENTICATED = "isAuthenticated()";

    /**
     * Returns the path to the folder were the touched files are stored (without
     * trailing slash), inside the 'SiteConfigs' Path. The folder is
     * ${CM1installRoot}/rxconfig/SiteConfigs/$log$
     *
     * @return a String, never <code>null</code> or empty.
     */
    public static String getSitesTouchedFilesPath()
    {
        String path = getSitesConfigPath();

        path += "/" + LOG_FOLDER_NAME;

        return path;
    }

    /**
     * Returns the path where the configuration for each site will be saved. The
     * path is: CM1InstallRoot/rxconfig/SitesConfig
     *
     * @return a String, never <code>null</code> or empty.
     */
    public static String getSitesConfigPath()
    {
        String path = getRootDirectory();

        path += (path.endsWith("/")) ? PSServer.BASE_CONFIG_DIR : "/" + PSServer.BASE_CONFIG_DIR;

        path += (path.endsWith("/")) ? SITE_CONFIGS_FOLDER_NAME : "/" + SITE_CONFIGS_FOLDER_NAME;

        return path;
    }

    /**
     * Returns the the root install directory of CM1
     *
     * @return a String, never <code>null</code>.
     */
    public static String getRootDirectory()
    {
        return PSServer.getRxDir().getPath();
    }

    /**
     * Builds the path to the Security configuration file so they can be
     * published. The path is: CM1InstallRoot/rxconfig/SitesConfig/sitename
     *
     * @param sitename The site to get its name.
     * @return a String, never <code>null</code>.
     */
    public static String getSecureFilesPath(String sitename)
    {
        String path = getSitesConfigPath();

        path += (path.endsWith("/")) ? sitename : "/" + sitename;

        return path;
    }

    /**
     * Returns the path were the initial configuration files for secure sites
     * are saved in the install directory.
     *
     * @return a {@link File} object. Never <code>null</code>.
     */
    public static File getSourceConfigurationFolder()
    {
        String path = getRootDirectory();

        path += (path.endsWith("/")) ? SECURE_FILES_SOURCE_FOLDER : "/" + SECURE_FILES_SOURCE_FOLDER;

        return new File(path);
    }

    /**
     * Checks if the secure specific site configuration folder exists for the
     * given site.
     *
     * @param site the site we want to check the existance for.
     * @return <code>true</code> if the configuration files exist for the given
     *         site. <code>false</code> otherwise.
     */
    public static boolean configFilesExist(IPSSite site)
    {
        return new File(getSecureFilesPath(site.getName())).exists();
    }

    /**
     * Remove the touched file for the given site. It does not matter if the file
     * exists or not.
     *
     * @param sitename the site for which the touched file will be removed.
     * @throws IOException if an error occurs when removing the file
     */
    public static void removeTouchedFile(String sitename) throws IOException
    {
        File tchFile = getTouchedFile(sitename);
        if(tchFile.exists())
        {
            FileUtils.forceDelete(tchFile);
        }
    }

    /**
     * Returns a {@link File} object for the touched file corresponding to the given
     * file.
     *
     * @param sitename the site for which the touched file will be returned.
     * @return {@link File} object for the touched file. Never <code>null</code>.
     */
    public static File getTouchedFile(String sitename)
    {
        return new File(getSitesTouchedFilesPath(), sitename
                + CONFIG_FILE_TOUCHED_EXTENSION);
    }

    /**
     * Renames the site-specific configuration folder, and removes the touched
     * file for the original site. Does not create the touched file for the new
     * site. Used when renaming a secure site.
     * Renames the site-specific configuration folder, and removes the touched
     * file for the original site. Does not create the touched file for the new
     * site. If the original site configuration folder does not exists, the configuration for the new site name is created.
     * Used when renaming and securing a site.
     *
     * @param srcSite original (or source) site name, assumed not blank.
     * @param destSite the new site name, assumed not blank.
     * @throws IOException if an error occurs when moving the directories.
     */
    public static void renameOrCreateSecureSiteConfiguration(String srcSite, String destSite) throws IOException
    {
        // if the user didn't change the name of the site, we don't need to
        // rename the configuration folders (the renaming operation will throw
        // an error)
        if(equalsIgnoreCase(srcSite, destSite))
        {
            return;
        }

        // just remove the touched file if it exists (this forces the next full
        // publish to copy the corresponding configure files to the live site)
        removeTouchedFile(srcSite);

        File srcDir = getSiteConfigFolder(srcSite);
        if (!srcDir.exists())
        {
            createSecureSiteConfiguration(destSite);
        }
        else
        {
            File destDir = getSiteConfigFolder(destSite);
            FileUtils.moveDirectory(srcDir, destDir);
        }
    }

    /**
     * Renames the site-specific configuration file. In this case for unsecure
     * sites, the touched file will be removed forcing this way the next full
     * publish to copy the default configuration into the live site.
     *
     * @param srcSite original (or source) site name, assumed not blank.
     * @param destSite the new site name, assumed not blank.
     * @throws IOException if an error occurs when removing the touch file
     */
    public static void renameNonSecureSiteConfiguration(String srcSite, String destSite) throws IOException
    {
        // if the user didn't change the name of the site, we don't need to
        // rename the configuration folders (the renaming operation will throw
        // an error)
        if(equalsIgnoreCase(srcSite, destSite))
        {
            return;
        }

        removeTouchedFile(srcSite);
    }

    /**
     * Returns a {@link File} object for the secure configuration folder for the
     * specified site. ${InstallRoot}/rxconfig/SitesConfig/${sitename}
     *
     * @param sitename the name of the site, assumed not blank.
     *
     * @return the configure folder, never <code>null</code>.
     */
    private static File getSiteConfigFolder(String sitename)
    {
        File siteConfigFolder = new File(getSitesConfigPath(), sitename);
        return siteConfigFolder;
    }

    /**
     * Creates site configuration folder and copy the secure sites configuration
     * files for the 1st time. These files will be copied to the remote server
     * as part of the next publishing process.
     *
     * @param sitename the name of the site, assumed not blank.
     *
     * @throws IOException if an error occurs when dealing with creation or
     *             writing of files.
     */
    public static void createSecureSiteConfiguration(String sitename) throws IOException
    {
        File siteConfigFolder = forceMkDirSiteConfig(sitename);
        FileUtils.copyDirectory(getSourceConfigurationFolder(), siteConfigFolder, false);
    }

    /**
     * Remove the entry that belongs to the given server id, for the given site.
     * If the tch file does not exist, or the server entry does not exist, then
     * nothing happens, no error is thrown.
     *
     * @param sitename {@link String} the name of the site. Must not be
     *            <code>null</code>.
     * @param serverId the id of the server we want to remove the entry for. It may or may not exist
     * @+-throws IOException
     */
    public static void removeServerEntry(String sitename, long serverId) throws IOException
    {
        FileOutputStream fos = null;

        File tchFile = getTouchedFile(sitename);
        if(!tchFile.exists())
        {
            return;
        }

        try
        {
            // remove the pair
            PSProperties siteProperties = loadTchFile(tchFile.getPath());
            siteProperties.remove(Long.toString(serverId));

            // save the changes
            fos = new FileOutputStream(tchFile);
            siteProperties.store(fos, "");
            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }

    /**
     * Creates the configuration site-specific folder. If any folder in the path
     * does not exists it will be created.
     *
     * @param sitename the name of the site for which the configuration folder
     *            will be created.
     * @return a {@link File} object representing the created folder.
     * @throws IOException if an error occurs when creating the folder.
     */
    private static File forceMkDirSiteConfig(String sitename) throws IOException
    {
        File siteConfigFolder = getSiteConfigFolder(sitename);
        if(!siteConfigFolder.exists())
        {
            FileUtils.forceMkdir(siteConfigFolder);
        }
        return siteConfigFolder;
    }

    /**
     * Remove the configuration files/folder for the given site. It does not
     * remove the 'tch' file for the given site.
     *
     * @param sitename the name of the given site, assumed not blank.
     *
     * @throws IOException if an error occurs when removing the files
     */
    public static void removeSiteConfiguration(String sitename) throws IOException
    {
        File siteConfigFolder = getSiteConfigFolder(sitename);
        if(siteConfigFolder.exists())
        {
            FileUtils.forceDelete(siteConfigFolder);
        }
    }

    /**
     * Remove the configuration files/folder for the given site. It also removes
     * the 'tch' file for the given site.
     *
     * @param sitename the name of the given site, assumed not blank.
     *
     * @throws IOException if an error occurs when removing the files
     */
    public static void removeSiteConfigurationAndTouchedFile(String sitename) throws IOException
    {
        removeSiteConfiguration(sitename);
        removeTouchedFile(sitename);
    }

    /**
     * Updates the published date for the given server in the 'sitename.tch'
     * file, so the next time it can be compared to the modified date.
     * <p>
     * It puts the current time in milliseconds, taking into account the publish
     * server that is doing the publishing. So the data stored in the file is a
     * set of pairs, key-value, where the key is the publishing server id, and
     * the value is the publishing timestamp.
     * <p>
     * The format of the tch file is a set of <code>serverid=timestamp</code>
     * pairs.
     *
     * @param sitename {@link String} with the name of the site for which the
     *            touched file will be updated. Assumed not <code>null</code>.
     * @param pubServer {@link IPSPubServer} object, to get the id of the
     *            server. It may be <code>null</code>, in which case a default
     *            value of 0 will be use as the server.
     * @throws IOException if an error occurs when writing into the 'tch' file
     */
    public static void updatePublishedDate(String sitename, IPSPubServer pubServer) throws IOException
    {
        File tchFile = getTouchedFile(sitename);
        if(!tchFile.exists())
        {
            createTouchedFile(sitename);
        }

        long serverId = 0;
        if(pubServer != null)
        {
            serverId = pubServer.getServerId();
        }
        setPublishedDateInTouchedFile(tchFile.getPath(), serverId, new Date());
    }

    /**
     * Creates the 'sitename.tch' file under the '$log$' folder.
     *
     * @param sitename the name of the site
     * @throws IOException if an error occurs when creating the file
     */
    public static void createTouchedFile(String sitename) throws IOException
    {
        File tchFile = getTouchedFile(sitename);
        File parent = tchFile.getParentFile();
        if(!parent.exists()){
            FileUtils.forceMkdir(parent);
        }
        tchFile.createNewFile();
    }

    /**
     * Compares the published-date-in-ms retrieved from the corresponding
     * touched file with the modified date of each of the secure config files
     * (if the site is secure) or the default config files (if the site is not
     * secure). If any of those files was modified after the publishing date, it
     * returns <code>true</code>. Otherwise, it returns <code>false</code>. For
     * the first publishing, the published-date-in-ms will be empty, so it
     * returns <code>true</code>. If the 'tch' file for the given site does not
     * exist, it returns <code>true</code>.
     *
     * @param sitename the name of the site for which the touched file will be
     *            queried.
     *
     * @return <code>true</code> if any configuration file was modified after
     *         the publishing date, or if the 'tch' file does not exist.
     *         <code>false</code> otherwise.
     * @throws IOException if an error occurs when reading the files.
     */
    @SuppressWarnings("unchecked")
    public static boolean filesModifiedAfterPublished(String sitename, long serverId) throws IOException
    {
        File tchFile = getTouchedFile(sitename);
        if(!tchFile.exists())
        {
            return true;
        }

        Date publishedDate = getPublishedDateFromTouchedFile(tchFile.getPath(), serverId);

        if (publishedDate == null)
        {
            return true;
        }

        File siteConfigFolder = getSiteConfigFolder(sitename);
        if(!siteConfigFolder.exists())
        {
            siteConfigFolder = getNonSecureConfigurationFolder();
        }

        Collection<File> configFiles = FileUtils.listFiles(siteConfigFolder, null, true);
        for (File configFile : configFiles)
        {
            if (publishedDate.getTime() < configFile.lastModified())
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Returns the default configuration folder for non secure sites.
     *
     * @return a {@link String}, never blank.
     */
    public static File getNonSecureConfigurationFolder()
    {
        String path = getRootDirectory();

        path += (path.endsWith("/")) ? NON_SECURE_FILES_SOURCE_FOLDER : "/" + NON_SECURE_FILES_SOURCE_FOLDER;

        return new File(path);
    }

    /**
     * Copy the secure site configuration folder from the source site to the
     * destination site. The source site must be secure.
     *
     * @param srcSitename the source site name to copy
     * @param destSitename the destination site name
     * @throws IOException if an error occurs when copying the files.
     */
    public static void copySecureSiteConfiguration(String srcSitename, String destSitename) throws IOException
    {
        File sourceConfigFolder = getSiteConfigFolder(srcSitename);
        File siteConfigFolder = forceMkDirSiteConfig(destSitename);
        FileUtils.copyDirectory(sourceConfigFolder, siteConfigFolder, false);
    }

    /**
     * Tries to read the published-date-in-ms property from the given file.
     * Notice that if it is not present, it returns <code>null</code>. The
     * property represents a time in milliseconds. So it builds a {@link Date}
     * object with that given time.
     *
     * @param path {@link String} with the path to the tch file. Assumed not
     *            <code>null</code>.
     * @param serverId the id of the server to use as a key.
     * @return a {@link Date} object. Maybe <code>null</code>
     * @throws IOException
     */
    public static Date getPublishedDateFromTouchedFile(String path, long serverId) throws IOException
    {
        PSProperties properties = new PSProperties(path);
        String dateString = properties.getProperty(Long.toString(serverId));

        if (!isBlank(dateString))
        {
            return new Date(Long.valueOf(dateString));
        }
        return null;
    }

    /**
     * Writes to the site's tch the given date (in milliseconds), for the given
     * server.
     *
     * @param path {@link String} with the path to the tch file. Assumed not
     *            <code>null</code>.
     * @param serverId the id of the server to use as a key.
     * @param date the date to set in the property. Assumed not
     *            <code>null</code>
     *
     * @throws FileNotFoundException if the tch file does not exist
     * @throws IOException if an error occurs when writing to the file
     */
    public static void setPublishedDateInTouchedFile(String path, long serverId, Date date)
            throws FileNotFoundException, IOException
    {

        FileOutputStream fos = null;
        try
        {
            PSProperties properties = loadTchFile(path);
            fos = new FileOutputStream(new File(path));
            properties.setProperty(Long.toString(serverId), Long.toString(date.getTime()));
            properties.store(fos, "");
            fos.flush();
        }
        finally
        {
            if (fos != null)
            {
                fos.close();
            }
        }
    }

    /**
     * It loads the properties files in the given path. This is needed to be
     * able to add properties into it. If this load is not done, the file gets
     * overwritten, loosing previous properties.
     *
     * @param path {@link String} with the path to the tch file. Assumed not
     *            <code>null</code>.
     * @return {@link PSProperties} object, never <code>null</code> but may be
     *         empty if the file does not exist.
     * @throws IOException if an error occurs dealing with files.
     */
    private static PSProperties loadTchFile(String path) throws IOException
    {
        PSProperties properties = new PSProperties();

        File props = new File(path);
        if(!props.exists())
        {
            return properties;
        }

        FileInputStream fis = new FileInputStream(props);
        properties.load(fis);
        fis.close();
        return properties;
    }

    /**
     * Handles the updating of the site properties. Works this way:
     * <ul>
     * <li>If Site renamed:
     * <ul>
     * <li>if secured (or site already was secure): rename config folder and
     * remove old touched file.</li>
     * <li>if unsecured (or site already was unsecure): remove old config folder
     * and remove old touched file. Nothing is created.</li>
     * </ul>
     * </li>
     * <li>If Site not renamed:
     * <ul>
     * <li>if secured: create config folder.</li>
     * <li>if unsecured: remove config folder and remove touched file.</li>
     * </ul>
     * </li> If another property of the site was changed (besides the sitename
     * or the security enabling), the touched file for the site is deleted, to
     * force publishing.
     *
     * @param site the modifiable site object representing the original site
     *            (without applying the update yet). Assumed not
     *            <code>null</code>
     * @param props a {@link PSSiteProperties} object, representing the data
     *            coming from the screen. Assumed not <code>null</code>
     * @throws IOException if an error occurs handling the configuration files.
     */
    public static void updateSiteConfiguration(IPSSite site, PSSiteProperties props) throws IOException
    {
        if(!site.getName().equals(props.getName()))
        {
            if(site.isSecure())
            {
                renameOrCreateSecureSiteConfiguration(site.getName(), props.getName());
            }
            else
            {
                removeSiteConfigurationAndTouchedFile(site.getName());
            }
        }
        else
        {
            if(changeToSecureSite(site, props))
            {
                createSecureSiteConfiguration(site.getName());
            }
            else if(changeToUnsecureSite(site, props))
            {
                removeSiteConfigurationAndTouchedFile(site.getName());
            }
            else
            {
                // a property that is not the name of the site or the security
                // was modified, so force publishing
                removeTouchedFile(site.getName());
            }
        }
    }

    /**
     * Check if the site was unsecure, and now it is secure.
     *
     * @param site the {@link IPSSite} object representing the site before
     *            modifications
     * @param props the {@link PSSiteProperties} object representing the
     *            modifications for the site
     * @return <code>true</code> if the site was not secure, and now it is
     *         secure. <code>false</code> otherwise.
     */
    private static boolean changeToSecureSite(IPSSite site, PSSiteProperties props)
    {
        return !site.isSecure() && props.isSecure();
    }

    /**
     * Check if the site was secure, and now it is not secure.
     *
     * @param site the {@link IPSSite} object representing the site before
     *            modifications
     * @param props the {@link PSSiteProperties} object representing the
     *            modifications for the site
     * @return <code>true</code> if the site was secure, and now it is not
     *         secure. <code>false</code> otherwise.
     */
    private static boolean changeToUnsecureSite(IPSSite site, PSSiteProperties props)
    {
        return site.isSecure() && !props.isSecure();
    }

    /**
     * Creates an object of {@link SecureXmlData} and then overrides the
     * <code>security-url-pattern.xml</code> file with the configuration that
     * corresponds to each section.
     *
     * @param sitename the name of the site. Assumed not blank.
     * @param loginPage the login page set for the site. May be blank.
     * @param loginErrorPage the error page for the login failure set for the
     *            site. May be blank.
     * @param sectionNode the root node of the section tree. Assumed not
     *            <code>null</null>
     * @throws TransformerException if an error occurs when writing the file.
     * @throws SAXException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void updateSecureSectionsConfiguration(String sitename, String loginPage, PSSectionNode sectionNode)
            throws TransformerException, FileNotFoundException, IOException, SAXException
    {
        boolean useHttpsForSecureSite = isUseHttpsForSecureSite();

        SecureXmlData xmlData = buildXmlDataForSite(sitename, loginPage, sectionNode, useHttpsForSecureSite);
        generateSecurityUrlPattern(xmlData);
        updateSecureWebXmlFile(xmlData);
        generateAccessGroupXMLFile(xmlData);
    }

    public static void generateAccessGroupXMLFile(SecureXmlData securityData) throws TransformerException
    {
        Document doc = PSXmlDocumentBuilder.createXmlDocument();
        Element rootElement = getRootElement(doc);
        String access = null;

        doc.appendChild(rootElement);

        Element http = doc.createElement("http");

        rootElement.appendChild(http);

        Map<String, Map<String, String>> sectionMap = securityData.getSecureAndMemberSectionUrls();
        if(sectionMap != null && sectionMap.keySet() != null)
        {
            for (String sectionUrl : sectionMap.keySet())
            {
                Map<String, String> properties = sectionMap.get(sectionUrl);

                access = properties.get(SecureXmlData.SECURE_SECTION_ACCESS_ATTRIBUTE_NAME);
                access = access.substring(access.indexOf('(')+1, access.indexOf(')'));
                http.appendChild(createInterceptUrlElementInGroupFile(doc, access));
            }
        }

        writeAccessGroupsToXmlFile(securityData, doc);
    }


    /**
     * Retrieves the useHttpsForSecureSiteProperty from the configuration bean.
     *
     * @return {@link PSServiceConfigurationBean#isUseHttpsForSecureSite()}
     */
    private static boolean isUseHttpsForSecureSite()
    {
        PSServiceConfigurationBean configurationBean = (PSServiceConfigurationBean) PSRxPublisherServiceLocator
                .getBean("sys_beanConfiguration");
        return configurationBean.isUseHttpsForSecureSite();
    }

    /**
     * Builds an object of {@link SecureXmlData} with the given site properties.
     * This is used when modifying the <code>security-url-pattern.xml</code>
     * file after the site creation.
     *
     * @param sitename the name of the site. Assumed not blank.
     * @param loginPage the login page set for the site. May be blank.
     * @param loginErrorPage the error page for the login failure. May be blank.
     * @param sectionNode the root node of the section tree for the site.
     *            Assumed not <code>null</code>
     * @param useHttpsForSecureSite if <code>true</code>
     * @return an object of {@link SecureXmlData}. Never <code>null</code>.
     */
    public static SecureXmlData buildXmlDataForSite(String sitename, String loginPage, PSSectionNode sectionNode,
                                                    boolean useHttpsForSecureSite)
    {
        SecureXmlData xmlData = new SecureXmlData();
        xmlData.setSitename(sitename);
        xmlData.setUseHttpsForSecureSite(useHttpsForSecureSite);

        if(!isBlank(loginPage))
        {
            xmlData.setLoginPage(loginPage);
        }

        // avoid the root node (/Home)
        for(PSSectionNode section : sectionNode.getChildNodes())
        {
            getSecureAndMembersSections(section, xmlData, sitename);
        }

        return xmlData;
    }

    /**
     * Recursively walks the section tree looking for secure sections. Adds the
     * corresponding data into the {@link SecureXmlData} object passed as a
     * parameter.
     *
     * @param sectionNode a node from the tree section. Assumed not
     *            <code>null</code>.
     * @param xmlData a {@link SecureXmlData} object to save the data. Assumed
     *            not <code>null</code>.
     * @param sitename the name of the site. Assumed not blank.
     */
    private static void getSecureAndMembersSections(PSSectionNode sectionNode, SecureXmlData xmlData, String sitename)
    {
        if (sectionNode.isRequiresLogin())
        {
            String folderPath = splitByWholeSeparator(sectionNode.getFolderPath(), SITE_ROOT + "/"
                    + xmlData.getSitename())[0];
            xmlData.addSecureOrMemberSection(folderPath + "/", sectionNode.getAllowAccessTo());

            // cut the tree as soon as we hit the first secure node
            return;
        }

        // base case
        if (isEmpty(sectionNode.getChildNodes()))
        {
            return;
        }

        for (PSSectionNode child : sectionNode.getChildNodes())
        {
            getSecureAndMembersSections(child, xmlData, sitename);
        }
    }


    /**
     * Generate the cache control filter url-pattern elements for each secure section
     *
     * @param xmlData The site security config info, not <code>null</code>.
     * @throws SAXException
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void updateSecureWebXmlFile(SecureXmlData xmlData) throws FileNotFoundException, IOException, SAXException
    {
        // read in web.xml
        String siteName = xmlData.getSitename();
        Document doc = readSecureWebXmlFile(siteName);

        generateCacheControlFilters(xmlData, doc);

        // write the file back
        writeSecureWebXmlFile(doc, siteName);
    }

    /**
     * Generate the cache control filter url-pattern elements for each secure section
     *
     * @param xmlData The site security config info, not <code>null</code>.
     * @param doc The web.xml document to modify
     */
    public static void generateCacheControlFilters(SecureXmlData xmlData, Document doc)
    {
        // get the filter-mapping element
        PSXmlTreeWalker tree = new PSXmlTreeWalker(doc);
        Element test = tree.getNextElement(FILTER_MAPPING, GET_NEXT_ALLOW_CHILDREN);
        Element filterMapping = null;
        while (test != null)
        {
            Element nameEl = tree.getNextElement(FILTER_NAME, GET_NEXT_ALLOW_CHILDREN);
            if (CACHE_CONTROL_FILTER.equals(tree.getElementData()))
            {
                filterMapping = test;
                tree.setCurrent(test);
                break;
            }

            tree.setCurrent(test);
            test = tree.getNextElement(FILTER_MAPPING, GET_NEXT_ALLOW_SIBLINGS);
        }

        if (filterMapping == null)
        {
            // weird bug if this is missing
            throw new RuntimeException("No filter-mapping element found for PSCacheControlFilter");
        }


        Element newFilterMapping = doc.createElement(FILTER_MAPPING);
        PSXmlDocumentBuilder.addElement(doc, newFilterMapping, FILTER_NAME, CACHE_CONTROL_FILTER);

        // append url-pattern element for each secure section
        Set<String> sectionFilters = xmlData.getSecureAndMemberSectionFilters();
        if (sectionFilters.isEmpty())
        {
            // no sections, so add a dummy servlet-name element to keep tomcat happy
            PSXmlDocumentBuilder.addElement(doc, newFilterMapping, SERVLET_NAME, NO_SECTIONS_SERVLET);
        }
        else
        {
            for (String filter : sectionFilters)
            {
                PSXmlDocumentBuilder.addElement(doc, newFilterMapping, URL_PATTERN, filter);
            }
        }


        Element root = doc.getDocumentElement();
        root.replaceChild(newFilterMapping, filterMapping);
    }

    /**
     * Generates the <code>security-xml-pattern.xml</code> file with the site
     * security configuration information. If the file does not exist, it
     * creates it. If it does exist, it overrides it.
     *
     * @param securityData a {@link SecureXmlData} object that holds the site
     *            config info. Assumed not <code>null</code>.
     * @throws TransformerException if an error occurs when writing the xml
     *             file.
     */
    public static void generateSecurityUrlPattern(SecureXmlData securityData) throws TransformerException
    {

        try {
             File baseSecurityFile = getBaseSecureXmlFile(securityData.getSitename());

            DocumentBuilderFactory documentBuilderFactory = PSSecureXMLUtils.getSecuredDocumentBuilderFactory(
                    false
            );

            DocumentBuilder documentBuilder = null;

            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(baseSecurityFile);
            Node http = document.getDocumentElement().getChildNodes().item(1);
            appendInterceptUrlElements(securityData, document, http);
            appendFormLoginProcessor(securityData, document, http);
            appendFormLoginElement(securityData, document, http);
            appendPortMappingsElement(securityData, document, http);
            appendLogoutElement(securityData, document, http);

            writeToXmlFile(securityData, document);
        } catch (ParserConfigurationException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);

        } catch (SAXException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage());
            log.debug(e.getMessage(), e);
        }

    }

    /**
     * Appends the <code>logout</code> element to the xml dom.
     *
     * @param securityData {@link SecureXmlData} object that holds the
     *            information.
     * @param doc the document of the xml file
     * @param http the {@link Element} where the created ones will be added.
     */
    private static void appendLogoutElement(SecureXmlData securityData, Document doc, Node http)
    {
        Element logout = doc.createElement("security:logout");
        logout.setAttribute("logout-url", securityData.LOGOUT_PROCESSING_URL);
        logout.setAttribute("success-handler-ref", securityData.LOGOUT_SUCCESS_HANDLER_REF);

        http.appendChild(logout);
    }

    private static void appendCsrfProperty(SecureXmlData securityData, Document doc, Element http, boolean disabled)
    {
        Element csrf = doc.createElement("csrf");
        csrf.setAttribute("disabled", ""+disabled);
        http.appendChild(csrf);
    }

    /**
     * Writes the content in the <code>security-url-pattern.xml</code> file. If
     * the file exists, it overrides it. If it does not exist, it creates it.
     *
     * @param securityData {@link SecureXmlData} object that holds the
     *            information.
     * @param doc the document of the xml file
     * @throws TransformerException if an error occurs when writing into the
     *             file.
     */
    private static void writeToXmlFile(SecureXmlData securityData, Document doc) throws TransformerException{
        File secureXmlFile = getSecureXmlFile(securityData.getSitename());
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(secureXmlFile);

        transformer.transform(domSource, streamResult);

    }

    private static void writeAccessGroupsToXmlFile(SecureXmlData securityData, Document doc) throws TransformerException
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        DOMSource source = new DOMSource(doc);

        File secureXmlFile = getAccessGroupXmlFile(securityData.getSitename());
        StreamResult result = new StreamResult(secureXmlFile);

        transformer.transform(source, result);
    }

    private static void writeSecureWebXmlFile(Document doc, String siteName) throws IOException
    {
        FileOutputStream out = new FileOutputStream(getSecureWebXmlFile(siteName));
        try
        {
            PSXmlDocumentBuilder.write(doc, out);
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }

    }

    private static Document readSecureWebXmlFile(String siteName) throws FileNotFoundException, IOException, SAXException
    {
        FileInputStream in = new FileInputStream(getSecureWebXmlFile(siteName));
        try
        {
            return PSXmlDocumentBuilder.createXmlDocument(in, false);
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }
    }

    private static File getSecureWebXmlFile(String siteName)
    {
        String path = getSecureFilesPath(siteName);

        path += "/" + WEB_XML_FILE;

        return new File(path);
    }

    /**
     * Gets the {@link File} object representing the
     * <code>security.xml</code> file that has the bsaic spring security settings.
     *
     * @param sitename the name of the site. Assumed not blank.
     * @return {@link File} object. Never <code>null</code>.
     */
    public static File getBaseSecureXmlFile(String sitename)
    {
        String path = getSecureFilesPath(sitename);

        path += "/" + CONFIG_FOLDER + "/" + BASE_SECURITY_FILE;

        return new File(path);
    }

    /**
     * Gets the {@link File} object representing the
     * <code>security-url-pattern.xml</code> file for the given site.
     *
     * @param sitename the name of the site. Assumed not blank.
     * @return {@link File} object. Never <code>null</code>.
     */
    public static File getSecureXmlFile(String sitename)
    {
        String path = getSecureFilesPath(sitename);

        path += "/" + CONFIG_FOLDER + "/" + SECURITY_URL_PATTERN_FILE;

        return new File(path);
    }

    public static File getAccessGroupXmlFile(String sitename)
    {
        String path = getSecureFilesPath(sitename);

        path += "/" + CONFIG_FOLDER + "/" + ACCESS_GROUP_FILE;

        return new File(path);
    }

    /**
     * Appends the <code>form-login</code> elements to the xml dom.
     *
     * @param securityData {@link SecureXmlData} object that holds the
     *            information.
     * @param doc the document of the xml file
     * @param http the {@link Element} where the created ones will be added.
     */
    private static void appendFormLoginElement(SecureXmlData securityData, Document doc, Node http)
    {
        Element formLogin = doc.createElement("security:form-login");

        String loginPage = securityData.getLoginPage();
        if(!isBlank(loginPage))
        {
            formLogin.setAttribute("login-page", loginPage);
            formLogin.setAttribute("authentication-failure-url", loginPage + securityData.AUTHENTICATION_FAILURE_URL_SUFFIX);
        }

        formLogin.setAttribute("login-processing-url", securityData.LOGING_PROCESSING_URL);
        formLogin.setAttribute("authentication-success-handler-ref", securityData.AUTHENTICATION_SUCCESS_HANDLER_REF);


        http.appendChild(formLogin);
    }

    /**
     * Appends the <code>port-mappings</code> elements to the xml dom.
     *
     * @param securityData {@link SecureXmlData} object that holds the
     *            information.
     * @param doc the document of the xml file
     * @param http the {@link Element} where the created ones will be added.
     */
    private static void appendPortMappingsElement(SecureXmlData securityData, Document doc, Node http)
    {
        Element portMappings = doc.createElement("security:port-mappings");
        Element mapping = doc.createElement("security:port-mapping");
        mapping.setAttribute("http", SecureXmlData.MAPPING_PORTS_HTTP);
        mapping.setAttribute("https", SecureXmlData.MAPPING_PORTS_HTTPS);
        portMappings.appendChild(mapping);

        http.appendChild(portMappings);
    }

    /**
     * Appends the <code>intercept-url</code> elements to the xml dom.
     *
     * @param securityData {@link SecureXmlData} object that holds the
     *            information.
     * @param doc the document of the xml file
     * @param http the {@link Element} where the created ones will be added.
     */
    private static void appendInterceptUrlElements(SecureXmlData securityData, Document doc, Node http)
    {
        // add an intercept-url element for the login page (if specified)
        if (!isBlank(securityData.getLoginPage()))
        {
            http.appendChild(createInterceptUrlElement(doc, securityData.getLoginPage(), null,
                    securityData.getRequiresChannelAttributeValue()));
        }

        // add the rest of the intercept-url members
        Map<String, Map<String, String>> sectionMap = securityData.getSecureAndMemberSectionUrls();
        if(sectionMap != null && sectionMap.keySet() != null)
        {
            for (String sectionUrl : sectionMap.keySet())
            {
                Map<String, String> properties = sectionMap.get(sectionUrl);

                String access = properties.get(SecureXmlData.SECURE_SECTION_ACCESS_ATTRIBUTE_NAME);
                String requiresChannel = properties.get(SecureXmlData.SECURE_SECTION_REQUIRES_CHANNEL_ATTRIBUTE_NAME);

                http.appendChild(createInterceptUrlElement(doc, sectionUrl, access, requiresChannel));
            }
        }
    }



    private static void appendFormLoginProcessor(SecureXmlData securityData, Document doc, Node http)
    {
        Element logout = doc.createElement("security:custom-filter");
        logout.setAttribute("before", securityData.FORM_LOGIN_FILTER);
        logout.setAttribute("ref", securityData.FORM_LOGIN_PROCESSOR);

        http.appendChild(logout);

    }
    /**
     * Creates an <code>intercept-url</code> {@link Element} with the given info.
     *
     * @param document the document of the xml file
     * @param pattern the pattern to set in the tag. May be blank.
     * @param access the access to set in the tag. May be blank.
     * @param requiresChannel the requires channel to set in the tag. May be blank.
     * @return an {@link Element}. Never <code>null</code>
     */
    private static Element createInterceptUrlElement(Document document, String pattern, String access,
                                                     String requiresChannel)
    {
        Element interceptUrl = document.createElement("security:intercept-url");
        if (!isBlank(pattern))
        {
            interceptUrl.setAttribute("pattern", pattern);
        }
        if (!isBlank(access))
        {
            interceptUrl.setAttribute("access", IS_AUTHENTICATED);
        }
        if (!isBlank(requiresChannel))
        {
            interceptUrl.setAttribute("requires-channel", requiresChannel);
        }
        return interceptUrl;
    }

    private static Element createInterceptUrlElementInGroupFile(Document document, String access)
    {
        Element interceptUrl = document.createElement("security:intercept-url");

        if (!isBlank(access))
        {
            interceptUrl.setAttribute("access", access);
        }

        return interceptUrl;
    }

    /**
     * Creates the root {@link Element} for the given {@link Document}.
     *
     * @param doc a {@link Document}. Assumed not <code>null</code>
     * @return {@link Element} object. Never <code>null</code>.
     */
    private static Element getRootElement(Document doc)
    {
        Element rootElement = doc.createElement("beans:beans");
        rootElement.setAttribute("xmlns:security", "http://www.springframework.org/schema/security");
        rootElement.setAttribute("xmlns:beans", "http://www.springframework.org/schema/beans");
        rootElement.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        rootElement.setAttribute("xsi:schemaLocation", "http://www.springframework.org/schema/beans "
                + "http://www.springframework.org/schema/beans/spring-beans.xsd "
                + "http://www.springframework.org/schema/security "
                + "http://www.springframework.org/schema/security/spring-security.xsd");
        return rootElement;
    }

    /**
     * Delegates to {@link PSStringUtils #getAllowedGroups()}
     */
    public static String[] getAllowedGroups(String allowAccessTo)
    {
        return PSStringUtils.getAllowedGroups(allowAccessTo);
    }

    /**
     * Utility data class to hold the information to write the
     * <code>security-url-pattern.xml</code> file according to the user
     * preferences in the site.
     *
     * @author Santiago M. Murchio
     *
     */
    public static class SecureXmlData
    {
        public static final String AUTHENTICATION_SUCCESS_HANDLER_REF = "percMembershipLoginHandler";

        public static final String LOGING_PROCESSING_URL = "/perc-login";

        public static final String LOGOUT_SUCCESS_HANDLER_REF = "percMembershipLogoutHandler";

        public static final String LOGOUT_PROCESSING_URL = "/perc-logout";

        public static final String FORM_LOGIN_FILTER = "FORM_LOGIN_FILTER";

        public static final String FORM_LOGIN_PROCESSOR = "authFormProcessor";

        public static final String AUTHENTICATION_FAILURE_URL_SUFFIX = "?error=true";

        public static final String MAPPING_PORTS_HTTP = "${perc.webserver.http.port}";

        public static final String MAPPING_PORTS_HTTPS = "${perc.webserver.https.port}";

        public static final String MEMBER_SECTION_ACCESS_ATTRIBUTE = "hasAnyRole(<groups>)";

        public static final String SECURE_SECTION_ACCESS_ATTRIBUTE = "isAuthenticated()";

        public static final String SECURE_SECTION_REQUIRES_HTTPS_CHANNEL_ATTRIBUTE = "https";

        public static final String SECURE_SECTION_REQUIRES_HTTP_CHANNEL_ATTRIBUTE = "http";

        public static final String SECURE_SECTION_REQUIRES_CHANNEL_ATTRIBUTE_NAME = "requires-channel";

        public static final String SECURE_SECTION_ACCESS_ATTRIBUTE_NAME = "access";

        private String sitename;
        private String loginPage;
        private Map<String, Map<String, String>> secureAndMemberSectionsUrls;
        private Set<String> secureAndMemberSectionsFilters = new TreeSet<>();
        private boolean useHttpsForSecureSite;

        public String getSitename()
        {
            return sitename;
        }

        public void setSitename(String sitename)
        {
            this.sitename = sitename;
        }

        public Map<String, Map<String, String>> getSecureAndMemberSectionUrls()
        {
            return secureAndMemberSectionsUrls;
        }

        public Set<String> getSecureAndMemberSectionFilters()
        {
            return secureAndMemberSectionsFilters;
        }

        public String getLoginPage()
        {
            return loginPage;
        }

        public void setLoginPage(String loginPage)
        {
            this.loginPage = loginPage;
        }

        public boolean isUseHttpsForSecureSite()
        {
            return useHttpsForSecureSite;
        }

        public void setUseHttpsForSecureSite(boolean useHttpsForSecureSite)
        {
            this.useHttpsForSecureSite = useHttpsForSecureSite;
        }

        /**
         * Adds a section to the local map. This section could be secure, in
         * which case the second parameter will be blank, or it could be secure
         * and have allowed groups, in which case the second parameter will have
         * them.
         *
         * @param sectionUrl the url of the given section. Assumed not blank.
         * @param allowAccessTo the allowed groups. May be blank.
         */
        public void addSecureOrMemberSection(String sectionUrl, String allowAccessTo)
        {
            if(secureAndMemberSectionsUrls == null)
            {
                secureAndMemberSectionsUrls = new HashMap<>();
            }
            secureAndMemberSectionsUrls.put(sectionUrl + "**", buildTagAttributes(allowAccessTo));

            secureAndMemberSectionsFilters.add(sectionUrl + "*");
        }

        /**
         * Builds the <code>access</code> attribute. The form is:
         * <ul>
         * <li>if member section:
         * <code>access="hasAnyRole('EI_MEMBERS', 'USERS')"</code></li>
         * <li>if secure section: <code>access="isAuthenticated()"</code></li>
         * </ul>
         *
         *
         * @param allowAccessTo the groups to assign for the sections. May be
         *            blank. In the case of a blank string, the section will be
         *            taken as secure.
         * @return a Map<String, String> object. Never <code>null</code> or empty
         */
        private Map<String, String> buildTagAttributes(String allowAccessTo)
        {
            Map<String, String> map = new HashMap<>();

            if(isBlank(allowAccessTo))
            {
                map.put(SECURE_SECTION_ACCESS_ATTRIBUTE_NAME, SECURE_SECTION_ACCESS_ATTRIBUTE);
                map.put(SECURE_SECTION_REQUIRES_CHANNEL_ATTRIBUTE_NAME,
                        getRequiresChannelAttributeValue());
                return map;
            }

            String[] groups = getAllowedGroups(allowAccessTo);

            String groupsString = "";
            for(String group : groups)
            {
                groupsString += "'" + group.toUpperCase() + "',";
            }
            // remove the last comma character
            groupsString = groupsString.substring(0, groupsString.length() - 1);

            map.put(SECURE_SECTION_ACCESS_ATTRIBUTE_NAME,
                    MEMBER_SECTION_ACCESS_ATTRIBUTE.replace("<groups>", groupsString));
            map.put(SECURE_SECTION_REQUIRES_CHANNEL_ATTRIBUTE_NAME,
                    getRequiresChannelAttributeValue());
            return map;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            SecureXmlData other = (SecureXmlData) obj;
            if (loginPage == null)
            {
                if (other.loginPage != null)
                    return false;
            }
            else if (!loginPage.equals(other.loginPage))
                return false;
            if (secureAndMemberSectionsUrls == null)
            {
                if (other.secureAndMemberSectionsUrls != null)
                    return false;
            }
            else if (!secureAndMemberSectionsUrls.equals(other.secureAndMemberSectionsUrls))
                return false;
            if (sitename == null)
            {
                if (other.sitename != null)
                    return false;
            }
            else if (!sitename.equals(other.sitename))
                return false;
            if(useHttpsForSecureSite != other.useHttpsForSecureSite)
                return false;
            return true;
        }

        public String getRequiresChannelAttributeValue()
        {
            return (useHttpsForSecureSite)
                    ? SECURE_SECTION_REQUIRES_HTTPS_CHANNEL_ATTRIBUTE
                    : SECURE_SECTION_REQUIRES_HTTP_CHANNEL_ATTRIBUTE;
        }
    }
}
