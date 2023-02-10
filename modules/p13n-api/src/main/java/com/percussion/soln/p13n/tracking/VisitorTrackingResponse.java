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

package com.percussion.soln.p13n.tracking;

import java.io.Serializable;

/**
 * Represents a tracking action response with the updated profile.
 * The service may or may not include the {@link VisitorProfile visitor profile}
 * in the response.
 * 
 * @author adamgent
 *
 */
public class VisitorTrackingResponse implements Serializable {


    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = -7670678158372152228L;
 
    private String status;
    private String errorId;
    private String errorMessage;
    private long visitorProfileId;
	private VisitorProfile visitorProfile;
	
    /**
     * Constructor for serializers and programmatic use.
     */
    public VisitorTrackingResponse() {
        // Safe to serialize.
    }

    /**
     * The visitor profile maybe set in the response
     * if its enabled.
     * @return maybe <code>null</code>.
     */
	public VisitorProfile getVisitorProfile() {
		return visitorProfile;
	}

	/**
	 * See getter.
	 * @param visitorProfile maybe <code>null</code>.
	 * @see #getVisitorProfile()
	 */
	public void setVisitorProfile(VisitorProfile visitorProfile) {
		this.visitorProfile = visitorProfile;
	}
    
	/**
	 * A non-localized error message.
	 * @return maybe <code>null</code>.
	 */
    public String getErrorMessage() {
        return errorMessage;
    }
    /**
     * Sets the error message.
     * @param errorMessage maybe <code>null</code>.
     * @see #getErrorMessage()
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
    /**
     * The status of the response could be an OK or an error.
     * @return either 'OK', 'WARN', or 'ERROR'
     * @see #getErrorId()
     */
    public String getStatus() {
        return status;
    }
    
    /**
     * See getter. 
     * @param status
     */
    public void setStatus(String status) {
        this.status = status;
    }


    /**
     * The visitor profile id.
     * @return <code>0</code> indicates the profile id is not set.
     */
    public long getVisitorProfileId() {
        return visitorProfileId;
    }


    /**
     * See getter.
     * @param visitorProfileId
     * @see #getVisitorProfileId()
     */
    public void setVisitorProfileId(long visitorProfileId) {
        this.visitorProfileId = visitorProfileId;
    }
    
    /**
     * The error id is usually the fully canonical class
     * name of the exception that was thrown. 
     * @return usually <code>null</code> if no error was thrown.
     */
    public String getErrorId() {
        return errorId;
    }
    
    /**
     * See getter.
     * @param errorId maybe <code>null</code>.
     * @see #getErrorId()
     */
    public void setErrorId(String errorId) {
        this.errorId = errorId;
    }

    
}
