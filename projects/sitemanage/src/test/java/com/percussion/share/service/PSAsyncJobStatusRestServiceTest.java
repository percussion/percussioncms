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

package com.percussion.share.service;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.test.PSRestTestCase;

public class PSAsyncJobStatusRestServiceTest extends PSRestTestCase<PSAsyncJobStatusRestClient>
{

    private static final Logger log = LogManager.getLogger(PSAsyncJobStatusRestServiceTest.class);

    @Override
    protected PSAsyncJobStatusRestClient getRestClient(@SuppressWarnings("unused")
    String baseUrl)
    {
        return new PSAsyncJobStatusRestClient();
    }

    @Test
    public void testGetStatus() throws Exception
    {
      long jobId = restClient.startDummyJob();
      assertNotNull(jobId);
      
      PSAsyncJobStatus jobStatus = restClient.getStatus(String.valueOf(jobId));
      assertNotNull (jobStatus);
      assertNotNull (jobStatus.getJobId());
      assertNotNull (jobStatus.getStatus());
      assertNotNull (jobStatus.getMessage());
      while (jobStatus.getStatus().intValue() < 100)
      {
          try
          {
              assertNotNull (jobStatus);
              assertTrue(jobStatus.getMessage().startsWith("STATUS"));
              Thread.sleep(5);
              jobStatus = restClient.getStatus(String.valueOf(jobId));
          }
          catch (InterruptedException e)
          {
              log.error(e.getMessage());
              log.debug(e.getMessage(), e);
          }
      }
      assertEquals(100, jobStatus.getStatus().intValue());
      assertEquals("COMPLETED", jobStatus.getMessage());

    }
    
    @Ignore
    @Test
    public void testGetStatusFail() throws Exception
    {
       String jobId = "123456789";
       PSAsyncJobStatus jobStatus = restClient.getStatus(jobId);
       assertNotNull (jobStatus);
       assertNull (jobStatus.getJobId());
       assertNull (jobStatus.getStatus());
       assertNull (jobStatus.getMessage());
    }
}
