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
