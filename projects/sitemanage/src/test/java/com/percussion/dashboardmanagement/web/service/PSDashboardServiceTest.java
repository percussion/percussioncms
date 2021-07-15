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
