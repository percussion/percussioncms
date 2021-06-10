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

import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.forceMkdir;

import com.percussion.server.PSServer;
import com.percussion.sitemanage.data.PSSiteImportCtx;
import com.percussion.sitemanage.importer.IPSSiteImportLogger;
import com.percussion.sitemanage.importer.IPSSiteImportLogger.PSLogObjectType;
import com.percussion.sitemanage.importer.PSSiteImportLogger;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.utils.types.PSPair;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.cactus.ServletTestCase;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.experimental.categories.Category;

/**
 * @author Santiago M. Murchio
 *
 */
@Category(IntegrationTest.class)
@Ignore("TODO: Test is incorrectly getting picked up as a unit test.  Ignoring until that is fixed.")
public class PSFileDownloaderTest extends ServletTestCase
{

    private static final Logger log = LogManager.getLogger(PSFileDownloaderTest.class);

    PSSiteImportCtx context;
    IPSSiteImportLogger logger = new PSSiteImportLogger(PSLogObjectType.SITE);
    IPSFileDownloader fileDownloader = new PSFileDownloader();
    
    private List<File> filesToDelete = new ArrayList<File>();
    private File tempDir = new File(PSServer.getRxDir().getAbsolutePath() + "/temp/ImageDownloaderTest");

    @Override
    public void setUp()
    {
        try
        {
            super.setUp();
        }
        catch (Exception e1)
        {
            // FIXME Auto-generated catch block
            log.error(e1.getMessage());
            log.debug(e1.getMessage(), e1);
        }
        
        if(!tempDir.exists())
        {
            try
            {
                forceMkdir(tempDir);
            }
            catch (IOException e)
            {
                fail("No exception should have been thrown.");
            }
        }
        
        context = new PSSiteImportCtx();
        context.setLogger(logger);
    }
    
    @Override
    public void tearDown()
    {
        try
        {
            super.tearDown();
        }
        catch (Exception e1)
        {
        }
        
        for(File fileToDelete : filesToDelete)
        {
            if(fileToDelete.exists())
            {
                try
                {
                    forceDelete(fileToDelete);
                }
                catch (IOException e)
                {
                    
                }
            }
        }
    }

    public void testDownloadFiles_emptyMap()
    {
        Map<String, String> filesMap = new HashMap<String, String>();
        List<PSPair<Boolean,String>> downloaded = fileDownloader.downloadFiles(filesMap, context, false);
                
        assertNotNull(downloaded);
        assertTrue(downloaded.isEmpty());
    }
    
    public void testDownloadFiles_malformedUrl()
    {
        File tempFile = createTempFile();
        assertNotNull("Could not create temporal file.", tempFile);
        
        String invalidUrl = "invalid.url(to test)";
        String invalidUrlName = "/nonExistingFile.css";
        String invalidUrlDestinationPath = tempDir.getAbsolutePath() + invalidUrlName;
        
        String validUrl = "file://" + tempFile.getAbsolutePath().replace('\\', '/');
        String validUrlName = "/validFile.css";
        String validUrlDestinationPath = tempDir.getAbsolutePath() + validUrlName;

        Map<String, String> filesMap = new HashMap<String, String>();
        filesMap.put(invalidUrl, invalidUrlDestinationPath);
        filesMap.put(validUrl, validUrlDestinationPath);
        List<PSPair<Boolean,String>> downloaded = fileDownloader.downloadFiles(filesMap, context, false);
        
        File invalid = new File(invalidUrlDestinationPath);
        File valid = new File(validUrlDestinationPath);
        
        filesToDelete.add(invalid);
        filesToDelete.add(valid);
        filesToDelete.add(tempFile);

        assertNotNull(downloaded);
        for(PSPair<Boolean, String> download : downloaded) 
        {
            assertNotNull(download);
        }
        assertTrue(valid.exists());
        assertFalse(invalid.exists());
    }

    public void testdownloadTest_UrlWithSpaces()
    {
        String url = "http://www.frsf.utn.edu.ar/images/banners/ofa-frsf 1.gif";
        String dest = "D:/Temp/ofa-frsf 1.gif";
        fileDownloader.downloadFile(url, dest);
    }
    
    public void testdownloadTest_WhenFileExists()
    {
        File tempFile = createTempFile();
        assertNotNull("Could not create temporal file.", tempFile);
        filesToDelete.add(tempFile);

        String url = "file://" + tempFile.getAbsolutePath().replace('\\', '/');
        String dest = tempDir.getAbsolutePath() + "/logo3w.png";
        PSPair<Boolean, String> downloadResult = fileDownloader.downloadFile(url, dest);
        
        // The file should be downloaded
        assertTrue(downloadResult.getFirst());
        
        File downloadedFile = new File(dest);
        
        // Call the downloader again, the file should not be downloaded
        String expectedMessage = "Skip download '" + url + "' to '" + dest + "', as such file already exists.";
        downloadResult = fileDownloader.downloadFile(url, dest);
        assertTrue(downloadResult.getFirst());
        assertTrue(downloadResult.getSecond().contentEquals(expectedMessage));
        
        filesToDelete.add(downloadedFile);
    }

    /**
     * Creates a temporal file with sample content.
     * 
     * @return {@link File} with sample content, or <code>null</code> if there
     *         was an error creating it.
     */
    private File createTempFile()
    {
        File tempFile = new File(tempDir, "tempFile.css");
        try
        {
            FileUtils.writeStringToFile(tempFile, "sample data for temp file.", StandardCharsets.UTF_8);
            return tempFile;
        }
        catch (IOException e)
        {
            return null;
        }
    }

}
