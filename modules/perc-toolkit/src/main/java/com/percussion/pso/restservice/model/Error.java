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
