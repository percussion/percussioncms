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

