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
package com.percussion.share.data;

import com.fasterxml.jackson.annotation.JsonRootName;

/**
 * Generic class to return information about a page of data. For example, it's
 * used to return a page of children items of a PSPathItem and it's children
 * count, or to paginate through a list of content IDs (Integer).
 * 
 * @author miltonpividori
 * 
 */
@JsonRootName(value = "PagedList")
public class PSPagedList
{
    /**
     * Count of items in the parent item.
     */
    private Integer childrenCount;
    
    /**
     * The start index of the returned page of elements in 'childrenInPage'.
     */
    private Integer startIndex;

    /**
     * The id of the first element of the Item List
     */
    private String firstItemId;
    
    public PSPagedList()
    {
        this(null, null);
    }
    
    public PSPagedList(Integer childrenCount, Integer startIndex)
    {
        this.childrenCount = childrenCount;
        this.startIndex = startIndex;
    }

    /**
     * @return the childrenCount
     */
    public Integer getChildrenCount()
    {
        return childrenCount;
    }

    /**
     * @param childrenCount the childrenCount to set
     */
    public void setChildrenCount(Integer childrenCount)
    {
        this.childrenCount = childrenCount;
    }

    /**
     * @return the startIndex
     */
    public Integer getStartIndex()
    {
        return startIndex;
    }

    /**
     * @param startIndex the startIndex to set
     */
    public void setStartIndex(Integer startIndex)
    {
        this.startIndex = startIndex;
    }
    
    /**
     * @return the firstItemId
     */    
    public String getFirstItemId()
    {
        return firstItemId;
    }
    
    /**
     * @param firstItemId the id of the item
     */
    public void setFirstItemId(String firstItemId)
    {
        this.firstItemId = firstItemId;
    }    
}
