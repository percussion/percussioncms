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
package com.percussion.pagemanagement.data;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.percussion.share.data.PSAbstractPersistantObject;


@XmlRootElement(name = "WidgetSummary")
public class PSWidgetSummary extends PSAbstractPersistantObject {

    private String id;
    private String name;
    private String label;
    private String icon;
    private boolean hasUserPrefs;
    private boolean hasCssPrefs; 
    private String type;
    private String category;
    private String description;
    private boolean isResponsive;
    
    @Override
    public String getId() {
        return id;
    }

    
    @Override
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }


    
    public void setName(String name) {
        this.name = name;
    }


    
    public String getLabel() {
        return label;
    }


    
    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }


    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getIcon() {
        return icon;
    }


    
    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean getHasCssPrefs()
    {
       return hasCssPrefs;
    }


    public void setHasCssPrefs(boolean hasCssPrefs)
    {
       this.hasCssPrefs = hasCssPrefs;
    }


    public boolean getHasUserPrefs()
    {
       return hasUserPrefs;
    }


    public void setHasUserPrefs(boolean hasUserPrefs)
    {
       this.hasUserPrefs = hasUserPrefs;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }



    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }



    public boolean isResponsive()
    {
        return isResponsive;
    }


    public void setResponsive(boolean isResponsive)
    {
        this.isResponsive = isResponsive;
    }



    /**
     * Safe to serialize
     */
    private static final long serialVersionUID = 8874560179085984761L;


}
