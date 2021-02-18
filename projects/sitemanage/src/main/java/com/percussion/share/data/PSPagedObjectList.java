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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;

/**
 * @author miltonpividori
 *
 */
public class PSPagedObjectList<T>
{
    /**
     * Children of the item in a particular page.
     */
    protected List<T> childrenInPage;
    
    /**
     * Count of items in the parent item.
     */
    protected Integer childrenCount;
    
    /**
     * The start index of the returned page of elements in 'childrenInPage'.
     */
    protected Integer startIndex;

    public PSPagedObjectList()
    {
        this.childrenInPage = new ArrayList<>();
        this.childrenCount = null;
        this.startIndex = null;
    }
    
    public PSPagedObjectList(List<T> childrenInPage, Integer childrenCount, Integer startIndex)
    {
        this.childrenInPage = childrenInPage;
        this.childrenCount = childrenCount;
        this.startIndex = startIndex;
    }

    /**
     * Get a page (sublist) of objects from the given list, according to the startIndex
     * and maxResults values.
     * 
     * @param allItems A List of Objects. Cannot be <code>null</code>.
     * @param startIndex The starting index. Should be greater than zero, if not, 1 is used.
     * Cannot be <code>null</code>.
     * @param maxResults The maximum amount of results. It can be <code>null</code>, in
     * that case it won't be used.
     * @return An Object array with two components:
     * <ol>
     *  <li>The sublist of objects (page).</li>
     *  </li>The real start index. This value makes sense for the consumer if a startIndex
     *  greater than the allItems.size() value is specified.</li>
     * </ol>
     */
    public static <T> PSPagedObjectList<T> getPage(List<T> allItems, Integer startIndex, Integer maxResults)
    {
        Validate.notNull(allItems, "allItems cannot be null");
        Validate.isTrue(maxResults == null || maxResults >= 1, "maxResults cannot be lesser than 1");
        Validate.notNull(startIndex, "startIndex cannot be null");
        Validate.isTrue(startIndex >= 1, "startIndex cannot be lesser than 1");
        
        Integer newStartIndex = startIndex;
        if (startIndex > allItems.size())
        {
            Integer processedMaxResults = maxResults;
            if (processedMaxResults == null)
                processedMaxResults = Integer.MAX_VALUE;
            
            newStartIndex = startIndex - processedMaxResults;
            while (newStartIndex > allItems.size())
                newStartIndex = newStartIndex - processedMaxResults;
        }
        
        if (newStartIndex <= 0)
            newStartIndex = 1;
        
        int realStartIndex = newStartIndex - 1;
        int realMaxResults = realStartIndex + (maxResults != null ? maxResults : allItems.size());
        
        if (realMaxResults > allItems.size())
            realMaxResults = allItems.size();
        
        return new PSPagedObjectList<>(allItems.subList(realStartIndex, realMaxResults), allItems.size(), realStartIndex + 1);
    }    
    
    /**
     * @return the children
     */
    public List<T> getChildrenInPage()
    {
        return childrenInPage;
    }

    /**
     * @param children the children to set
     */
    public void setChildrenInPage(List<T> children)
    {
        this.childrenInPage = children;
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
}
