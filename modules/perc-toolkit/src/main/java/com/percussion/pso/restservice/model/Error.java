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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;


@XmlRootElement(name="Error")
public class Error {
	
	public static enum ErrorCode { NOT_FOUND, PURGED, UNKNOWN_ERROR, ASSEMBLY_ERROR, SKIP }; 
	private ErrorCode errorCode;
	private String errorMessage;
	private Integer contentId;
	public Error() {
		setErrorCode(ErrorCode.UNKNOWN_ERROR);
	}
	public Error(ErrorCode errorCode, String message) {
		setErrorCode(errorCode);
		setErrorMessage(message);
	}
	public Error(ErrorCode errorCode, Integer contentId, String message) {
		setErrorCode(errorCode);
		setErrorMessage(message);
		setContentId(contentId);
	}
	
	public Error(ErrorCode errorCode) {
		setErrorCode(errorCode);
	}
	
	@XmlAttribute
	public ErrorCode getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(ErrorCode errorCode) {
		this.errorCode = errorCode;
	}
	
	@XmlValue
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}
	@XmlAttribute
	public Integer getContentId() {
		return contentId;
	}
	
}
