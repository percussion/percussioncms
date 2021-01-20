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
package com.percussion.utils.service;

import static com.percussion.utils.service.impl.PSSiteConfigUtils.copySecureSiteConfiguration;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.createSecureSiteConfiguration;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.createTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.filesModifiedAfterPublished;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getAllowedGroups;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getNonSecureConfigurationFolder;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getPublishedDateFromTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getSitesConfigPath;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getSourceConfigurationFolder;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.getTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeSiteConfiguration;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeSiteConfigurationAndTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.renameNonSecureSiteConfiguration;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.renameOrCreateSecureSiteConfiguration;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.setPublishedDateInTouchedFile;
import static com.percussion.utils.service.impl.PSSiteConfigUtils.removeServerEntry;
import static org.apache.commons.io.FileUtils.forceDelete;
import static org.apache.commons.io.FileUtils.listFiles;
import static org.apache.commons.io.FileUtils.touch;

import com.percussion.sitemanage.data.PSSectionNode;
import com.percussion.utils.service.impl.PSSiteConfigUtils;
import com.percussion.utils.service.impl.PSSiteConfigUtils.SecureXmlData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * Test cases for the {@link PSSiteConfigUtils} class.
 * 
 * @author Santiago M. Murchio
 * 
 */
@Category(IntegrationTest.class)
public class PSSiteConfigUtilsTest extends ServletTestCase
{

    private File secureSiteDefaultConfigFolder = null;

    private File nonsecureSiteDefaultConfigFolder = null;

    private File tchFile1 = null;

    private File tchFile2 = null;

    private File siteConfig1 = null;

    private File siteConfig2 = null;

    private File mockFile = null;

    String sitename = null;
    
    long server1 = 100;
    
    long server2 = 200;

    /**
     * @throws java.lang.Exception
     */
    @Before
    @Override
    public void setUp() throws Exception
    {
        if (secureSiteDefaultConfigFolder == null)
        {
            secureSiteDefaultConfigFolder = getSourceConfigurationFolder();
        }

        if (nonsecureSiteDefaultConfigFolder == null)
        {
            nonsecureSiteDefaultConfigFolder = getNonSecureConfigurationFolder();
        }

        sitename = "testSite" + System.currentTimeMillis();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    @Override
    public void tearDown() throws Exception
    {
        cleanCreatedFiles();
    }

    /**
     * Delete the files or folder that were created during the tests
     * 
     * @throws IOException if an error occurs when deleting the file
     */
    private void cleanCreatedFiles() throws IOException
    {
        if (tchFile1 != null && tchFile1.exists())
        {
            forceDelete(tchFile1);
        }

        if (tchFile2 != null && tchFile2.exists())
        {
            forceDelete(tchFile2);
        }

        if (siteConfig1 != null && siteConfig1.exists())
        {
            forceDelete(siteConfig1);
        }

        if (siteConfig2 != null && siteConfig2.exists())
        {
            forceDelete(siteConfig2);
        }

        if (mockFile != null && mockFile.exists())
        {
            forceDelete(mockFile);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCreateSiteConfiguration() throws IOException
    {
        createSecureSiteConfiguration(sitename);

        siteConfig1 = new File(getSitesConfigPath(), sitename);
        assertTrue(siteConfig1.exists());

        Collection<File> defaultConfigFiles = listFiles(secureSiteDefaultConfigFolder, null, true);
        Collection<File> siteConfigFiles = listFiles(siteConfig1, null, true);

        assertTrue("Different collection sizes.", defaultConfigFiles.size() == siteConfigFiles.size());
        assertTrue("Different collection elements", sameFilesInCollection(defaultConfigFiles, siteConfigFiles));
    }

    @Test
    public void testRemoveSiteConfiguration_nonExistingFolder()
    {
        File siteConfig = new File(getSitesConfigPath(), sitename);
        assertTrue(!siteConfig.exists());

        try
        {
            removeSiteConfiguration(sitename);
            assertTrue(!siteConfig.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown.");
        }
    }

    @Test
    public void testRemoveSiteConfiguration() throws IOException
    {
        createSecureSiteConfiguration(sitename);

        File siteConfig = new File(getSitesConfigPath(), sitename);
        assertTrue(siteConfig.exists());

        removeSiteConfiguration(sitename);
        assertTrue(!siteConfig.exists());
    }

    @Test
    public void testCreateTouchedFile() throws IOException
    {
        createTouchedFile(sitename);

        tchFile1 = getTouchedFile(sitename);
        assertTrue("Tch file should exist", tchFile1.exists());
    }

    @Test
    public void testRemoveSiteConfigurationAndTouchedFile_nonExistingConfigFolder() throws IOException
    {
        createTouchedFile(sitename);

        File siteConfig = new File(getSitesConfigPath(), sitename);
        File tchFile = getTouchedFile(sitename);

        assertTrue(!siteConfig.exists());
        assertTrue(tchFile.exists());

        try
        {
            removeSiteConfigurationAndTouchedFile(sitename);

            assertTrue(!siteConfig.exists());
            assertTrue(!tchFile.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown.");
        }
    }

    @Test
    public void testRemoveSiteConfigurationAndTouchedFile_nonExistingTouchedFile() throws IOException
    {
        createSecureSiteConfiguration(sitename);

        File siteConfig = new File(getSitesConfigPath(), sitename);
        File tchFile = getTouchedFile(sitename);

        assertTrue(siteConfig.exists());
        assertTrue(!tchFile.exists());

        try
        {
            removeSiteConfigurationAndTouchedFile(sitename);

            assertTrue(!siteConfig.exists());
            assertTrue(!tchFile.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown.");
        }
    }

    @Test
    public void testRemoveSiteConfigurationAndTouchedFile_nonExistingTouchedAndConfig()
    {
        File siteConfig = new File(getSitesConfigPath(), sitename);
        File tchFile = getTouchedFile(sitename);

        assertTrue(!siteConfig.exists());
        assertTrue(!tchFile.exists());

        try
        {
            removeSiteConfigurationAndTouchedFile(sitename);

            assertTrue(!siteConfig.exists());
            assertTrue(!tchFile.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown.");
        }
    }

    @Test
    public void testRemoveSiteConfigurationAndTouchedFile() throws IOException
    {
        createSecureSiteConfiguration(sitename);
        createTouchedFile(sitename);

        File siteConfig = new File(getSitesConfigPath(), sitename);
        File tchFile = getTouchedFile(sitename);

        assertTrue(siteConfig.exists());
        assertTrue(tchFile.exists());

        removeSiteConfigurationAndTouchedFile(sitename);

        assertTrue(!siteConfig.exists());
        assertTrue(!tchFile.exists());
    }

    @Test
    public void testRemoveTouchedFile_nonExisting()
    {
        File tchFile = getTouchedFile(sitename);
        assertTrue("Tch file should not exist", !tchFile.exists());

        try
        {
            removeTouchedFile(sitename);
            assertTrue(!tchFile.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown.");
        }
    }

    @Test
    public void testRemoveTouchedFile() throws IOException
    {
        createTouchedFile(sitename);

        File tchFile = getTouchedFile(sitename);
        assertTrue("Tch file should exist", tchFile.exists());

        removeTouchedFile(sitename);
        assertTrue(!tchFile.exists());
    }

    @Test
    public void testRenameSecureSiteConfiguration_equalNames() throws IOException
    {
        createSecureSiteConfiguration(sitename);

        siteConfig1 = new File(getSitesConfigPath(), sitename);
        assertTrue(siteConfig1.exists());

        try
        {
            renameOrCreateSecureSiteConfiguration(sitename, sitename);
            assertTrue(siteConfig1.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testRenameSecureSiteConfiguration_differentNamesNoTouchedFile() throws IOException
    {
        String newSitename = sitename + "renamed";
        createSecureSiteConfiguration(sitename);

        File oldSiteConfig = new File(getSitesConfigPath(), sitename);
        assertTrue(oldSiteConfig.exists());

        try
        {
            renameOrCreateSecureSiteConfiguration(sitename, newSitename);

            siteConfig1 = new File(getSitesConfigPath(), newSitename);
            assertTrue(!oldSiteConfig.exists());
            assertTrue(siteConfig1.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testRenameSecureSiteConfiguration_differentNamesWithTouchedFile() throws IOException
    {
        String newSitename = sitename + "renamed";
        createSecureSiteConfiguration(sitename);
        createTouchedFile(sitename);

        File oldSiteConfig = new File(getSitesConfigPath(), sitename);
        File oldSiteTch = getTouchedFile(sitename);
        assertTrue(oldSiteConfig.exists());
        assertTrue(oldSiteTch.exists());

        try
        {
            renameOrCreateSecureSiteConfiguration(sitename, newSitename);

            siteConfig1 = new File(getSitesConfigPath(), newSitename);
            File newSiteTch = getTouchedFile(sitename);
            assertTrue(!oldSiteConfig.exists());
            assertTrue(!oldSiteTch.exists());
            assertTrue(!newSiteTch.exists());
            assertTrue(siteConfig1.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testRenameNonSecureSiteConfiguration_equalNames() throws IOException
    {
        createTouchedFile(sitename);

        File siteConfig = new File(getSitesConfigPath(), sitename);
        File oldSiteTch = getTouchedFile(sitename);
        tchFile1 = oldSiteTch;
        assertTrue(!siteConfig.exists());
        assertTrue(oldSiteTch.exists());

        try
        {
            renameNonSecureSiteConfiguration(sitename, sitename);
            assertTrue(!siteConfig.exists());
            assertTrue(oldSiteTch.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testRenameNonSecureSiteConfiguration_differentNamesNoTouchedFile() throws IOException
    {
        String newSitename = sitename + "renamed";

        File oldSiteTch = getTouchedFile(sitename);
        assertTrue(!oldSiteTch.exists());

        try
        {
            renameOrCreateSecureSiteConfiguration(sitename, newSitename);

            siteConfig1 = new File(getSitesConfigPath(), newSitename);
            File newSiteTch = getTouchedFile(sitename);
            assertTrue(!oldSiteTch.exists());
            assertTrue(!newSiteTch.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testRenameNonSecureSiteConfiguration_differentNamesWithTouchedFile() throws IOException
    {
        String newSitename = sitename + "renamed";
        createTouchedFile(sitename);

        File oldSiteConfig = new File(getSitesConfigPath(), sitename);
        File oldSiteTch = getTouchedFile(sitename);
        assertTrue(!oldSiteConfig.exists());
        assertTrue(oldSiteTch.exists());

        try
        {
            renameNonSecureSiteConfiguration(sitename, newSitename);

            File siteConfig = new File(getSitesConfigPath(), newSitename);
            File newSiteTch = getTouchedFile(sitename);
            assertTrue(!oldSiteConfig.exists());
            assertTrue(!oldSiteTch.exists());
            assertTrue(!newSiteTch.exists());
            assertTrue(!siteConfig.exists());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testFilesModifiedAfterPublished_unsecureNoTouchedFile()
    {
        try
        {
            assertTrue("Touched File does not exist, should have returned true",
                    filesModifiedAfterPublished(sitename, server1));
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testFilesModifiedAfterPublished_unsecureModifiedForServer1() 
    {
        try
        {
            createTouchedFile(sitename);
            tchFile1 = getTouchedFile(sitename);

            // add the date for the first server
            Calendar millis = Calendar.getInstance();
            millis.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server1, date);

            // modify the xml file 
            mockFile = new File(getNonSecureConfigurationFolder(), sitename + ".xml");
            touch(mockFile);

            // set the date for the second server
            setPublishedDateInTouchedFile(tchFile1.getPath(), server2, new Date());

            // At this point, server 1 was published before modifications where
            // made. That is not the case for server 2            
            assertTrue("Default config modified, should have returned true",
                    filesModifiedAfterPublished(sitename, server1));
            assertFalse("Default config not modified, should have returned false",
                    filesModifiedAfterPublished(sitename, server2));
        }
        catch(Exception e)
        {
            fail("No exception should have been thrown");
        }
    }
    
    @Test
    public void testFilesModifiedAfterPublished_unsecureModifiedDefaultConfig()
    {
        createOnlyTouchedFile();
        try
        {
            mockFile = new File(getNonSecureConfigurationFolder(), sitename + ".xml");
            touch(mockFile);
            assertTrue("Default config modified, should have returned true",
                    filesModifiedAfterPublished(sitename, server1));
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testFilesModifiedAfterPublished_secureModifiedDefaultConfig()
    {
        createSiteConfigurationAndTouchedFile();
        try
        {
            mockFile = new File(getSitesConfigPath() + "/" + sitename, sitename + ".xml");
            touch(mockFile);
            assertTrue("Default config modified, should have returned true", filesModifiedAfterPublished(sitename, 0));
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testFilesModifiedAfterPublished_secureModifiedForServer1() 
    {
        try
        {
            createTouchedFile(sitename);
            tchFile1 = getTouchedFile(sitename);

            // add the date for the first server
            Calendar millis = Calendar.getInstance();
            millis.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server1, date);

            // modify the xml file 
            mockFile = new File(getSitesConfigPath() + "/" + sitename, sitename + ".xml");
            touch(mockFile);

            // set the date for the second server
            setPublishedDateInTouchedFile(tchFile1.getPath(), server2, new Date());

            // At this point, server 1 was published before modifications where
            // made. That is not the case for server 2            
            assertTrue("Default config modified, should have returned true",
                    filesModifiedAfterPublished(sitename, server1));
            assertFalse("Default config not modified, should have returned false",
                    filesModifiedAfterPublished(sitename, server2));
        }
        catch(Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testCopySecureSiteConfiguration()
    {
        try
        {
            String sitename2 = sitename + "-copy";
            createSiteConfigurationAndTouchedFile();

            mockFile = new File(getSitesConfigPath() + "/" + sitename, sitename + ".xml");
            touch(mockFile);
            copySecureSiteConfiguration(sitename, sitename2);

            siteConfig2 = new File(getSitesConfigPath(), sitename2);
            File tchFile = getTouchedFile(sitename2);

            assertTrue(siteConfig2.exists());
            assertTrue(!tchFile.exists());

            Collection<File> site1ConfigFiles = listFiles(siteConfig1, null, true);
            Collection<File> site2ConfigFiles = listFiles(siteConfig2, null, true);

            assertTrue("Different collection sizes.", site1ConfigFiles.size() == site2ConfigFiles.size());
            assertTrue("Different collection elements", sameFilesInCollection(site1ConfigFiles, site2ConfigFiles));
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
    }

    @Test
    public void testGetPublishedDateFromTouchedFile_nonExistingDate() throws IOException
    {
        createTouchedFile(sitename);

        tchFile1 = getTouchedFile(sitename);
        assertTrue(tchFile1.exists());

        Date publishedDate = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);
        assertNull("Published date should be null", publishedDate);
    }

    @Test
    public void testGetPublishedDateFromTouchedFile() throws IOException
    {
        Date date = createOnlyTouchedFile();

        Date publishedDate = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);
        assertEquals("Published dates should be the same", date, publishedDate);
    }

    @Test
    public void testSetPublishedDateInTouchedFile()
    {
        try
        {
            createTouchedFile(sitename);

            tchFile1 = getTouchedFile(sitename);
            assertTrue(tchFile1.exists());

            Calendar millis = Calendar.getInstance();
            millis.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server1, date);

            Date publishedDate = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);
            assertEquals("Published dates should be the same", date, publishedDate);

            millis.add(Calendar.DAY_OF_MONTH, -2);
            date = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server2, date);

            publishedDate = getPublishedDateFromTouchedFile(tchFile1.getPath(), server2);
            assertEquals("Published dates should be the same", date, publishedDate);

        }
        catch (FileNotFoundException e)
        {
            fail("No exception should have been thrown");
        }
        catch (IOException e)
        {
            fail("No exception should have been thrown");
        }
    }
    
    @Test
    public void testRemoveServerEntry_nonExistingTchFile()
    {
        tchFile1 = getTouchedFile(sitename);

        try
        {
            assertFalse(tchFile1.exists());
            removeServerEntry(sitename, server1);
            assertFalse(tchFile1.exists());
            removeServerEntry(sitename, server2);
            assertFalse(tchFile1.exists());
        }
        catch(Exception e)
        {
            fail("No exception should have been thrown");
        }
    }
    
    @Test
    public void testRemoveServerEntry_nonExistingServerEntry()
    {
        try
        {
            createTouchedFile(sitename);
            tchFile1 = getTouchedFile(sitename);

            // add the date for the first server
            Calendar millis = Calendar.getInstance();
            millis.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server1, date);

            Date dateServer1 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);            
            Date dateServer2 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server2);            

            assertTrue(dateServer1.equals(date));
            assertTrue(dateServer2 == null);

            removeServerEntry(sitename, server2);

            dateServer1 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);            
            dateServer2 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server2);            

            assertTrue(dateServer1.equals(date));
            assertTrue(dateServer2 == null);
            
        }
        catch(Exception e)
        {
            fail("No exception should have been thrown");
        }
    }
    
    @Test
    public void testRemoveServerEntry()
    {
        try
        {
            createTouchedFile(sitename);
            tchFile1 = getTouchedFile(sitename);

            // add the date for the first server
            Calendar millis = Calendar.getInstance();
            millis.add(Calendar.DAY_OF_MONTH, -1);
            Date date = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server1, date);

            // add the date for the first server
            millis.add(Calendar.DAY_OF_MONTH, -2);
            Date date2 = new Date(millis.getTimeInMillis());
            setPublishedDateInTouchedFile(tchFile1.getPath(), server2, date2);

            Date dateServer1 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);            
            Date dateServer2 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server2);            

            assertTrue(dateServer1.equals(date));
            assertTrue(dateServer2.equals(date2));

            removeServerEntry(sitename, server1);

            dateServer1 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);            
            dateServer2 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server2);            

            assertTrue(dateServer1 == null);
            assertTrue(dateServer2.equals(date2));
            
            removeServerEntry(sitename, server2);

            dateServer1 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server1);            
            dateServer2 = getPublishedDateFromTouchedFile(tchFile1.getPath(), server2);            
            
            assertTrue(dateServer1 == null);
            assertTrue(dateServer2 == null);
        }
        catch(Exception e)
        {
            fail("No exception should have been thrown");
        }
    }
    
    
    @Test
    public void testSplitAllowedGroups()
    {
        String allowedGroups = null; 
        String[] expected = new String[]{};
        String[] actual = getAllowedGroups(allowedGroups);
        
        testEqualArrays(expected, actual);

        allowedGroups = ""; 
        expected = new String[]{};
        actual = getAllowedGroups(allowedGroups);
        
        testEqualArrays(expected, actual);

        allowedGroups = "firstGroup,secondGroup"; 
        expected = new String[]{"firstGroup", "secondGroup"};
        actual = getAllowedGroups(allowedGroups);
        
        testEqualArrays(expected, actual);

        allowedGroups = "first\\,group,secondGroup"; 
        expected = new String[]{"first\\,group", "secondGroup"};
        actual = getAllowedGroups(allowedGroups);
        
        testEqualArrays(expected, actual);

        allowedGroups = "first\\,group,second\\,Group"; 
        expected = new String[]{"first\\,group", "second\\,Group"};
        actual = getAllowedGroups(allowedGroups);
        
        testEqualArrays(expected, actual);

        allowedGroups = "first\\,,group,secondGroup"; 
        expected = new String[]{"first\\,", "group", "secondGroup"};
        actual = getAllowedGroups(allowedGroups);
        
        testEqualArrays(expected, actual);
    }

    @Test
    public void testBuildXmlData()
    {
        boolean useHttpsForSecureSite = true;
        
        SecureXmlData expectedXmlData = new SecureXmlData();
        PSSectionNode root = generateSectionTree(expectedXmlData, useHttpsForSecureSite);
        
        assertEquals(expectedXmlData, PSSiteConfigUtils.buildXmlDataForSite(expectedXmlData.getSitename(),
                expectedXmlData.getLoginPage(), root, useHttpsForSecureSite));
    }
    
    /**
     * 
     * root 
     *  - section1 (secure, no groups) <<<<<
     *      - section1-1
     *      - section 1-2
     *  - section2 (unsecure)
     *      - section2-1
     *      - section2-2 (secure, groups: 'editor', 'admin') <<<<<
     *          - section2-2-1
     *          - section2-2-2
     *          - section2-2-3
     *  - section3 (unsecure)
     *      - section3-1 (secure, no groups) <<<<<
     *      - section3-2 (unsecure) 
     * 
     */
    private PSSectionNode generateSectionTree(SecureXmlData expected, boolean useHttpsForSecureSite)
    {

        String siteName = "TestSite" + System.currentTimeMillis();
        String loginPage = "/index.html";

        // Secure XML Data
        expected.setSitename(siteName);
        expected.setLoginPage(loginPage);
        expected.setUseHttpsForSecureSite(useHttpsForSecureSite);
        expected.addSecureOrMemberSection("/section1/", "");
        expected.addSecureOrMemberSection("/section2/section2-2/", "editor,admin");
        expected.addSecureOrMemberSection("/section3/section3-1/", "");

        // Section 1
        List<PSSectionNode> section1childs = new ArrayList<PSSectionNode>();
        section1childs.add(createNode("//Sites/" + siteName + "/section1/section1-1", false, "", null));
        section1childs.add(createNode("//Sites/" + siteName + "/section1/section1-2", false, "", null));

        PSSectionNode section1 = createNode("//Sites/" + siteName + "/section1", true, "", section1childs);

        // Section 2
        List<PSSectionNode> section22childs = new ArrayList<PSSectionNode>();
        section22childs.add(createNode("//Sites/" + siteName + "/section2/section2-2/section2-2-1", false, "", null));
        section22childs.add(createNode("//Sites/" + siteName + "/section2/section2-2/section2-2-2", false, "", null));
        section22childs.add(createNode("//Sites/" + siteName + "/section2/section2-2/section2-2-3", false, "", null));

        List<PSSectionNode> section2childs = new ArrayList<PSSectionNode>();
        section2childs.add(createNode("//Sites/" + siteName + "/section2/section2-1", false, "", null));
        section2childs.add(createNode("//Sites/" + siteName + "/section2/section2-2", true, "editor,admin",
                section22childs));

        PSSectionNode section2 = createNode("//Sites/" + siteName + "/section2", false, "", section2childs);

        // Section 3
        List<PSSectionNode> section3childs = new ArrayList<PSSectionNode>();
        section3childs.add(createNode("//Sites/" + siteName + "/section3/section3-1", true, "", null));
        section3childs.add(createNode("//Sites/" + siteName + "/section3/section3-2", false, "", null));

        PSSectionNode section3 = createNode("//Sites/" + siteName + "/section3", false, "", section3childs);

        // Root Section
        List<PSSectionNode> rootChilds = new ArrayList<PSSectionNode>();
        rootChilds.add(section1);
        rootChilds.add(section2);
        rootChilds.add(section3);
        
        PSSectionNode root = createNode("//Sites/" + siteName, true, "", rootChilds);
        
        return root;
    }

    private PSSectionNode createNode(String folderPath, boolean requiresLogin, String allowAccessTo,
            List<PSSectionNode> childs)
    {
        PSSectionNode node = new PSSectionNode();
        node.setFolderPath(folderPath);
        node.setRequiresLogin(requiresLogin);
        node.setAllowAccessTo(allowAccessTo);
        node.setChildNodes(childs);
        return node;
    }
    
    private void testEqualArrays(String[] expected, String[] actual)
    {
        assertNotNull(expected);
        assertNotNull(actual);
        assertTrue(expected.length == actual.length);
        assertTrue(Arrays.equals(expected, actual));        
    }

    /**
     * Creates the site configuration folder and its corresponding tch file.
     * 
     * @return the date that has been set as the timestamp in the tch file.
     */
    private Date createSiteConfigurationAndTouchedFile()
    {
        try
        {
            createSecureSiteConfiguration(sitename);
            Date publishedDate = createOnlyTouchedFile();

            siteConfig1 = new File(getSitesConfigPath(), sitename);
            assertTrue(siteConfig1.exists());

            return publishedDate;
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
        return null;
    }

    /**
     * Only creates the corresponding tch file for the site.
     * 
     * @return the date that has been set as the timestamp in the tch file.
     */
    private Date createOnlyTouchedFile()
    {
        try
        {
            createTouchedFile(sitename);

            tchFile1 = getTouchedFile(sitename);
            assertTrue(tchFile1.exists());

            Calendar millis = Calendar.getInstance();
            millis.add(Calendar.DAY_OF_MONTH, -1);
            millis.add(Calendar.MONTH, -1);
            setPublishedDateInTouchedFile(tchFile1.getPath(), server1, new Date(millis.getTimeInMillis()));

            return new Date(millis.getTimeInMillis());
        }
        catch (Exception e)
        {
            fail("No exception should have been thrown");
        }
        return null;
    }

    /**
     * Iterates over the two collections of File objects, comparing the names of
     * each one. It does not compare the size of the collections.
     * 
     * @param defaultConfigFiles the first collection. Assumed not
     *            <code>null</code>
     * @param siteConfigFiles the second collection. Assumed not
     *            <code>null</code>
     * @return <code>true</code> if the two collections have the same elements.
     *         <code>false</code> otherwise.
     */
    private boolean sameFilesInCollection(Collection<File> defaultConfigFiles, Collection<File> siteConfigFiles)
    {
        for (File defaultFile : defaultConfigFiles)
        {
            boolean contains = false;
            for (File siteFile : siteConfigFiles)
            {
                if (defaultFile.getName().equals(siteFile.getName()))
                {
                    contains = true;
                    break;
                }
            }
            if (!contains)
            {
                return false;
            }
        }
        return true;
    }
    

}
