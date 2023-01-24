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

package com.percussion.delivery.metadata.data;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "visitQuery")
public class PSVisitQuery {
	private String sectionPath;
	private String promotedPagePaths;
	private String limit;
	private String timePeriod;
	private String sortOrder;

	public String getTimePeriod() {
		return timePeriod;
	}

	public void setTimePeriod(String timePeriod) {
		this.timePeriod = timePeriod;
	}

	public String getSectionPath() {
		return sectionPath;
	}

	public void setSectionPath(String pagePath) {
		this.sectionPath = pagePath;
	}

	public String getPromotedPagePaths() {
		return promotedPagePaths;
	}
	
	public void setPromotedPagePaths(String promotedPagePaths) {
		this.promotedPagePaths = promotedPagePaths;
	}
	
	public String getLimit() {
		return limit;
	}

	public void setLimit(String limit) {
		this.limit = limit;
	}

	public String getSortOrder() {
		return sortOrder;
	}

	public void setSortOrder(String sortOrder) {
		this.sortOrder = sortOrder;
	}
}
