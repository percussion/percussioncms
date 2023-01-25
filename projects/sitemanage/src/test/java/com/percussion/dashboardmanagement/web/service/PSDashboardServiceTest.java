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
package com.percussion.dashboardmanagement.web.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.percussion.dashboardmanagement.data.PSDashboard;
import com.percussion.share.test.PSDataServiceRestClient;
import com.percussion.share.test.PSRestTestCase;

import org.junit.Ignore;
import org.junit.Test;

@Ignore("SUT are not used")
public class PSDashboardServiceTest extends PSRestTestCase<PSDashboardServiceTest.DashboardRestClient> {
    
    protected DashboardRestClient restClient;

    @Test
    public void testLoad() throws Exception {
        PSDashboard actual = restClient.load();
        assertNotNull(actual);
    }

    @Test
    public void testSave() throws Exception
    {
        PSDashboard gadget = new PSDashboard();
        PSDashboard actual = restClient.save(gadget);
        assertNotNull(actual);
        assertEquals("Dashboard should have admin1 as id: ", "admin1", actual.getId());
        //assertEquals("Gadget ids should be the same", gadget.getId(), actual.getId());
    }

    
    @Override
    protected DashboardRestClient getRestClient(String baseUrl) {
        restClient = new DashboardRestClient(baseUrl);
        return restClient;
    }
    
    
    public static class DashboardRestClient extends PSDataServiceRestClient<PSDashboard> {
        public DashboardRestClient(String url) {
            super(PSDashboard.class, url, "/Rhythmyx/services/dashboardmanagement/dashboard/");
        }        
 
        public PSDashboard load() {
            return getObjectFromPath(getPath());
        }
        @Override
        public PSDashboard save(PSDashboard dashboard) {
            return getObjectFromPath(getPath());
        }
    }

}
