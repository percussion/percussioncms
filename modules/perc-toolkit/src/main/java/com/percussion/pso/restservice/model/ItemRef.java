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



/**
 */
public class ItemRef {
	/**
	 * Field contentId.
	 */
	private Integer contentId;
	/**
	 * Field revision.
	 */
	private Integer revision;
	/**
	 * Field href.
	 */
	private String href;
	/**
	 * Field contentType.
	 */
	private String contentType;

	/**
	 * Field keyField.
	 */
	private String keyField;
	/**
	 * Field contextRoot.
	 */
	private String contextRoot;
	
	private String title;
	/**
	 * Method getKeyField.
	 * @return String
	 */
	@XmlAttribute
	public String getKeyField() {
		return keyField;
	}

	/**
	 * Method setKeyField.
	 * @param keyField String
	 */
	public void setKeyField(String keyField) {
		this.keyField = keyField;
	}
	/**
	 * Method getContextRoot.
	 * @return String
	 */
	@XmlAttribute
	public String getContextRoot() {
		return contextRoot;
	}
	
	/**
	 * Method getContentId.
	 * @return int
	 */
	@XmlAttribute
	public Integer getContentId() {
		return contentId;
	}
	/**
	 * Method setContentId.
	 * @param contentId int
	 */
	public void setContentId(Integer contentId) {
		this.contentId = contentId;
	}
	
	/**
	 * Method getRevision.
	 * @return int
	 */
	@XmlAttribute
	public Integer getRevision() {
		return revision;
	}
	/**
	 * Method setRevision.
	 * @param revision int
	 */
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	
	/**
	 * Method setHref.
	 * @param href String
	 */
	@XmlAttribute
	public void setHref(String href) {
		this.href = href;
	}
	/**
	 * Method getHref.
	 * @return String
	 */
	public String getHref() {
		return href;
	}
	/**
	 * Method setContentType.
	 * @param contentType String
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	/**
	 * Method getContentType.
	 * @return String
	 */
	@XmlAttribute
	public String getContentType() {
		return contentType;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}
	

}
