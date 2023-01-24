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
package com.percussion.ui.data;

import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * A simplified version of <code>PSDisplayFormat</code>. Used
 * for CMS's list view.
 * @author erikserating
 *
 */
@JsonRootName("SimpleDisplayFormat")
public class PSSimpleDisplayFormat
{
 
    private int id;
    private String name;
    private String displayName;
    private String description;
    private List<PSDisplayFormatColumn> columns = new ArrayList<>();
    private String sortby;
    private boolean sortAscending = true;    
    
    
    /**
     * @return the id
     */
    public int getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id)
    {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        this.name = name;
    }    

    /**
     * @return the displayName
     */
    public String getDisplayName()
    {
        return displayName;
    }

    /**
     * @param displayName the displayName to set
     */
    public void setDisplayName(String displayName)
    {
        this.displayName = displayName;
    }

    /**
     * @return the description
     */
    public String getDescription()
    {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description)
    {
        this.description = description;
    }

    /**
     * @return the columns
     */
    public List<PSDisplayFormatColumn> getColumns()
    {
        return columns;
    }

    /**
     * @param columns the columns to set
     */
    public void setColumns(List<PSDisplayFormatColumn> columns)
    {
        this.columns = columns;
    }

    /**
     * @return the sortby
     */
    public String getSortby()
    {
        return sortby;
    }

    /**
     * @param sortby the sortby to set
     */
    public void setSortby(String sortby)
    {
        this.sortby = sortby;
    }    

    /**
     * @return the sortAscending
     */
    public boolean isSortAscending()
    {
        return sortAscending;
    }

    /**
     * @param sortAscending the sortAscending to set
     */
    public void setSortAscending(boolean sortAscending)
    {
        this.sortAscending = sortAscending;
    }

    
}
