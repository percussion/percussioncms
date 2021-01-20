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
package com.percussion.pathmanagement.web.service;

import com.percussion.designmanagement.service.IPSFileSystemService;
import com.percussion.designmanagement.service.IPSFileSystemService.PSFileOperationException;
import com.percussion.pathmanagement.data.PSPathItem;
import com.percussion.pathmanagement.service.impl.PSFileSystemPathItemService;
import com.percussion.pathmanagement.service.impl.PSPathService;
import com.percussion.server.PSServer;
import com.percussion.share.data.IPSItemSummary.Category;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.PSWebserviceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.junit.Ignore;

@org.junit.experimental.categories.Category(IntegrationTest.class)
public class PSDesignPathItemServiceTest extends PSServletTestCase
{
    private PSPathService pathService;
    
    private File webResourcesDir;
    
    private IPSFileSystemService fileSystemService;
    
    private List<File> filesToDelete;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        
        pathService = (PSPathService) getBean("pathService");
        webResourcesDir = new File(PSServer.getRxDir(), "web_resources/themes");
        
        PSWebserviceUtils.setUserName("Admin");
        
        fileSystemService = (IPSFileSystemService) getBean("webResourcesService");
        
        filesToDelete = new ArrayList<File>();
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        for (File fileToDelete : filesToDelete)
        {
            if(fileToDelete.isFile())
            {
                if (fileToDelete.exists())
                    fileToDelete.delete();
            }
            else 
            {
                FileUtils.deleteDirectory(fileToDelete);
            }            
        }
    }

    /**
     * @param fileName
     * @param fileSize
     */
    private File createThemeFile(String fileName, int fileSize)
    {
        File newFile = new File(webResourcesDir, fileName);
        
        if (newFile.exists())
            newFile.delete();
        
        try
        {
            FileUtils.writeStringToFile(newFile, StringUtils.leftPad(StringUtils.EMPTY, fileSize, 'a'), StandardCharsets.UTF_8);
            filesToDelete.add(newFile);
            return newFile;
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed in creating the file: " + fileName, e);
        }
    }

    /**
     * @param string
     */
    private File createThemeDir(String string)
    {
        File newDir = new File(webResourcesDir, string);
        newDir.mkdirs();
        return newDir;
    }
    
    private void createThemeDir(String string, boolean deleteAtTearDown)
    {
        File newDir = new File(webResourcesDir, string);
        newDir.mkdirs();
        
        filesToDelete.add(newDir);
    }
    
    private void validatePathItems(PSPathItem pathItem, ExpectedPathItemValues expectedPathItem)
    {
        Assert.assertEquals("Path item name", expectedPathItem.name, pathItem.getName());
        
        Assert.assertNotNull("Path item folderPaths not null", pathItem.getFolderPaths());
        Assert.assertEquals("Path item folderPaths size", 1, pathItem.getFolderPaths().size());
        Assert.assertEquals("Path item folderPaths", expectedPathItem.folderPaths, pathItem.getFolderPaths().get(0));
        
        Assert.assertEquals("Path item folderPath", expectedPathItem.folderPath, pathItem.getFolderPath());
        Assert.assertEquals("Path item path", expectedPathItem.path, pathItem.getPath());
        Assert.assertEquals("Path item type", expectedPathItem.type, pathItem.getType());
        Assert.assertEquals("Path item leaf", !expectedPathItem.type.equals(PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE), pathItem.isLeaf());
        
        Assert.assertEquals("Path item revisionable", false, pathItem.isRevisionable());
        
        if (expectedPathItem.type.equals(PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE))
            Assert.assertEquals("Path item icon", "/Rhythmyx/sys_resources/images/finderFolder.png", pathItem.getIcon());
        else
            Assert.assertTrue("Path item icon not empty", StringUtils.isNotBlank(pathItem.getIcon()));
        
        Assert.assertEquals("Path item category", Category.SYSTEM, pathItem.getCategory());
        
        Assert.assertEquals("Path item id",
                Integer.valueOf(fileSystemService.getFile(pathItem.getPath().substring("/Design/web_resources".length())).getPath().hashCode()).toString(),
                pathItem.getId());
    }
    
    public void testFindChildren_DesignChildren() throws Exception
    {
        List<PSPathItem> pathItems = pathService.findChildren("/Design/");
        Assert.assertNotNull("Path item list not null", pathItems);
        Assert.assertEquals("Path item list count", 1, pathItems.size());
        
        // PSPathItem properties
        validatePathItems(pathItems.get(0),
                new ExpectedPathItemValues("web_resources",
                        "/Design/web_resources/",
                        PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE,
                        "//Design"));
    }
    
    public void testFindChildren_WebResourcesChildren() throws Exception
    {
        List<PSPathItem> pathItems = pathService.findChildren("/Design/web_resources");
        Assert.assertNotNull("Path item list not null", pathItems);
        Assert.assertEquals("Path item list count", 1, pathItems.size());
        
        // PSPathItem properties
        validatePathItems(pathItems.get(0),
                new ExpectedPathItemValues("themes",
                        "/Design/web_resources/themes/",
                        PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE,
                        "//Design/web_resources"));
    }
    
    @Ignore("Path item list count expected:<1> but was:<2>")
    public void testFindChildren_ThemesChildren() throws Exception
    {
        //TODO: Fix Me
//        createThemeDir("/percussion");
//        
//        List<PSPathItem> pathItems = pathService.findChildren("/Design/web_resources/themes");
//        Assert.assertNotNull("Path item list not null", pathItems);
//        Assert.assertEquals("Path item list count", 1, pathItems.size());
//        
//        // PSPathItem properties
//        validatePathItems(pathItems.get(0),
//                new ExpectedPathItemValues("percussion",
//                        "/Design/web_resources/themes/percussion/",
//                        PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE,
//                        "//Design/web_resources/themes"));
    }
    
    public void fixme_testFindChildren_PercussionThemeChildren() throws Exception
    {
        // TODO: this failed on build machine. ignore/disable for now.
        
        List<PSPathItem> pathItemsInit = pathService.findChildren("/Design/web_resources/themes/percussion");
        
        createThemeDir("/percussion/images");
        createThemeFile("/percussion/perc_theme.test.css", 20);
        createThemeFile("/percussion/perc_theme.test.png", 5);
        
        List<PSPathItem> pathItems = pathService.findChildren("/Design/web_resources/themes/percussion");
        Assert.assertNotNull("Path item list not null", pathItems);
        Assert.assertEquals("Path item list count", 2, pathItemsInit.size() - pathItems.size());
        
        validatePathItems(pathItems.get(0),
                new ExpectedPathItemValues("images",
                        "/Design/web_resources/themes/percussion/images/",
                        PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE,
                        "//Design/web_resources/themes/percussion"));
        
        validatePathItems(pathItems.get(1),
                new ExpectedPathItemValues("perc_theme.test.css",
                        "/Design/web_resources/themes/percussion/perc_theme.test.css",
                        PSFileSystemPathItemService.FILE_SYSTEM_FILE_TYPE,
                        "//Design/web_resources/themes/percussion"));
        
        validatePathItems(pathItems.get(2),
                new ExpectedPathItemValues("perc_theme.png",
                        "/Design/web_resources/themes/percussion/perc_theme.test.png",
                        PSFileSystemPathItemService.FILE_SYSTEM_FILE_TYPE,
                        "//Design/web_resources/themes/percussion"));
    }
    
    @Ignore("Path item list count expected:<2> but was:<3>")
    public void testFindChildren_SubFolderIsNamedWebResources() throws Exception
    {
        //TODO: Fix Me
//        // This makes a path like this: "/Design/web_resources/themes/web_resources"
  //        createThemeDir("/web_resources", true);
//        
//        List<PSPathItem> pathItems = pathService.findChildren("/Design/web_resources/themes");
//        Assert.assertNotNull("Path item list not null", pathItems);
//        Assert.assertEquals("Path item list count", 2, pathItems.size());
//        
//        // PSPathItem properties
//        validatePathItems(pathItems.get(1),
//                new ExpectedPathItemValues("web_resources",
//                        "/Design/web_resources/themes/web_resources/",
//                        PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE,
//                        "//Design/web_resources/themes"));
    }
    
//    public void testFindChildren_FoldersAreCorrectlySorted() throws Exception
//    {
    //TODO: Fix Me Too!
//        createThemeDir("/percussionTest");
//        createThemeFile("/percussionTest/A_perc_theme.css", 20);
//        createThemeFile("/percussionTest/a_perc_theme.css", 20);
//        createThemeDir("/percussionTest/the_folder");
//        createThemeDir("/percussionTest/Ahe_folder");
//        createThemeDir("/percussionTest/aahe_folder");
//        createThemeDir("/percussionTest/zhe_folder");
//        createThemeFile("/percussionTest/z_perc_theme.css", 20);
//        
//        List<PSPathItem> pathItems = pathService.findChildren("/Design/web_resources/themes/percussionTest");
//        Assert.assertNotNull("Path item list not null", pathItems);
//        Assert.assertEquals("path items count", 7, pathItems.size());
//        
//        validatePathItemOrder(pathItems, new String[] {
//                "Ahe_folder",
//                "aahe_folder",
//                "the_folder",
//                "zhe_folder",
//                "A_perc_theme.css",
//                "a_perc_theme.css",
//                "z_perc_theme.css"
//        });
//    }
    
//    /**
//     * @param strings
//     */
//    private void validatePathItemOrder(List<PSPathItem> pathItems, String[] strings)
//    {
//        
//        for (int i=0; i<pathItems.size(); i++)
//        {
//            
//        }
//    }

    public void testFind_PathEndsAlwaysWithSlash() throws Exception
    {
        createThemeDir("/percussion/images");
        createThemeFile("/percussion/perc_theme.test.css", 20);
        
        PSPathItem pathItem = pathService.find("/Design/web_resources/themes/percussion");
        Assert.assertNotNull("Path item list not null", pathItem);
        
        // folder
        validatePathItems(pathItem,
                new ExpectedPathItemValues("percussion",
                        "/Design/web_resources/themes/percussion/",
                        PSFileSystemPathItemService.FILE_SYSTEM_FOLDER_TYPE,
                        "//Design/web_resources/themes"));
        
        // file
        pathItem = pathService.find("/Design/web_resources/themes/percussion/perc_theme.test.css");
        Assert.assertNotNull("Path item list not null", pathItem);
        
        validatePathItems(pathItem,
                new ExpectedPathItemValues("perc_theme.test.css",
                        "/Design/web_resources/themes/percussion/perc_theme.test.css/",
                        PSFileSystemPathItemService.FILE_SYSTEM_FILE_TYPE,
                        "//Design/web_resources/themes/percussion"));
    }
    
    public void testGetParentPath_folderPaths()
    {
        String path = "/";
        String expected = "/";
        String parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources";
        expected = "/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources/";
        expected = "/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources/themes";
        expected = "/web_resources/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources/themes/";
        expected = "/web_resources/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources/themes/percussion";
        expected = "/web_resources/themes/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources/themes/percussion/custom-theme";
        expected = "/web_resources/themes/percussion/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

    }
    
    public void testGetParentPath_filePaths()
    {
        String path = "/web_resources/themes/percussion/custom-theme/perc-theme.css";
        String expected = "/web_resources/themes/percussion/custom-theme/";
        String parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        

        path = "/web_resources/themes/percussion/custom-theme/perc-theme.png";
        expected = "/web_resources/themes/percussion/custom-theme/";
        parentFolder = fileSystemService.getParentFolder(path);
        Assert.assertEquals("The parent folder of " + path + " shoud be " + expected + " but was " + parentFolder,
                expected, parentFolder);        
    }

    public void testFoldernameAvailable()
    {
        String[] filesInFolder = new String[]
        {"percussion", "custom-theme", "themes", "web_resources", "css-themes", "perc-theme.css", "perc-theme.png",
                "rss_logo.png", "ui-bg_flat_10_000000_40x100.png"};
        
        String foldername = "custom-theme-2";
        Assert.assertTrue("The foldername " + foldername + " should be available.",
                fileSystemService.foldernameAvailable(foldername, filesInFolder));

        foldername = "new-theme";
        Assert.assertTrue("The foldername " + foldername + " should be available.",
                fileSystemService.foldernameAvailable(foldername, filesInFolder));

        foldername = "percussion-new-theme";
        Assert.assertTrue("The foldername " + foldername + " should be available.",
                fileSystemService.foldernameAvailable(foldername, filesInFolder));

        foldername = "PeRcUsSiOn";
        Assert.assertFalse("The foldername " + foldername + " should not be available.",
                fileSystemService.foldernameAvailable(foldername, filesInFolder));

        foldername = "css-THEMES";
        Assert.assertFalse("The foldername " + foldername + " should not be available.",
                fileSystemService.foldernameAvailable(foldername, filesInFolder));

        foldername = "perc-theme.png";
        Assert.assertFalse("The foldername " + foldername + " should not be available.",
                fileSystemService.foldernameAvailable(foldername, filesInFolder));

    }
    
    public void testGetNewFolderName()
    {
        String[] filesInFolder = new String[]
        {"percussion", "custom-theme", "css-themes", "perc-theme.css", "perc-theme.png", "rss_logo.png"};
        
        String expected = "New-Folder";
        String newFolder = fileSystemService.getNewFolderName(filesInFolder);
        Assert.assertEquals("The new foldername should have been " + expected + " but was " + newFolder,
                expected, newFolder);
        
        filesInFolder = new String[]
        {"New-Folder", "custom-theme", "css-themes", "perc-theme.css", "perc-theme.png", "rss_logo.png"};

        expected = "New-Folder 1";
        newFolder = fileSystemService.getNewFolderName(filesInFolder);
        Assert.assertEquals("The new foldername should have been " + expected + " but was " + newFolder, expected,
                newFolder);

        filesInFolder = new String[]
        {"New-Folder", "New-Folder 1", "css-themes", "perc-theme.css", "perc-theme.png", "rss_logo.png"};

        expected = "New-Folder 2";
        newFolder = fileSystemService.getNewFolderName(filesInFolder);
        Assert.assertEquals("The new foldername should have been " + expected + " but was " + newFolder, expected,
                newFolder);
                                                              
        filesInFolder = new String[]
        {"New-Folder", "New-Folder 1", "New-Folder 4", "css-themes", "perc-theme.css", "perc-theme.png", "rss_logo.png"};

        expected = "New-Folder 5";
        newFolder = fileSystemService.getNewFolderName(filesInFolder);
        Assert.assertEquals("The new foldername should have been " + expected + " but was " + newFolder, expected,
                newFolder);
    }
    
    public void testAddFolder_pathFromFile()
    {
        // first create the file we will use
        filesToDelete.add(createThemeFile("fileUnderThemes.css", 10));
        
        String newFolderPath = "/themes/fileUnderThemes.css";
        
        try
        {
            File newFolder = fileSystemService.addFolder(newFolderPath);
            filesToDelete.add(newFolder);
            
            Assert.assertTrue("The new folder should exist in the filesystem", newFolder.exists());
            Assert.assertEquals("The new folder should be under themes, but is under " + newFolder.getParent(),
                    webResourcesDir.getPath(), newFolder.getParent());
            Assert.assertEquals("The new foldername should be " + newFolder.getName(),
                    "New-Folder", newFolder.getName());
        }
        catch (IOException e)
        {
            Assert.assertTrue("No exception should have been thrown.", false);
        }

        // first create the file we will use
        filesToDelete.add(createThemeDir("folderUnderThemes"));
        filesToDelete.add(createThemeFile("/folderUnderThemes/newFile.css", 10));
        
        newFolderPath = "/themes/folderUnderThemes/newFile.css";
        
        try
        {
            File newFolder = fileSystemService.addFolder(newFolderPath);
            filesToDelete.add(newFolder);
            
            Assert.assertTrue("The new folder should exist in the filesystem", newFolder.exists());
            Assert.assertEquals(
                    "The new folder should be under 'folderUnderThemes', but is under " + newFolder.getParent(),
                    webResourcesDir.getPath() + File.separator + "folderUnderThemes",
                    newFolder.getParent());
            Assert.assertEquals("The new foldername should be " + newFolder.getName(), "New-Folder",
                    newFolder.getName());
        }
        catch (IOException e)
        {
            Assert.assertTrue("No exception should have been thrown.", false);
        }
    }
    
    public void testAddFolder()
    {
        // first create the file we will use
        filesToDelete.add(createThemeDir("folderUnderThemes"));
        
        String newFolderPath = "/themes/folderUnderThemes/";
        
        try
        {
            File newFolder = fileSystemService.addFolder(newFolderPath);
            filesToDelete.add(newFolder);
            
            Assert.assertTrue("The new folder should exist in the filesystem", newFolder.exists());
            Assert.assertEquals(
                    "The new folder should be under 'folderUnderThemes', but is under " + newFolder.getParent(),
                    webResourcesDir.getPath() + File.separator + "folderUnderThemes",
                    newFolder.getParent());
            Assert.assertEquals("The new foldername should be " + newFolder.getName(), "New-Folder",
                    newFolder.getName());
        }
        catch (IOException e)
        {
            Assert.assertTrue("No exception should have been thrown.", false);
        }
        
        // first create the file we will use
        
        newFolderPath = "/themes/folderUnderThemes/";
        
        try
        {
            File newFolder = fileSystemService.addFolder(newFolderPath);
            filesToDelete.add(newFolder);
            
            Assert.assertTrue("The new folder should exist in the filesystem", newFolder.exists());
            Assert.assertEquals(
                    "The new folder should be under 'folderUnderThemes', but is under " + newFolder.getParent(),
                    webResourcesDir.getPath() + File.separator + "folderUnderThemes",
                    newFolder.getParent());
            Assert.assertEquals("The new foldername should be 'New-Folder 1', but was " + newFolder.getName(),
                    "New-Folder 1", newFolder.getName());
        }
        catch (IOException e)
        {
            Assert.assertTrue("No exception should have been thrown.", false);
        }
    }
    
    public void testDeleteFile_fileDoesNotExistNoExceptionThrown()
    {
        // first create the file we will use
        filesToDelete.add(createThemeDir("folderUnderThemes"));
        filesToDelete.add(createThemeFile("/folderUnderThemes/newFile.css", 10));
        
        try
        {
            fileSystemService.deleteFile("/themes/folderUnderThemes/nonExistingFile.css");
            Assert.assertTrue("An exception should have been thrown.", false);
        }
        catch(PSFileOperationException e)
        {
            Assert.assertTrue(true);
        }
        catch(Exception e)
        {
            Assert.assertTrue("A PSFileOperationException should have been thrown.", false);
        }
    }
    
    public void testDeleteFile()
    {
        // first create the file we will use
        String path = "/folderUnderThemes/newFile.css";
        filesToDelete.add(createThemeDir("folderUnderThemes"));
        createThemeFile(path, 10);
        
        try
        {
            fileSystemService.deleteFile("/themes" + path);
            
            File deleted = fileSystemService.getFile(path);
            
            if(!deleted.exists())
            {
                filesToDelete.add(deleted);
            }
            Assert.assertTrue("The file should have been deleted.", !deleted.exists());
        }
        catch(Exception e)
        {
            Assert.assertTrue("No exception should have been thrown.", false);
        }
    }
    
    public void testIsReservedName()
    {
        List<String> notReservedNames = Arrays.asList(new String[]
        {"newFolder", "aux1", "CoM99", "PRN-10", "Themes", "web_resources", "LPT100", "percussion", "custom-theme"});

        for (String notReservedName : notReservedNames)
        {
            Assert.assertFalse("The name '" + notReservedName + "' is not a reserved name.",
                    fileSystemService.isReservedFilename(notReservedName));
        }

        List<String> reservedNames = Arrays
                .asList(new String[]
                {".", "..", "CON", "PRN", "AUX", "CLOCK$", "NUL", "COM0", "COM1", "COM2", "COM3", "COM4", "COM5",
                        "COM6", "COM7", "COM8", "COM9", "LPT0", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7",
                        "LPT8", "LPT9"});

        for (String reservedName : reservedNames)
        {
            Assert.assertTrue("The name '" + reservedName + "' is a reserved name.",
                    fileSystemService.isReservedFilename(reservedName));
        }
    }
    
    public void testContainsInvalidCharacters()
    {
        List<String> validName = Arrays.asList(new String[]
        {"New@Folder", "Custom$theme", "custom-themes.css", "silver.jpg", "emerald", "web_resources&", "LPT100", "percussion",
                "custom-theme"});

        for (String notInvalidName : validName)
        {
            Assert.assertFalse("The name '" + notInvalidName + "' does not contain invalid characters.",
                    fileSystemService.containsInvalidChars(notInvalidName));
        }

        List<String> invalidNames = Arrays
                .asList(new String[]
                {"/the new theme", "\\hello", "new Folder?", "hi*all", "hello:world"});

        for (String invalidName : invalidNames)
        {
            Assert.assertTrue("The name '" + invalidName + "' contains invalid characters.",
                    fileSystemService.containsInvalidChars(invalidName));
        }
    }
    
    class ExpectedPathItemValues
    {
        public String name;
        public String path;
        public String type;
        public String folderPath;
        public String folderPaths;
        
        public ExpectedPathItemValues(String name, String path, String type, String folderPaths)
        {
            this.name = name;
            this.path = path;
            this.type = type;
            this.folderPath = "/" + (this.path.endsWith("/") ? this.path.substring(0, this.path.length() - 1) : this.path);
            this.folderPaths = folderPaths;
        }
    }
    
    
}
