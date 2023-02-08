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
