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

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement
@ApiModel(description = "Represents a multi-user lock on an object in the system.")
public class ObjectLockSummary {

    /**
     * The session which has this object locked, never <code>null</code> or
     * empty.
     */
    @ApiModelProperty(notes="The session id of the user who has this oject locked.")
    private String session;

    /**
     * The user who has this object locked, never <code>null</code> or empty.
     */
    @ApiModelProperty(notes="The username of the user that has the object locked, never null or empty")
    private String locker;

    /**
     * The remaining lock time, always > 0.
     */
    @ApiModelProperty(notes="The remaining lock time, always >0")
    private long remainingTime;

    @ApiModelProperty(notes="The date and time that the API client last checked this lock.  Can be used for retries.")
    private String callerAccessTime;

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public String getLocker() {
        return locker;
    }

    public void setLocker(String locker) {
        this.locker = locker;
    }

    public long getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(long remainingTime) {
        this.remainingTime = remainingTime;
    }

    public String getCallerAccessTime() {
        return callerAccessTime;
    }

    public void setCallerAccessTime(String callerAccessTime) {
        this.callerAccessTime = callerAccessTime;
    }

    public ObjectLockSummary(){}
}
