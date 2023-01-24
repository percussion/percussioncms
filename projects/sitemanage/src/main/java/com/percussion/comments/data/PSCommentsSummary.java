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
