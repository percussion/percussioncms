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
