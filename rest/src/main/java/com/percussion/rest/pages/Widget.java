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
