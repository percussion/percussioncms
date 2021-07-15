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

/**
 * 
 */
package com.percussion.pagemanagement.assembler;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;

import org.apache.commons.lang.StringUtils;

/**
 * @author davidpardini
 * 
 */
public class PSMetadataProperty
{

    private PropertyId id;

    private VALUETYPE valuetype;

    private String stringvalue;

    private String textvalue;

    private Date datevalue;

    private Double numbervalue;

    private PSMetadataProperty()
    {

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
    public PSMetadataProperty(String name, VALUETYPE type, Object value)
    {
        this();

        if (name == null || name.length() == 0) {
            throw new IllegalArgumentException("name cannot be null or empty.");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null.");
        }
        this.setName(name);
        boolean nan = true;
        if (type == VALUETYPE.DATE)
        {
            if (!(value instanceof Date)) {
                throw new IllegalArgumentException(
                        "Value type 'Date' was specified but the passed in value is not a date object.");
            }
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
            if (nan) {
                throw new IllegalArgumentException(
                        "The valuetype specified is 'NUMBER', but the passed in value is not a number.");
            }
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
            if (val.length() > 4000) {
                throw new IllegalArgumentException(
                        "The maximum length for a string value is 4000 chars, use a text value for greater lengths.");
            }
            setStringvalue(val);
        }
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

    /**
     * @return the id
     */
    public PropertyId getId()
    {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(PropertyId id)
    {
        this.id = id;
    }

    /**
     * @return the metadataEntry
     */
    public PSMetadataEntry getMetadataEntry()
    {
        if (id != null) {
            return id.getMetadataEntry();
        }

        return null;
    }

    /**
     * @param metadataEntry the metadataEntry to set
     */
    public void setMetadataEntry(PSMetadataEntry metadataEntry)
    {
        createIdIfNull();
        id.setMetadataEntry(metadataEntry);
    }

    /**
     * @return the name
     */
    public String getName()
    {
        if (id != null) {
            return id.getName();
        }

        return null;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name)
    {
        createIdIfNull();
        id.setName(name);
    }

    public String getHash()
    {
        if (id != null) {
            return id.getValueHash();
        }

        return null;
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
        if (valuetype == VALUETYPE.STRING) {
            return stringvalue;
        }
        if (valuetype == VALUETYPE.TEXT) {
            return textvalue;
        }
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

        id.calculateHash(this.stringvalue);
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

        id.calculateHash(this.textvalue);
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

        id.calculateHash(this.datevalue);
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

        id.calculateHash(this.numbervalue);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PSMetadataProperty) || obj == null) {
            return false;
        }

        PSMetadataProperty other = (PSMetadataProperty) obj;

        if (this.id == null || other.id == null) {
            return false;
        }

        return this.id.equals(other.id);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        if (this.id != null) {
            return this.id.hashCode();
        }

        return 0;
    }

    /**
     * Creates a new PropertyId if the current is null.
     */
    private void createIdIfNull()
    {
        if (id == null) {
            id = new PropertyId();
        }
    }

    public enum VALUETYPE {
        DATE, NUMBER, STRING, TEXT
    }
}

/**
 * Class that represents a composite key for PSMetadataProperty. The composite
 * key for a metadata property consists in a metadata entry (which the property
 * belongs to) and the property name.
 * 
 * @author davidpardini
 * 
 */
@Embeddable
class PropertyId implements Serializable
{

    /**
     * HashCalculator instance used to get the hash of the metadata property's
     * value.
     */
    private static HashCalculator hashCalculator = new HashCalculator();

    /**
     * Metadata entry that owns the property.
     */
    @ManyToOne
    @JoinColumns(@JoinColumn(name = "ENTRY_ID", referencedColumnName = "pagePath"))
    private PSMetadataEntry metadataEntry;

    /**
     * Property name. For example: dcterms:creator
     */
    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "VALUE_HASH", nullable = false, length = 40)
    private String valueHash;

    /**
     * @return the metadataEntry
     */
    public PSMetadataEntry getMetadataEntry()
    {
        return metadataEntry;
    }

    /**
     * @param metadataEntry the metadataEntry to set
     */
    public void setMetadataEntry(PSMetadataEntry metadataEntry)
    {
        this.metadataEntry = metadataEntry;
    }

    /**
     * @return the propertyName
     */
    public String getName()
    {
        return name;
    }

    /**
     * @param propertyName the propertyName to set
     */
    public void setName(String name)
    {
        if (StringUtils.isEmpty(name)) {
            this.name = null;
        }
        else {
            this.name = name;
        }
    }

    /**
     * @return the hash over the metadata property's value.
     */
    public String getValueHash()
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
        if (value == null) {
            valueHash = hashCalculator.calculateHash(StringUtils.EMPTY);
        }
        else {
            valueHash = hashCalculator.calculateHash(value.toString());
        }
    }

    @Override
    public int hashCode()
    {
        int pagePathHashCode = (metadataEntry != null && metadataEntry.getPagepath() != null) ? metadataEntry
                .getPagepath().hashCode() : 0;
        int propertyNameHashCode = name != null ? name.hashCode() : 0;
        int hashCode = valueHash != null ? valueHash.hashCode() : 0;

        return pagePathHashCode + propertyNameHashCode + hashCode;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof PropertyId) || obj == null) {
            return false;
        }

        PropertyId other = (PropertyId) obj;

        if (this.metadataEntry == null || other.metadataEntry == null) {
            return false;
        }

        return StringUtils.equals(this.metadataEntry.getPagepath(), other.metadataEntry.getPagepath())
                && StringUtils.equals(this.name, other.name) && StringUtils.equals(this.valueHash, other.valueHash);
    }
}

/**
 * Responsible for calculating a hash over a value. It uses SHA-1 by default and
 * UTF-8 to convert the string value.
 * 
 * @author davidpardini
 * 
 */
class HashCalculator
{
    private static final String HEXES = "0123456789ABCDEF";

    private static final String HASH_ALGORITHM = "SHA-1";

    private static final String CONTENT_ENCODING = "UTF-8";

    private MessageDigest digest;

    public HashCalculator()
    {
        try
        {
            digest = MessageDigest.getInstance(HASH_ALGORITHM);
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new RuntimeException(e);
        }
    }

    public String calculateHash(String value)
    {
        digest.reset();
        byte[] hashResult = null;

        try
        {
            hashResult = digest.digest(value.getBytes(CONTENT_ENCODING));
            return getHex(hashResult);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException(e);
        }
    }

    private String getHex(byte[] raw)
    {
        if (raw == null) {
            return null;
        }

        final StringBuilder hex = new StringBuilder(2 * raw.length);

        for (final byte b : raw)
        {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }

        return hex.toString();
    }
}
