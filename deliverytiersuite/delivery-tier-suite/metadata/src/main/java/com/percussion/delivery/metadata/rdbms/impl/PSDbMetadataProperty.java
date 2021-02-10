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
package com.percussion.delivery.metadata.rdbms.impl;

import com.percussion.delivery.metadata.IPSMetadataProperty;
import com.percussion.delivery.metadata.utils.PSHashCalculator;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Nationalized;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Represents a metadata property namnatue value pair.
 * 
 * @author erikserating
 * 
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region = "PSMetadataProperty")
@Table(name = "PERC_PAGE_METADATA_PROPERTIES")
public class PSDbMetadataProperty implements IPSMetadataProperty, Serializable
{
    @Id
    @Column(unique = true,name = "ID",nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Basic
    @Nationalized
    private VALUETYPE valuetype;

    @Column(length = 4000)
    @Nationalized
    private String stringvalue;

    /**
     * Property name. For example: dcterms:creator
     */
    @Column(nullable = false, length = PSDbMetadataProperty.MAX_PROPERTY_NAME_LENGTH)
    @Nationalized
    private String name;


    @Column(length = Integer.MAX_VALUE)
    @Nationalized
    private String textvalue;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    private Date datevalue;

    @Basic
    private Double numbervalue;

    /**
     * Hash of the property's value. It's updated when calculateHash function is
     * called.
     */
    @Column(name = "VALUE_HASH", nullable = false, length = 40)
    @Nationalized
    private String valueHash;

    /**
     * This field represents the max length that the name of an instance of this
     * class can have.
     */
    public static final int MAX_PROPERTY_NAME_LENGTH = 100;


    public PSDbMetadataProperty() { }

    /**
     * HashCalculator instance used to get the hash of the metadata property's
     * value.
     */
    private static PSHashCalculator hashCalculator = new PSHashCalculator();
    /**
     * Ctor to create a property of the specified valuetype.
     * 
     * @param name the property name, cannot be <code>null</code> or empty.
     * @param type the {@link #valuetype} for the property. Cannot be
     *            <code>null</code>.
     * @param value the value to be stored in the property. May be
     *            <code>null</code> or empty.
     */
    public PSDbMetadataProperty(String name, VALUETYPE type, Object value)
    {
        this();

        if (name == null || name.length() == 0)
            throw new IllegalArgumentException("name cannot be null or empty.");
        if (type == null)
            throw new IllegalArgumentException("type cannot be null.");
        this.setName(name);
        boolean nan = true;
        if (type == VALUETYPE.DATE)
        {
            if (!(value instanceof Date))
                throw new IllegalArgumentException(
                        "Value type 'Date' was specified but the passed in value is not a date object.");
            setDatevalue((Date) value);
        }
        else if (type == VALUETYPE.NUMBER)
        {
            Double d = null;
            if (value instanceof Integer || value instanceof Float || value instanceof Long || value instanceof Short)
            {
                d = new Double(value.toString());
                nan = false;
            }
            else if (value instanceof Double)
            {
                d = (Double) value;
                nan = false;
            }
            else if (value instanceof String)
            {
                try
                {
                    d = Double.parseDouble(value.toString());
                    nan = false;
                }
                catch (NumberFormatException ignore)
                {

                }
            }
            if (nan)
                throw new IllegalArgumentException(
                        "The valuetype specified is 'NUMBER', but the passed in value is not a number.");
            setNumbervalue(d);
        }
        else if (type == VALUETYPE.TEXT)
        {
            String val = value.toString();
            setTextvalue(val);
        }
        else if (type == VALUETYPE.STRING)
        {
            String val = value.toString();
            if (val.length() > 4000)
                throw new IllegalArgumentException(
                        "The maximum length for a string value is 4000 chars, use a text value for greater lengths.");
            setStringvalue(val);
        }
    }

    /**
     * Convenience ctor to create a string value type property.
     * 
     * @param name cannot be <code>null</code> or empty.
     * @param value the value, may be <code>null</code>.
     */
    public PSDbMetadataProperty(String name, String value)
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
    public PSDbMetadataProperty(String name, int value)
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
    public PSDbMetadataProperty(String name, double value)
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
    public PSDbMetadataProperty(String name, float value)
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
    public PSDbMetadataProperty(String name, long value)
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
    public PSDbMetadataProperty(String name, short value)
    {
        this(name, VALUETYPE.NUMBER, value);
    }

    /**
     * Convenience ctor to create a Date value type property.
     * 
     * @param name name cannot be <code>null</code> or empty.
     * @param value may be <code>null</code>.
     */
    public PSDbMetadataProperty(String name, Date value)
    {
        this(name, VALUETYPE.DATE, value);
    }


    /**
     * @return the metadataEntry
     */
    public PSDbMetadataEntry getMetadataEntry()
    {
        return entry;
    }

    /**
     * @param metadataEntry the metadataEntry to set
     */
    public void setMetadataEntry(PSDbMetadataEntry metadataEntry)
    {
        entry = metadataEntry;
    }

    /**
     * @return the name
     */
    public String getName()
    {
       return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
       this.name=name;
    }

    @ManyToOne(optional = false)
    @JoinColumns(@JoinColumn(name = "ENTRY_ID", referencedColumnName = "pagepathhash"))
    private PSDbMetadataEntry entry;

    public String getHash()
    {
        return valueHash;

    }

     /**
     * Calculates the hash of the given value, using HashCalculator. If the
     * parameter is null, then the hash is calculated over an empty string. If
     * not, the hash is calculated over the result of 'toString' method on the
     * parameter.
     */
    public void calculateHash(Object value)
    {
        if (value == null)
            valueHash = hashCalculator.calculateHash(StringUtils.EMPTY);
        else
            valueHash = hashCalculator.calculateHash(value.toString());
    }




    /**
     * @return the valuetype
     */
    public VALUETYPE getValuetype()
    {
        return valuetype;
    }

    /**
     * @param valuetype the valuetype to set
     */
    public void setValuetype(VALUETYPE valuetype)
    {
        this.valuetype = valuetype;
    }

    /**
     * Returns the untyped value.
     * 
     * @return May be <code>null</code>.
     */
    public Object getValue()
    {
        Object result = null;
        switch (getValuetype())
        {
            case STRING :
                result = getStringvalue();
                break;

            case TEXT :
                result = getTextvalue();
                break;

            case DATE :
                result = getDatevalue();
                break;

            case NUMBER :
                result = getNumbervalue();
                break;
        }

        return result;
    }

    /**
     * @return the stringvalue
     */
    public String getStringvalue()
    {
        if (valuetype == VALUETYPE.STRING)
            return stringvalue;
        if (valuetype == VALUETYPE.TEXT)
            return textvalue;
        if (valuetype == VALUETYPE.DATE)
        {
            return datevalue.toString();
        }
        if (valuetype == VALUETYPE.NUMBER)
        {
            return numbervalue.toString();
        }
        return "";
    }

    /**
     * @param stringvalue the stringvalue to set
     */
    public void setStringvalue(String stringvalue)
    {
        this.valuetype = VALUETYPE.STRING;
        this.stringvalue = stringvalue;

        calculateHash(this.stringvalue);
    }

    /**
     * @return the textvalue
     */
    public String getTextvalue()
    {
        return textvalue;
    }

    /**
     * @param textvalue the textvalue to set
     */
    public void setTextvalue(String textvalue)
    {
        this.valuetype = VALUETYPE.TEXT;
        this.textvalue = textvalue;

        calculateHash(this.textvalue);
    }

    /**
     * @return the datevalue
     */
    public Date getDatevalue()
    {
        return datevalue;
    }

    /**
     * @param datevalue the datevalue to set
     */
    public void setDatevalue(Date datevalue)
    {
        this.valuetype = VALUETYPE.DATE;
        this.datevalue = datevalue;

        calculateHash(this.datevalue);
    }

    /**
     * @return the numbervalue
     */
    public Double getNumbervalue()
    {
        return numbervalue;
    }

    /**
     * @param numbervalue the numbervalue to set
     */
    public void setNumbervalue(Double numbervalue)
    {
        this.valuetype = VALUETYPE.NUMBER;
        this.numbervalue = numbervalue;

        calculateHash(this.numbervalue);
    }

    public int getId() {
        return this.id;
    }
}