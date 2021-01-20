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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * This is typically used for an operation in REST layer that does not have
 * anything to return to the client (caller) when the operation is
 * successfully completed.
 * <p>
 * When a server does not have anything in the response (for a request),
 * then the client will get an 204 (No Content) HTTP status, which may cause
 * underlying jQuery to generate a JavaScript error. So the REST operation can 
 * return this object to avoid the 204 HTTP status.  
 * 
 * @author yubingchen
 */
@JsonRootName(value = "NoContent")
public class PSNoContent
{
    /**
     * See {@link #getOperation()} for detail.
     */
    private String operation;
    
    /**
     * See {@link #getResult()} for detail.
     */
  	private String result;
    
    /*
     * Default constructor, needed for serialization in REST layer.
     */
    public PSNoContent()
    {
    }
    
    /**
     * Create an object with the specified operation.
     * 
     * @param operation the successfully completed operation
     */
    public PSNoContent(String operation)
    {
        
        this.operation = operation;
    }
    
    /**
     * Gets the name of the operation.
     * 
     * @return the of the operation, should not be blank for a valid operation.
     */
    public String getOperation()
    {
        return operation;
    }
    
    /**
     * Sets the operation.
     * 
     * @param operation the new operation, should not be blank for a valid
     * response. 
     */
    public void setOperation(String operation)
    {
        this.operation = operation;
    }
    
    /**
     * Gets the name of the result.
     * 
     * @return the of the result, should not be blank for a valid operation.
     */
    public String getResult()
    {
        return result;
    }
    
    /**
     * Sets the result.
     * 
     * @param result the result of the operation
     * response. 
     */
    public void setResult(String result)
    {
        this.result = result;
    }
    
    
}
