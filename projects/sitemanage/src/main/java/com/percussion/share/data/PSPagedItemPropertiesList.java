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
