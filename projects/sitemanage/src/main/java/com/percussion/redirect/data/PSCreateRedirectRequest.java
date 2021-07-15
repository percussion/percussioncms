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

package com.percussion.redirect.data;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/***
 * Encapsulates a request for a new redirect.
 *  
 * @author natechadwick
 *
 */
@SuppressWarnings("serial")
@XmlRootElement
public class PSCreateRedirectRequest implements Serializable{
	 
	private String category;
	private String condition;
	private boolean enabled;
	private String key;
	private boolean permanent;
	private String redirectTo;
	private String site;
	private String type;
	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}
	/**
	 * Sets the category for the redirect 
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}
	/**
	 * @return the condition
	 */
	public String getCondition() {
		return condition;
	}
	/**
	 * @param condition the condition to set
	 */
	public void setCondition(String condition) {
		this.condition = condition;
	}
	/**
	 * @return the enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}
	/**
	 * @param enabled the enabled to set
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	/**
	 * The license key used to sign the request.
	 * 
	 * @return the key
	 */
	public String getKey() {
		return key;
	}
	/**
	 * The license key used to sign the request.
	 * 
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	/**
	 * @return the permanent
	 */
	public boolean isPermanent() {
		return permanent;
	}
	/**
	 * @param permanent the permanent to set
	 */
	public void setPermanent(boolean permanent) {
		this.permanent = permanent;
	}
	/**
	 * @return the redirectTo
	 */
	public String getRedirectTo() {
		return redirectTo;
	}
	/**
	 * @param redirectTo the redirectTo to set
	 */
	public void setRedirectTo(String redirectTo) {
		this.redirectTo = redirectTo;
	}
	/**
	 * The Amazon S3 bucket name for the site under management.
	 * 
	 * @return the site
	 */
	public String getSite() {
		return site;
	}
	/**
	 * Sets the site, should be an Amazon S3 bucket name.
	 * @param site the site to set
	 */
	public void setSite(String site) {
		this.site = site;
	}
	
	/**
	 * Gets the redirect type.
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Stes the redirect type.
	 * 
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
}
