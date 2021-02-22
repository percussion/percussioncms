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
@JsonRootName(value = "PagedItemPropertiesList")
public class PSPagedItemPropertiesList extends PSPagedList 
{
    /**
     * Children of the item in a particular page.
     */
    private List<PSItemProperties> childrenInPage;
    
    public PSPagedItemPropertiesList()
    {
        this(new ArrayList<>(), null, null);
    }
    
    public PSPagedItemPropertiesList(List<PSItemProperties> childrenInPage, Integer childrenCount, Integer startIndex)
    {
        super(childrenCount, startIndex);
        this.childrenInPage = childrenInPage;
    }

    /**
     * @return the children
     */
    public List<PSItemProperties> getChildrenInPage()
    {
        return childrenInPage;
    }

    /**
     * @param children the children to set
     */
    public void setChildrenInPage(List<PSItemProperties> children)
    {
        this.childrenInPage = children;
    }
}
