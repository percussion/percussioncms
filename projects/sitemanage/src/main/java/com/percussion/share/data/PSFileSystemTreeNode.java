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

package com.percussion.share.data;

import java.util.List;

/***
 * A lightweight tree node meant to hold a directory tree on the file system.  
 * @author natechadwick
 *
 * @param <T> A PSFileSystemItem
 */
public class PSFileSystemTreeNode<T> implements IPSTreeNode<PSFileSystemItem> {

	private IPSTreeNode<PSFileSystemItem> parent;
	private List<IPSTreeNode<PSFileSystemItem>> children;
	private PSFileSystemItem value;
	
	@Override
	public IPSTreeNode<PSFileSystemItem> getParent() {
		return parent;
	}

	@Override
	public void setParent(IPSTreeNode<PSFileSystemItem> node) {
		this.parent = node;
	}

	@Override
	public List<IPSTreeNode<PSFileSystemItem>> getChildren() {
		return this.children;
	}

	@Override
	public PSFileSystemItem getValue() {
		return value;
	}

	@Override
	public void setValue(PSFileSystemItem val) {
		value = val;
	}

}
