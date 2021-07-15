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

import java.util.Date;
import java.util.List;

import com.percussion.pagemanagement.data.PSWidgetDefinition.AbstractUserPref;

/**
 * Represents the different data types that a widget property can have.
 * 
 * @see PSWidgetItem#getProperties()
 * @see PSWidgetItem#getCssProperties()
 * @author adamgent
 *
 */
public enum PSWidgetPropertyDataType {
    
    STRING("string", String.class),
    ENUM("enum", String.class),
    NUMBER("number", Number.class),
    BOOL("bool", Boolean.class),
    HIDDEN("hidden", Object.class),
    DATE("date", Date.class),
    LIST("list", List.class);
    
    private String name;
    private Class<?> javaType;

    private PSWidgetPropertyDataType(String name, Class<?> javaType)
    {
        this.name = name;
        this.javaType = javaType;
    }

    /**
     * Gets the nominal value of the data type.
     * @return never <code>null</code> or empty.
     */
    public String getName()
    {
        return name;
    }

    /**
     * The java type that the widget property should be.
     * @return never <code>null</code>.
     */
    public Class<?> getJavaType()
    {
        return javaType;
    }
    
    /**
     * Gets the data type from widget property definition.
     * @param userPref never <code>null</code>.
     * @return never <code>null</code>.
     */
    public static PSWidgetPropertyDataType fromDefinition(AbstractUserPref userPref) {
        return parseType(userPref.getDatatype());
    }
    
    /**
     * Parse the {@link #getName()} property definition type.
     * @param name
     * @return never <code>null</code>.
     */
    public static PSWidgetPropertyDataType parseType(String name) {
        String n = name.toUpperCase();
        return valueOf(n);
    }
    
}


