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
 *      https://www.percusssion.com
 *
 *     You should have received a copy of the GNU Affero General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>
 */

package com.percussion.theme.data;

import static org.apache.commons.lang.Validate.notNull;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;
import com.percussion.utils.types.PSPair;

import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * It contains CSS properties for a specific region.
 */
@XmlRootElement(name = "RegionCSS")
@JsonRootName("RegionCSS")
public class PSRegionCSS extends PSAbstractDataObject implements Serializable, Comparable
{
    public static String REGION_CLASS = ".perc-region";
    
    private static final long serialVersionUID = 1L;

    @NotBlank
    @NotNull
    private String regionName;

    @NotBlank
    @NotNull
    private String outerRegionName;

    private List<Property> properties = new ArrayList<Property>();

    public static class Property extends PSAbstractDataObject implements Serializable {

        private static final long serialVersionUID = 1L;
        
        @NotBlank
        @NotNull
        private String name;
        private String value;
        
        public Property()
        {
        }
        
        public Property(String name, String value)
        {
            this.name = name;
            this.value = value;
        }
        
        @NotBlank
        @NotNull
        public String getName()
        {
            return name;
        }
        public void setName(String name)
        {
            this.name = name;
        }
        public String getValue()
        {
            return value;
        }
        public void setValue(String value)
        {
            this.value = value;
        }

    }
    public PSRegionCSS()
    {
    }

    public PSRegionCSS(String outerName, String name)
    {
        this.outerRegionName = outerName;
        this.regionName = name;
    }

    public void setRegionName(String name)
    {
        this.regionName = name;
    }

    /**
     * The name of the region that contains the CSS properties.
     * If this is the name of the outer region, then this is the
     * same as {@link #getOuterRegionName()}.
     * @return region name, should not be blank for a valid object.
     */
    @NotBlank
    @NotNull
    public String getRegionName()
    {
        return regionName;
    }

    /**
     * Gets the most outer region name.
     * @return the outer region name, should not be blank for a valid object.
     */
    @NotBlank
    @NotNull
    public String getOuterRegionName()
    {
        return outerRegionName;
    }

    public void setOuterRegionName(String outName)
    {
        outerRegionName = outName;
    }

    /**
     * Gets the region CSS properties.
     * @return CSS properties, never <code>null</code>.
     */
    public List<Property> getProperties()
    {
        return properties;
    }

    public void setProperties(List<Property> props)
    {
        notNull(props);
        properties = props;
    }

    public String getAsCSSString()
    {
        StringBuffer buffer = new StringBuffer();
        
        if (outerRegionName == null)
            return buffer.toString();
        
        addAsCSSStringRegionName(buffer, outerRegionName);
        
        if (!outerRegionName.equalsIgnoreCase(regionName))
            addAsCSSStringRegionName(buffer, regionName);
        
        buffer.append("{\n");
        addAsCSSStringPropertys(buffer);        
        buffer.append("}\n");
        
        return buffer.toString();
    }

    private void addAsCSSStringRegionName(StringBuffer buffer, String name)
    {
        buffer.append("#");
        buffer.append(name);
        buffer.append(REGION_CLASS);
        buffer.append(" ");
    }
    
    private void addAsCSSStringPropertys(StringBuffer buffer)
    {
        for (Property p : properties)
        {
            buffer.append("   ");
            buffer.append(p.getName());
            buffer.append(": ");
            buffer.append(p.value);
            buffer.append(";\n");
        }
    }

    @Override
    public int compareTo(Object o)
    {
        if (!(o instanceof PSRegionCSS))
            return -1;
        
        PSRegionCSS other = (PSRegionCSS) o;
        int outer = outerRegionName.compareTo(other.outerRegionName);
        if (outer != 0)
        {
            return outer;
        }
        
        return regionName.compareTo(other.regionName);
    }
}
