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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.percussion.rest.assets.Asset;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.xml.bind.annotation.XmlRootElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
@XmlRootElement(name = "Widget")
@Schema(name="Widget",description="Represents a Widget.")
public class Widget implements Cloneable
{
    public static final String SCOPE_LOCAL = "local";

    public static final String SCOPE_SHARED = "shared";
    
    @Schema(name="id", description="Id of the widget.")
    private String id;
    
    @Schema(name="name",description="Name of the widget.")
    private String name;

    @Schema(name="type", description="Type of widget.")
    private String type;

    @Schema(name="scope", description="Scope of the widget.", allowableValues = "local,shared")
    private String scope;

    @Schema(name="editable", description="Denotes if widget is editable.")
    private Boolean editable;

    @Schema(name="asset", description="Asset within the widget.")
    private Asset asset;

    
    public Widget() 
    {
       // required for json
    }
    
    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }
    
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

    public String getScope()
    {
        return scope;
    }

    public void setScope(String scope)
    {
        this.scope = scope;
    }

    public Boolean getEditable()
    {
        return editable;
    }

    public void setEditable(Boolean editable)
    {
        this.editable = editable;
    }

    public Asset getAsset()
    {
        return asset;
    }

    public void setAsset(Asset asset)
    {
        this.asset = asset;
    }

    @Override
    protected Widget clone() throws CloneNotSupportedException {
        return (Widget) super.clone();
    }


}
