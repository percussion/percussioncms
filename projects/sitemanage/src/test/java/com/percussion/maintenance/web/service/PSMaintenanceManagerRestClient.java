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
