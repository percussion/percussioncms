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

package com.percussion.theme.data;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.percussion.share.data.PSAbstractDataObject;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotNull;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang.Validate.notNull;

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

    private List<Property> properties = new ArrayList<>();

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Property)) return false;
            Property property = (Property) o;
            return Objects.equals(getName(), property.getName()) && Objects.equals(getValue(), property.getValue());
        }

        @Override
        public int hashCode() {
            return Objects.hash(getName(), getValue());
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Property{");
            sb.append("name='").append(name).append('\'');
            sb.append(", value='").append(value).append('\'');
            sb.append('}');
            return sb.toString();
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
        StringBuilder buffer = new StringBuilder();
        
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

    private void addAsCSSStringRegionName(StringBuilder buffer, String name)
    {
        buffer.append("#");
        buffer.append(name);
        buffer.append(REGION_CLASS);
        buffer.append(" ");
    }
    
    private void addAsCSSStringPropertys(StringBuilder buffer)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PSRegionCSS)) return false;
        PSRegionCSS that = (PSRegionCSS) o;
        return Objects.equals(getRegionName(), that.getRegionName()) && Objects.equals(getOuterRegionName(), that.getOuterRegionName()) && Objects.equals(getProperties(), that.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRegionName(), getOuterRegionName(), getProperties());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PSRegionCSS{");
        sb.append("regionName='").append(regionName).append('\'');
        sb.append(", outerRegionName='").append(outerRegionName).append('\'');
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }
}
