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
package com.percussion.widgetbuilder.data;

import com.percussion.share.data.PSAbstractDataObject;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a single widget field definition
 * 
 * @author JaySeletz
 *
 */
@XmlRootElement(name="WidgetBuilderFieldData")
public class PSWidgetBuilderFieldData extends PSAbstractDataObject
{

    String name;
    String label;
    String type;
    
    
    public PSWidgetBuilderFieldData()
    {
    }
    
    
    public String getName()
    {
        return name;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public String getLabel()
    {
        return label;
    }


    public void setLabel(String label)
    {
        this.label = label;
    }


    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        FieldType.valueOf(type);
        this.type = type;
    }


    public enum FieldType
    {
        TEXT,
        TEXT_AREA,
        DATE,
        RICH_TEXT,
        FILE,
        FILE_LINK,
        IMAGE,
        IMAGE_LINK,
        PAGE,
        PAGE_LINK;
    }
}
