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

import com.percussion.pathmanagement.data.PSPathItem;
import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.ArrayList;
import java.util.List;



/**
 * Generic class to return information about a page of data. For example, it's
 * used to return a page of children items of a PSPathItem and it's children
 * count, or to paginate through a list of content IDs (Integer).
 * 
 * @author miltonpividori
 * 
 */
@JsonRootName(value = "PagedItemList")
public class PSPagedItemList extends PSPagedList 
{
    /**
     * Children of the item in a particular page.
     */
    private List<PSPathItem> childrenInPage;
    
    public PSPagedItemList()
    {
        this(new ArrayList<PSPathItem>(), null, null);
    }
    
    public PSPagedItemList(List<PSPathItem> childrenInPage, Integer childrenCount, Integer startIndex)
    {
        super(childrenCount, startIndex);
        this.childrenInPage = childrenInPage;
    }

    /**
     * @return the children
     */
    public List<PSPathItem> getChildrenInPage()
    {
        return childrenInPage;
    }

    /**
     * @param children the children to set
     */
    public void setChildrenInPage(List<PSPathItem> children)
    {
        this.childrenInPage = children;
    }
}
