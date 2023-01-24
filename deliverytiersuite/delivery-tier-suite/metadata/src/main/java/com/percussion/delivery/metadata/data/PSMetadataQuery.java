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
