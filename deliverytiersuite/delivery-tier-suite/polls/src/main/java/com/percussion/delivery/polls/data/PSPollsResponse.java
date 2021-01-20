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
