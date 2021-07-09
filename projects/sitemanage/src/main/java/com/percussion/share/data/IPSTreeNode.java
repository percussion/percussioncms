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
 * Defines a TreeNode.  @see IPSTree
 * @author natechadwick
 *
 */
public interface IPSTreeNode<T> {

	/***
	 * Gets the Node's parent.
	 * @return
	 */
	public IPSTreeNode<T> getParent();
	
	/***
	 * Sets the Nodes parent.
	 * @param node
	 */
	public void setParent(IPSTreeNode<T> node);
	
	/***
	 * Gets the children of this node.
	 * @return
	 */
	public List<IPSTreeNode<T>> getChildren();

	/***
	 * Gets the Value stored in this node.
	 * @return
	 */
	public T getValue();
    
	/***
	 * Sets the value store din this node. 
	 * @param x
	 */
	void setValue(T x);
}
