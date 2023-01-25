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


/**
 */
@XmlRootElement(name="FileValue")
public class FileValue implements Value {

	/**
	 * Field stringValue.
	 */
	private String stringValue;
	
	/**
	 * Field href.
	 */
	private String href;
	/**
	 * Field type.
	 */
	public static final int TYPE=3;  
	

	private String mimeType;
	private String lastModified;
	private String ETag;
	/**
	 * Method getStringValue.
	 * @return String
	 * @see Value#getStringValue()
	 */
	@XmlValue
	public String getStringValue() {
		return stringValue;
	}
	/**
	 * Method setStringValue.
	 * @param stringValue String
	 * @see Value#setStringValue(String)
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}
	/**
	 * Method setHref.
	 * @param href String
	 */
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * Method getHref.
	 * @return String
	 */
	@XmlAttribute
	public String getHref() {
		return href;
	}
//	public void setLastModified(String lastModified) {
//		this.lastModified = lastModified;
//	}
//	
//	public String getLastModified() {
//		return lastModified;
//	}
//	
//	public void setETag(String eTag) {
//		ETag = eTag;
//	}
//	
//	public String getETag() {
//		return ETag;
//	}
	
	
	
	
}
