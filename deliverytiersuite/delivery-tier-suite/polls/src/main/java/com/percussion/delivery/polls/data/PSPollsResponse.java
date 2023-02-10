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

package com.percussion.delivery.polls.data;

/**
 * Generic response object for polls responses. It has a status and result.
 * When there is an error, the ajax response will be success full but the response object will have the 
 * status as error and the result object will be a String of error message.
 *
 */
public class PSPollsResponse
{
    private PollResponseStatus status;
    private Object result;

    public PSPollsResponse(){

    }
    public PSPollsResponse(PollResponseStatus status, Object result)
    {
        this.status = status;
        this.result = result;
    }
    public PollResponseStatus getStatus()
    {
        return status;
    }
    public void setStatus(PollResponseStatus status)
    {
        this.status = status;
    }
    public Object getResult()
    {
        return result;
    }
    public void setResult(Object result)
    {
        this.result = result;
    }
    public static enum PollResponseStatus
    {
        SUCCESS, ERROR
    }

}
