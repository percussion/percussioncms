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

package com.percussion.sitemanage.importer.theme;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;

import org.junit.Test;

public class PSURLConverterTest
{
    private IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.SITE);

    private static final String BASE_URL = "http://generic:9980";
    private static final String THEME_NAME = "ExampleTheme";
    private static final String SITE_NAME = "generic";
    private static final String ABSOLUTE_THEME_PATH = "c:/themePath/" + THEME_NAME;
    private static final String THEME_URL = "/web_resources/themes/" + THEME_NAME;
    private static final String ASSETS_PATH = "/Assets/uploads/" + THEME_NAME;
    
    @Test
    public void testUrlConverter_WithEmptyValues()
    {
        String remoteUrl = "";
        String baseUrl = "";
        String siteName = "";
        String absoluteThemePath = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // Empty baseUrl, link, siteName, themePath and themeName
        urlConverter = new PSURLConverter(baseUrl, siteName, absoluteThemePath, "", logger);
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, "");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), "");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), "");

        siteName = "generic";
        // Empty baseUrl, link, themePath and themeName
        urlConverter = new PSURLConverter(baseUrl, siteName, absoluteThemePath, "", logger);
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, "");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), "");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), "");
        
        absoluteThemePath = "c:/themePath/";
        // Empty baseUrl, link, siteName and themeName
        urlConverter = new PSURLConverter(baseUrl, siteName, absoluteThemePath, "", logger);
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, "");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), "");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), "");

        // Empty baseUrl and link
        urlConverter = new PSURLConverter(baseUrl, siteName, absoluteThemePath, "", logger);
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, "");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), "");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), "");

        baseUrl = "http://generic:9980/folder1/folder2/cssFile.css";
        // Empty link
        urlConverter = new PSURLConverter(baseUrl, siteName, absoluteThemePath, "", logger);
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, "");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), "");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), "");
    }

    @Test
    public void testUrlConverter_RelativeCurrentPath()
    {
        String remoteUrl = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // Relative to the current path
        urlConverter = new PSURLConverter(BASE_URL + "/folder1/folder2/", SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        link = "images/ExampleImage.png";
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, BASE_URL + "/folder1/folder2/" + "images/ExampleImage.png");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), THEME_URL
                + "/import/" + SITE_NAME + "/folder1/folder2/images/ExampleImage.png");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/folder1/folder2/images/ExampleImage.png");
    }

    @Test
    public void testUrlConverter_RelativeHost()
    {
        String remoteUrl = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // Relative to host
        urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        link = "/backgroundImages/ExampleBackGround1.png";
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, BASE_URL + "/backgroundImages/ExampleBackGround1.png");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), THEME_URL
                + "/import/" + SITE_NAME + "/backgroundImages/ExampleBackGround1.png");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/backgroundImages/ExampleBackGround1.png");
    }

    @Test
    public void testUrlConverter_RelativeUsingDots()
    {
        String remoteUrl = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // Relative using ../
        urlConverter = new PSURLConverter(BASE_URL + "/folder1/folder2/", SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        link = "../textures/texture1.jpg";
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, BASE_URL + "/folder1" + "/textures/texture1.jpg");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), THEME_URL
                + "/import/" + SITE_NAME + "/folder1/textures/texture1.jpg");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/folder1/textures/texture1.jpg");
    }

    @Test
    public void testUrlConverter_FixUrlUsingDots()
    {
        String remoteUrl = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // http://generic:9980/../textures/texture1.jpg should be converted to
        // http://generic:9980/textures/texture1.jpg
        urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        link = "../textures/texture1.jpg";
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, BASE_URL + "/textures/texture1.jpg");
    }

    @Test
    public void testUrlConverter_BinaryImage()
    {
        String remoteUrl = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // Binary Image
        urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        link = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAARMA...";
        remoteUrl = urlConverter.getFullUrl(link);
        assertEquals(remoteUrl, "");
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), "");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), "");
    }

    @Test
    public void testUrlConverter_AbsoluteUrlWithSameHost()
    {
        String remoteUrl = "";
        String link = "";
        PSURLConverter urlConverter = null;

        // Absolute Url
        urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        link = BASE_URL + "/images/srpr/logo3w.png";
        remoteUrl = urlConverter.getFullUrl(link);
        assertTrue(remoteUrl.length() > 0);
        assertEquals(urlConverter.convertToThemeLink(remoteUrl), THEME_URL
                + "/import/" + SITE_NAME + "/images/srpr/logo3w.png");
        assertEquals(urlConverter.getFileSystemPath(remoteUrl), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/images/srpr/logo3w.png");
    }

    @Test
    public void testGetFileSystemPathForCss_cssEndedFile()
    {
        String remoteUrl = BASE_URL + "/folder1/textures/texture1.css";

        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        assertEquals(urlConverter.getFileSystemPathForCss(remoteUrl), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/folder1/textures/texture1.css");
    }

    @Test
    public void testGetFileSystemPathForCss_otherSuffix()
    {
        String remoteUrl = BASE_URL + "/folder1/textures/texture1.cfm";

        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        assertEquals(urlConverter.getFileSystemPathForCss(remoteUrl), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/folder1/textures/texture1.cfm.css");
    }

    @Test
    public void testConvertToThemeLinkForCss_cssEndedFile()
    {
        String remoteUrl = BASE_URL + "/folder1/textures/texture1.css";

        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        assertEquals(urlConverter.convertToThemeLinkForCss(remoteUrl), THEME_URL
                + "/import/" + SITE_NAME + "/folder1/textures/texture1.css");
    }

    @Test
    public void testConvertToThemeLinkForCss_otherSuffix()
    {
        String remoteUrl = BASE_URL + "/folder1/textures/texture1.cfm";

        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        assertEquals(urlConverter.convertToThemeLinkForCss(remoteUrl), THEME_URL
                + "/import/" + SITE_NAME + "/folder1/textures/texture1.cfm.css");
    }

    @Test
    public void testGetFileSystemPathForImg_cssEndedFile() throws Exception
    {
        String remoteUrl = BASE_URL + "/homepage/2011/047.jpg";

        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        assertEquals(urlConverter.getCmsFolderPathForImageAsset(remoteUrl, THEME_NAME),
                ASSETS_PATH + "/import/" + SITE_NAME + "/homepage/2011/047.jpg");
    }

    @Test
    public void testConvertCssResourceWithParamenters()
    {
        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        
        String remoteUrl1 = BASE_URL + "/min/?f=/includes/templates/freetemplate2/css/stylesheet1.css&1327766570";
        String remoteUrl2 = BASE_URL + "/?css=_stylesheets/print.v.1317061408";
        
        assertEquals(urlConverter.getFileSystemPathForCss(remoteUrl1), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/generic_1.css");
        assertEquals(urlConverter.getFileSystemPathForCss(remoteUrl2), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/generic_2.css");
    }
    
    @Test
    public void testConvertResourceWithInvalidCharacters()
    {
        String remoteUrlWithColon = "http://generic:9980/media%3a/js/site-min.js";
        
        PSURLConverter urlConverter = new PSURLConverter(BASE_URL, SITE_NAME, ABSOLUTE_THEME_PATH, THEME_URL, logger);
        assertEquals(urlConverter.getFileSystemPath(remoteUrlWithColon), ABSOLUTE_THEME_PATH
                + "/import/" + SITE_NAME + "/media-/js/site-min.js");
    }
}
