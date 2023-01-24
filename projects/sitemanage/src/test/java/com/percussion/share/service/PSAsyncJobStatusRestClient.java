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

import com.percussion.share.async.PSAsyncJobStatus;
import com.percussion.share.test.PSObjectRestClient;

public class PSAsyncJobStatusRestClient extends PSObjectRestClient
{
    private String path = "/Rhythmyx/services/share/jobstatus/";

    public PSAsyncJobStatus getStatus(String jobId)
    {
        return getObjectFromPath(concatPath(path, jobId), PSAsyncJobStatus.class);
    }
    
    public Long startDummyJob()
    {
        //return getObjectFromPath(concatPath(path, "startDummyJob"), Long.class);
        return Long.valueOf(GET(concatPath(path, "startTestJob")));
    }
}
