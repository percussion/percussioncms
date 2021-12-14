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
package com.percussion.comments.data;

import com.percussion.itemmanagement.data.IPSEditableItem;
import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "commentsSummary")
public class PSCommentsSummary
	extends PSAbstractDataObject
	implements IPSEditableItem {

	private static final long serialVersionUID = 1L;

	private String id;

	private String pagePath;
	
	private String path;
	
	private String pageLinkTitle;

    private int commentCount = 0;

	private int approvedCount = 0;

    private int newCount = 0;
    
    private String datePosted;
    
    private String summary;
    
	/**
	 * @param id The id number identifying that page.
	 */
	public void setId(String id) {
		this.id = id;
	}
	
	/**
     * @return The id number identifying that page.
     */
	public String getId() {
		return id;
	}
	
	/**
	 * @return The type of the object, in this case ASSET_TYPE.
	 */
	public String getType() {
		return IPSEditableItem.ASSET_TYPE;
	}

	/**
	 * @return The path of the page which has a comment. Relative to the delivery tier server.
	 */
	public String getPagePath() {
		return pagePath;
	}

	/**
	 * @param pagePath The path of the page which has a comment. Relative to the delivery tier server.
	 */
	public void setPagePath(String pagePath) {
		this.pagePath = pagePath;
	}

	/**
	 * @return The CMS path of the page which has a comment.
	 */
	public String getPath() {
		return path;
	}

	/**
	 * @param path The CMS path of the page which has a comment.
	 */
	public void setPath(String path) {
		this.path = path;
	}

	/**
	 * @return The total number of comments for that page.
	 */
	public int getCommentCount() {
		return commentCount;
	}

	/**
	 * @param commentCount The total number of comments for that page.
	 */
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}

	/**
	 * @return The number of approved comments for that page.
	 */
	public int getApprovedCount() {
		return approvedCount;
	}

	/**
	 * @param approvedCount The number of approved comments for that page.
	 */
	public void setApprovedCount(int approvedCount) {
		this.approvedCount = approvedCount;
	}
	
	/**
     * @return the pageTitle
     */
    public String getPageLinkTitle()
    {
        return pageLinkTitle;
    }

    /**
     * @param pageLinkTitle the link title to set
     */
    public void setPageLinkTitle(String pageLinkTitle)
    {
        this.pageLinkTitle = pageLinkTitle;
    }

    /**
     * @return the newCount
     */
    public int getNewCount()
    {
        return newCount;
    }

    /**
     * @param newCount the newCount to set
     */
    public void setNewCount(int newCount)
    {
        this.newCount = newCount;
    }

    /**
     * @return the datePosted
     */
    public String getDatePosted()
    {
        return datePosted;
    }

    /**
     * @param datePosted the datePosted to set
     */
    public void setDatePosted(String datePosted)
    {
        this.datePosted = datePosted;
    }

    /**
     * @return the summary
     */
    public String getSummary()
    {
        return summary;
    }

    /**
     * @param summary the summary to set
     */
    public void setSummary(String summary)
    {
        this.summary = summary;
    }
}
