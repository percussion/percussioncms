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
