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

import com.percussion.share.test.PSObjectRestClient;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.Response.Status;

/**
 * @author JaySeletz
 *
 */
public class PSMaintenanceManagerRestClient extends PSObjectRestClient
{
    private String path = "/Rhythmyx/services/maintenance/manager/";

    public PSMaintenanceManagerRestClient(String baseUrl)
    {
        super(baseUrl);
    }
    
    /**
     * Determine if maintenance work has failed.  May be called regardless of whether work is in progress.
     * 
     * @param clearErrors <code>true</code> to clear errors if found, <code>false</code> otherwise
     * @return <code>true</code> if a maintenance process has failed, <code>false</code> if not.
     */
    public boolean hasFailures(boolean clearErrors)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("clearErrors", Boolean.toString(clearErrors));
        
        try
        {
            GET(concatPath(path, "status/process"), params.entrySet());
        }
        catch (RestClientException e)
        {
            if (Status.CONFLICT.equals(Status.fromStatusCode(e.getStatus())))
                return true;
            
            throw e;
        }
        
        return false;
        
    }
    
    /**
     * Determine if maintenance work is in progress.
     * 
     * @return <code>true</code> if so, <code>false</code> if not.
     */
    public boolean isWorkInProgress()
    {
        try
        {
            GET(concatPath(path, "status/server"));
        }
        catch (RestClientException e)
        {
            if (Status.CONFLICT.equals(Status.fromStatusCode(e.getStatus())))
                return true;
            
            throw e;
        }
        
        return false;
    }
}
