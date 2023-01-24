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

package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.json.JSONObject;

/**
 * A generic REST response data object.
 * @author BJoginipally
 *
 */
@JsonRootName(value = "RestResponse")
public class PSRestResponse
{
    private PSRestResponseStatus status;
    private String result;
    
    public PSRestResponse()
    {
        
    }
    
    /**
     * Constructor for creating default error or success responses.
     * @param responseType if <code>true</code> creates a success response otherwise error response.
     */
    @SuppressWarnings("unchecked")
    public PSRestResponse(boolean responseType)
    {
        JSONObject res = new JSONObject();
        if(responseType)
        {
            status = PSRestResponseStatus.SUCCESS;
            res.put(DEFAULT_MESSAGE, DEFAULT_SUCCESS_MESSAGE);
        }
        else
        {
            status = PSRestResponseStatus.ERROR;
            res.put(DEFAULT_MESSAGE, DEFAULT_ERROR_MESSAGE);
        }
        result = res.toString();
    }
    
    public PSRestResponse(PSRestResponseStatus status, String result)
    {
        this.status = status;
        this.result = result;
    }
    
    /**
     * @return response the status may be <code>null</code> if not set.
     */
    public PSRestResponseStatus getStatus()
    {
        return status;
    }

    public void setStatus(PSRestResponseStatus status)
    {
        this.status = status;
    }
    
    /**
     * @return Object the result object may be <code>null</code> if not set.
     */
    public String getResult()
    {
        return result;
    }
    
    public void setResult(String result)
    {
        this.result = result;
    }
    
    public static enum PSRestResponseStatus
    {
        SUCCESS, ERROR
    }
    
    private static final String DEFAULT_MESSAGE = "message";
    private static final String DEFAULT_SUCCESS_MESSAGE = "Your request has been successfully completed.";
    private static final String DEFAULT_ERROR_MESSAGE = "Unexpected error occurred while executing your request.";
    
}
