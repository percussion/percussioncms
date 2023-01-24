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

package com.percussion.rest.errors;

import java.util.ResourceBundle;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.XmlRootElement;
@XmlRootElement(name = "Error")
public class RestExceptionBase extends WebApplicationException
{
    private RestErrorCode errorCode;

    private String message;

    private String detailMessage;

    private Object errorData;

    private Status status;

    public RestExceptionBase()
    {
        
    }
    
    public RestExceptionBase(RestErrorCode errorCode, String detailMessage, Object errorData, Status status)
    {
        this(errorCode, null, detailMessage, errorData, status);
    }

    public RestExceptionBase(RestErrorCode errorCode, String message, String detailMessage, Object errorData, Status status)
    {
        this.errorCode = errorCode;
        if (message == null)
        {
            ResourceBundle errorMsg = ResourceBundle.getBundle("com.percussion.rest.errors.ErrorMessages");
            this.message = errorMsg.getString(Integer.toString(errorCode.getNumVal()));
        }
        else
        {
            this.message = message;
        }
        this.detailMessage = detailMessage;
        this.errorData = errorData;
        if (status == null)
        {
            this.status = Status.INTERNAL_SERVER_ERROR;
        }
        else
        {
            this.status = status;
        }

    }

    public RestErrorCode getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(RestErrorCode errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getDetailMessage()
    {
        return detailMessage;
    }

    public void setDetailMessage(String detailMessage)
    {
        this.detailMessage = detailMessage;
    }

    public Object getErrorData()
    {
        return errorData;
    }

    public void setErrorData(Object errorData)
    {
        this.errorData = errorData;
    }

    public Status getStatus()
    {
        return status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public RestExceptionBase(Throwable cause){
        super(cause);
    }
}
