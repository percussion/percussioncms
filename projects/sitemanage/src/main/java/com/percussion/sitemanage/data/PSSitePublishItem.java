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
package com.percussion.sitemanage.data;

import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonRootName;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEqual;
import net.sf.oval.constraint.NotNull;

import com.percussion.share.data.PSAbstractDataObject;

@XmlRootElement(name = "SitePublishItem")
@JsonRootName("SitePublishItem")
public class PSSitePublishItem extends PSAbstractDataObject {
	
	
	/**
	 * unique key for this item status
	 */
	@NotEqual(value = "0")
	private long itemStatusId;
	
	/**
	 * Status indicates success or failure
	 */
	@NotBlank
	@NotNull
	private String status;
	
	/**
	 * Published file name
	 */
	private String fileName;
	
	/**
	 * Published file folder location.
	 */
	private String fileLocation;

	private long folderid;

	private long templateid;

	private String deliveryType;

	/**
	 * Content id of page or asset. 
	 */
	@NotEqual(value = "0")
	private long contentid;

	private long revisionid;

	private String assemblyUrl;

	/**
	 * Publishing time in milliseconds. 
	 */
	private long elapsedTime;
	
	/**
	 * Publishing operation: publish or remove. 
	 */
	@NotBlank
	@NotNull
	private String operation; 
	
	/**
	 * Error message if item has failed. 
	 */
	private String errorMessage;
	
	/**
	 * @return the itemStatusId
	 */
	public long getItemStatusId() {
		return itemStatusId;
	}
	/**
	 * @param itemStatusId the itemStatusId to set
	 */
	public void setItemStatusId(long itemStatusId) {
		this.itemStatusId = itemStatusId;
	}
	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}
	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return the fileLocation
	 */
	public String getFileLocation() {
		return fileLocation;
	}
	/**
	 * @param fileLocation the fileLocation to set
	 */
	public void setFileLocation(String fileLocation) {
		this.fileLocation = fileLocation;
	}
	/**
	 * @return the contentid
	 */
	public long getContentid() {
		return contentid;
	}
	/**
	 * @param contentid the contentid to set
	 */
	public void setContentid(long contentid) {
		this.contentid = contentid;
	}
	/**
	 * @return the elapsedTime
	 */
	public long getElapsedTime() {
		return elapsedTime;
	}
	/**
	 * @param elapsedTime the elapsedTime to set
	 */
	public void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}
	/**
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}
	/**
	 * @return the errorMessage
	 */
	public String getErrorMessage() {
		return errorMessage;
	}
	/**
	 * @param errorMessage the errorMessage to set
	 */
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	/**
	 * Folder Id
	 */
	public long getFolderid() {
		return folderid;
	}

	public void setFolderid(long folderid) {
		this.folderid = folderid;
	}

	/**
	 * Template id
	 */
	public long getTemplateid() {
		return templateid;
	}

	public void setTemplateid(long templateid) {
		this.templateid = templateid;
	}

	/***
	 * Delivery Type
	 */
	public String getDeliveryType() {
		return deliveryType;
	}

	public void setDeliveryType(String deliveryType) {
		this.deliveryType = deliveryType;
	}

	/**
	 * Revision id
	 */
	public long getRevisionid() {
		return revisionid;
	}

	public void setRevisionid(long revisionid) {
		this.revisionid = revisionid;
	}

	/***
	 * URL used for assembly the item
	 */
	public String getAssemblyUrl() {
		return assemblyUrl;
	}

	public void setAssemblyUrl(String assemblyUrl) {
		this.assemblyUrl = assemblyUrl;
	}
}
