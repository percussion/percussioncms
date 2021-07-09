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

package com.percussion.rest.pages;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
@XmlRootElement(name = "Region")
@ApiModel(value="Region",description="Represents a region of a page either defined locally or by the template.")
public class Region
{
    @ApiModelProperty(value="name",notes="Name of the region.")
    private String name;

    @ApiModelProperty(value="type", notes="Type of region.",allowableValues = "TEMPLATE,LOCAL")
    private String type;

    @ApiModelProperty(value="editable", notes="Denotes if region is editable.")
    private boolean editable;

    @ApiModelProperty(value="widgets", notes="List of widgets within the region."  )
    private List<Widget> widgets;

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public boolean isEditable()
    {
        return editable;
    }

    public void setEditable(boolean editable)
    {
        this.editable = editable;
    }

    public List<Widget> getWidgets()
    {
        return widgets;
    }

    public void setWidgets(List<Widget> widgets)
    {
        this.widgets = widgets;
    }

}
