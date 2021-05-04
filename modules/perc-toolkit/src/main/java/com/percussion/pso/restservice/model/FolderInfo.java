/*******************************************************************************
 * Copyright (c) 1999-2011 Percussion Software.
 * 
 * Permission is hereby granted, free of charge, to use, copy and create derivative works of this software and associated documentation files (the "Software") for internal use only and only in connection with products from Percussion Software. 
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.  IN NO EVENT SHALL PERCUSSION SOFTWARE BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 ******************************************************************************/
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
