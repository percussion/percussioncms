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
