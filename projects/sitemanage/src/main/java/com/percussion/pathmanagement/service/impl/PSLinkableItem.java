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

package com.percussion.pathmanagement.service.impl;

import com.percussion.share.service.IPSLinkableItem;

public class PSLinkableItem implements IPSLinkableItem {

	private String folderPath = null;
	private String id = null;
	private String type = null;
			
	public PSLinkableItem(String id, String folderPath, String type)
	{
		this.id=id;
		this.folderPath=folderPath;
		this.type=type;
	}
			
	@Override
	public String getFolderPath() {
		return folderPath;
	}

	@Override
	public void setFolderPath(String path) {
		this.folderPath=path;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return type;
	}

}
