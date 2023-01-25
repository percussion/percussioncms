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

package com.percussion.share.service.impl;

import com.percussion.pagemanagement.data.PSPage;
import com.percussion.pagemanagement.data.PSTemplateSummary;
import com.percussion.share.service.impl.PSThumbnailRunner.Function;
import com.percussion.sitemanage.data.PSSiteSummary;

public class PSWorkPackage {
	private Function function;
	private String id;
	private PSPage page;
	private PSTemplateSummary template;
	private String siteFolderPath;
	private PSSiteSummary site;
	private String fileSuffix;

	/**
	 * @param function
	 *            the function to set
	 */
	public void setFunction(Function function) {
		this.function = function;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the template
	 */
	public PSTemplateSummary getTemplate() {
		return template;
	}

	/**
	 * @param template
	 *            the template to set
	 */
	public void setTemplate(PSTemplateSummary template) {
		this.template = template;
	}

	/**
	 * @return the page
	 */
	public PSPage getPage() {
		return page;
	}

	/**
	 * @param page
	 *            the page to set
	 */
	public void setPage(PSPage page) {
		this.page = page;
	}

	/**
	 * @return the siteFolderPath
	 */
	public String getSiteFolderPath() {
		return siteFolderPath;
	}

	/**
	 * @param siteFolderPath
	 *            the siteFolderPath to set
	 */
	public void setSiteFolderPath(String siteFolderPath) {
		this.siteFolderPath = siteFolderPath;
	}

	/**
	 * @return the site
	 */
	public PSSiteSummary getSite() {
		return site;
	}

	/**
	 * @param site
	 *            the site to set
	 */
	public void setSite(PSSiteSummary site) {
		this.site = site;
	}

	public Function getFunction() {
		return function;
	}

	public String getId() {
		return id;
	}

	public PSWorkPackage(String id, Function function) {
		this.id = id;
		this.function = function;
	}

	public String getFileSuffix() {
		return fileSuffix;
	}

	public void setFileSuffix(String fileSuffix) {
		this.fileSuffix = fileSuffix;
	}
}
