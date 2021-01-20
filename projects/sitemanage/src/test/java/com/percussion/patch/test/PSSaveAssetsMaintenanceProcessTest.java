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

package com.percussion.patch.test;

import com.percussion.assetmanagement.data.PSAsset;
import com.percussion.cms.objectstore.PSInvalidContentTypeException;
import com.percussion.cms.objectstore.server.PSItemDefManager;
import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.patch.PSSaveAssetsMaintenanceProcess;
import com.percussion.patch.PSSaveAssetsMaintenanceProcess.ItemWrapper;
import com.percussion.test.PSServletTestCase;
import com.percussion.util.PSSqlHelper;
import com.percussion.utils.testing.IntegrationTest;
import org.junit.experimental.categories.Category;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Unit Test for Saving assets in maintenance process during maintenance manager tasks
 * @author robertjohansen
 *
 */
@Category(IntegrationTest.class)
public class PSSaveAssetsMaintenanceProcessTest extends PSServletTestCase {
    
    MockMaintMgr maintMgr;
    PSSaveAssetsMaintenanceProcess proc;
    
    @Override
    public void setUp() throws Exception
    {
        maintMgr = new MockMaintMgr();
        proc = new PSSaveAssetsMaintenanceProcess(maintMgr);
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
    
    @Override
    public void tearDown()
    {
    }
    
    /**
     * test that we can obtain a connection to the db and close it
     */
    public void testConnection()
    {
        try
        {
           Connection conn = null;
           conn = proc.getConnection();
           assertTrue(conn!=null);
           proc.closeConnection();
        }
        catch(Exception e)
        {
            fail("Exception Thrown getting connection.");
        }
    }

    /**
     * Text checking of managed link fields in types.
     */
    public void testGetManagedLinkFields()
    {
       
        List<String> richTextManagedFields = proc.getManagedLinkFields("percRichTextAsset");
        assertEquals(1,richTextManagedFields.size());
        assertEquals("text",richTextManagedFields.get(0));
        
        List<String> rawHtmlManagedFields = proc.getManagedLinkFields("percRawHtmlAsset");
        assertEquals(1,rawHtmlManagedFields.size());
        assertEquals("html",rawHtmlManagedFields.get(0));
        
        List<String> simpleTestManagedFields = proc.getManagedLinkFields("percSimpleTextAsset");
        assertEquals(1,simpleTestManagedFields.size());
     
        PSItemDefManager defMgr = PSItemDefManager.getInstance();
        String[] allContentTypes = defMgr.getContentTypeNames(-1);
        
        HashSet<String> knownManagedAssets = new HashSet<String>(
                Arrays.asList("percRichTextAsset", "percRawHtmlAsset", 
                        "percBlogPostAsset", "percPage", "percPerson","percSimpleTextAsset", "percCookieConsent"));
       
        HashSet<String> unknownManagedAssets = new HashSet<String>();
        
        for (String contentType : allContentTypes)
        {
            if (proc.getManagedLinkFields(contentType).size() > 0)
            {

                List<String> managedFields = proc.getManagedLinkFields(contentType);
                if (managedFields.size() > 0)
                {
                    if (knownManagedAssets.contains(contentType))
                    {
                        knownManagedAssets.remove(contentType);
                    }
                    else
                    {
                        unknownManagedAssets.add(contentType);
                    }

                }

            }

        }

        if (knownManagedAssets.size() > 0)
        {
            fail("Did not find all known managed assets " + knownManagedAssets);
        }
        if (unknownManagedAssets.size() > 0)
        {
            fail("Found unknown managed assets " + unknownManagedAssets);
        }
        
    }
    
    /**
     * execute sql on CT_RICHTEXTASSET and CT_PERCRAWHTMLASSET
     */
    public void sqlExecution()
    {
        
        //issue running this test because the connection object needs to be set 
        Statement typeIdStat = null;
        ResultSet rawHtmlResult = null;
       
        try
        {
            
            PSItemDefManager defMgr = PSItemDefManager.getInstance();
            long rawHtmlId = defMgr.contentTypeNameToId("CT_PERCRAWHTMLASSET");
            
            Connection conn = proc.getConnection();
            
            
            String CONTENTSTATUS = PSSqlHelper.qualifyTableName("CONTENTSTATUS");
            String typeIdSelect = "SELECT CONTENTID FROM " + CONTENTSTATUS +" WHERE CONTENTTYPEID = "+ rawHtmlId;
           
           
            typeIdStat = conn.createStatement();
           
            rawHtmlResult = proc.executeSqlStatement(typeIdStat,typeIdSelect);
           
            assertTrue(rawHtmlResult != null); 
        }
        catch(Exception e)
        {
            fail("Exception testing sql execution");
        }
        finally
        {
            try{rawHtmlResult.close();} catch(Exception e){}
            try{typeIdStat.close();} catch(Exception e){}
            proc.closeConnection();
        }
    }
    
    /**
     * Test the loading of assets from the database
     * This also tests the the sql execution along with
     * the connection to the db
     */
    public void testLoadingAssets()
    {
        //load assets from DB
        Set<ItemWrapper> assets = null;
        proc.loadAssetsFromDB();
        assets = proc.getAssetListSet();
        assertTrue(assets!=null);
    }
    
    /**
     * Test the qualify process to see if the expected links
     * come back as qualified to be managed or not.
     */
    public void testQualifiedLinks()
    {
        PSAsset htmlAsset = new PSAsset();
        
        //raw html
        htmlAsset.setType("percRawHtmlAsset");
        htmlAsset.getFields().put("html", "<p>This is <a href=\"http://jsoup.org/\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p><img src=\"/Assets/img\">jsoup</img>.</p></body>");
        assertTrue(proc.qualifyAsset(htmlAsset));
        htmlAsset.getFields().put("html", "<p>This is <a href=\"//Sites/page1\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p>.</p></body>");
        assertFalse(proc.qualifyAsset(htmlAsset));
        htmlAsset.getFields().put("html", "<p>This is <a href=\"//Sites/page1\" perc-managed=\"true\"/>");
        assertTrue(proc.qualifyAsset(htmlAsset));
        
        //rich text
        PSAsset richTextAsset = new PSAsset();
        
        richTextAsset.setType("percRichTextAsset");
        richTextAsset.getFields().put("text", "<p>This is <a href=\"http://jsoup.org/\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p><img src=\"/Assets/img\">jsoup</img>.</p></body>");
        assertTrue(proc.qualifyAsset(richTextAsset));
        richTextAsset.getFields().put("text", "<p>This is <a href=\"//Sites/page1\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p>.</p></body>");
        assertFalse(proc.qualifyAsset(richTextAsset));
        richTextAsset.getFields().put("text", "<p>This is <a href=\"//Sites/page1\" perc-managed=\"true\"/>");
        assertTrue(proc.qualifyAsset(richTextAsset));
        
        //blog
        PSAsset blogAsset = new PSAsset();
        
        blogAsset.setType("percBlogPostAsset");
        blogAsset.getFields().put("postbody", "<p>This is <a href=\"http://jsoup.org/\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p><img src=\"/Assets/img\">jsoup</img>.</p></body>");
        assertTrue(proc.qualifyAsset(blogAsset));
        blogAsset.getFields().put("postbody", "<p>This is <a href=\"//Sites/page1\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p>.</p></body>");
        assertFalse(proc.qualifyAsset(blogAsset));
        blogAsset.getFields().put("postbody", "<p>This is <a href=\"//Sites/page1\" perc-managed=\"true\"/>");
        assertTrue(proc.qualifyAsset(blogAsset));
        
      //Event 
        /*
         * Event does not currently handle managed links
        PSAsset eventAsset = new PSAsset();
        
        eventAsset.setType("percEventAsset");
        eventAsset.getFields().put("body", "<p>This is <a href=\"http://jsoup.org/\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p><img src=\"/Assets/img\">jsoup</img>.</p></body>");
        assertTrue(proc.qualifyAsset(eventAsset));
        eventAsset.getFields().put("body", "<p>This is <a href=\"//Sites/page1\" data-perc-linkid=\"2\">jsoup</a>.</p><a href=\"http://test.org/\" >jsoup</a>.</p>.</p></body>");
        assertFalse(proc.qualifyAsset(eventAsset));
        eventAsset.getFields().put("body", "<p>This is <a href=\"//Sites/page1\" perc-managed=\"true\"/>");
        assertTrue(proc.qualifyAsset(eventAsset));
        */
    }
    
    
    /**
     * mock Maintenance Manager for testing purposes
     * @author robertjohansen
     *
     */
    private class MockMaintMgr implements IPSMaintenanceManager
    {

        boolean didStartWork = false;
        String procId = null;
        boolean didStopWork = false;
        boolean hasFailures = false;
        
        @Override
        public void startingWork(IPSMaintenanceProcess process)
        {
            procId = process.getProcessId();
            didStartWork = true;
        }

        @Override
        public boolean isWorkInProgress()
        {
            return didStartWork && !didStopWork;
        }

        @Override
        public void workCompleted(IPSMaintenanceProcess process)
        {
            if (process.getProcessId().equals(procId))
                didStopWork = true;
        }

        @Override
        public boolean hasFailures()
        {
            return hasFailures;
        }

        @Override
        public void workFailed(IPSMaintenanceProcess process)
        {
            hasFailures = true;
        }

        @Override
        public boolean clearFailures()
        {
            boolean hadFailures = hasFailures;
            hasFailures = false;
            return hadFailures;
        }        
    }
    
    
}
