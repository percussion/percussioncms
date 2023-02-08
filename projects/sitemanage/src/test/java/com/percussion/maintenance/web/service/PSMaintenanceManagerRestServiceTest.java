/*
 * Copyright 1999-2023 Percussion Software, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.percussion.maintenance.web.service;

import static org.junit.Assert.assertFalse;

import com.percussion.share.test.PSRestTestCase;

import org.junit.Before;
import org.junit.Test;

/**
 * @author JaySeletz
 *
 */
public class PSMaintenanceManagerRestServiceTest extends PSRestTestCase<PSMaintenanceManagerRestClient>
{
    
    @Test
    public void test()
    {
        assertFalse(restClient.isWorkInProgress());
        assertFalse(restClient.hasFailures(false));
        assertFalse(restClient.hasFailures(true));
    }
    
    @Before
    public void setupClient() throws Exception 
    {
        restClient = getRestClient(baseUrl);
        setupClient(restClient, "Admin", 10);
    }

    /* (non-Javadoc)
     * @see com.percussion.share.test.PSRestTestCase#getRestClient(java.lang.String)
     */
    @Override
    protected PSMaintenanceManagerRestClient getRestClient(String baseUrl)
    {
        return new PSMaintenanceManagerRestClient(baseUrl);
    }

    public PSMaintenanceManagerRestClient getRestClient()
    {
        return restClient;
    }
}
