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

package com.percussion.dashboardmanagement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.percussion.dashboardmanagement.data.DashboardContent;
import com.percussion.dashboardmanagement.data.DashboardContent.GadgetDef;
import com.percussion.dashboardmanagement.data.PSGadget;
import com.percussion.metadata.web.service.PSMetadataServiceRestClient;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("If you want to run these unit tests, adjust the SERVER_URL constant and start your CMS "
        + "server.")
public class PSDashboardGadgetGeneratorTest
{
    private static final String SERVER_URL = "http://localhost:9982";
    private static final String USERNAME = "Admin";
    private static final String PASSWORD = "demo";
    
    private static final String VALID_XML_FILE = "data/validGadgetsXml.xml";
    private static final String VALID_XML_FILE2 = "data/validGadgetsXml2.xml";
    private static final String INVALID_XML_FILE = "data/invalidGadgetsXml.xml";
    private static final String VALID_XML_FILE_WITH_ALL_GADGETS = "data/validGadgetsXmlWithAllGadgets.xml";
    
    private static final String GADGET_REPOSITORY_PATH = "/cm/gadgets/repository/";
    
    private PSMetadataServiceRestClient restClient;
    
    private String getGadgetUrl(String gadgetType)
    {
        if (!gadgetType.contains("/"))
            return GADGET_REPOSITORY_PATH +
                gadgetType + "/" + gadgetType + ".xml";
        else
            return GADGET_REPOSITORY_PATH + gadgetType;
    }
    
    private InputStream getDataFile(String filePath) throws FileNotFoundException
    {
        return getClass().getResourceAsStream(filePath);
    }
    
    @Before
    public void setUp()
    {
        restClient = new PSMetadataServiceRestClient(SERVER_URL);
        restClient.login(USERNAME, PASSWORD);
        restClient.removeAllGadgets();
    }
    
    @Test
    public void testGadgetGenerator_LoadsXmlFile_XmlIsValid() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        PSDashboardGadgetGenerator contentGenerator = new PSDashboardGadgetGenerator(SERVER_URL, xmlFile, USERNAME, PASSWORD);
        contentGenerator.cleanup();
        
        assertTrue("data is valid", contentGenerator.dataSuccessfullyLoaded());
        
        DashboardContent dashboardContent = contentGenerator.getRootData();
        assertNotNull("dashboard content not null", dashboardContent);
        assertEquals("gadget count", 3, dashboardContent.getGadgetDef().size());
        
        int count = 0;
        
        for (GadgetDef gad : dashboardContent.getGadgetDef())
        {
            if (gad.getGadgetType().equals("perc_comments_gadget"))
            {
                count += 7;
                assertEquals("comments gadget - column", 1, gad.getColumn());
                assertEquals("comments gadget - extended", false, gad.isExpanded());
                
                assertEquals("comments gadget - settings count", 2, gad.getUserPref().size());
                assertEquals("comments gadget - setting 1 - name", "site", gad.getUserPref().get(0).getName());
                assertEquals("comments gadget - setting 1 - value", "Site1", gad.getUserPref().get(0).getValue());
                
                assertEquals("comments gadget - setting 2 - name", "zrows", gad.getUserPref().get(1).getName());
                assertEquals("comments gadget - setting 2 - value", "5", gad.getUserPref().get(1).getValue());
            }
            else if (gad.getGadgetType().equals("cm1_welcome_gadget/perc_welcome_gadget.xml"))
            {
                count += 11;
                assertEquals("welcome gadget - column", 0, gad.getColumn());
                assertEquals("welcome gadget - extended", true, gad.isExpanded());
                
                assertEquals("welcome gadget - settings count", 0, gad.getUserPref().size());
            }
            else if (gad.getGadgetType().equals("perc_workflow_status_gadget"))
            {
                count += 13;
                assertEquals("workflow gadget - column", 1, gad.getColumn());
                assertEquals("workflow gadget - extended", false, gad.isExpanded());
                
                assertEquals("workflow gadget - settings count", 3, gad.getUserPref().size());
                assertEquals("workflow gadget - setting 1 - name", "site", gad.getUserPref().get(0).getName());
                assertEquals("workflow gadget - setting 1 - value", "@all", gad.getUserPref().get(0).getValue());
                
                assertEquals("workflow gadget - setting 2 - name", "status", gad.getUserPref().get(1).getName());
                assertEquals("workflow gadget - setting 2 - value", "Pending", gad.getUserPref().get(1).getValue());
                
                assertEquals("workflow gadget - setting 3 - name", "zrows", gad.getUserPref().get(2).getName());
                assertEquals("workflow gadget - setting 3 - value", "5", gad.getUserPref().get(2).getValue());
            }
            else
            {
                fail("gadget not expected");
            }
        }
        
        assertEquals("all gadgets loaded", 31, count);
    }
    
    @Test
    public void testGadgetGenerator_LoadsXmlFile_XmlIsInvalid() throws Exception
    {
        InputStream xmlFile = getDataFile(INVALID_XML_FILE);
        PSDashboardGadgetGenerator contentGenerator =
            new PSDashboardGadgetGenerator(SERVER_URL, xmlFile, USERNAME, PASSWORD);
        contentGenerator.cleanup();
        
        assertFalse("data is valid", contentGenerator.dataSuccessfullyLoaded());
    }
    
    @Test
    public void testGadgetGenerator_GenerateGadgets() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        PSDashboardGadgetGenerator contentGenerator =
            new PSDashboardGadgetGenerator(SERVER_URL, xmlFile, USERNAME, PASSWORD);
        
        contentGenerator.generateContent();
        
        assertTrue("data is valid", contentGenerator.dataSuccessfullyLoaded());
        
        List<PSGadget> gadgets = restClient.getCurrentGadgets();
        assertEquals("gadget count", 3, gadgets.size());
        
        int count = 0;
        
        for (PSGadget aGadget : gadgets)
        {
            if (aGadget.getUrl().equals(getGadgetUrl("perc_comments_gadget")))
            {
                count += 7;
                
                assertNotNull("1 - instanceId not null", aGadget.getInstanceId());
                assertTrue("1 - instanceId", aGadget.getInstanceId().equals(1));
                assertEquals("1 - column", 1, (int)aGadget.getCol());
                assertEquals("1 - row", 0, (int)aGadget.getRow());
                assertFalse("1 - expanded", aGadget.isExpanded());
                
                // Specific gadget settings
                assertNotNull("1 - settings not null", aGadget.getSettings());
                assertEquals("1 - settings count", 2, aGadget.getSettings().size());
                assertEquals("1 - site", "Site1", aGadget.getSettings().get("site"));
                assertEquals("1 - zrows", "5", aGadget.getSettings().get("zrows"));
            }
            else if (aGadget.getUrl().equals(getGadgetUrl("cm1_welcome_gadget/perc_welcome_gadget.xml")))
            {
                count += 11;
                
                assertNotNull("2 - instanceId not null", aGadget.getInstanceId());
                assertTrue("2 - instanceId", aGadget.getInstanceId().equals(2));
                assertEquals("2 - column", 0, (int)aGadget.getCol());
                assertEquals("2 - row", 0, (int)aGadget.getRow());
                assertTrue("2 - expanded", aGadget.isExpanded());
                
                // Specific gadget settings
                assertNotNull("2 - settings not null", aGadget.getSettings());
                assertEquals("2 - settings count", 0, aGadget.getSettings().size());
            }
            else if (aGadget.getUrl().equals(getGadgetUrl("perc_workflow_status_gadget")))
            {
                count += 13;
                
                assertNotNull("3 - instanceId not null", aGadget.getInstanceId());
                assertTrue("3 - instanceId", aGadget.getInstanceId().equals(3));
                assertEquals("3 - column", 1, (int)aGadget.getCol());
                assertEquals("3 - row", 1, (int)aGadget.getRow());
                assertFalse("3 - expanded", aGadget.isExpanded());
                
                // Specific gadget settings
                assertNotNull("3 - settings not null", aGadget.getSettings());
                assertEquals("3 - settings count", 3, aGadget.getSettings().size());
                assertEquals("3 - site", "@all", aGadget.getSettings().get("site"));
                assertEquals("3 - zrows", "5", aGadget.getSettings().get("zrows"));
                assertEquals("3 - status", "Pending", aGadget.getSettings().get("status"));
            }
            else
            {
                fail("Invalid gadget url");
            }
        }
        
        assertEquals("count", 31, count);
    }
    
    @Test
    public void testGadgetGenerator_GenerateAllGadgets() throws Exception
    {
        InputStream xmlFile = getDataFile(VALID_XML_FILE_WITH_ALL_GADGETS);
        PSDashboardGadgetGenerator contentGenerator =
            new PSDashboardGadgetGenerator(SERVER_URL, xmlFile, USERNAME, PASSWORD);
        
        contentGenerator.generateContent();
        
        assertTrue("data is valid", contentGenerator.dataSuccessfullyLoaded());
        
        List<PSGadget> gadgets = restClient.getCurrentGadgets();
        assertEquals("gadget count", 10, gadgets.size());
    }
    
    @Test
    public void testGadgetGenerator_GenerateAllGadgets_UsingMainMethod() throws Exception
    {
        URL dataFile = getClass().getResource(VALID_XML_FILE_WITH_ALL_GADGETS);
        
        String[] args = new String[4];
        args[0] = SERVER_URL;
        args[1] = USERNAME;
        args[2] = PASSWORD;
        args[3] = dataFile.getFile();
        
        PSDashboardGadgetGenerator.main(args);
        
        List<PSGadget> gadgets = restClient.getCurrentGadgets();
        assertEquals("gadget count", 10, gadgets.size());
    }
    
    @Test
    @SuppressFBWarnings({"NP_NONNULL_PARAM_VIOLATION","NP_NULL_PARAM_DEREF_NONVIRTUAL"})
    public void testGadgetGenerator_MainMethod_ArgumentsNull() throws Exception
    {
        try
        {
            PSDashboardGadgetGenerator.main(null);
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
    
    @Test
    public void testGadgetGenerator_MainMethod_SomeArgumentsNotSpecified() throws Exception
    {
        try
        {
            PSDashboardGadgetGenerator.main(new String[2]);
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
    
    @Test
    public void testGadgetGenerator_MainMethod_ServerURLIsEmptyOrNull() throws Exception
    {
        try
        {
            PSDashboardGadgetGenerator.main(new String[] {null, "username", "password", "filepath"});
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
        
        try
        {
            PSDashboardGadgetGenerator.main(new String[] {StringUtils.EMPTY, "username", "password", "filepath"});
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
    
    @Test
    public void testGadgetGenerator_MainMethod_XMLFileIsEmptyOrNull() throws Exception
    {
        try
        {
            PSDashboardGadgetGenerator.main(new String[] {"serverurl", "username", "password", null});
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
        
        try
        {
            PSDashboardGadgetGenerator.main(new String[] {"serverurl", "username", "password", StringUtils.EMPTY});
            fail("An exception should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
        }
    }
    
    @Test
    public void testGadgetGenerator_Cleanup() throws Exception
    {
        // Add some gadgets
        InputStream xmlFile = getDataFile(VALID_XML_FILE);
        PSDashboardGadgetGenerator contentGenerator =
            new PSDashboardGadgetGenerator(SERVER_URL, xmlFile, USERNAME, PASSWORD);
        
        contentGenerator.generateContent();
        
        List<PSGadget> gadgets = restClient.getCurrentGadgets();
        assertEquals("gadget count", 3, gadgets.size());
        
        // Cleanup. Use a different file with different gadgets. Make sure
        // that the cleanup method is removing only the gadgets present
        // in the XML file.
        xmlFile = getDataFile(VALID_XML_FILE2);
        contentGenerator =
            new PSDashboardGadgetGenerator(SERVER_URL, xmlFile, USERNAME, PASSWORD);
        contentGenerator.cleanup();
        
        gadgets = restClient.getCurrentGadgets();
        assertEquals("gadget count", 1, gadgets.size());
        
        assertEquals("gadget url", getGadgetUrl("perc_comments_gadget"), gadgets.get(0).getUrl());
    }
}
