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
import javax.xml.bind.annotation.XmlTransient;

/**
 */
public class SlotItem extends Relationship implements Comparable<SlotItem>{
	/**
	 * Field template.
	 */
	private String template;

	/**
	 * Field sortRank.
	 */
	private int sortRank;
	/**
	 * Field site.
	 */
	private String site;
	/**
	 * Field folder.
	 */
	private String folder;

	
	
	
	/**
	 * Method getTemplate.
	 * @return String
	 */
	@XmlAttribute
	public String getTemplate() {
		return template;
	}
	/**
	 * Method setTemplate.
	 * @param template String
	 */
	public void setTemplate(String template) {
		this.template = template;
	}

	
	/**
	 * Method getSite.
	 * @return String
	 */
	@XmlAttribute
	public String getSite() {
		return site;
	}
	/**
	 * Method setSite.
	 * @param site String
	 */
	public void setSite(String site) {
		this.site = site;
	}
	/**
	 * Method getFolder.
	 * @return String
	 */
	@XmlAttribute
	public String getFolder() {
		return folder;
	}
	/**
	 * Method setFolder.
	 * @param folder String
	 */
	public void setFolder(String folder) {
		this.folder = folder;
	}
	/**
	 * Method getSortRank.
	 * @return int
	 */
	@XmlTransient
	public int getSortRank() {
		return sortRank;
	}
	/**
	 * Method setSortRank.
	 * @param sortRank int
	 */
	public void setSortRank(int sortRank) {
		this.sortRank = sortRank;
	}

	/**
	 * Method compareTo.
	 * @param o SlotItem
	 * @return int
	 */
	public int compareTo(SlotItem o) {
		return this.sortRank - o.sortRank;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((folder == null) ? 0 : folder.hashCode());
		result = prime * result + ((site == null) ? 0 : site.hashCode());
		result = prime * result + sortRank;
		result = prime * result
				+ ((template == null) ? 0 : template.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SlotItem other = (SlotItem) obj;
		if (folder == null) {
			if (other.folder != null)
				return false;
		} else if (!folder.equals(other.folder))
			return false;
		if (site == null) {
			if (other.site != null)
				return false;
		} else if (!site.equals(other.site))
			return false;
		if (sortRank != other.sortRank)
			return false;
		if (template == null) {
			if (other.template != null)
				return false;
		} else if (!template.equals(other.template))
			return false;
		return true;
	}
	


	  
}

