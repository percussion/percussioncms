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

import com.percussion.pathmanagement.data.PSPathItem;

/***
 * Represents a node in the CMS file tree.
 * 
 * @author natechadwick
 *
 */
public class PSCMSTreeNode implements IPSTreeNode<PSPathItem> {

	private IPSTreeNode<PSPathItem> parent;
	private List<PSCMSTreeNode> children;
	private PSPathItem value;
	
	@Override
	public IPSTreeNode<PSPathItem> getParent() {
		return this.parent;
	}

	@Override
	public void setParent(IPSTreeNode<PSPathItem> node) {
		this.parent = node;
	}
	
	@Override
	public List<IPSTreeNode<PSPathItem>> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PSPathItem getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(PSPathItem x) {
		// TODO Auto-generated method stub
		
	}

}
