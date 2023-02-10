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

/**
 * Service to get status of async jobs handled by CMS.
 * 
 * @author federicoromanelli
 */
public interface IPSAsyncJobStatusRestService
{
    /**
     * Gets the status for the given job using the async job service.
     * 
     * @param jobId - the id of the job provided by IPSAsyncJobService.startJob(String, Object).
     * Never <code>null</code>.
     *  
     * @return PSAsyncJobStatus -  The corresponding status for the given Job. Never <code>null</code>.
     * Returns an empty PSAsyncJobStatus object if no status was found with the provided jobId.
     */
    public PSAsyncJobStatus getStatus(Long jobId);
}
