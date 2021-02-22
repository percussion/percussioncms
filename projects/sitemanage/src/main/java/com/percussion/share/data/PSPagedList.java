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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
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
