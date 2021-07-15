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
