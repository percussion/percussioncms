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
