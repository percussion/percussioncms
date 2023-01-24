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
