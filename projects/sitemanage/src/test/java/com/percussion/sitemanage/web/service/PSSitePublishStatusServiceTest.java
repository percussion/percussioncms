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
package com.percussion.sitemanage.web.service;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import com.percussion.utils.testing.IntegrationTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.percussion.share.test.PSRestTestCase;
import com.percussion.sitemanage.data.PSSitePublishItem;
import com.percussion.sitemanage.data.PSSitePublishJob;
import com.percussion.sitemanage.data.PSSitePublishLogDetailsRequest;
import com.percussion.sitemanage.data.PSSitePublishLogRequest;
import com.percussion.sitemanage.data.PSSitePublishPurgeRequest;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class PSSitePublishStatusServiceTest
{
    private PSSitePublishStatusRestClient publishStatusClient;
    
    @Before
    public void setUp() throws Exception {
        publishStatusClient = new PSSitePublishStatusRestClient();
        PSRestTestCase.setupClient(publishStatusClient);
    }
    
    @Test
    public void testGetCurrentJobs() throws Exception
    {
        List<PSSitePublishJob> jobs = publishStatusClient.getCurrentJobs();
        log.debug("Jobs: " + jobs);
        assertNotNull(jobs);
    }
    
    @Test
    public void testGetJobDetails() throws Exception
    {
        PSSitePublishLogDetailsRequest r = new PSSitePublishLogDetailsRequest();
        r.setJobid(100);
        r.setShowOnlyFailures(false);
        r.setSkipCount(0);
        
        List<PSSitePublishItem> items = publishStatusClient.getJobDetails(r);
        log.debug("Items: " + items);
        assertNotNull(items);
    }
    
    @Test
    public void testGetLogs() throws Exception
    {
     
        PSSitePublishLogRequest lr = new PSSitePublishLogRequest();
        lr.setDays(0);
        lr.setMaxcount(100);
        lr.setShowOnlyFailures(false);
        lr.setSkipCount(0);
        List<PSSitePublishJob> logs = publishStatusClient.getLogs(lr);
        log.debug("logs: " + logs);
        assertNotNull(logs);
    }
    
    @Test
    public void testPurgeLog() throws Exception
    {
        PSSitePublishPurgeRequest pr = new PSSitePublishPurgeRequest();
        pr.setJobids(asList(100L,200L));
        publishStatusClient.purgeLog(pr);
    }
    
    /**
     * The log instance to use for this class, never <code>null</code>.
     */
    private static final Logger log = LogManager.getLogger(PSSitePublishStatusServiceTest.class);


}

