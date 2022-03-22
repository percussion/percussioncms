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
package com.percussion.util;

import java.util.concurrent.ConcurrentHashMap;

/**
 * An inheritance-aware mapping of classes to objects. The object
 * that maps to the most derived class will be found first. Given a
 * class, a search for a mapped object will start with the given
 * class. If no mapping is found, then the search will look for a
 * mapping for the immediate superclass, and so on. If no mapping
 * is found for any class in the inheritance hierarchy of the
 * given class, then <CODE>null</CODE> will be returned.
 */
public class PSMapClassToObject
{
   public PSMapClassToObject()
   {
      m_hash = new ConcurrentHashMap();
   }

   /** 
    *   add all of the mappings from the given map to this map, replacing
    *   mappings from the given map which exist in this map.
    */
   public void addReplaceMappings(PSMapClassToObject map)
   {
      m_hash.putAll(map.m_hash);
   }

   /**
    *   add all of the mappings from the given map to this map, but will not
    *   add mappings from the given map if they already exist in this map.
    */
   public void addNoReplaceMappings(PSMapClassToObject map)
   {
      java.util.Enumeration keys = map.m_hash.keys();
      Class currentKey = null;
      while (keys.hasMoreElements())
      {
         currentKey = (Class)keys.nextElement();
         if (!m_hash.contains(currentKey))
            m_hash.put(currentKey, map.m_hash.get(currentKey));
      }
   }

   /**
    *   add the given mapping to this map, replacing any existing mapping
    *
    *   @return   The object that was previously associated with this class, or
    *   <NULL>null</NULL> if no mapping existed for this class. The
    *   return value will still be <CODE>null</CODE> even if a superclass of
    *   the given class has a mapping.
    */
   public Object addReplaceMapping(Class c, Object o)
   {
      Object ret = m_hash.get(c);
      m_hash.put(c, o);
      return ret;
   }

   /**
    *   add the given mapping to this map only if no mapping exists for this
    *   class
    */
   public void addNoReplaceMapping(Class c, Object o)
   {
      if (!m_hash.contains(c))
         m_hash.put(c, o);
   }

   /**
    *   gets the Object associated with this class. if no mapping exists for
    *   this class, will return the mapping associated with the most
    *   immediate superclass. if no mapping exists for any superclass (including
    *   Object) of this class, will return <CODE>null</CODE>
    */
   public Object getMapping(Class c)
   {
      if (c == null)
         return null;

      Object o = m_hash.get(c);
      if (o == null)
      {
         return getMapping(c.getSuperclass());
      }
      return o;
   }

   /**
    * Gets the object associated with this class. If no mapping exists for
    * this class, then if <CODE>doSuper</CODE> is <CODE>true</CODE>, will
    * return the mapping for the most immediate superclass for which
    * a mapping is defined. Returns <CODE>null</CODE> if no mapping was
    * found.
    *
    * @author   chadloder
    * 
    * @version 1.4 1999/02/05
    * 
    * 
    * @param   c
    * @param   doSuper
    * 
    * @return   Object
    */
   public Object getMapping(Class c, boolean doSuper)
   {
      if (c == null)
         return null;

      Object o = m_hash.get(c);
      if (o == null)
      {
         if (doSuper)
            return getMapping(c.getSuperclass());
      }
      return o;      
   }

   public java.util.Enumeration getClasses()
   {
      return m_hash.keys();
   }

   public java.util.Enumeration getMappedObjects()
   {
      return m_hash.elements();
   }

   private ConcurrentHashMap m_hash;
}
