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
package com.percussion.test.http;

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
public class HttpHeaders
{
   public void addHeader(String headerName, String headerValue)
   {
      // TODO: make the headerName in case insensitive
      addMultiMapping(m_hdrs, headerName, headerValue);  
   }

   public Iterator getHeaders(String headerName)
   {
      // TODO: make the headerName in case insensitive
      return getMultiValues(m_hdrs, headerName); 
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

   public void addAll(HttpHeaders headers)
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
    * @author   chad loder
    * 
    * @version 1.0 1999/8/20
    * 
    * @param   m
    * @param   key
    * @param   value
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
    * @author   chad loder
    * 
    * @version 1.0 1999/8/20
    * 
    * 
    * @param   m
    * @param   key
    * 
    * @return   Iterator
    */
   protected Iterator getMultiValues(Map m, String key)
   {
      class SingleIterator implements Iterator
      {
         public SingleIterator(Object value)   {   m_value = value;   }

         public boolean hasNext() {   return m_value != null;   }
         
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
