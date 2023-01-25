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
package com.percussion.pso.restservice.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class FolderInfo {
	
	private FolderAcl folderAcl;
	private String pubFileName;
	private String globalTemplate;
	
	List<ItemRef> folderItems;
	@XmlElementWrapper(name="Contents")
	@XmlElement(name="Item")
	public List<ItemRef> getFolderItems() {
		return folderItems;
	}

	public void setFolderItems(List<ItemRef> folderItems) {
		this.folderItems = folderItems;
	}

	public void setFolderAcl(FolderAcl folderAcl) {
		this.folderAcl = folderAcl;
	}

	@XmlElement
	public FolderAcl getFolderAcl() {
		return folderAcl;
	}

	public void setPubFileName(String pubFileName) {
		this.pubFileName = pubFileName;
	}
	@XmlAttribute
	public String getPubFileName() {
		return pubFileName;
	}
	

	public void setGlobalTemplate(String globalTemplate) {
		this.globalTemplate = globalTemplate;
	}
	@XmlAttribute
	public String getGlobalTemplate() {
		return globalTemplate;
	}
}
