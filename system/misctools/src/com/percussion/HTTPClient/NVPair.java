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

package com.percussion.HTTPClient;


/**
 * This class holds a Name/Value pair of strings. It's used for headers,
 * form-data, attribute-lists, etc. This class is immutable.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 */
@Deprecated
public final class NVPair
{
    /** the name */
    private String name;

    /** the value */
    private String value;


    // Constructors

    /**
     * Creates a new name/value pair and initializes it to the
     * specified name and value.
     *
     * @param name  the name
     * @param value the value
     */
    public NVPair(String name, String value)
    {
	this.name  = name;
	this.value = value;
    }

    /**
     * Creates a copy of a given name/value pair.
     *
     * @param p the name/value pair to copy
     */
    public NVPair(NVPair p)
    {
	this(p.name, p.value);
    }


    // Methods

    /**
     * Get the name.
     *
     * @return the name
     */
    public final String getName()
    {
	return name;
    }

    /**
     * Get the value.
     *
     * @return the value
     */
    public final String getValue()
    {
	return value;
    }


    /**
     * Produces a string containing the name and value of this instance.
     *
     * @return a string containing the class name and the name and value
     */
    public String toString()
    {
	return getClass().getName() + "[name=" + name + ",value=" + value + "]";
    }
}
