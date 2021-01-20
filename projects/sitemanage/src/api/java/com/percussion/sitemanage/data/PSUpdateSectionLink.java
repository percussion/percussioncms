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
