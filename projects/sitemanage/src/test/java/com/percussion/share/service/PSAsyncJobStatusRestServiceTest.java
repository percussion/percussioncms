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

package com.percussion.share.service;

import com.percussion.error.PSExceptionUtils;
import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.test.PSRestTestCase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
              log.error(PSExceptionUtils.getMessageForLog(e));
              log.debug(PSExceptionUtils.getDebugMessageForLog(e));
              Thread.currentThread().interrupt();
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
