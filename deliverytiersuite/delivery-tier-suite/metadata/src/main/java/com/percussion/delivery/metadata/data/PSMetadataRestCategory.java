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

import java.util.ArrayList;
import java.util.List;

import com.percussion.delivery.metadata.impl.utils.PSPair;

/**
 * Represents an object with a category name, the count of pages that are
 * categorized with it and their children.
 * 
 */
public class PSMetadataRestCategory
{
    private String category;

    private PSPair<Integer, Integer> count;

    private List<PSMetadataRestCategory> children;

    public PSMetadataRestCategory()
    {
        super();
    }

    /**
     * @param category
     */
    public PSMetadataRestCategory(String category)
    {
        super();
        this.category = category;
        this.count = new PSPair<>(0, 0);
        this.children = new ArrayList<>();
    }

    /**
     * @param category
     * @param count
     * @param children
     */
    public PSMetadataRestCategory(String category, PSPair<Integer, Integer> count, List<PSMetadataRestCategory> children)
    {
        super();
        this.category = category;
        this.count = count;
        this.children = children;
    }

    /**
     * @return the category
     */
    public String getCategory()
    {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category)
    {
        this.category = category;
    }

    /**
     * @return the count
     */
    public PSPair<Integer, Integer> getCount()
    {
        return count;
    }

    /**
     * @param count the count to set
     */
    public void setCount(PSPair<Integer, Integer> count)
    {
        this.count = count;
    }

    /**
     * @return the children
     */
    public List<PSMetadataRestCategory> getChildren()
    {
        return children;
    }

    /**
     * @param children the children to set
     */
    public void setChildrens(List<PSMetadataRestCategory> children)
    {
        this.children = children;
    }
}
