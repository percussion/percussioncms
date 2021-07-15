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
