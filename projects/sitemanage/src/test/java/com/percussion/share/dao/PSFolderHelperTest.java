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
package com.percussion.share.dao;

import java.util.ArrayList;
import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.cactus.ServletTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.cms.IPSConstants;
import com.percussion.share.dao.impl.PSFolderHelper;
import com.percussion.share.data.IPSItemSummary;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.webservices.security.IPSSecurityWs;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSFolderHelperTest extends ServletTestCase
{
    
    public static class PSFolderPathTest {
        private PSFolderHelper folderHelper;
        
        @Before
        public void setup() 
        {
            folderHelper = new PSFolderHelper(null,null,null,null,null,null,null,null,null,null);
        }
                
        @Test
        public void testTwoLevelParentPath() throws Exception
        {
            assertParent("//one/two", "//one");   
        }
        
        @Test
        public void testTwoLevelParentPathWithTrailingSlash() throws Exception
        {
            assertParent("//one/two/", "//one");   
        }
        
        @Test
        public void testOneLevelParentPath() throws Exception
        {
            assertParent("//one", "//");   
        }
        
        @Test(expected=IllegalArgumentException.class)
        public void testNullPath() throws Exception
        {
            folderHelper.parentPath(null);
        }
        
        private void assertParent(String path, String expected) {
            String p = folderHelper.parentPath(path);
            assertEquals("Parent path:", expected, p);
        }
    }
    
    @Override
    protected void setUp() throws Exception
    {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        securityWs.login(request, response, "admin1", "demo", null, "Enterprise_Investments_Admin", null);
    }
    
    public void testFindItems() throws Exception
    {
        List<IPSItemSummary> items = folderHelper.findItems("//Sites");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        log.debug(items);
    }
        
    public void testFindChildren() throws Exception
    {
        List<String> paths = folderHelper.findChildren("//Sites");
        log.debug("Paths : " + paths);
        assertNotNull("Paths should not be null", paths);
        assertFalse("Paths should not be empty", paths.isEmpty());        
    }
    
    public void testFindFolderChildren() throws Exception
    {
        List<IPSItemSummary> items = folderHelper.findItems("//Sites/EnterpriseInvestments");
        assertNotNull(items);
        assertFalse(items.isEmpty());
        
        List<IPSItemSummary> allitems = folderHelper.findItems("//Sites/EnterpriseInvestments", false);
        assertNotNull(allitems);
        assertFalse(allitems.isEmpty());
        assertEquals(items.size(), allitems.size());
        int folderCount = 0;
        for (IPSItemSummary itemSummary : allitems)
        {
            if (itemSummary.isFolder())
                folderCount++;
        }
        
        List<IPSItemSummary> folderItems = folderHelper.findItems("//Sites/EnterpriseInvestments", true);
        assertNotNull(folderItems);
        assertFalse(folderItems.isEmpty());
        assertTrue(items.size() > folderItems.size());
        assertEquals(folderCount, folderItems.size());
        for (IPSItemSummary itemSummary : folderItems)
        {
            assertTrue(itemSummary.isFolder());
        }
    }
        
    public void testUniqueNameInFolder() throws Exception
    {
        try
        {
            // create an item named "<ITEM-NAME>" and verify that a unique name
            // based on that item's name is "<ITEM-NAME>-copy"
            folderHelper.createFolder("//Sites/UniqueNameInFolderTest");
            String name = folderHelper.getUniqueNameInFolder("//Sites", "UniqueNameInFolderTest", "copy", 1, true);
            assertTrue(name.equals("UniqueNameInFolderTest-copy"));

            // create an item named "<ITEM-NAME>-copy" and verify that the
            // unique name based on that item's name is "<ITEM-NAME>-copy-2"
            // because the first unique name "<ITEM-NAME>-copy" was taken
            folderHelper.createFolder("//Sites/UniqueNameInFolderTest-copy");
            String name1 = folderHelper.getUniqueNameInFolder("//Sites", "UniqueNameInFolderTest", "copy", 1, true);
            assertTrue(name1.equals("UniqueNameInFolderTest-copy-2"));

            // create an item named "<ITEM-NAME>-copy-2" and verify that the
            // unique name based on that item's name is "<ITEM-NAME>-copy-3"
            // because the first unique name "<ITEM-NAME>-copy" was taken
            folderHelper.createFolder("//Sites/UniqueNameInFolderTest-copy-2");
            String name2 = folderHelper.getUniqueNameInFolder("//Sites", "UniqueNameInFolderTest", "copy", 1, true);
            assertTrue(name2.equals("UniqueNameInFolderTest-copy-3"));

            // remove "<ITEM-NAME>-copy" and verify that the unique name based
            // on that item's name is again "<ITEM-NAME>-copy"
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest-copy");
            String name3 = folderHelper.getUniqueNameInFolder("//Sites", "UniqueNameInFolderTest", "copy", 1, true);
            assertTrue(name3.equals("UniqueNameInFolderTest-copy"));

            // verify that skipFirstIndex = false generates unique name based on
            // that item's name is again "<ITEM-NAME>-copy-1"
            String name4 = folderHelper.getUniqueNameInFolder("//Sites", "UniqueNameInFolderTest", "copy", 1, false);
            assertTrue(name4.equals("UniqueNameInFolderTest-copy-1"));

            // verify that firstIndex = 10 generates unique name based on that
            // item's name "<ITEM-NAME>-copy-10"
            String name5 = folderHelper.getUniqueNameInFolder("//Sites", "UniqueNameInFolderTest", "copy", 10, false);
            assertTrue(name5.equals("UniqueNameInFolderTest-copy-10"));
        }
        finally
        {
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest");
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest-copy");
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest-copy-1");
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest-copy-2");
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest-copy-3");
            folderHelper.deleteFolder("//Sites/UniqueNameInFolderTest-copy-10");
        }
    }    
    
    public void testUniqueFolderName() throws Exception
    {
        List<String> pathsToDelete = new ArrayList<String>();
                        
        try
        {
            String folderName = "UniqueFolderNameTest";
            
            // folder name doesn't exist
            String name = folderHelper.getUniqueFolderName("//Sites", folderName);
            assertTrue(name.equals(folderName));
            
            // create an item named "<ITEM-NAME>" and verify that a unique name
            // based on that item's name is "<ITEM-NAME>-2"
            folderHelper.createFolder("//Sites/" + folderName);
            pathsToDelete.add("//Sites/" + folderName);
            String name2 = folderHelper.getUniqueFolderName("//Sites", folderName);
            assertTrue(name2.equals(folderName + "-2"));

            // create an item named "<ITEM-NAME>-2" and verify that the
            // unique name based on that item's name is "<ITEM-NAME>-3"
            // because the first unique names "<ITEM-NAME>", "<ITEM-NAME>-2" were taken
            folderHelper.createFolder("//Sites/" + name2);
            String name3 = folderHelper.getUniqueFolderName("//Sites", folderName);
            assertTrue(name3.equals(folderName + "-3"));

            // create a third folder
            folderHelper.createFolder("//Sites/" + name3);
            pathsToDelete.add("//Sites/" + name3);
            
            // delete the middle folder
            folderHelper.deleteFolder("//Sites/" + name2);
            
            // previous middle folder name should be generated
            String name4 = folderHelper.getUniqueFolderName("//Sites", folderName);
            assertTrue(name4.equals(name2));
            
            // previous middle folder name should be generated (from invalid name)
            String name5 = folderHelper.getUniqueFolderName("//Sites",
                    folderName + IPSConstants.INVALID_ITEM_NAME_CHARACTERS);
            assertTrue(name5.equals(name2));
            
            // invalid name that doesn't exist
            String name6 = folderHelper.getUniqueFolderName("//Sites",
                    "Invalid" + IPSConstants.INVALID_ITEM_NAME_CHARACTERS + "FolderName");
            assertTrue(name6.equals("InvalidFolderName"));
        }
        finally
        {
            for (String path : pathsToDelete)
            {
                folderHelper.deleteFolder(path);
            }
        }
    }    
    
    private IPSFolderHelper folderHelper;
    private IPSSecurityWs securityWs;
    
    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }

    public IPSFolderHelper getFolderHelper()
    {
        return folderHelper;
    }

    public void setFolderHelper(IPSFolderHelper folderHelper)
    {
        this.folderHelper = folderHelper;
    }
        
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSFolderHelperTest.class);

}
