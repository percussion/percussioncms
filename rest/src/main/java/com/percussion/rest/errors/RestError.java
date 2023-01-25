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

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "Error")
public class RestError
{
    private int errorCode;

    private String errorType;

    private String message;

    private String detailMessage;

    private Object errorData;

    public RestError()
    {

    }

    public RestError(int errorCode, String errorType, String message, String detailMessage, Object errorData)
    {
        this.errorCode = errorCode;
        this.errorType = errorType;
        this.message = message;
        this.errorData = errorData;
        this.detailMessage = detailMessage;
    }

    public int getErrorCode()
    {
        return errorCode;
    }

    public void setErrorCode(int errorCode)
    {
        this.errorCode = errorCode;
    }

    public String getErrorType()
    {
        return errorType;
    }

    public void setErrorType(String errorType)
    {
        this.errorType = errorType;
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

    @Override
    public String toString()
    {
        return "RestError [errorCode=" + errorCode + ", errorType=" + errorType + ", message=" + message
                + ", detailMessage=" + detailMessage + ", errorData=" + errorData + "]";
    }

    
    
}
