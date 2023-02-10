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

