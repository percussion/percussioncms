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
