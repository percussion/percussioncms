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
package com.percussion.maintenance.web.service;

import com.percussion.maintenance.service.IPSMaintenanceManager;
import com.percussion.maintenance.service.IPSMaintenanceProcess;
import com.percussion.maintenance.service.PSMockMaintenanceProcess;
import com.percussion.servlets.PSSecurityFilter;
import com.percussion.share.test.PSRestClient.RestClientException;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.request.PSRequestInfo;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.core.Response.Status;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.junit.experimental.categories.Category;

/**
 * Server-side testing of REST services and ANT calls so that we can control the state of the maintenance manager using
 * server-side APIs not exposed by the REST layer.
 * 
 * @author JaySeletz
 *
 */
@Category(IntegrationTest.class)
public class PSMaintenanceManagerServerTest extends PSServletTestCase
{
    IPSMaintenanceManager maintenanceManager;
    IPSMaintenanceProcess maintenanceProcess;
    PSMaintenanceManagerRestClient restClient;
    
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        maintenanceManager = (IPSMaintenanceManager) getBean("maintenanceManager");
        maintenanceProcess = new PSMockMaintenanceProcess("PSMaintenanceManagerServerTest");
        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_USER, "Admin");
        PSSecurityFilter.authenticate(request, response, "Admin", "demo");
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        maintenanceManager.clearFailures();
        super.tearDown();
    }
    
    public void testRestClient() throws Exception
    {
        boolean workInProgress = false;
        
        PSMaintenanceManagerRestServiceTest restTest = new PSMaintenanceManagerRestServiceTest();
        restTest.baseUrl = "http://localhost:9992/Rhythmyx";
        restClient = restTest.getRestClient(restTest.baseUrl);
        
        try
        {
            assertFalse(restClient.isWorkInProgress());
            assertFalse(restClient.hasFailures(false));
            
            maintenanceManager.startingWork(maintenanceProcess);
            workInProgress = true;
            
            assertTrue(restClient.isWorkInProgress());
            assertFalse(restClient.hasFailures(false));
            
            maintenanceManager.workCompleted(maintenanceProcess);
            assertFalse(maintenanceManager.isWorkInProgress());
            workInProgress = false;
            assertFalse(maintenanceManager.hasFailures());
            assertFalse(restClient.hasFailures(false));
            assertFalse(restClient.isWorkInProgress());
            
            
            maintenanceManager.startingWork(maintenanceProcess);
            workInProgress = true;
            
            assertTrue(restClient.isWorkInProgress());
            assertFalse(restClient.hasFailures(false));
            
            maintenanceManager.workFailed(maintenanceProcess);
            assertTrue(maintenanceManager.hasFailures());
            assertFalse(maintenanceManager.isWorkInProgress());
            workInProgress = false;
            assertTrue(restClient.hasFailures(false));
            assertFalse(restClient.isWorkInProgress());
            
            // test clear errors unauthenticated
            boolean didThrow = false;
            try
            {
                restClient.hasFailures(true);
            }
            catch (RestClientException e)
            {
                assertEquals(Status.FORBIDDEN, Status.fromStatusCode(e.getStatus()));
                didThrow = true;
            }
            assertTrue(didThrow);
            assertTrue(maintenanceManager.hasFailures());
            
            // now "login" as Admin
            restTest.setupClient();
            restClient = restTest.getRestClient();
            restClient.hasFailures(true);
            assertFalse(maintenanceManager.hasFailures());
        }
        finally
        {
            if (workInProgress)
                maintenanceManager.workCompleted(maintenanceProcess);
        }
    }
    
    public void testAntCalls() throws Exception
    {
        File buildFile = createBuildFile();
        runAntTask(buildFile, "checkForMaint");
        runAntTask(buildFile, "checkForErrors");
        
        boolean workInProgress = false;
        
        try
        {
            maintenanceManager.startingWork(maintenanceProcess);
            assertTrue(maintenanceManager.isWorkInProgress());
            workInProgress = true;
            
            boolean didFail = false;
            try
            {
                runAntTask(buildFile, "checkForMaint");
            }
            catch (BuildException e)
            {
                assertEquals("check-maint-timeout", e.getLocalizedMessage());
                didFail = true;
            }
            assertTrue(didFail);
            
            maintenanceManager.workFailed(maintenanceProcess);
            assertTrue(maintenanceManager.hasFailures());
            assertFalse(maintenanceManager.isWorkInProgress());
            workInProgress = false;
            
            didFail = false;
            try
            {
                runAntTask(buildFile, "checkForErrors");
            }
            catch (BuildException e)
            {
                assertEquals("check-errors-timeout", e.getLocalizedMessage());
                didFail = true;
            }
            assertTrue(didFail);
            
            maintenanceManager.clearFailures();
            
            didFail = false;
            try
            {
                runAntTask(buildFile, "checkForBoth");
            }
            catch (BuildException e)
            {
                fail(e.getLocalizedMessage());
            }
            
        }
        finally
        {
            if (workInProgress)
                maintenanceManager.workCompleted(maintenanceProcess);
        }
    }
    
    private void runAntTask(File buildFile, String task)
    {
        Project p = new Project();
        p.setUserProperty("ant.file", buildFile.getAbsolutePath());
        p.init();
        ProjectHelper helper = ProjectHelper.getProjectHelper();
        p.addReference("ant.projectHelper", helper);
        helper.parse(p, buildFile);
        p.executeTarget(task);
    }

    /**
     * @return The build file, not <code>null</code>
     * @throws IOException 
     */
    private File createBuildFile() throws IOException
    {
        File tmpFile = File.createTempFile("PSMaintMgrTest", ".xml");
        FileUtils.copyInputStreamToFile(this.getClass().getResourceAsStream("build.xml"), tmpFile);
        
        return tmpFile;
    }
}
