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

package com.percussion.integritymanagement.service.impl;

import com.percussion.integritymanagement.data.PSIntegrityStatus;
import com.percussion.integritymanagement.data.PSIntegrityStatus.Status;
import com.percussion.integritymanagement.data.PSIntegrityTask;
import com.percussion.integritymanagement.data.PSIntegrityTask.TaskStatus;
import com.percussion.integritymanagement.service.IPSIntegrityCheckerService.IntegrityTaskType;
import com.percussion.server.PSRequest;
import com.percussion.share.service.exception.PSDataServiceException;
import com.percussion.share.spring.PSSpringWebApplicationContextUtils;
import com.percussion.test.PSServletTestCase;
import com.percussion.utils.request.PSRequestInfo;
import com.percussion.utils.service.IPSUtilityService;
import com.percussion.utils.testing.IntegrationTest;
import com.percussion.webservices.security.IPSSecurityWs;
import com.percussion.webservices.security.PSSecurityWsLocator;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSIntegrityCheckerServiceTest  extends PSServletTestCase
{
    private PSIntegrityCheckerService service;
    private IPSSecurityWs securityWs;
    private IPSUtilityService utilityService;
    
    @Override
    protected void setUp() throws Exception
    { 
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        init("Admin", "demo", "Default");
        //FB:IJU_SETUP_NO_SUPER NC 1-16-16
        super.setUp();
    }
    @SuppressWarnings("unchecked")
    public void init(String uid, String pwd, String community) throws Exception {
        PSSpringWebApplicationContextUtils.injectDependencies(this);
        PSRequestInfo.resetRequestInfo();
        PSRequest req = PSRequest.getContextForRequest();
        PSRequestInfo.initRequestInfo((Map) null);
        PSRequestInfo.setRequestInfo(PSRequestInfo.KEY_PSREQUEST, req);
        setSecurityWs(PSSecurityWsLocator.getSecurityWebservice());
        securityWs.login(request, response, uid, pwd, null, community, null);
    }
    @Test
    public void testIntegrityService() throws PSDataServiceException {
        if (utilityService.isSaaSEnvironment())
        {
            // Check the start and status methods
            PSIntegrityStatus status = start();
            assertNotNull(status);
            assertTrue(status.getStatus().equals(Status.SUCCESS));
            Set<PSIntegrityTask> tasks = status.getTasks();
            for (PSIntegrityTask task : tasks)
            {
                assertTrue(task.getStatus().equals(TaskStatus.SUCCESS));
            }
            // Check the delete method
            service.delete(status.getToken());
            status = service.getStatus(status.getToken());
            assertNull(status);
        }
    }
    @Test
    public void testIntegrityServiceHistory() throws PSDataServiceException {
        if (utilityService.isSaaSEnvironment())
        {
            List<PSIntegrityStatus> statuses = service.getHistory();
            int initialSize = statuses.size();
            start();
            statuses = service.getHistory();
            int curSize = statuses.size();
            assertEquals(curSize, initialSize + 1);
            start();
            statuses = service.getHistory();
            curSize = statuses.size();
            assertEquals(curSize, initialSize + 2);
            // Test Delete also
            for (PSIntegrityStatus status : statuses)
            {
                service.delete(status.getToken());
            }
            statuses = service.getHistory();
            assertEquals(statuses.size(), 0);
        }
    }
    private PSIntegrityStatus start() throws PSDataServiceException {
        String token = service.start(IntegrityTaskType.cm1);
        long startTime = new Date().getTime();
        long endTime = startTime;
        boolean processed = false;
        PSIntegrityStatus status = null;
        while(!processed && endTime - startTime < 10000){
            status = service.getStatus(token);
            if(!status.getStatus().equals(Status.RUNNING)){
                processed = true;
            }
            else{
                endTime = new Date().getTime();
            }
        }
        return status;
    }

    public PSIntegrityCheckerService getService()
    {
        return service;
    }
    public void setService(PSIntegrityCheckerService service)
    {
        this.service = service;
    }
    public IPSSecurityWs getSecurityWs()
    {
        return securityWs;
    }

    public void setSecurityWs(IPSSecurityWs securityWs)
    {
        this.securityWs = securityWs;
    }
    public IPSUtilityService getUtilityService()
    {
        return utilityService;
    }
    public void setUtilityService(IPSUtilityService utilityService)
    {
        this.utilityService = utilityService;
    }


}
