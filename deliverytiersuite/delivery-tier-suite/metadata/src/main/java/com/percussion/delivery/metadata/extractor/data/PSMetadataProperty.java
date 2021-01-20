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

package com.percussion.delivery.metadata.extractor.data;

import com.percussion.delivery.metadata.IPSMetadataProperty;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Represents a metadata property name value pair.
 * 
 * @author miltonpividori
 * 
 */
@XmlRootElement(name="property")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class PSMetadataProperty implements Serializable, IPSMetadataProperty{
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
    
    /**
     * Property name. For example: dcterms:creator
     */
    private String name;
    
    @XmlTransient
    private VALUETYPE valuetype = VALUETYPE.STRING;

    /**
     * Value of the metadata property. It may be a String, Date or Double. You
     * can get the value type by reading the "valuetype" field.
     */
    
    private Object value;

    public PSMetadataProperty() {
        //Default constructor
    }
    /**
     * Ctor to create a property of the specified valuetype.
     * 
     * @param name the property name, cannot be <code>null</code> or empty.
     * @param type the {@link #valuetype} for the property. Cannot be
     *            <code>null</code>.
     * @param value the value to be stored in the property. May be
     *            <code>null</code> or empty.
     */
    public PSMetadataProperty(String name, VALUETYPE valuetype, Object value)
    {
        this.name = name;
        this.valuetype = valuetype;
        this.value = value;
    }

    /**
     * Convenience ctor to create a string value type property.
     * 
     * @param name cannot be <code>null</code> or empty.
     * @param value the value, may be <code>null</code>.
     */
    public PSMetadataProperty(String name, String value)
    {
        this(name, VALUETYPE.STRING, value);
    }

    /**
     * Convenience ctor to create a number value type property from an int
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSMetadataProperty(String name, int value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a double
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSMetadataProperty(String name, double value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a float
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSMetadataProperty(String name, float value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a long
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSMetadataProperty(String name, long value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a number value type property from a short
     * value.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value
     */
    public PSMetadataProperty(String name, short value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a Date value type property.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value may be <code>null</code>.
     */
    public PSMetadataProperty(String name, Date value)
    {
        this(name, VALUETYPE.DATE, value);
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#getName()
	 */
    @XmlElement
    public String getName()
    {
        return name;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#setName(java.lang.String)
	 */
    public void setName(String name)
    {
        this.name = name;
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#getValuetype()
	 */
    @XmlTransient
    public VALUETYPE getValuetype()
    {
        return valuetype;
    }
    @XmlTransient
    public void setValuetype(VALUETYPE type)
    {
        valuetype=type;
        if (value != null)
        {
            value = convertVal(value,type);
        }
    }

    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#getValue()
	 */
    
    @XmlTransient
    public Object getValue()
    {
        return value;
    }
    
    @XmlElement(name="value")
    public String getStringValue()
    {
        return value.toString();
    }
    
    @XmlElement(name="value")
    public void setStringValue(String value)
    {
        this.value = value;
        valuetype = VALUETYPE.STRING;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#hashCode()
	 */
    @Override
    public int hashCode()
    {
        int nameHash = name != null ? name.hashCode() : 0;
        int valueTypeHash = valuetype.hashCode();
        int valueHash = value != null ? value.hashCode() : 0;
        
        return nameHash + valueTypeHash + valueHash;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#equals(java.lang.Object)
	 */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PSMetadataProperty))
            return false;
        
        PSMetadataProperty other = (PSMetadataProperty) obj;
        
        if (StringUtils.equals(this.name, other.name) &&
                ObjectUtils.equals(this.valuetype, other.valuetype) &&
                ObjectUtils.equals(this.value, other.value))
            return true;
        
        return false;
    }
    
    /* (non-Javadoc)
	 * @see com.percussion.delivery.metadata.extractor.data.IPSMetadataProperty#toString()
	 */
    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", name)
            .append("value", value)
            .toString();
    }

	public Date getDatevalue() 
	{
	    if(valuetype != VALUETYPE.DATE)
            throw new RuntimeException("Cannot return a date for property type " + valuetype.toString());
        return (Date)value;
	}

	public Double getNumbervalue() 
	{
		if(valuetype != VALUETYPE.NUMBER)
		    throw new RuntimeException("Cannot return a number for property type " + valuetype.toString());
		return (Double)value;
		    
	}		

	public String getStringvalue() 
	{
	    return StringUtils.defaultString(value.toString());
	}
	
	@XmlTransient
    public void setDatevalue(Date val)
    {
       valuetype = VALUETYPE.DATE;
       value = val;
        
    }
	
	@XmlTransient
    public void setNumbervalue(Double val)
    {
        valuetype = VALUETYPE.NUMBER;
        value = val;
        
    }
	
	@XmlTransient
    public void setStringvalue(String val)
    {
        valuetype = VALUETYPE.STRING;
        value = val;        
    }
	
	@XmlTransient
    public void setTextvalue(String val)
    {
        valuetype = VALUETYPE.TEXT;
                
        value = val;
        
    }
	
	@XmlTransient
    public void setValue(Object val)
    {
        if (valuetype == null)
        {
            value = val;
        } else {
            convertVal(val,valuetype);
        }
    }
    
    private Object convertVal(Object val, VALUETYPE type)
    {
        if (val instanceof String)
        {
            if (type == VALUETYPE.STRING || type == VALUETYPE.TEXT)
            {
                valuetype = type;
                return val;
            }
            else if (type == VALUETYPE.NUMBER)
            {
                if (NumberUtils.isNumber((String) val))
                {
                    Double doub = Double.parseDouble((String) val);
                    valuetype = VALUETYPE.NUMBER;
                    return doub;
                }
                throw new IllegalArgumentException("value does not match number type");
            }
            else if (type == VALUETYPE.DATE)
            {
                try
                {
                    Date date = sdf.parse((String) val);
                    valuetype = VALUETYPE.DATE;
                    return date;
                }
                catch (ParseException e)
                {
                    throw new IllegalArgumentException("value does not match date type");
                }
            }
            valuetype = VALUETYPE.STRING;
            return val;
        }
        else if (val instanceof Double && type != VALUETYPE.NUMBER)
        {
            throw new IllegalArgumentException("value type does not match Double");
        }
        else if (val instanceof Date && type != VALUETYPE.DATE)
        {
            throw new IllegalArgumentException("value type does not match Date");
        } 
        valuetype = type;
        return val;
    }
	
}
