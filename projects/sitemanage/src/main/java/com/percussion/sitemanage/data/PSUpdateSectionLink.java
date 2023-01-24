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

package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

/**
 * This class contains information for updating the section links. The old section ident. is replaced with the new section
 * in the supplied par
 * @author BJoginipally
 *
 */
@XmlRootElement(name="UpdateSectionLink")
@JsonRootName("UpdateSectionLink")
public class PSUpdateSectionLink 
{
	/**
	 * @return the old section id, the string format of the guid.
	 */
	public String getOldSectionId() {
		return oldSectionId;
	}
	
	/**
	 * @param oldSectionId the string format of the guid of the old section, should not be blank for a valid request.
	 */
	public void setOldSectionId(String oldSectionId) {
		this.oldSectionId = oldSectionId;
	}
	
	/**
	 * @return the new section id, the string format of the guid.
	 */
	public String getNewSectionId() {
		return newSectionId;
	}
	
	/**
	 * @param newSectionId the string format of the guid of the new section, should not be blank for a valid request.
	 */
	public void setNewSectionId(String newSectionId) {
		this.newSectionId = newSectionId;
	}
	
	/**
	 * @return the parent section id, the string format of the guid.
	 */
	public String getParentSectionId() {
		return parentSectionId;
	}

	/**
	 * 
	 * @param parentSectionId the string format of the guid of the parent section, should not be blank for a valid request.
	 */
	public void setParentSectionId(String parentSectionId) {
		this.parentSectionId = parentSectionId;
	}
	
    @NotBlank
    @NotNull
	private String oldSectionId;

    @NotBlank
    @NotNull
	private String newSectionId;

    @NotBlank
    @NotNull
	private String parentSectionId;
}
