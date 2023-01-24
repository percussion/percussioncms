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
package com.percussion.membership.data;

/**
 * Base class for membership rest call results
 * 
 * @author JaySeletz
 */
public class PSMembershipResult
{

    protected STATUS status;
    protected String message;

    public PSMembershipResult(STATUS status, String message)
    {
        this.status = status;
        this.message = message;
    }
    
    public STATUS getStatus()
    {
        return status;
    }

    public String getMessage()
    {
        return message;
    }

    /**
     * Enumeration of result status.
     */
    public enum STATUS
    {
       SUCCESS,
       INVALID_PARAM,
       UNEXPECTED_ERROR, 
       MEMBER_EXISTS, 
       AUTH_FAILED,
       INVALID_RESET_KEY
    }

}
