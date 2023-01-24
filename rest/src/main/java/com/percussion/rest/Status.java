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

package com.percussion.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;


/**
 * @author stephenbolton
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description="Status")
public class Status
{
    /**
     * 
     */
    @Schema(name="message", description="The message for the Status response")
    private String message;

    @Schema(name="statusCode", description="The numeric code for the Status message")
    private int statusCode;

    public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public Status()
    {
    }
    
    public Status(String message)
    {
        this.message=message;
    }
    
    public Status(int statusCode, String message)
    {
    	this.statusCode = statusCode;
        this.message=message;
    }

    /**
     * @return status message
     */
    public String getMessage()
    {
        return message;
    }

    /**
     * @param message
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
}
