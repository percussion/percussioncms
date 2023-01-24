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
package com.percussion.tools;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * A multimap class for storing HTTP headers (which can be 1:many)
 */
public class PSHttpHeaders
{
	public void addHeader(String headerName, String headerValue)
	{
		addMultiMapping(m_hdrs, headerName.toUpperCase(), headerValue);
	}

   public void replaceHeader( String headerName, String headerValue )
   {
      m_hdrs.put( headerName.toUpperCase(), headerValue );
   }

	public Iterator getHeaders(String headerName)
	{
		return getMultiValues(m_hdrs, headerName.toUpperCase());
	}

	public String getHeader(String headerName)
	{
		String val = null;
		Iterator i = getHeaders(headerName);
		if (i.hasNext())
		{
			val = i.next().toString();
		}
		return val;
	}

	public Set getHeaderNames()
	{
		return m_hdrs.keySet();
	}

	public void addAll(PSHttpHeaders headers)
	{
		if (headers == this) // don't add ourself because we could loop forever
			return;

		Collection keySet = headers.getHeaderNames();
		for (Iterator i = keySet.iterator(); i.hasNext(); )
		{
			String headerName = i.next().toString();
			for (Iterator j = headers.getHeaders(headerName); j.hasNext(); )
			{
				addHeader(headerName, j.next().toString());
			}
		}
	}

	/**
	 * Adds a value to a Map in a 1:1 or 1:many way, if applicable. 1:many
	 * mappings will be Lists containing values.
	 *
	 * @author	chad loder
	 *
	 * @version 1.0 1999/8/20
	 *
	 * @param	m
	 * @param	key
	 * @param	value
	 *
	 */
	protected void addMultiMapping(Map m, Object key, Object value)
	{
		// we support 1:many header mappings by storing Lists for 1:many
		// headers. For 1:1 headers, we store Strings

		Object existingVal = m.get(key);

		if (existingVal instanceof List)
		{
			// there are multiple occurrences of value header, so add the new one
			((List)existingVal).add(value); // add the new string
		}
		else if (existingVal != null)
		{
			// there was only one value, now there are 2, so make a list
			List valList = new ArrayList();
			valList.add(existingVal); // add the previous existing value
			valList.add(value); // add the new value
         existingVal = valList;
		}
		else
		{
			existingVal = value;
		}

		m.put(key, existingVal);
	}

	/**
	 * Gets all the values out of the multimap for a particular key
	 * 
	 * @author	chad loder
	 * 
	 * @version 1.0 1999/8/20
	 * 
	 * 
	 * @param	m
	 * @param	key
	 * 
	 * @return	Iterator
	 */
	protected Iterator getMultiValues(Map m, String key)
	{
		class SingleIterator implements Iterator
		{
			public SingleIterator(Object value)	{	m_value = value;	}

			public boolean hasNext() {	return m_value != null;	}
			
			public Object next()
			{
				if (m_value == null)
					throw new NoSuchElementException();
				Object val = m_value;
				m_value = null;
				return val;
			}

			public void remove() { throw new UnsupportedOperationException(); }

			private Object m_value;
		}

		Object val = m.get(key);
		
		if (val instanceof List)
		{
			return ((List)val).iterator();
		}

		return new SingleIterator(val);
	}

	private Map m_hdrs = new HashMap();
}
