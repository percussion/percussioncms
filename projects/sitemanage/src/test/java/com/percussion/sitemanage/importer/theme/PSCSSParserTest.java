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

import static org.apache.commons.lang.Validate.notNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.util.PSPurgableTempFile;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Unit tests to cover 
 * 
 * @author Ignacio Erro
 * 
 */
@Category(IntegrationTest.class)
public class PSCSSParserTest
{
    private static String BASE_URL = "http://generic:8080";

    private static String THEME_NAME = "ownTheme";
    
    private static String SITE_NAME = "generic";

    private static String THEME_PATH = "/web_resources/themes/" + THEME_NAME;

    private static String SAMPLE_UNCOMPRESSED_FILE_BACKUP = "src/test/resources/importer/sample_backup.css";

    private static String SAMPLE_COMPRESSED_FILE_BACKUP = "src/test/resources/importer/sample-min_backup.css";

    private static String IMPORT_FILE = "src/test/resources/importer";

    private static String IMPORT_A = "/importA.css";

    private static String IMPORT_B = "/importB.css";

    private static String IMPORT_C = "/importC.cfm";

    private String absoluteThemePath = "ImportedTheme/" + THEME_NAME;

    IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.SITE);

    PSCSSParser parser;

    private PSPurgableTempFile tempCSSUncompressedFile;

    private PSPurgableTempFile tempCSSCompressedFile;

    private Downloader downloader;

    /**
     * Inner class which implements {@link IPSFileDownloader} and redefines the
     * methods to download files. Redefined methods are used to copy local files
     * to a temp directory instead of downloading them from a url.
     * 
     * @author Ignacio Erro
     * 
     */
    private class Downloader implements IPSFileDownloader
    {

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.percussion.sitemanage.importer.theme.IPSFileDownloader#downloadFiles
         * (java.util.Map,
         * com.percussion.sitemanage.importer.IPSSiteImportLogger)
         */
        @Override
        public List<PSPair<Boolean, String>> downloadFiles(Map<String, String> urlToPathMap, PSSiteImportCtx context, boolean createAsset)
        {
            notNull(urlToPathMap);
            notNull(context);
            notNull(context.getLogger());

            List<PSPair<Boolean, String>> downloaded = new ArrayList<PSPair<Boolean, String>>();

            Set<String> urls = urlToPathMap.keySet();
            for (String url : urls)
            {
                String fileName = getFileName(url);
                String filePath = getFilePath(fileName);
                downloaded.add(downloadFile(filePath, urlToPathMap.get(url).replace(fileName, "")));
            }

            return downloaded;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.percussion.sitemanage.importer.theme.IPSFileDownloader#
         * downloadFile(java.lang.String, java.lang.String)
         */
        @Override
        public PSPair<Boolean, String> downloadFile(String url, String destination)
        {
            notNull(url);
            notNull(destination);
            notNull(logger);

            String fileName = getFileName(url);
            String filePath = getFilePath(fileName);
            
            File destFile = new File(destination);
            try
            {
                File in = new File(filePath);
                File dest = new File(destFile.getParent());
                org.apache.commons.io.FileUtils.copyFileToDirectory(in, dest);
                org.apache.commons.io.FileUtils.moveFile(new File(dest + fileName), destFile);
                return new PSPair<Boolean, String>(true, "Success");
            }
            catch (Exception e)
            {
                return new PSPair<Boolean, String>(false, "Error");
            }
        }
    }

    @Before
    public void init()
    {
        try
        {
            tempCSSUncompressedFile = new PSPurgableTempFile("TempUncompressed", ".css", null);

            tempCSSCompressedFile = new PSPurgableTempFile("TempCompressed", ".css", null);

            absoluteThemePath = tempCSSUncompressedFile.getParent() + "/" + absoluteThemePath;

            parser = new PSCSSParser(SITE_NAME, absoluteThemePath, THEME_PATH, logger);

            downloader = new Downloader();

            // Make a temporary copy of css files to be processed.
            String uncompressed = loadFileFromDisk(SAMPLE_UNCOMPRESSED_FILE_BACKUP);
            saveFile(new StringBuffer(uncompressed), tempCSSUncompressedFile.getAbsolutePath());

            String compressed = loadFileFromDisk(SAMPLE_COMPRESSED_FILE_BACKUP);
            saveFile(new StringBuffer(compressed), tempCSSCompressedFile.getAbsolutePath());
        }
        catch (Exception e)
        {
            fail("Error copying sample file.");
        }
    }

    @Test
    public void testParserCSSInline()
    {
        parser.setFileDownloader(downloader);

        String cssInline = loadFileFromDisk(tempCSSUncompressedFile.getAbsolutePath());
        
        PSPair<Map<String, String>, String> results = parser.parse(BASE_URL, cssInline);

        Map<String, String> imagesToDownload = results.getFirst();
        String parsedCSS = results.getSecond();

        assertUrls(parsedCSS);

        assertImages(imagesToDownload);
    }

    @Test
    public void testParserUnCompressedCSS()
    {
        parser.setFileDownloader(downloader);

        Map<String, String> cssFiles = new HashMap<String, String>();

        cssFiles.put(BASE_URL, tempCSSUncompressedFile.getAbsolutePath());

        Map<String, String> imagesToDownload = parser.parse(cssFiles);

        String cssFile = loadFileFromDisk(tempCSSUncompressedFile.getAbsolutePath());

        // Check that the file has changed.
        assertFileChanged(SAMPLE_UNCOMPRESSED_FILE_BACKUP, tempCSSUncompressedFile.getAbsolutePath());

        // Check that only known urls has changed.
        assertKnownChanges(SAMPLE_UNCOMPRESSED_FILE_BACKUP, tempCSSUncompressedFile.getAbsolutePath());

        assertUrls(cssFile);

        assertImages(imagesToDownload);
    }

    @Test
    public void testParserCompressedCSS()
    {
        parser.setFileDownloader(downloader);

        Map<String, String> cssFiles = new HashMap<String, String>();

        cssFiles.put(BASE_URL, tempCSSCompressedFile.getAbsolutePath());

        Map<String, String> imagesToDownload = parser.parse(cssFiles);

        String cssFile = loadFileFromDisk(tempCSSCompressedFile.getAbsolutePath());

        // Check that the file has changed.
        assertFileChanged(SAMPLE_COMPRESSED_FILE_BACKUP, tempCSSCompressedFile.getAbsolutePath());

        // Check that only known urls has changed.
        assertKnownChanges(SAMPLE_COMPRESSED_FILE_BACKUP, tempCSSCompressedFile.getAbsolutePath());

        assertUrls(cssFile);

        assertImages(imagesToDownload);
    }

    @Test
    public void testParseImports()
    {
        parser.setFileDownloader(downloader);

        Map<String, String> cssFiles = new HashMap<String, String>();

        cssFiles.put(BASE_URL, tempCSSUncompressedFile.getAbsolutePath());

        Map<String, String> imagesToDownload = parser.parse(cssFiles);

        String cssImportA = "";
        String cssImportB = "";
        String cssImportC = "";
        try
        {
            cssImportA = loadFileFromDisk(absoluteThemePath + "/import/generic" + IMPORT_A);
            cssImportB = loadFileFromDisk(absoluteThemePath + "/import/generic" + IMPORT_B);
            cssImportC = loadFileFromDisk(absoluteThemePath + "/import/generic" + IMPORT_C + ".css");
        }
        catch (Exception e)
        {
            fail("Error reading files.");
        }

        assertTrue(!cssImportA.equals(""));
        assertTrue(!cssImportB.equals(""));
        assertTrue(!cssImportC.equals(""));
        assertTrue(imagesToDownload.size() == 7);
        
        assertTrue(cssImportA.contains("@import \"/web_resources/themes/ownTheme/import/generic/importB.css\";"));
        assertTrue(cssImportA.contains("url(/web_resources/themes/ownTheme/import/generic/images/buttonOk.png)"));
        
        assertTrue(cssImportB.contains("@import \"/web_resources/themes/ownTheme/import/generic/importC.cfm.css\";"));
        assertTrue(cssImportB.contains("@import \"/web_resources/themes/ownTheme/import/generic/importA.css\";"));
        assertTrue(cssImportB.contains("url(http://www.percussion.com/images/images/buttonOk.png);"));
        
        assertTrue(cssImportC.contains("url(/web_resources/themes/ownTheme/import/generic/images/buttonCancel.png)"));
    }

    @After
    public void tearDown() throws Exception
    {
        tempCSSUncompressedFile.delete();
        tempCSSCompressedFile.delete();
        try
        {
            FileUtils.forceDelete(new File(absoluteThemePath));
        }
        catch (Exception e)
        {
            System.out.println("Error deleting temp files.");
        }

    }

    private void assertFileChanged(String sampleFilePath, String modifiedFilePath)
    {
        String sampleFile = loadFileFromDisk(sampleFilePath);
        String modifiedFile = loadFileFromDisk(modifiedFilePath);

        assertTrue(!sampleFile.equals(modifiedFile));
    }

    private void assertKnownChanges(String sampleFilePath, String modifiedFilePath)
    {
        String sampleFile = loadFileFromDisk(sampleFilePath);
        String modifiedFile = loadFileFromDisk(modifiedFilePath);

        modifiedFile = modifiedFile.replace("/web_resources/themes/ownTheme/import/generic/importA.css", "importA.css");
        modifiedFile = modifiedFile.replace("url(/web_resources/themes/ownTheme/import/generic/images/fondo_1.png)", "url(\"../images/fondo_1.png\")");
        modifiedFile = modifiedFile.replace("url(/web_resources/themes/ownTheme/import/generic/images/corner.modulo.png)",
                "url(\"/images/corner.modulo.png\")");
        modifiedFile = modifiedFile.replace("url(/web_resources/themes/ownTheme/import/generic/images/boton.gif)", "url(\"images/boton.gif\")");
        modifiedFile = modifiedFile.replace("url(http://generic:9980/images/back.modulo.png)",
                "url(\"http://generic:9980/images/back.modulo.png\")");
        modifiedFile = modifiedFile
                .replace(
                        "url(data:image/gif;base64,AABgASAAAIMAAVCBxIsKDBgwgTDkzAsKGAhxARSJx4oKJFAxgzFtjIkYDHjwNCigxAsiSAkygDAgA7)",
                        "url(data:image/gif;base64,AABgASAAAIMAAVCBxIsKDBgwgTDkzAsKGAhxARSJx4oKJFAxgzFtjIkYDHjwNCigxAsiSAkygDAgA7)");

        assertTrue(sampleFile.equals(modifiedFile));
    }

    private void assertUrls(String cssFile)
    {
        assertTrue(cssFile.contains("url(/web_resources/themes/ownTheme/import/generic/images/fondo_1.png)"));
        assertTrue(cssFile.contains("url(/web_resources/themes/ownTheme/import/generic/images/corner.modulo.png)"));
        assertTrue(cssFile.contains("url(/web_resources/themes/ownTheme/import/generic/images/boton.gif)"));
        assertTrue(cssFile.contains("url(http://generic:9980/images/back.modulo.png)"));
        assertTrue(cssFile.contains("url(\"c:/malformed/url.png/\")"));
        assertTrue(cssFile
                .contains("url(data:image/gif;base64,AABgASAAAIMAAVCBxIsKDBgwgTDkzAsKGAhxARSJx4oKJFAxgzFtjIkYDHjwNCigxAsiSAkygDAgA7)"));
    }

    private void assertImages(Map<String, String> imagesToDownload)
    {
        assertTrue(imagesToDownload.get("http://generic:8080/images/corner.modulo.png").equals(
                absoluteThemePath + "/import/generic/images/corner.modulo.png"));
        assertTrue(imagesToDownload.get("http://generic:8080/images/boton.gif").equals(
                absoluteThemePath + "/import/generic/images/boton.gif"));
        assertTrue(imagesToDownload.get("http://generic:8080/images/fondo_1.png").equals(
                absoluteThemePath + "/import/generic/images/fondo_1.png"));
        assertTrue(imagesToDownload.get("http://generic:9980/images/back.modulo.png").equals(
                absoluteThemePath + "/import/generic/images/back.modulo.png"));
        assertTrue(imagesToDownload.get("http://generic:8080/images/buttonCancel.png").equals(
                absoluteThemePath + "/import/generic/images/buttonCancel.png"));
        assertTrue(imagesToDownload.get("http://generic:8080/images/buttonOk.png").equals(
                absoluteThemePath + "/import/generic/images/buttonOk.png"));
        assertTrue(!imagesToDownload.containsKey("/malformed/url.png/"));
    }

    private String loadFileFromDisk(String path)
    {
        String cssText = "";
        InputStream in = null;
        try
        {
            in = new FileInputStream(new File(path));

            cssText = IOUtils.toString(in);
        }
        catch (Exception e)
        {
            System.out.println("Error, ");
        }
        finally
        {
            IOUtils.closeQuietly(in);
        }

        return cssText;
    }

    private void saveFile(StringBuffer sb, String path)
    {
        FileWriter fstream = null;
        PrintWriter out = null;
        try
        {
            fstream = new FileWriter(path);
            out = new PrintWriter(fstream);

            out.write(sb.toString());
        }
        catch (Exception e)
        {
            System.err.println("Error: " + e.getMessage());
        }
        finally
        {
            IOUtils.closeQuietly(out);
        }
        return;
    }

    /**
     * Get the file's name from the given url
     * 
     * @param url the url of the file.
     * @return the file's name.
     */
    private String getFileName(String url)
    {
        try
        {
            URL u = new URL(url);
            return u.getFile();
        }
        catch (Exception e)
        {
            System.out.println("Error trying to get file name.");
        }
        return null;
    }

    /**
     * Get the file's path for the given file name.
     * 
     * @param fileName the name of the file.
     * @return
     */
    private String getFilePath(String fileName)
    {
        if (fileName != null)
        {
            if (fileName.equals(IMPORT_A))
            {
                return IMPORT_FILE + IMPORT_A;
            }
            if (fileName.equals(IMPORT_B))
            {
                return IMPORT_FILE + IMPORT_B;
            }
            if (fileName.equals(IMPORT_C))
            {
                return IMPORT_FILE + IMPORT_C;
            }
        }
        return null;
    }
}
