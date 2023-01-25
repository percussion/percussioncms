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
package com.percussion.licensemanagement.data;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Container for status and message from calls to Netsuite.
 * Will be serialized to JSON.
 *
 */
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
@XmlType(name = "", propOrder = {
    "status","message"
})
@XmlRootElement(name = "netsuiteResponse")
public class PSNetsuiteResponse
{
    private String status;
    private String message;
   
    public PSNetsuiteResponse(String status, String message){
        this.status = status;
        this.message = message;
    }
    
    public PSNetsuiteResponse()
    {
        this.status = new String();
        this.message = new String();
    }
    
    /**
     * @return the status
     */
    public String getStatus()
    {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(String status)
    {
        this.status = status;
    }
    /**
     * @return the message
     */
    public String getMessage()
    {
        return message;
    }
    /**
     * @param message the message to set
     */
    public void setMessage(String message)
    {
        this.message = message;
    }
    
}
