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
 *      https://www.percussion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.itemmanagement.data;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.percussion.itemmanagement.service.IPSItemService;

/**
 * Provides the revision summary that has list of PSRevision objects and other info like the item can 
 * be restored from a prior revision or not. 
 */
@XmlRootElement(name="RevisionsSummary")
public class PSRevisionsSummary 
{
	/**
	 * Indicates whether the item can be restored from a prior revision or not.
	 * @return <code>true</code> if the item can be restored from prior revision otherwise <code>false</code>.
	 */
	public boolean isRestorable() {
		return isRestorable;
	}
	
	/**
	 * Sets whether the item can be restored from a prior revision or not. 
	 * @param isRestorable pass <code>true</code>, if the item can be restored from a prior revision otherwise 
	 * <code>false</code>.
	 * @see IPSItemService#restoreRevision(String) for the cases where an item is not restorable.
	 */
	public void setRestorable(boolean isRestorable) {
		this.isRestorable = isRestorable;
	}
	
	/**
	 * @return List of revisions, may be <code>null</code>, if not set. 
	 */
	public List<PSRevision> getRevisions() {
		return revisions;
	}
	
	/**
	 * @param revisions, the list of revisions to set.
	 */
	public void setRevisions(List<PSRevision> revisions) {
		this.revisions = revisions;
	}

	public List<PSComment> getComments() {
		return comments;
	}

	public void setComments(List<PSComment> comments) {
		this.comments = comments;
	}

	private boolean isRestorable;
	private List<PSRevision> revisions;
	private List<PSComment> comments;
	
}
