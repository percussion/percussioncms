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

package com.percussion.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.xml.bind.annotation.XmlRootElement;



/**
 * @author stephenbolton
 *
 */
@XmlRootElement
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(value="Status")
public class Status
{
    /**
     * 
     */
    @ApiModelProperty(name="message", value="The message for the Status response")
    private String message;

    @ApiModelProperty(name="statusCode", value="The numeric code for the Status message")
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
