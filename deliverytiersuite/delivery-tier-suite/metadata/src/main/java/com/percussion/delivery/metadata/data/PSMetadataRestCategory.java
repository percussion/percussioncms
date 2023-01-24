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
