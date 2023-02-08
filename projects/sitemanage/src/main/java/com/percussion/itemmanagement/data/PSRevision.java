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

package com.percussion.itemmanagement.data;

import static org.apache.commons.lang.Validate.notEmpty;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;
import javax.xml.bind.annotation.XmlRootElement;
import net.sf.oval.constraint.NotEmpty;

/**
 * Encapsulates revision information for a page or asset including revision id, last time it was modified,
 * who modified it last, and its current state
 */
@XmlRootElement(name="Revision")
@JsonRootName("Revision")
public class PSRevision extends PSAbstractDataObject
{
    int revId;
    
    @NotEmpty
    String lastModifiedDate;
    
    String lastModifier;
    
    @NotEmpty
    String status;

    /**
     * Default constructor. For serializers.
     */
    public PSRevision()
    {
    }
    
    /**
     * Constructs an instance of the class.
     * 
     * @param revId revision id not an actual ID but a numeric counter
     * @param lastModifiedDate date when this item was last modified, never blank
     * @param lastModifier the user name who modified the item last, never blank
     * @param status status of this page or asset, never blank
     */
    public PSRevision(int revId, String lastModifiedDate, String lastModifier, String status)
    {
        notEmpty(lastModifiedDate, "lastModifiedDate");
        notEmpty(status, "status");
        
        this.revId = revId;
        this.lastModifiedDate = lastModifiedDate;
        this.lastModifier = lastModifier;
        this.status = status;
    }
    
    /**
     * Gets the revision ID of the item.
     * 
     * @return revision ID, not blank for a valid item
     */
   	public int getRevId() {
		return revId;
	}

    /**
     * Sets revision ID for the item.
     * 
     * @param revId ID of item, not blank for a valid item
     */
	public void setRevId(int revId) {
		this.revId = revId;
	}

    /**
     * Gets the last date/time the item was modified.
     * <p>
     * Note, this is the last date/time the item was modified, checked in/out;
     * but not the date/time the item was (workflow) transitioned.
     * 
     * @return the modified date and time, not blank for a valid item.
     */
	public String getLastModifiedDate() {
		return lastModifiedDate;
	}

    /**
     * Sets the last modified date and time.
     * 
     * @param lastModifiedDate the new date and time, not blank for a valid item.
     * @see #getLastModifiedDate()
     */
	public void setLastModifiedDate(String lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}

    /**
     * Gets the user name who modified the item last.
     * <p>
     * Note, this is the last user who modified the item, checked in/out the item;
     * but not the user who (workflow) transitioned the item.
     *  
     * @return the user name, not blank for a valid item. It may be 
     * {@link #SYSTEM_USER} if the item was modified by the system.
     */
	public String getLastModifier() {
		return lastModifier;
	}

    /**
     * Sets the user name who modified the item last.
     * 
     * @param lastModifier the user name, should not be blank for a valid item.
     */
	public void setLastModifier(String lastModifier) {
		this.lastModifier = lastModifier;
	}

    /**
     * Gets the (workflow) state name.
     * 
     * @return status, never blank
     */
	public String getStatus() {
		return status;
	}

    /**
     * Sets the (workflow) state name.
     * 
     * @param status the new state name, never blank
     */
	public void setStatus(String status) {
		this.status = status;
	}
}
