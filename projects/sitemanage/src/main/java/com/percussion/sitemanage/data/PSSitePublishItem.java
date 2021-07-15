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
