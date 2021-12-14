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
package com.percussion.delivery.metadata.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

/**
 * An object that represents a query made against the metadata service.
 * 
 * @author erikserating
 * 
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PSMetadataQuery
{

    /**
     * Indicates weather the call was made from the editor or preview vs published website
     */
    private boolean isEditMode;

	/**
	 * Indicates that the request should be tracked.
	 */
	private boolean trackBlogPost;
	
	/***
	 * Indicates the full path to the blog post
	 */
	private String blogPostFullPath;
	
	
    /**
     * A list of query criteria that is used to limit the results of a query.
     * The criteria will be put together with AND's (OR is not supported).
     * 
     * <pre>
     *  The following operators are supported:
     *  Equals:  =
     *  Not Equals: !=
     *  Greater Than: &gt;
     *  Less Than: &lt;
     *  Greater than or equal to: &gt;=
     *  Less than or equal to: &lt;=
     *  LIKE
     *  IN
     * </pre>
     */
    private List<String> criteria;

    /**
     * The maximum number of results to return per page.
     */
    private int maxResults;

    /***
     * Sets the Query limit configured on the client. 
     */
    private int totalMaxResults;
  
	/**
     * Indicates which record to start with in the returned result set, using
     * maxResults and startIndex together allow for paging.
     */
    private int startIndex;

    /**
     * Property name and sort direction that specifies how results are sorted.
     */
    private String orderBy;
    
    /**
     * Property name and sort direction that specifies how results are sorted.
     */
    private boolean returnTotalEntries = false;

    /**
     * Pagination label to use, default is "pages".
     */
    private String pagingPagesText;

    private String sortTagsBy;

    private String currentPageId;
    
    public static final String FIELD_CRITERIA = "criteria";

    public static final String FIELD_MAX_RESULTS = "maxResults";
    
    public static final String FIELD_TOTAL_MAX_RESULTS = "totalMaxResults";

    public static final String FIELD_START_INDEX = "startIndex";

    public static final String FIELD_ORDER_BY = "orderBy";
    
    public static final String FIELD_RETURN_TOTAL_ENTRIES = "returnTotalEntries";
    
    public static final String FIELD_PAGING_PAGES_TEXT = "pagingPagesText";

    public PSMetadataQuery() {
    }

    /**
     * Sets the query criteria list.
     * 
     * @param criteria the criteria string list. May be <code>null</code> or
     *            empty.
     */
    public void setCriteria(List<String> criteria)
    {
        this.criteria = criteria;
    }

    /**
     * Sets the orderBy clause.
     * 
     * @param orderby orderby string list, may be <code>null</code> or empty.
     */
    public void setOrderBy(String orderby)
    {
        this.orderBy = orderby;
    }

    /**
     * Sets the maxResults's value.
     * 
     * @param max an integer greater than zero.
     */
    public void setMaxResults(int max)
    {
        maxResults = max;
    }

    /**
     * Sets the startIndex's value.
     * 
     * @param start an integer of zero or more.
     */
    public void setStartIndex(int start)
    {
        startIndex = start;
    }

    /**
     * Sets the returnTotalEntries's value.
     * 
     * @param returnTE a boolean - specifies if totalEntries field should be set in response.
     */
    public void setReturnTotalEntries(boolean returnTE)
    {
    	returnTotalEntries = returnTE;
    }
    
    /**
     * Sets the pagination label
     * 
     * @param pagingPagesText - Our pagination Label.
     */
    public void setPagingPagesText(String pagingPagesText)
    {
    	this.pagingPagesText = pagingPagesText;
    }

    /**
     * @return the criteria
     */
    public List<String> getCriteria()
    {
        return criteria;
    }

    /**
     * @return the maxResults
     */
    public int getMaxResults()
    {
        return maxResults;
    }

    /**
     * @return the startIndex
     */
    public int getStartIndex()
    {
        return startIndex;
    }

    /**
     * @return the orderBy
     */
    public String getOrderBy()
    {
        return orderBy;
    }

    /**
     * @return the returnTotalEntries
     */
    public boolean getReturnTotalEntries()
    {
        return returnTotalEntries;
    }

    /**
     * @return the pagination label
     */
    public String getPagingPagesText()
    {
    	return pagingPagesText;
    }

	public boolean isTrackBlogPost() {
		return trackBlogPost;
	}

	public void setTrackBlogPost(boolean trackBlogPost) {
		this.trackBlogPost = trackBlogPost;
	}

	public String getBlogPostFullPath() {
		return blogPostFullPath;
	}

	public void setBlogPostFullPath(String blogPostFullPath) {
		this.blogPostFullPath = blogPostFullPath;
	}
	
   public int getTotalMaxResults() {
		return totalMaxResults;
	}

	public void setTotalMaxResults(int totalMaxResults) {
		this.totalMaxResults = totalMaxResults;
	}

    public String getSortTagsBy() {
        return sortTagsBy;
    }

    public void setSortTagsBy(String sortTagsBy) {
        this.sortTagsBy = sortTagsBy;
    }

    public String getCurrentPageId() {
        return currentPageId;
    }

    public void setCurrentPageId(String currentPageId) {
        this.currentPageId = currentPageId;
    }

    public boolean isEditMode() {
        return isEditMode;
    }

    public void setEditMode(boolean editMode) {
        this.isEditMode = editMode;
    }
}
