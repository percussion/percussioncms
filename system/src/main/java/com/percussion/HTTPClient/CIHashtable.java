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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class implements a Hashtable with case-insensitive Strings as keys.
 *
 * @version	0.3-3  06/05/2001
 * @author	Ronald Tschalär
 * @deprecated
 */
@Deprecated
class CIHashtable extends Hashtable
{
    // Constructors

    /**
     * Create a new CIHashtable with the specified initial capacity and the
     * specified load factor.
     *
     * @param initialCapacity the initial number of buckets
     * @param loadFactor a number between 0.0 and 1.0
     * @see java.util.Hashtable#Hashtable(int, float)
     */
    public CIHashtable(int initialCapacity, float loadFactor)
    {
	super(initialCapacity, loadFactor);
    }


    /**
     * Create a new CIHashtable with the specified initial capacity.
     *
     * @param initialCapacity the initial number of buckets
     * @see java.util.Hashtable#Hashtable(int)
     */
    public CIHashtable(int initialCapacity)
    {
	super(initialCapacity);
    }


    /**
     * Create a new CIHashtable with a default initial capacity.
     *
     * @see java.util.Hashtable#Hashtable()
     */
    public CIHashtable()
    {
	super();
    }


    // Methods

    /**
     * Retrieves the object associated with the specified key. The key lookup
     * is case-insensitive.
     *
     * @param key the key
     * @return the object associated with the key, or null if none found.
     * @see java.util.Hashtable#get(Object)
     */
    public Object get(String key)
    {
	return super.get(new CIString(key));
    }


    /**
     * Stores the specified object with the specified key.
     *
     * @param key the key
     * @param value the object to be associated with the key
     * @return the object previously associated with the key, or null if
     *         there was none.
     * @see java.util.Hashtable#put(Object, Object)
     */
    public Object put(String key, Object value)
    {
	return super.put(new CIString(key), value);
    }


    /**
     * Looks whether any object is associated with the specified key. The
     * key lookup is case insensitive.
     *
     * @param key the key
     * @return true is there is an object associated with key, false otherwise
     * @see java.util.Hashtable#containsKey(Object)
     */
    public boolean containsKey(String key)
    {
	return super.containsKey(new CIString(key));
    }


    /**
     * Removes the object associated with this key from the Hashtable. The
     * key lookup is case insensitive.
     *
     * @param key the key
     * @return the object associated with this key, or null if there was none.
     * @see java.util.Hashtable#remove(Object)
     */
    public Object remove(String key)
    {
	return super.remove(new CIString(key));
    }


    /**
     * Returns an enumeration of all the keys in the Hashtable.
     *
     * @return the requested Enumerator
     * @see java.util.Hashtable#keys()
     */
    public Enumeration keys()
    {
	return new CIHashtableEnumeration(super.keys());
    }
}


/**
 * A simple enumerator which delegates everything to the real enumerator.
 * If a CIString element is returned, then the string it represents is
 * returned instead.
 */
final class CIHashtableEnumeration implements Enumeration
{
    Enumeration HTEnum;

    public CIHashtableEnumeration(Enumeration e)
    {
	HTEnum = e;
    }

    public boolean hasMoreElements()
    {
	return HTEnum.hasMoreElements();
    }

    public Object nextElement()
    {
	Object tmp = HTEnum.nextElement();
	if (tmp instanceof CIString)
	    return ((CIString) tmp).getString();

	return tmp;
    }
}


/**
 * This class' raison d'etre is that I want to use a Hashtable using
 * Strings as keys and I want the lookup be case insensitive, but I
 * also want to be able retrieve the keys with original case (otherwise
 * I could just use toLowerCase() in the get() and put()). Since the
 * class String is final we create a new class that holds the string
 * and overrides the methods hashCode() and equals().
 */
final class CIString
{
    /** the string */
    private String string;

    /** the hash code */
    private int hash;


    /** the constructor */
    public CIString(String string)
    {
	this.string = string;
	this.hash   = calcHashCode(string);
    }

    /** return the original string */
    public final String getString()
    {
	return string;
    }

    /** the hash code was precomputed */
    public int hashCode()
    {
	return hash;
    }


    /**
     * We smash case before calculation so that the hash code is
     * "case insensitive". This is based on code snarfed from
     * java.lang.String.hashCode().
     */
    private static final int calcHashCode(String str)
    {
	int  hash  = 0;
	char llc[] = lc;
	int  len   = str.length();

	for (int idx= 0; idx<len; idx++)
	    hash = 31*hash + llc[str.charAt(idx)];

	return hash;
    }


    /**
     * Uses the case insensitive comparison.
     */
    public boolean equals(Object obj)
    {
	if (obj != null)
	{
	    if (obj instanceof CIString)
		return string.equalsIgnoreCase(((CIString) obj).string);

	    if (obj instanceof String)
		return string.equalsIgnoreCase((String) obj);
	}

	return false;
    }

    /**
     * Just return the internal string.
     */
    public String toString()
    {
	return string;
    }


    private static final char[] lc = new char[256];

    static
    {
	// just ISO-8859-1
	for (char idx=0; idx<256; idx++)
	    lc[idx] = Character.toLowerCase(idx);
    }
}
