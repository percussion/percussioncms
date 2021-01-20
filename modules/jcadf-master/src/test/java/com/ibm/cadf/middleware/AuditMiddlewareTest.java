/*
 * Copyright 2016 IBM Corp.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.ibm.cadf.middleware;

import java.io.File;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.ibm.cadf.exception.CADFException;
import com.ibm.cadf.middleware.AuditContext;
import com.ibm.cadf.middleware.AuditMiddleware;
import com.ibm.cadf.model.Event;
import com.ibm.cadf.util.Constants;

public class AuditMiddlewareTest
{

    @Before
    public void setUp()
    {
        System.setProperty(Constants.API_AUDIT_MAP, "api_audit_map.conf");
    }

    @Test
    public void audit()
    {
        try
        {
            AuditMiddleware middleware = new AuditMiddleware(Constants.AUDIT_FORMAT_TYPE_JSON);
            AuditContext ctx = new AuditContext();
            ctx.setIniatorName("root");
            ctx.setTargetName("swift");
            ctx.setTargetUrl("http://hostname:8080");
            ctx.setTargetUsername("test:tester");
            ctx.setObserverName("gpfs");
            ctx.setInitiatorIP("192.0.0.1");
            Event event = middleware.createEvent(Constants.MIGRATE_ACTION, "SUCCESS", ctx);

            // Assert for the data
            Assert.assertEquals("root", event.getInitiator().getName());
            Assert.assertEquals("swift", event.getTarget().getName());
            Assert.assertEquals("http://hostname:8080", event.getTarget().getAddresses().get(0).getUrl());
            Assert.assertEquals("gpfs", event.getObserver().getName());
            Assert.assertEquals("192.0.0.1", event.getInitiator().getHost().getAddress());

            middleware.audit(event);
        }
        catch (CADFException e)
        {
            Assert.fail();
        }
    }

    @AfterClass
    public static void clean()
    {
        File auditFile = new File(Constants.JSON_AUDIT_FILES_NAME);
        //auditFile.delete();
    }

}
