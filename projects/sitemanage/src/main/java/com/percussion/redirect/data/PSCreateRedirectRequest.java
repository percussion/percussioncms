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
